package de.delinkedde.redstoneItemClear.webpanel;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.function.Consumer;

/**
 * Manages web panel authentication and API communication
 */
public class WebPanelManager {

    private final RedstoneItemClear plugin;
    private final ApiClient apiClient;
    private boolean initialized = false;

    public WebPanelManager(RedstoneItemClear plugin) {
        this.plugin = plugin;
        this.apiClient = new ApiClient(plugin);
    }

    /**
     * Initialize web panel features
     */
    public void initialize() {
        if (!checkSecurityConfig()) {
            plugin.getLogger().severe("═══════════════════════════════════════════");
            plugin.getLogger().severe("  WEBPANEL DEAKTIVIERT - UNSICHERE CONFIG  ");
            plugin.getLogger().severe("═══════════════════════════════════════════");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("Server ist NICHT sicher konfiguriert!");
            plugin.getLogger().severe("Spieler könnten UUIDs faken.");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("Behebe das Problem und starte neu.");
            plugin.getLogger().severe("═══════════════════════════════════════════");
            return;
        }

        // Register server with API
        if (apiClient.registerServer()) {
            initialized = true;
            plugin.getLogger().info("✅ WebPanel initialized successfully");

            // Start periodic TPS reporting (every 30 seconds)
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::reportTps, 600L, 600L);

            // Report user access every 5 minutes
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::reportUserAccess, 6000L, 6000L);
        } else {
            plugin.getLogger().warning("⚠️ Failed to register server with API - WebPanel features disabled");
        }
    }

    /**
     * Security check: Verify server is in online mode OR behind BungeeCord
     */
    private boolean checkSecurityConfig() {
        boolean isBungeeCord = isBungeeCordMode();
        boolean isOnlineMode = Bukkit.getOnlineMode();

        if (isBungeeCord && isOnlineMode) {
            plugin.getLogger().severe("═══════════════════════════════════════════");
            plugin.getLogger().severe("  FEHLER: Falsche Konfiguration!           ");
            plugin.getLogger().severe("═══════════════════════════════════════════");
            plugin.getLogger().severe("BungeeCord Mode ist aktiviert, aber:");
            plugin.getLogger().severe("  online-mode=true in server.properties");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("Das MUSS so sein:");
            plugin.getLogger().severe("  online-mode=false");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("Der Auth-Check läuft auf BungeeCord!");
            plugin.getLogger().severe("═══════════════════════════════════════════");
            return false;
        }

        if (!isBungeeCord && !isOnlineMode) {
            plugin.getLogger().severe("═══════════════════════════════════════════");
            plugin.getLogger().severe("  FEHLER: Unsichere Konfiguration!         ");
            plugin.getLogger().severe("═══════════════════════════════════════════");
            plugin.getLogger().severe("Dieser Server läuft NICHT hinter BungeeCord!");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("Das MUSS so sein:");
            plugin.getLogger().severe("  online-mode=true in server.properties");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("Sonst können Spieler UUIDs faken!");
            plugin.getLogger().severe("═══════════════════════════════════════════");
            return false;
        }

        plugin.getLogger().info("✅ Security Check passed - Server ist sicher!");
        return true;
    }

    /**
     * Check if server is running in BungeeCord mode
     * Reads spigot.yml directly to avoid deprecated API
     */
    private boolean isBungeeCordMode() {
        try {
            File spigotConfig = new File("spigot.yml");
            if (!spigotConfig.exists()) {
                return false;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(spigotConfig);
            return config.getBoolean("settings.bungeecord", false);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not read spigot.yml: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify a code for a player
     */
    public void verifyCode(String code, Player player, Consumer<Boolean> callback) {
        if (!initialized) {
            callback.accept(false);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = apiClient.verifyCode(code, player.getUniqueId().toString(), player.getName());
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(success));
        });
    }

    /**
     * Report current TPS to API
     */
    private void reportTps() {
        if (!initialized) return;

        try {
            double tps = Bukkit.getTPS()[0]; // 1-minute average
            int playersOnline = Bukkit.getOnlinePlayers().size();
            int chunksLoaded = Bukkit.getWorlds().stream()
                    .mapToInt(w -> w.getLoadedChunks().length)
                    .sum();

            apiClient.sendTpsData(tps, playersOnline, chunksLoaded);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to report TPS: " + e.getMessage());
        }
    }

    /**
     * Report users with webpanel permission
     */
    private void reportUserAccess() {
        if (!initialized) return;

        try {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("redstoneitemclear.webpanel"))
                    .forEach(p -> {
                        apiClient.updateUserAccess(p.getUniqueId().toString(), p.getName());
                    });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to report user access: " + e.getMessage());
        }
    }

    /**
     * Report a problem zone to API
     */
    public void reportProblem(String world, int chunkX, int chunkZ, String problemType, String severity, int entityCount) {
        if (!initialized) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                apiClient.sendProblemData(world, chunkX, chunkZ, problemType, severity, entityCount);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to report problem: " + e.getMessage());
            }
        });
    }

    public boolean isInitialized() {
        return initialized;
    }
}

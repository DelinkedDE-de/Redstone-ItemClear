package de.delinkedde.redstoneItemClear.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Sends plugin messages to BungeeCord to announce this backend server
 * and keep it alive via heartbeats. Channels:
 * - redstone:register
 * - redstone:heartbeat
 * - redstone:unregister
 */
public class BungeeMessenger {
    private static final String CH_REGISTER = "redstone:register";
    private static final String CH_HEARTBEAT = "redstone:heartbeat";
    private static final String CH_UNREGISTER = "redstone:unregister";

    private final RedstoneItemClear plugin;
    private String serverName;
    private boolean registered = false;
    private BukkitRunnable heartbeatTask;

    public BungeeMessenger(RedstoneItemClear plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        // Always prefer the configured WebPanel hostname for mapping
        // This matches the value stored in the WebPanel DB on registration
        String configuredHostname = plugin.getConfig().getString("webpanel.hostname", "backend");
        serverName = (configuredHostname == null || configuredHostname.isBlank()) ? "backend" : configuredHostname.trim();

        // Register outgoing channels
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CH_REGISTER);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CH_HEARTBEAT);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CH_UNREGISTER);

        // Try initial register after short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                Player p = anyOnlinePlayer();
                if (p != null) {
                    sendRegister(p);
                }
            }
        }.runTaskLater(plugin, 40L);

        // Schedule heartbeat every 30 seconds
        heartbeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player p = anyOnlinePlayer();
                if (p == null) return;
                if (!registered) {
                    sendRegister(p);
                } else {
                    sendHeartbeat(p);
                }
            }
        };
        heartbeatTask.runTaskTimer(plugin, 600L, 600L);

        plugin.getLogger().info("Bungee messaging initialized for server name: " + serverName);
    }

    public void shutdown() {
        // Attempt to send unregister if possible
        try {
            Player p = anyOnlinePlayer();
            if (p != null && registered) {
                sendUnregister(p);
            }
        } catch (Exception ignored) {}

        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
        registered = false;
    }

    private Player anyOnlinePlayer() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            return p;
        }
        return null;
    }

    private void sendRegister(Player player) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(serverName);
            out.writeUTF(getServerId());
            player.sendPluginMessage(plugin, CH_REGISTER, out.toByteArray());
            registered = true;
            plugin.getLogger().fine("Sent redstone:register to Bungee for server " + serverName);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send register to Bungee: " + e.getMessage());
        }
    }

    private void sendHeartbeat(Player player) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(serverName);
            out.writeUTF(getServerId());
            player.sendPluginMessage(plugin, CH_HEARTBEAT, out.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send heartbeat to Bungee: " + e.getMessage());
        }
    }

    private void sendUnregister(Player player) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(serverName);
            out.writeUTF(getServerId());
            player.sendPluginMessage(plugin, CH_UNREGISTER, out.toByteArray());
            plugin.getLogger().fine("Sent redstone:unregister to Bungee for server " + serverName);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send unregister to Bungee: " + e.getMessage());
        }
    }

    private String getServerId() {
        try {
            String id = plugin.getConfig().getString("webpanel.server-id", "");
            return (id == null || id.isBlank()) ? "unknown" : id.trim();
        } catch (Exception e) {
            return "unknown";
        }
    }
}

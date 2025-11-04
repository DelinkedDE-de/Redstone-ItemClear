package de.delinkedde.redstoneItemClear.manager;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.action.ActionExecutor;
import de.delinkedde.redstoneItemClear.analyzer.ProblemAnalyzer;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import de.delinkedde.redstoneItemClear.discord.DiscordWebhook;
import de.delinkedde.redstoneItemClear.model.ActionLevel;
import de.delinkedde.redstoneItemClear.model.ProblemZone;
import de.delinkedde.redstoneItemClear.monitor.TPSMonitor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Verwaltet die gesamte Performance-Überwachung und -Optimierung
 */
public class PerformanceManager implements TPSMonitor.TPSListener {
    private final RedstoneItemClear plugin;
    private final ConfigManager config;
    private final ProblemAnalyzer analyzer;
    private final ActionExecutor executor;
    private final DiscordWebhook discord;

    private ActionLevel lastActionLevel = ActionLevel.NORMAL;
    private long lastActionTime = 0;
    private static final long ACTION_COOLDOWN = 30000; // 30 Sekunden zwischen Aktionen

    public PerformanceManager(RedstoneItemClear plugin, ConfigManager config,
                              ProblemAnalyzer analyzer, ActionExecutor executor,
                              DiscordWebhook discord) {
        this.plugin = plugin;
        this.config = config;
        this.analyzer = analyzer;
        this.executor = executor;
        this.discord = discord;
    }

    @Override
    public void onLevelChange(ActionLevel oldLevel, ActionLevel newLevel, double currentTPS) {
        plugin.getLogger().info("TPS-Level geändert: " + oldLevel.getDisplayName() +
                              " -> " + newLevel.getDisplayName() +
                              " (TPS: " + String.format("%.2f", currentTPS) + ")");

        // Bei Verbesserung: Auto-Recovery
        if (newLevel.ordinal() < oldLevel.ordinal()) {
            handleRecovery(newLevel, currentTPS);
        }
        // Bei Verschlechterung: Warnung senden
        else if (newLevel.ordinal() > oldLevel.ordinal()) {
            handleDegradation(newLevel, currentTPS);
        }

        lastActionLevel = newLevel;
    }

    @Override
    public void onTPSWarning(ActionLevel level, double currentTPS) {
        // Nur aktiv werden wenn Cooldown abgelaufen
        long now = System.currentTimeMillis();
        if (now - lastActionTime < ACTION_COOLDOWN) {
            return;
        }

        // Analysiere und handle async
        new BukkitRunnable() {
            @Override
            public void run() {
                List<ProblemZone> problemZones = analyzer.analyzeLoadedChunks();

                if (!problemZones.isEmpty()) {
                    // Zurück zum Main-Thread für Aktionen
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            handleProblems(level, currentTPS, problemZones);
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);

        lastActionTime = now;
    }

    /**
     * Behandelt Performance-Probleme
     */
    private void handleProblems(ActionLevel level, double currentTPS, List<ProblemZone> zones) {
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("Behandle Performance-Probleme: Level=" + level.getDisplayName() +
                                  ", TPS=" + String.format("%.2f", currentTPS) +
                                  ", Zonen=" + zones.size());
        }

        // Führe Aktionen aus
        ActionExecutor.ActionResult result = executor.executeActions(level, zones);

        if (result.hasActions()) {
            plugin.getLogger().info("Maßnahmen durchgeführt: " + result);

            // In-Game Benachrichtigung
            if (config.areInGameNotificationsEnabled() && config.notifyOps()) {
                notifyOps(level, currentTPS, result);
            }

            // Discord Benachrichtigung
            discord.sendActionTaken(level, currentTPS, result, zones);
        } else {
            // Nur Warnung, keine Aktionen
            discord.sendTPSWarning(level, currentTPS, zones);
        }
    }

    /**
     * Behandelt TPS-Verschlechterung
     */
    private void handleDegradation(ActionLevel newLevel, double currentTPS) {
        // Sofortige Analyse bei kritischen Levels
        if (newLevel == ActionLevel.SEVERE || newLevel == ActionLevel.EMERGENCY) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    List<ProblemZone> zones = analyzer.analyzeLoadedChunks();
                    discord.sendTPSWarning(newLevel, currentTPS, zones);
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    /**
     * Behandelt TPS-Verbesserung (Auto-Recovery)
     */
    private void handleRecovery(ActionLevel newLevel, double currentTPS) {
        if (!config.isAutoRecoveryEnabled()) {
            return;
        }

        // Wenn TPS wieder gut sind, reaktiviere alle Einschränkungen
        if (currentTPS >= config.getRecoveryTPS() && newLevel == ActionLevel.NORMAL) {
            int delay = config.getRecoveryDelay();

            new BukkitRunnable() {
                @Override
                public void run() {
                    executor.enableAllRestrictions();
                    plugin.getLogger().info("Auto-Recovery: Alle Einschränkungen aufgehoben (TPS: " +
                                          String.format("%.2f", currentTPS) + ")");

                    if (config.areInGameNotificationsEnabled() && config.notifyOps()) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.isOp()) {
                                player.sendMessage(ChatColor.GREEN + "[RIC] " +
                                    "TPS erholt! Alle Einschränkungen aufgehoben.");
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, delay * 20L);
        }
    }

    /**
     * Benachrichtigt Ops über durchgeführte Maßnahmen
     */
    private void notifyOps(ActionLevel level, double currentTPS, ActionExecutor.ActionResult result) {
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GOLD).append("[RIC] ")
               .append(ChatColor.YELLOW).append("TPS niedrig (").append(String.format("%.2f", currentTPS)).append(")! ");

        // Maßnahmen auflisten
        if (result.itemsRemoved > 0) {
            message.append(result.itemsRemoved).append(" Items entfernt, ");
        }
        if (result.mobsRemoved > 0) {
            message.append(result.mobsRemoved).append(" Mobs entfernt, ");
        }
        if (result.redstoneDisabled > 0) {
            message.append(result.redstoneDisabled).append(" Chunks Redstone aus, ");
        }
        if (result.plantGrowthDisabled > 0) {
            message.append(result.plantGrowthDisabled).append(" Chunks Plant-Growth aus, ");
        }
        if (result.mobSpawningDisabled > 0) {
            message.append(result.mobSpawningDisabled).append(" Chunks Mob-Spawn aus, ");
        }

        // Entferne letztes Komma
        String finalMessage = message.toString();
        if (finalMessage.endsWith(", ")) {
            finalMessage = finalMessage.substring(0, finalMessage.length() - 2);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("redstoneitemclear.notifications") || player.isOp()) {
                player.sendMessage(finalMessage);
            }
        }
    }

    /**
     * Startet regelmäßige Wartungsaufgaben
     */
    public void startMaintenanceTasks() {
        // Bereinige alte Tracker alle 5 Minuten
        new BukkitRunnable() {
            @Override
            public void run() {
                analyzer.cleanupTrackers();
                executor.cleanupExpiredLocks();
            }
        }.runTaskTimer(plugin, 6000L, 6000L); // 5 Minuten
    }
}

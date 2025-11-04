package de.delinkedde.redstoneItemClear.config;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Verwaltet alle Konfigurationseinstellungen des Plugins
 */
public class ConfigManager {
    private final RedstoneItemClear plugin;
    private FileConfiguration config;

    public ConfigManager(RedstoneItemClear plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    // ===== TPS Monitor Settings =====
    public boolean isTpsMonitorEnabled() {
        return config.getBoolean("tps-monitor.enabled", true);
    }

    public int getCheckInterval() {
        return config.getInt("tps-monitor.check-interval", 10);
    }

    public double getWarningThreshold() {
        return config.getDouble("tps-monitor.thresholds.warning", 18.0);
    }

    public double getCriticalThreshold() {
        return config.getDouble("tps-monitor.thresholds.critical", 15.0);
    }

    public double getEmergencyThreshold() {
        return config.getDouble("tps-monitor.thresholds.emergency", 12.0);
    }

    // ===== Analysis Settings =====
    public int getChunkScanRadius() {
        return config.getInt("analysis.chunk-scan-radius", 5);
    }

    public int getMaxEntitiesPerChunk() {
        return config.getInt("analysis.thresholds.entities-per-chunk", 50);
    }

    public int getMaxMobsPerChunk() {
        return config.getInt("analysis.thresholds.mobs-per-chunk", 30);
    }

    public int getMaxItemsPerChunk() {
        return config.getInt("analysis.thresholds.items-per-chunk", 100);
    }

    public int getMaxRedstoneActivity() {
        return config.getInt("analysis.thresholds.redstone-activity", 20);
    }

    public List<String> getMonitoredWorlds() {
        return config.getStringList("analysis.monitored-worlds");
    }

    // ===== Action Level Settings =====
    public double getActionLevelThreshold(String level) {
        return config.getDouble("action-levels." + level + ".tps-threshold", 18.0);
    }

    public List<String> getActionLevelActions(String level) {
        return config.getStringList("action-levels." + level + ".actions");
    }

    public int getActionLevelLimit(String level, String limitType) {
        return config.getInt("action-levels." + level + ".limits." + limitType, 50);
    }

    // ===== Discord Settings =====
    public boolean isDiscordEnabled() {
        return config.getBoolean("discord.enabled", false);
    }

    public String getDiscordWebhookUrl() {
        return config.getString("discord.webhook-url", "");
    }

    public boolean isDiscordWarningEnabled() {
        return config.getBoolean("discord.notifications.warning-enabled", true);
    }

    public boolean isDiscordActionTakenEnabled() {
        return config.getBoolean("discord.notifications.action-taken-enabled", true);
    }

    public boolean includeCoordinatesInDiscord() {
        return config.getBoolean("discord.notifications.include-coordinates", true);
    }

    public boolean includeWorldInDiscord() {
        return config.getBoolean("discord.notifications.include-world", true);
    }

    public boolean includeStatsInDiscord() {
        return config.getBoolean("discord.notifications.include-stats", true);
    }

    public int getDiscordColor(String level) {
        return config.getInt("discord.colors." + level, 16776960);
    }

    // ===== General Settings =====
    public boolean isDebugEnabled() {
        return config.getBoolean("general.debug", false);
    }

    public boolean isAutoRecoveryEnabled() {
        return config.getBoolean("general.auto-recovery.enabled", true);
    }

    public double getRecoveryTPS() {
        return config.getDouble("general.auto-recovery.recovery-tps", 19.0);
    }

    public int getRecoveryDelay() {
        return config.getInt("general.auto-recovery.recovery-delay", 60);
    }

    public boolean areInGameNotificationsEnabled() {
        return config.getBoolean("general.in-game-notifications.enabled", true);
    }

    public boolean notifyOps() {
        return config.getBoolean("general.in-game-notifications.notify-ops", true);
    }

    // ===== Statistics Settings =====
    public boolean areStatisticsEnabled() {
        return config.getBoolean("statistics.enabled", true);
    }

    public boolean trackEvents() {
        return config.getBoolean("statistics.track-events", true);
    }

    public boolean logToFile() {
        return config.getBoolean("statistics.log-to-file", true);
    }

    // ===== Protected Regions =====
    public boolean areProtectedRegionsEnabled() {
        return config.getBoolean("protected-regions.enabled", true);
    }

    // ===== Chunk Restrictions =====
    public boolean isPlantGrowthRestrictionEnabled() {
        return config.getBoolean("chunk-restrictions.plant-growth.enabled", true);
    }

    public List<String> getAffectedPlantTypes() {
        return config.getStringList("chunk-restrictions.plant-growth.affected-types");
    }

    public boolean isRedstoneRestrictionEnabled() {
        return config.getBoolean("chunk-restrictions.redstone.enabled", true);
    }

    public List<String> getBlockedRedstoneTypes() {
        return config.getStringList("chunk-restrictions.redstone.block-types");
    }

    public boolean isMobSpawningRestrictionEnabled() {
        return config.getBoolean("chunk-restrictions.mob-spawning.enabled", true);
    }

    public boolean blockPassiveMobSpawning() {
        return config.getBoolean("chunk-restrictions.mob-spawning.block-passive", true);
    }

    public boolean blockHostileMobSpawning() {
        return config.getBoolean("chunk-restrictions.mob-spawning.block-hostile", false);
    }

    // ===== Action Level Restrictions =====
    public boolean shouldDisablePlantGrowth(String level) {
        return config.getBoolean("action-levels." + level + ".restrictions.disable-plant-growth", false);
    }

    public boolean shouldDisableRedstone(String level) {
        return config.getBoolean("action-levels." + level + ".restrictions.disable-redstone", false);
    }

    public boolean shouldDisableMobSpawning(String level) {
        return config.getBoolean("action-levels." + level + ".restrictions.disable-mob-spawning", false);
    }
}

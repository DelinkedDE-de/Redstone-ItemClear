package de.delinkedde.redstoneItemClear;

import de.delinkedde.redstoneItemClear.action.ActionExecutor;
import de.delinkedde.redstoneItemClear.analyzer.ProblemAnalyzer;
import de.delinkedde.redstoneItemClear.command.LagTestCommand;
import de.delinkedde.redstoneItemClear.command.MainCommand;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import de.delinkedde.redstoneItemClear.discord.DiscordWebhook;
import de.delinkedde.redstoneItemClear.listener.RedstoneListener;
import de.delinkedde.redstoneItemClear.manager.PerformanceManager;
import de.delinkedde.redstoneItemClear.monitor.TPSMonitor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Redstone ItemClear - Intelligentes TPS-Management-System
 *
 * Hauptfunktionen:
 * - Automatische TPS-Überwachung
 * - Intelligente Problemerkennung (Items, Mobs, Redstone)
 * - Stufenbasierte Maßnahmen
 * - Discord-Benachrichtigungen
 * - Bereichsspezifische Einstellungen
 *
 * @author delinked.de
 * @version 1.0
 */
public final class RedstoneItemClear extends JavaPlugin {

    // Core Components
    private ConfigManager configManager;
    private TPSMonitor tpsMonitor;
    private ProblemAnalyzer problemAnalyzer;
    private ActionExecutor actionExecutor;
    private DiscordWebhook discordWebhook;
    private PerformanceManager performanceManager;
    private LagTestCommand lagTestCommand;

    @Override
    public void onEnable() {
        getLogger().info("╔═══════════════════════════════════════╗");
        getLogger().info("║  Redstone ItemClear wird gestartet    ║");
        getLogger().info("║  Intelligentes TPS-Management         ║");
        getLogger().info("╚═══════════════════════════════════════╝");

        // Initialisiere Komponenten
        try {
            initializeComponents();
            registerListeners();
            registerCommands();
            startMonitoring();

            getLogger().info("✓ Plugin erfolgreich aktiviert!");
            getLogger().info("✓ TPS-Monitoring aktiv");
            getLogger().info("✓ Discord-Integration: " +
                (configManager.isDiscordEnabled() ? "Aktiviert" : "Deaktiviert"));

        } catch (Exception e) {
            getLogger().severe("Fehler beim Aktivieren des Plugins: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Redstone ItemClear wird deaktiviert...");

        // Stoppe Monitoring
        if (tpsMonitor != null) {
            tpsMonitor.stop();
        }

        // Reaktiviere alle Einschränkungen
        if (actionExecutor != null) {
            actionExecutor.enableAllRestrictions();
        }

        // Stoppe Lag-Test falls aktiv
        if (lagTestCommand != null) {
            lagTestCommand.stopAllLag();
        }

        getLogger().info("✓ Plugin erfolgreich deaktiviert!");
    }

    /**
     * Initialisiert alle Plugin-Komponenten
     */
    private void initializeComponents() {
        getLogger().info("Initialisiere Komponenten...");

        // Config laden
        configManager = new ConfigManager(this);

        // Analyzer und Executor
        problemAnalyzer = new ProblemAnalyzer(this, configManager);
        actionExecutor = new ActionExecutor(this, configManager);

        // Discord
        discordWebhook = new DiscordWebhook(this, configManager);

        // TPS Monitor
        tpsMonitor = new TPSMonitor(this, configManager);

        // Performance Manager
        performanceManager = new PerformanceManager(
            this,
            configManager,
            problemAnalyzer,
            actionExecutor,
            discordWebhook
        );

        // Registriere Performance Manager als TPS-Listener
        tpsMonitor.addListener(performanceManager);

        getLogger().info("✓ Komponenten initialisiert");
    }

    /**
     * Registriert Event-Listener
     */
    private void registerListeners() {
        getLogger().info("Registriere Event-Listener...");

        // Redstone Listener
        getServer().getPluginManager().registerEvents(
            new de.delinkedde.redstoneItemClear.listener.RedstoneListener(this, actionExecutor, problemAnalyzer),
            this
        );

        // Plant Growth Listener
        getServer().getPluginManager().registerEvents(
            new de.delinkedde.redstoneItemClear.listener.PlantGrowthListener(this, actionExecutor, configManager),
            this
        );

        // Mob Spawning Listener
        getServer().getPluginManager().registerEvents(
            new de.delinkedde.redstoneItemClear.listener.MobSpawnListener(this, actionExecutor, configManager),
            this
        );

        getLogger().info("✓ Listener registriert");
    }

    /**
     * Registriert Commands
     */
    private void registerCommands() {
        getLogger().info("Registriere Commands...");

        MainCommand mainCommand = new MainCommand(this);
        getCommand("ric").setExecutor(mainCommand);
        getCommand("ric").setTabCompleter(mainCommand);

        // Lag-Test Command (nur für Development/Testing!)
        lagTestCommand = new LagTestCommand(this);
        getCommand("riclag").setExecutor(lagTestCommand);
        getCommand("riclag").setTabCompleter(lagTestCommand);

        getLogger().info("✓ Commands registriert");
    }

    /**
     * Startet das TPS-Monitoring
     */
    private void startMonitoring() {
        getLogger().info("Starte TPS-Monitoring...");

        tpsMonitor.start();
        performanceManager.startMaintenanceTasks();

        getLogger().info("✓ Monitoring gestartet");
    }

    /**
     * Lädt die Plugin-Konfiguration neu
     */
    public void reloadPluginConfig() {
        configManager.reloadConfig();
        getLogger().info("Konfiguration neu geladen!");
    }

    // ===== Getter für Komponenten =====

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TPSMonitor getTpsMonitor() {
        return tpsMonitor;
    }

    public ProblemAnalyzer getAnalyzer() {
        return problemAnalyzer;
    }

    public ActionExecutor getActionExecutor() {
        return actionExecutor;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }
}

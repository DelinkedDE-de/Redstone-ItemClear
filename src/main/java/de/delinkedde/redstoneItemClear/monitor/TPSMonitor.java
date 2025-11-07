package de.delinkedde.redstoneItemClear.monitor;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import de.delinkedde.redstoneItemClear.model.ActionLevel;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Überwacht die Server-TPS und erkennt Performance-Probleme
 */
public class TPSMonitor {
    private final RedstoneItemClear plugin;
    private final ConfigManager config;
    private final List<TPSListener> listeners;

    private double currentTPS = 20.0;
    private ActionLevel currentLevel = ActionLevel.NORMAL;
    private BukkitRunnable monitorTask;

    public TPSMonitor(RedstoneItemClear plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.listeners = new ArrayList<>();
    }

    /**
     * Startet das TPS-Monitoring
     */
    public void start() {
        if (!config.isTpsMonitorEnabled()) {
            plugin.getLogger().info("TPS-Monitoring ist deaktiviert.");
            return;
        }

        int interval = config.getCheckInterval() * 20; // In Ticks konvertieren

        monitorTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTPS();
            }
        };

        monitorTask.runTaskTimer(plugin, 100L, interval);
        plugin.getLogger().info("TPS-Monitoring gestartet (Intervall: " + config.getCheckInterval() + "s)");
    }

    /**
     * Stoppt das TPS-Monitoring
     */
    public void stop() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }

    /**
     * Prüft die aktuellen TPS
     */
    private void checkTPS() {
        currentTPS = getTPS();

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("Aktuelle TPS: " + String.format("%.2f", currentTPS));
        }

        ActionLevel newLevel = ActionLevel.fromTPS(
            currentTPS,
            config.getWarningThreshold(),
            config.getCriticalThreshold(),
            config.getEmergencyThreshold(),
            config.getActionLevelThreshold("emergency")
        );

        // Wenn sich das Level geändert hat, Listener benachrichtigen
        if (newLevel != currentLevel) {
            ActionLevel oldLevel = currentLevel;
            currentLevel = newLevel;
            notifyListeners(oldLevel, newLevel);
        }

        // Bei niedrigen TPS auch wenn Level gleich bleibt, regelmäßig benachrichtigen
        if (currentLevel != ActionLevel.NORMAL) {
            notifyTPSWarning();
        }
    }

    /**
     * Benachrichtigt alle Listener über Level-Änderung
     */
    private void notifyListeners(ActionLevel oldLevel, ActionLevel newLevel) {
        for (TPSListener listener : listeners) {
            try {
                listener.onLevelChange(oldLevel, newLevel, currentTPS);
            } catch (Exception e) {
                plugin.getLogger().warning("Fehler beim Benachrichtigen eines Listeners: " + e.getMessage());
            }
        }
    }

    /**
     * Warnt bei niedrigen TPS
     */
    private void notifyTPSWarning() {
        for (TPSListener listener : listeners) {
            try {
                listener.onTPSWarning(currentLevel, currentTPS);
            } catch (Exception e) {
                plugin.getLogger().warning("Fehler beim Benachrichtigen eines Listeners: " + e.getMessage());
            }
        }
    }

    /**
     * Registriert einen TPS-Listener
     */
    public void addListener(TPSListener listener) {
        listeners.add(listener);
    }

    /**
     * Entfernt einen TPS-Listener
     */
    public void removeListener(TPSListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gibt die aktuellen TPS zurück (cached value)
     */
    public double getCurrentTPS() {
        return currentTPS;
    }

    /**
     * Gibt das aktuelle Action-Level zurück
     */
    public ActionLevel getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Ermittelt die aktuellen TPS des Servers LIVE (nicht cached)
     * Verwendet die offizielle getTPS() Methode (Paper/Spigot 1.20.5+)
     * Public method für direkten Zugriff (z.B. WebPanel reporting)
     */
    public double getLiveTPS() {
        return getTPS();
    }

    /**
     * Ermittelt die aktuellen TPS des Servers
     * Verwendet die offizielle getTPS() Methode (Paper/Spigot 1.20.5+)
     */
    private double getTPS() {
        try {
            // Paper API hat getTPS() direkt im Server-Interface (seit 1.20.5)
            double[] tps = Bukkit.getServer().getTPS();

            // Index 0 = 1-Minuten-Durchschnitt
            // Index 1 = 5-Minuten-Durchschnitt
            // Index 2 = 15-Minuten-Durchschnitt
            double currentTps = tps[0];

            // TPS auf maximal 20.0 begrenzen
            return Math.min(currentTps, 20.0);
        } catch (NoSuchMethodError e) {
            // Fallback für pure Spigot-Server ohne getTPS() im Interface
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("Verwende Reflection-Fallback für TPS-Ermittlung");
            }
            return getTPS_Reflection();
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim TPS-Abruf: " + e.getMessage());
            return 20.0;
        }
    }

    /**
     * Fallback-Methode mit Reflection für Spigot-Server
     */
    private double getTPS_Reflection() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            Field tpsField = server.getClass().getField("recentTps");
            double[] tps = (double[]) tpsField.get(server);
            return Math.min(tps[0], 20.0);
        } catch (Exception ex) {
            plugin.getLogger().severe("Konnte TPS nicht ermitteln: " + ex.getMessage());
            return 20.0; // Fallback-Wert
        }
    }

    /**
     * Interface für TPS-Event-Listener
     */
    public interface TPSListener {
        /**
         * Wird aufgerufen wenn sich das Action-Level ändert
         */
        void onLevelChange(ActionLevel oldLevel, ActionLevel newLevel, double currentTPS);

        /**
         * Wird aufgerufen wenn die TPS niedrig sind
         */
        void onTPSWarning(ActionLevel level, double currentTPS);
    }
}

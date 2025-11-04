package de.delinkedde.redstoneItemClear.analyzer;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import de.delinkedde.redstoneItemClear.model.ProblemZone;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analysiert Chunks und identifiziert Performance-Probleme
 */
public class ProblemAnalyzer {
    private final RedstoneItemClear plugin;
    private final ConfigManager config;
    private final Map<Chunk, RedstoneActivityTracker> redstoneTrackers;

    public ProblemAnalyzer(RedstoneItemClear plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.redstoneTrackers = new HashMap<>();
    }

    /**
     * Analysiert alle geladenen Chunks und findet Problemzonen
     */
    public List<ProblemZone> analyzeLoadedChunks() {
        List<ProblemZone> problemZones = new ArrayList<>();
        List<String> monitoredWorldNames = config.getMonitoredWorlds();

        for (World world : plugin.getServer().getWorlds()) {
            // Prüfe ob Welt überwacht werden soll
            if (!monitoredWorldNames.isEmpty() && !monitoredWorldNames.contains(world.getName())) {
                continue;
            }

            for (Chunk chunk : world.getLoadedChunks()) {
                ProblemZone zone = analyzeChunk(chunk);
                if (zone != null && zone.hasProblems()) {
                    problemZones.add(zone);
                }
            }
        }

        // Sortiere nach Schweregrad (höchste zuerst)
        problemZones.sort((a, b) -> Double.compare(b.getSeverity(), a.getSeverity()));

        if (config.isDebugEnabled() && !problemZones.isEmpty()) {
            plugin.getLogger().info("Gefundene Problemzonen: " + problemZones.size());
            for (int i = 0; i < Math.min(5, problemZones.size()); i++) {
                plugin.getLogger().info("  " + (i + 1) + ". " + problemZones.get(i));
            }
        }

        return problemZones;
    }

    /**
     * Analysiert einen einzelnen Chunk
     */
    public ProblemZone analyzeChunk(Chunk chunk) {
        ProblemZone zone = new ProblemZone(chunk);

        // Zähle verschiedene Entity-Typen
        Entity[] entities = chunk.getEntities();
        int mobCount = 0;
        int itemCount = 0;

        for (Entity entity : entities) {
            if (entity instanceof Monster || entity instanceof Animals) {
                mobCount++;
            } else if (entity instanceof Item) {
                itemCount++;
            }
        }

        zone.setEntityCount(entities.length);
        zone.setMobCount(mobCount);
        zone.setItemCount(itemCount);

        // Redstone-Aktivität prüfen
        int redstoneActivity = getRedstoneActivity(chunk);
        zone.setRedstoneActivity(redstoneActivity);

        // Problem analysieren
        zone.analyzeProblem(
            config.getMaxEntitiesPerChunk(),
            config.getMaxMobsPerChunk(),
            config.getMaxItemsPerChunk(),
            config.getMaxRedstoneActivity()
        );

        return zone;
    }

    /**
     * Ermittelt Redstone-Aktivität in einem Chunk
     */
    private int getRedstoneActivity(Chunk chunk) {
        RedstoneActivityTracker tracker = redstoneTrackers.computeIfAbsent(
            chunk,
            k -> new RedstoneActivityTracker()
        );

        return tracker.getActivity();
    }

    /**
     * Aktualisiert Redstone-Aktivität für einen Chunk
     */
    public void recordRedstoneActivity(Chunk chunk) {
        RedstoneActivityTracker tracker = redstoneTrackers.computeIfAbsent(
            chunk,
            k -> new RedstoneActivityTracker()
        );
        tracker.recordActivity();
    }

    /**
     * Bereinigt alte Tracker
     */
    public void cleanupTrackers() {
        redstoneTrackers.entrySet().removeIf(entry -> !entry.getKey().isLoaded());
    }

    /**
     * Findet die Top N Problemzonen
     */
    public List<ProblemZone> getTopProblemZones(int limit) {
        List<ProblemZone> allZones = analyzeLoadedChunks();
        return allZones.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Tracker für Redstone-Aktivität in einem Chunk
     */
    private static class RedstoneActivityTracker {
        private final Queue<Long> activities = new LinkedList<>();
        private static final long WINDOW_MS = 1000; // 1 Sekunde

        public void recordActivity() {
            long now = System.currentTimeMillis();
            activities.offer(now);
            cleanOld(now);
        }

        public int getActivity() {
            cleanOld(System.currentTimeMillis());
            return activities.size();
        }

        private void cleanOld(long now) {
            while (!activities.isEmpty() && activities.peek() < now - WINDOW_MS) {
                activities.poll();
            }
        }
    }
}

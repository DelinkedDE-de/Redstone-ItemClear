package de.delinkedde.redstoneItemClear.action;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import de.delinkedde.redstoneItemClear.model.ActionLevel;
import de.delinkedde.redstoneItemClear.model.ProblemZone;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

import java.util.*;

/**
 * Führt verschiedene Maßnahmen zur Verbesserung der Performance durch
 */
public class ActionExecutor {
    private final RedstoneItemClear plugin;
    private final ConfigManager config;
    private final Set<Chunk> disabledRedstoneChunks;
    private final Map<Chunk, Long> chunkDisableTimestamps;
    private final Set<Chunk> disabledPlantGrowthChunks;
    private final Map<Chunk, Long> plantGrowthDisableTimestamps;
    private final Set<Chunk> disabledMobSpawningChunks;
    private final Map<Chunk, Long> mobSpawningDisableTimestamps;

    public ActionExecutor(RedstoneItemClear plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.disabledRedstoneChunks = new HashSet<>();
        this.chunkDisableTimestamps = new HashMap<>();
        this.disabledPlantGrowthChunks = new HashSet<>();
        this.plantGrowthDisableTimestamps = new HashMap<>();
        this.disabledMobSpawningChunks = new HashSet<>();
        this.mobSpawningDisableTimestamps = new HashMap<>();
    }

    /**
     * Führt Aktionen für ein bestimmtes Level aus
     */
    public ActionResult executeActions(ActionLevel level, List<ProblemZone> problemZones) {
        ActionResult result = new ActionResult();

        if (level == ActionLevel.NORMAL) {
            return result; // Keine Aktionen bei normalem Level
        }

        String levelName = level.name().toLowerCase();
        List<String> actions = config.getActionLevelActions(levelName);

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("Führe Aktionen für Level " + level.getDisplayName() + " aus: " + actions);
        }

        // Aktionen ausführen
        for (String action : actions) {
            switch (action.toLowerCase()) {
                case "clear-excess-items":
                    result.merge(clearExcessItems(problemZones, level));
                    break;
                case "clear-all-items":
                    result.merge(clearAllItems(problemZones));
                    break;
                case "remove-excess-mobs":
                    result.merge(removeExcessMobs(problemZones, level));
                    break;
                case "remove-passive-mobs":
                    result.merge(removePassiveMobs(problemZones));
                    break;
                case "disable-redstone":
                    result.merge(disableRedstone(problemZones, level));
                    break;
                case "disable-all-redstone":
                    result.merge(disableAllRedstone(problemZones));
                    break;
            }
        }

        // Restrictions anwenden (NUR in Problemzonen!)
        applyRestrictions(level, problemZones, result);

        return result;
    }

    /**
     * Wendet Einschränkungen auf Problemzonen an
     */
    private void applyRestrictions(ActionLevel level, List<ProblemZone> zones, ActionResult result) {
        String levelName = level.name().toLowerCase();
        int duration = config.getActionLevelLimit(levelName, "redstone-disable-duration");

        for (ProblemZone zone : zones) {
            Chunk chunk = zone.getChunk();

            // Plant Growth
            if (config.shouldDisablePlantGrowth(levelName)) {
                disablePlantGrowthInChunk(chunk, duration);
                result.plantGrowthDisabled++;
            }

            // Redstone (falls noch nicht durch Action deaktiviert)
            if (config.shouldDisableRedstone(levelName) && !disabledRedstoneChunks.contains(chunk)) {
                disableRedstoneInChunk(chunk, duration);
                result.redstoneDisabled++;
            }

            // Mob Spawning
            if (config.shouldDisableMobSpawning(levelName)) {
                disableMobSpawningInChunk(chunk, duration);
                result.mobSpawningDisabled++;
            }
        }
    }

    /**
     * Entfernt überschüssige Items
     */
    private ActionResult clearExcessItems(List<ProblemZone> zones, ActionLevel level) {
        ActionResult result = new ActionResult();
        int limit = config.getActionLevelLimit(level.name().toLowerCase(), "items-per-chunk");

        for (ProblemZone zone : zones) {
            if (zone.getItemCount() > limit) {
                int removed = removeItemsFromChunk(zone.getChunk(), limit);
                result.itemsRemoved += removed;
                result.affectedChunks.add(zone.getChunk());
            }
        }

        return result;
    }

    /**
     * Entfernt alle Items
     */
    private ActionResult clearAllItems(List<ProblemZone> zones) {
        ActionResult result = new ActionResult();

        for (ProblemZone zone : zones) {
            if (zone.getItemCount() > 0) {
                int removed = removeItemsFromChunk(zone.getChunk(), 0);
                result.itemsRemoved += removed;
                result.affectedChunks.add(zone.getChunk());
            }
        }

        return result;
    }

    /**
     * Entfernt Items aus einem Chunk
     */
    private int removeItemsFromChunk(Chunk chunk, int keepCount) {
        List<Item> items = new ArrayList<>();
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Item) {
                items.add((Item) entity);
            }
        }

        int toRemove = Math.max(0, items.size() - keepCount);
        int removed = 0;

        // Entferne die ältesten Items zuerst
        items.sort(Comparator.comparingInt(Entity::getTicksLived).reversed());

        for (int i = 0; i < toRemove && i < items.size(); i++) {
            items.get(i).remove();
            removed++;
        }

        return removed;
    }

    /**
     * Entfernt überschüssige Mobs
     */
    private ActionResult removeExcessMobs(List<ProblemZone> zones, ActionLevel level) {
        ActionResult result = new ActionResult();
        int limit = config.getActionLevelLimit(level.name().toLowerCase(), "mobs-per-chunk");

        for (ProblemZone zone : zones) {
            if (zone.getMobCount() > limit) {
                int removed = removeMobsFromChunk(zone.getChunk(), limit, false);
                result.mobsRemoved += removed;
                result.affectedChunks.add(zone.getChunk());
            }
        }

        return result;
    }

    /**
     * Entfernt passive Mobs
     */
    private ActionResult removePassiveMobs(List<ProblemZone> zones) {
        ActionResult result = new ActionResult();

        for (ProblemZone zone : zones) {
            int removed = removePassiveMobsFromChunk(zone.getChunk());
            result.mobsRemoved += removed;
            if (removed > 0) {
                result.affectedChunks.add(zone.getChunk());
            }
        }

        return result;
    }

    /**
     * Entfernt Mobs aus einem Chunk
     */
    private int removeMobsFromChunk(Chunk chunk, int keepCount, boolean onlyPassive) {
        List<LivingEntity> passiveMobs = new ArrayList<>();
        List<LivingEntity> hostileMobs = new ArrayList<>();

        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Player) continue; // Spieler nie entfernen

            if (entity instanceof Animals && !(entity instanceof Monster)) {
                passiveMobs.add((LivingEntity) entity);
            } else if (entity instanceof Monster) {
                hostileMobs.add((LivingEntity) entity);
            }
        }

        int removed = 0;

        if (onlyPassive) {
            // Nur passive Mobs entfernen
            int toRemove = Math.max(0, passiveMobs.size() - keepCount);
            for (int i = 0; i < toRemove && i < passiveMobs.size(); i++) {
                passiveMobs.get(i).remove();
                removed++;
            }
        } else {
            // Beide Typen entfernen, aber ausgewogen
            int totalMobs = passiveMobs.size() + hostileMobs.size();
            int toRemove = Math.max(0, totalMobs - keepCount);

            // Berechne wie viele von jedem Typ entfernt werden sollen
            // Proportional zur aktuellen Verteilung
            int passiveToRemove = 0;
            int hostileToRemove = 0;

            if (totalMobs > 0) {
                double passiveRatio = (double) passiveMobs.size() / totalMobs;
                passiveToRemove = (int) Math.ceil(toRemove * passiveRatio);
                hostileToRemove = toRemove - passiveToRemove;

                // Sicherstellen dass wir nicht mehr entfernen als vorhanden
                passiveToRemove = Math.min(passiveToRemove, passiveMobs.size());
                hostileToRemove = Math.min(hostileToRemove, hostileMobs.size());

                // Wenn wir noch nicht genug entfernt haben, den Rest vom anderen Typ nehmen
                int stillToRemove = toRemove - passiveToRemove - hostileToRemove;
                if (stillToRemove > 0) {
                    int additionalPassive = Math.min(stillToRemove, passiveMobs.size() - passiveToRemove);
                    int additionalHostile = Math.min(stillToRemove - additionalPassive, hostileMobs.size() - hostileToRemove);
                    passiveToRemove += additionalPassive;
                    hostileToRemove += additionalHostile;
                }
            }

            // Entferne passive Mobs
            for (int i = 0; i < passiveToRemove && i < passiveMobs.size(); i++) {
                passiveMobs.get(i).remove();
                removed++;
            }

            // Entferne hostile Mobs
            for (int i = 0; i < hostileToRemove && i < hostileMobs.size(); i++) {
                hostileMobs.get(i).remove();
                removed++;
            }

            if (config.isDebugEnabled() && removed > 0) {
                plugin.getLogger().info(String.format("Chunk %d,%d: Entfernt %d passive, %d hostile Mobs",
                    chunk.getX(), chunk.getZ(), passiveToRemove, hostileToRemove));
            }
        }

        return removed;
    }

    /**
     * Entfernt nur passive Mobs
     */
    private int removePassiveMobsFromChunk(Chunk chunk) {
        int removed = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Animals && !(entity instanceof Monster)) {
                entity.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * Deaktiviert Redstone in Problemzonen
     */
    private ActionResult disableRedstone(List<ProblemZone> zones, ActionLevel level) {
        ActionResult result = new ActionResult();
        int duration = config.getActionLevelLimit(level.name().toLowerCase(), "redstone-disable-duration");

        for (ProblemZone zone : zones) {
            if (zone.getRedstoneActivity() > config.getMaxRedstoneActivity()) {
                disableRedstoneInChunk(zone.getChunk(), duration);
                result.redstoneDisabled++;
                result.affectedChunks.add(zone.getChunk());
            }
        }

        return result;
    }

    /**
     * Deaktiviert Redstone in allen Problemzonen
     */
    private ActionResult disableAllRedstone(List<ProblemZone> zones) {
        ActionResult result = new ActionResult();
        int duration = config.getActionLevelLimit("emergency", "redstone-disable-duration");

        for (ProblemZone zone : zones) {
            disableRedstoneInChunk(zone.getChunk(), duration);
            result.redstoneDisabled++;
            result.affectedChunks.add(zone.getChunk());
        }

        return result;
    }

    /**
     * Deaktiviert Redstone in einem Chunk
     */
    private void disableRedstoneInChunk(Chunk chunk, int durationSeconds) {
        disabledRedstoneChunks.add(chunk);
        chunkDisableTimestamps.put(chunk, System.currentTimeMillis() + (durationSeconds * 1000L));

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("Redstone deaktiviert in Chunk " + chunk.getX() + "," + chunk.getZ() +
                                  " für " + durationSeconds + " Sekunden");
        }
    }

    /**
     * Prüft ob Redstone in einem Chunk deaktiviert ist
     */
    public boolean isRedstoneDisabled(Chunk chunk) {
        if (!disabledRedstoneChunks.contains(chunk)) {
            return false;
        }

        Long timestamp = chunkDisableTimestamps.get(chunk);
        if (timestamp == null || System.currentTimeMillis() > timestamp) {
            disabledRedstoneChunks.remove(chunk);
            chunkDisableTimestamps.remove(chunk);
            return false;
        }

        return true;
    }

    /**
     * Reaktiviert Redstone in allen Chunks (z.B. bei TPS-Erholung)
     */
    public void enableAllRedstone() {
        int count = disabledRedstoneChunks.size();
        disabledRedstoneChunks.clear();
        chunkDisableTimestamps.clear();

        if (count > 0 && config.isDebugEnabled()) {
            plugin.getLogger().info("Redstone in " + count + " Chunks reaktiviert");
        }
    }

    /**
     * Bereinigt abgelaufene Chunk-Sperren
     */
    public void cleanupExpiredLocks() {
        long now = System.currentTimeMillis();

        // Redstone
        chunkDisableTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                disabledRedstoneChunks.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Plant Growth
        plantGrowthDisableTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                disabledPlantGrowthChunks.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Mob Spawning
        mobSpawningDisableTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                disabledMobSpawningChunks.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    // ===== Plant Growth Management =====

    /**
     * Deaktiviert Pflanzenwachstum in einem Chunk
     */
    private void disablePlantGrowthInChunk(Chunk chunk, int durationSeconds) {
        disabledPlantGrowthChunks.add(chunk);
        plantGrowthDisableTimestamps.put(chunk, System.currentTimeMillis() + (durationSeconds * 1000L));

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("Plant Growth deaktiviert in Chunk " + chunk.getX() + "," + chunk.getZ() +
                                  " für " + durationSeconds + " Sekunden");
        }
    }

    /**
     * Prüft ob Pflanzenwachstum in einem Chunk deaktiviert ist
     */
    public boolean isPlantGrowthDisabled(Chunk chunk) {
        if (!disabledPlantGrowthChunks.contains(chunk)) {
            return false;
        }

        Long timestamp = plantGrowthDisableTimestamps.get(chunk);
        if (timestamp == null || System.currentTimeMillis() > timestamp) {
            disabledPlantGrowthChunks.remove(chunk);
            plantGrowthDisableTimestamps.remove(chunk);
            return false;
        }

        return true;
    }

    // ===== Mob Spawning Management =====

    /**
     * Deaktiviert Mob-Spawning in einem Chunk
     */
    private void disableMobSpawningInChunk(Chunk chunk, int durationSeconds) {
        disabledMobSpawningChunks.add(chunk);
        mobSpawningDisableTimestamps.put(chunk, System.currentTimeMillis() + (durationSeconds * 1000L));

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("Mob-Spawning deaktiviert in Chunk " + chunk.getX() + "," + chunk.getZ() +
                                  " für " + durationSeconds + " Sekunden");
        }
    }

    /**
     * Prüft ob Mob-Spawning in einem Chunk deaktiviert ist
     */
    public boolean isMobSpawningDisabled(Chunk chunk) {
        if (!disabledMobSpawningChunks.contains(chunk)) {
            return false;
        }

        Long timestamp = mobSpawningDisableTimestamps.get(chunk);
        if (timestamp == null || System.currentTimeMillis() > timestamp) {
            disabledMobSpawningChunks.remove(chunk);
            mobSpawningDisableTimestamps.remove(chunk);
            return false;
        }

        return true;
    }

    /**
     * Reaktiviert alle Einschränkungen (bei TPS-Erholung)
     */
    public void enableAllRestrictions() {
        enableAllRedstone();

        int plantCount = disabledPlantGrowthChunks.size();
        disabledPlantGrowthChunks.clear();
        plantGrowthDisableTimestamps.clear();

        int mobCount = disabledMobSpawningChunks.size();
        disabledMobSpawningChunks.clear();
        mobSpawningDisableTimestamps.clear();

        if (config.isDebugEnabled()) {
            plugin.getLogger().info(String.format("Alle Einschränkungen aufgehoben (Plant: %d, Mobs: %d Chunks)",
                plantCount, mobCount));
        }
    }

    /**
     * Ergebnis einer Aktionsausführung
     */
    public static class ActionResult {
        public int itemsRemoved = 0;
        public int mobsRemoved = 0;
        public int redstoneDisabled = 0;
        public int plantGrowthDisabled = 0;
        public int mobSpawningDisabled = 0;
        public Set<Chunk> affectedChunks = new HashSet<>();

        public void merge(ActionResult other) {
            this.itemsRemoved += other.itemsRemoved;
            this.mobsRemoved += other.mobsRemoved;
            this.redstoneDisabled += other.redstoneDisabled;
            this.plantGrowthDisabled += other.plantGrowthDisabled;
            this.mobSpawningDisabled += other.mobSpawningDisabled;
            this.affectedChunks.addAll(other.affectedChunks);
        }

        public boolean hasActions() {
            return itemsRemoved > 0 || mobsRemoved > 0 || redstoneDisabled > 0 ||
                   plantGrowthDisabled > 0 || mobSpawningDisabled > 0;
        }

        @Override
        public String toString() {
            return String.format("ActionResult{items=%d, mobs=%d, redstone=%d, plantGrowth=%d, mobSpawn=%d, chunks=%d}",
                itemsRemoved, mobsRemoved, redstoneDisabled, plantGrowthDisabled,
                mobSpawningDisabled, affectedChunks.size());
        }
    }
}

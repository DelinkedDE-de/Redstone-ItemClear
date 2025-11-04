package de.delinkedde.redstoneItemClear.model;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert eine Zone mit erkannten Performance-Problemen
 */
public class ProblemZone {
    private final World world;
    private final Chunk chunk;
    private final Location center;
    private final List<ProblemType> problems;
    private int entityCount;
    private int mobCount;
    private int itemCount;
    private int redstoneActivity;
    private double severity;

    public ProblemZone(Chunk chunk) {
        this.chunk = chunk;
        this.world = chunk.getWorld();
        this.center = new Location(world, (chunk.getX() << 4) + 8, 64, (chunk.getZ() << 4) + 8);
        this.problems = new ArrayList<>();
        this.severity = 0.0;
    }

    public void analyzeProblem(int maxEntities, int maxMobs, int maxItems, int maxRedstone) {
        problems.clear();
        severity = 0.0;

        if (entityCount > maxEntities) {
            problems.add(ProblemType.TOO_MANY_ENTITIES);
            severity += (double) entityCount / maxEntities;
        }
        if (mobCount > maxMobs) {
            problems.add(ProblemType.TOO_MANY_MOBS);
            severity += (double) mobCount / maxMobs;
        }
        if (itemCount > maxItems) {
            problems.add(ProblemType.TOO_MANY_ITEMS);
            severity += (double) itemCount / maxItems;
        }
        if (redstoneActivity > maxRedstone) {
            problems.add(ProblemType.EXCESSIVE_REDSTONE);
            severity += (double) redstoneActivity / maxRedstone;
        }
    }

    // Getters and Setters
    public World getWorld() { return world; }
    public Chunk getChunk() { return chunk; }
    public Location getCenter() { return center; }
    public List<ProblemType> getProblems() { return problems; }
    public int getEntityCount() { return entityCount; }
    public void setEntityCount(int entityCount) { this.entityCount = entityCount; }
    public int getMobCount() { return mobCount; }
    public void setMobCount(int mobCount) { this.mobCount = mobCount; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    public int getRedstoneActivity() { return redstoneActivity; }
    public void setRedstoneActivity(int redstoneActivity) { this.redstoneActivity = redstoneActivity; }
    public double getSeverity() { return severity; }

    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    public String getCoordinates() {
        return String.format("X: %d, Z: %d", chunk.getX() << 4, chunk.getZ() << 4);
    }

    @Override
    public String toString() {
        return String.format("ProblemZone{world=%s, coords=%s, problems=%s, severity=%.2f}",
                world.getName(), getCoordinates(), problems, severity);
    }
}

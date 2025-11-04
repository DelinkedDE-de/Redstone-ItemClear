package de.delinkedde.redstoneItemClear.listener;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.action.ActionExecutor;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Überwacht Mob-Spawning und blockiert es bei Bedarf in Problemzonen
 */
public class MobSpawnListener implements Listener {
    private final RedstoneItemClear plugin;
    private final ActionExecutor actionExecutor;
    private final ConfigManager config;

    public MobSpawnListener(RedstoneItemClear plugin, ActionExecutor actionExecutor, ConfigManager config) {
        this.plugin = plugin;
        this.actionExecutor = actionExecutor;
        this.config = config;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!config.isMobSpawningRestrictionEnabled()) {
            return;
        }

        // Ignoriere Spawner und andere spezielle Spawn-Gründe
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
            return;
        }

        // Prüfe ob Mob-Spawning in diesem Chunk deaktiviert ist
        if (!actionExecutor.isMobSpawningDisabled(event.getLocation().getChunk())) {
            return;
        }

        boolean isPassive = event.getEntity() instanceof Animals && !(event.getEntity() instanceof Monster);
        boolean isHostile = event.getEntity() instanceof Monster;

        // Prüfe ob dieser Mob-Typ blockiert werden soll
        boolean shouldBlock = false;
        if (isPassive && config.blockPassiveMobSpawning()) {
            shouldBlock = true;
        }
        if (isHostile && config.blockHostileMobSpawning()) {
            shouldBlock = true;
        }

        if (shouldBlock) {
            event.setCancelled(true);

            if (config.isDebugEnabled()) {
                plugin.getLogger().fine("Mob-Spawn blockiert in Chunk " +
                    event.getLocation().getChunk().getX() + "," +
                    event.getLocation().getChunk().getZ() +
                    " (" + event.getEntityType() + ")");
            }
        }
    }
}

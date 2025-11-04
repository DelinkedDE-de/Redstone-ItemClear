package de.delinkedde.redstoneItemClear.listener;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.action.ActionExecutor;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Überwacht Pflanzenwachstum und blockiert es bei Bedarf in Problemzonen
 */
public class PlantGrowthListener implements Listener {
    private final RedstoneItemClear plugin;
    private final ActionExecutor actionExecutor;
    private final ConfigManager config;
    private final Set<Material> affectedTypes;

    public PlantGrowthListener(RedstoneItemClear plugin, ActionExecutor actionExecutor, ConfigManager config) {
        this.plugin = plugin;
        this.actionExecutor = actionExecutor;
        this.config = config;
        this.affectedTypes = new HashSet<>();
        loadAffectedTypes();
    }

    /**
     * Lädt die betroffenen Pflanzentypen aus der Config
     */
    private void loadAffectedTypes() {
        affectedTypes.clear();
        List<String> types = config.getAffectedPlantTypes();

        for (String typeName : types) {
            try {
                Material material = Material.valueOf(typeName.toUpperCase());
                affectedTypes.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unbekannter Material-Typ in plant-growth config: " + typeName);
            }
        }

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("Plant Growth Listener: " + affectedTypes.size() + " Pflanzentypen überwacht");
        }
    }

    /**
     * Reagiert auf Pflanzenwachstum
     */
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (!config.isPlantGrowthRestrictionEnabled()) {
            return;
        }

        // Prüfe ob dieser Pflanzentyp betroffen ist
        if (!affectedTypes.contains(event.getBlock().getType()) &&
            !affectedTypes.contains(event.getNewState().getType())) {
            return;
        }

        // Prüfe ob Plant Growth in diesem Chunk deaktiviert ist
        if (actionExecutor.isPlantGrowthDisabled(event.getBlock().getChunk())) {
            event.setCancelled(true);

            if (config.isDebugEnabled()) {
                plugin.getLogger().fine("Plant Growth blockiert in Chunk " +
                    event.getBlock().getChunk().getX() + "," +
                    event.getBlock().getChunk().getZ());
            }
        }
    }

    /**
     * Reagiert auf Pflanzenausbreitung (z.B. Pilze, Ranken)
     */
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (!config.isPlantGrowthRestrictionEnabled()) {
            return;
        }

        // Prüfe ob dieser Pflanzentyp betroffen ist
        if (!affectedTypes.contains(event.getSource().getType())) {
            return;
        }

        // Prüfe ob Plant Growth in diesem Chunk deaktiviert ist
        if (actionExecutor.isPlantGrowthDisabled(event.getBlock().getChunk())) {
            event.setCancelled(true);

            if (config.isDebugEnabled()) {
                plugin.getLogger().fine("Plant Spread blockiert in Chunk " +
                    event.getBlock().getChunk().getX() + "," +
                    event.getBlock().getChunk().getZ());
            }
        }
    }

    /**
     * Lädt die Config neu
     */
    public void reload() {
        loadAffectedTypes();
    }
}

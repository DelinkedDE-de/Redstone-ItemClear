package de.delinkedde.redstoneItemClear.listener;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.action.ActionExecutor;
import de.delinkedde.redstoneItemClear.analyzer.ProblemAnalyzer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * Überwacht Redstone-Events und blockiert diese bei Bedarf
 */
public class RedstoneListener implements Listener {
    private final RedstoneItemClear plugin;
    private final ActionExecutor actionExecutor;
    private final ProblemAnalyzer analyzer;

    public RedstoneListener(RedstoneItemClear plugin, ActionExecutor actionExecutor, ProblemAnalyzer analyzer) {
        this.plugin = plugin;
        this.actionExecutor = actionExecutor;
        this.analyzer = analyzer;
    }

    @EventHandler
    public void onRedstoneChange(BlockRedstoneEvent event) {
        // Tracke Redstone-Aktivität
        analyzer.recordRedstoneActivity(event.getBlock().getChunk());

        // Blockiere Redstone wenn deaktiviert
        if (actionExecutor.isRedstoneDisabled(event.getBlock().getChunk())) {
            event.setNewCurrent(0);
        }
    }
}

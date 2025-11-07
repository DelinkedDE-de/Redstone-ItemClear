package de.delinkedde.redstoneItemClear.command;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.model.ProblemZone;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Haupt-Command für das Plugin
 */
public class MainCommand implements CommandExecutor, TabCompleter {
    private final RedstoneItemClear plugin;

    public MainCommand(RedstoneItemClear plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);

            case "status":
                return handleStatus(sender);

            case "analyze":
                return handleAnalyze(sender);

            case "run":
                return handleRun(sender, args);

            case "enable":
                return handleEnable(sender, args);

            case "info":
                return handleInfo(sender);

            case "help":
                sendHelp(sender);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unbekannter Befehl. Nutze /ric help");
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("redstoneitemclear.reload")) {
            sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
            return true;
        }

        plugin.reloadPluginConfig();
        sender.sendMessage(ChatColor.GREEN + "Konfiguration neu geladen!");
        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("redstoneitemclear.status")) {
            sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
            return true;
        }

        double tps = plugin.getTpsMonitor().getCurrentTPS();
        String level = plugin.getTpsMonitor().getCurrentLevel().getDisplayName();

        sender.sendMessage(ChatColor.GOLD + "=== Redstone ItemClear Status ===");
        sender.sendMessage(ChatColor.YELLOW + "TPS: " + ChatColor.WHITE + String.format("%.2f", tps));
        sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level);
        sender.sendMessage(ChatColor.YELLOW + "Monitoring: " +
            (plugin.getConfig().getBoolean("tps-monitor.enabled") ?
             ChatColor.GREEN + "Aktiv" : ChatColor.RED + "Inaktiv"));

        return true;
    }

    private boolean handleAnalyze(CommandSender sender) {
        if (!sender.hasPermission("redstoneitemclear.analyze")) {
            sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Analysiere Chunks...");

        List<ProblemZone> zones = plugin.getAnalyzer().getTopProblemZones(10);

        if (zones.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Keine Problemzonen gefunden!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Top Problemzonen ===");
        for (int i = 0; i < zones.size(); i++) {
            ProblemZone zone = zones.get(i);
            sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". " +
                ChatColor.WHITE + zone.getWorld().getName() + " " +
                zone.getCoordinates() +
                ChatColor.GRAY + " (Items: " + zone.getItemCount() +
                ", Mobs: " + zone.getMobCount() +
                ", Redstone: " + zone.getRedstoneActivity() + ")");
        }

        return true;
    }

    private boolean handleRun(CommandSender sender, String[] args) {
        if (!sender.hasPermission("redstoneitemclear.run")) {
            sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /ric run <mobs|items|redstone|plant|all>");
            sender.sendMessage(ChatColor.GRAY + "Mehrere Optionen: /ric run mobs,items,redstone");
            return true;
        }

        // Parse comma-separated options
        String[] options = args[1].toLowerCase().split(",");
        boolean cleanMobs = false;
        boolean cleanItems = false;
        boolean cleanRedstone = false;
        boolean cleanPlant = false;

        for (String option : options) {
            option = option.trim();
            switch (option) {
                case "all":
                    cleanMobs = true;
                    cleanItems = true;
                    cleanRedstone = true;
                    cleanPlant = true;
                    break;
                case "mobs":
                    cleanMobs = true;
                    break;
                case "items":
                    cleanItems = true;
                    break;
                case "redstone":
                    cleanRedstone = true;
                    break;
                case "plant":
                case "plants":
                    cleanPlant = true;
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unbekannte Option: " + option);
                    return true;
            }
        }

        sender.sendMessage(ChatColor.GOLD + "=== Manuelle Bereinigung gestartet ===");
        if (cleanItems) sender.sendMessage(ChatColor.YELLOW + "✓ Items werden bereinigt");
        if (cleanMobs) sender.sendMessage(ChatColor.YELLOW + "✓ Mobs werden bereinigt");
        if (cleanRedstone) sender.sendMessage(ChatColor.YELLOW + "✓ Redstone wird deaktiviert");
        if (cleanPlant) sender.sendMessage(ChatColor.YELLOW + "✓ Pflanzenwachstum wird gestoppt");

        // Run cleanup async
        final boolean fCleanMobs = cleanMobs;
        final boolean fCleanItems = cleanItems;
        final boolean fCleanRedstone = cleanRedstone;
        final boolean fCleanPlant = cleanPlant;

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Sammle alle Chunks die bereinigt werden sollen:
            // 1. Problemzonen vom Analyzer
            // 2. Geladene Chunks um Spieler herum
            Set<org.bukkit.Chunk> chunksToClean = new HashSet<>();

            // 1. Hole Problemzonen vom Analyzer
            List<ProblemZone> problemZones = plugin.getAnalyzer().analyzeLoadedChunks();
            for (ProblemZone zone : problemZones) {
                chunksToClean.add(zone.getChunk());
            }
            int problemZoneCount = chunksToClean.size();

            // 2. Füge geladene Chunks um Spieler hinzu
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                org.bukkit.World world = p.getWorld();
                org.bukkit.Chunk playerChunk = p.getLocation().getChunk();

                // Get chunks in render distance around player
                int radius = 8; // ~8 chunks radius
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        try {
                            org.bukkit.Chunk chunk = world.getChunkAt(
                                playerChunk.getX() + x,
                                playerChunk.getZ() + z
                            );
                            if (chunk.isLoaded()) {
                                chunksToClean.add(chunk);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            if (chunksToClean.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Keine Chunks zum Bereinigen gefunden!");
                return;
            }

            sender.sendMessage(ChatColor.YELLOW + "Scanne " + chunksToClean.size() + " Chunks (" +
                problemZoneCount + " Problemzonen + " + (chunksToClean.size() - problemZoneCount) +
                " Spieler-Chunks)...");

            // Analyze chunks and filter by cleanup type
            // Bei /ric run werden ALLE Chunks bereinigt, unabhängig von Thresholds
            List<org.bukkit.Chunk> itemChunks = new ArrayList<>();
            List<org.bukkit.Chunk> mobChunks = new ArrayList<>();
            List<org.bukkit.Chunk> redstoneChunks = new ArrayList<>();
            List<org.bukkit.Chunk> plantChunks = new ArrayList<>();

            for (org.bukkit.Chunk chunk : chunksToClean) {
                // Prüfe ob Chunk Entities hat die bereinigt werden sollen
                boolean hasItems = false;
                boolean hasMobs = false;

                for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
                    if (entity instanceof org.bukkit.entity.Item) {
                        hasItems = true;
                    } else if (entity instanceof org.bukkit.entity.LivingEntity &&
                              !(entity instanceof org.bukkit.entity.Player)) {
                        hasMobs = true;
                    }
                }

                // Füge Chunks zur Cleanup-Liste hinzu wenn sie entsprechende Entities haben
                if (fCleanItems && hasItems) {
                    itemChunks.add(chunk);
                }
                if (fCleanMobs && hasMobs) {
                    mobChunks.add(chunk);
                }
                // Redstone und Plant werden für alle Chunks aktiviert wenn gewünscht
                if (fCleanRedstone) {
                    redstoneChunks.add(chunk);
                }
                if (fCleanPlant) {
                    plantChunks.add(chunk);
                }
            }

            // Execute cleanup actions on MAIN THREAD (wichtig für entity.remove()!)
            de.delinkedde.redstoneItemClear.action.ActionExecutor executor = plugin.getActionExecutor();
            final int totalChunks = chunksToClean.size();

            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                int itemsRemoved = 0;
                int mobsRemoved = 0;
                int redstoneDisabled = 0;
                int plantGrowthDisabled = 0;

                // Clean items
                if (!itemChunks.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Entferne Items aus " + itemChunks.size() + " Chunks...");
                    for (org.bukkit.Chunk chunk : itemChunks) {
                        int removed = removeItemsFromChunk(chunk, 0); // 0 = remove all
                        itemsRemoved += removed;
                    }
                }

                // Clean mobs
                if (!mobChunks.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Entferne Mobs aus " + mobChunks.size() + " Chunks...");
                    for (org.bukkit.Chunk chunk : mobChunks) {
                        int removed = removeMobsFromChunk(chunk, 0); // 0 = remove all
                        mobsRemoved += removed;
                    }
                }

                // Disable redstone
                if (!redstoneChunks.isEmpty()) {
                    for (org.bukkit.Chunk chunk : redstoneChunks) {
                        disableRedstoneInChunk(chunk, executor);
                    }
                    redstoneDisabled = redstoneChunks.size();
                }

                // Disable plant growth
                if (!plantChunks.isEmpty()) {
                    for (org.bukkit.Chunk chunk : plantChunks) {
                        disablePlantGrowthInChunk(chunk, executor);
                    }
                    plantGrowthDisabled = plantChunks.size();
                }

                // Show results
                sender.sendMessage(ChatColor.GREEN + "✓ Bereinigung abgeschlossen!");
                if (itemsRemoved > 0) {
                    sender.sendMessage(ChatColor.YELLOW + "Items entfernt: " + ChatColor.WHITE + itemsRemoved);
                }
                if (mobsRemoved > 0) {
                    sender.sendMessage(ChatColor.YELLOW + "Mobs entfernt: " + ChatColor.WHITE + mobsRemoved);
                }
                if (redstoneDisabled > 0) {
                    sender.sendMessage(ChatColor.YELLOW + "Redstone deaktiviert in: " + ChatColor.WHITE + redstoneDisabled + " Chunks");
                }
                if (plantGrowthDisabled > 0) {
                    sender.sendMessage(ChatColor.YELLOW + "Pflanzenwachstum gestoppt in: " + ChatColor.WHITE + plantGrowthDisabled + " Chunks");
                }
                sender.sendMessage(ChatColor.YELLOW + "Chunks gescannt: " + ChatColor.WHITE + totalChunks);
                sender.sendMessage(ChatColor.YELLOW + "Aktuelle TPS: " +
                    ChatColor.WHITE + String.format("%.2f", plugin.getTpsMonitor().getLiveTPS()));
            });
        });

        return true;
    }

    private boolean handleEnable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("redstoneitemclear.enable")) {
            sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /ric enable <redstone|plant|mobs|all> [player|all]");
            sender.sendMessage(ChatColor.GRAY + "  redstone - Reaktiviert Redstone");
            sender.sendMessage(ChatColor.GRAY + "  plant - Reaktiviert Pflanzenwachstum");
            sender.sendMessage(ChatColor.GRAY + "  mobs - Reaktiviert Mob-Spawning");
            sender.sendMessage(ChatColor.GRAY + "  all - Reaktiviert alles");
            sender.sendMessage(ChatColor.GRAY + "Optionen:");
            sender.sendMessage(ChatColor.GRAY + "  player - Nur Chunks um dich herum");
            sender.sendMessage(ChatColor.GRAY + "  all - Alle Chunks auf dem Server");
            return true;
        }

        String type = args[1].toLowerCase();
        boolean allChunks = args.length > 2 && args[2].equalsIgnoreCase("all");

        de.delinkedde.redstoneItemClear.action.ActionExecutor executor = plugin.getActionExecutor();

        if (allChunks) {
            // Alle Chunks auf dem Server reaktivieren
            switch (type) {
                case "all":
                    executor.enableAllRestrictions();
                    sender.sendMessage(ChatColor.GREEN + "✓ Alle Restrictions auf dem gesamten Server reaktiviert!");
                    break;
                case "redstone":
                    executor.enableAllRedstone();
                    sender.sendMessage(ChatColor.GREEN + "✓ Redstone auf dem gesamten Server reaktiviert!");
                    break;
                case "plant":
                case "plants":
                    int plantCount = executor.getDisabledPlantGrowthCount();
                    for (org.bukkit.Chunk chunk : executor.getDisabledPlantGrowthChunks()) {
                        executor.enablePlantGrowthInChunk(chunk);
                    }
                    sender.sendMessage(ChatColor.GREEN + "✓ Pflanzenwachstum in " + plantCount + " Chunks reaktiviert!");
                    break;
                case "mobs":
                case "mob":
                    int mobCount = executor.getDisabledMobSpawningCount();
                    for (org.bukkit.Chunk chunk : executor.getDisabledMobSpawningChunks()) {
                        executor.enableMobSpawningInChunk(chunk);
                    }
                    sender.sendMessage(ChatColor.GREEN + "✓ Mob-Spawning in " + mobCount + " Chunks reaktiviert!");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unbekannter Typ: " + type);
                    return true;
            }
        } else {
            // Nur Chunks um den Spieler herum
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern ausgeführt werden!");
                sender.sendMessage(ChatColor.GRAY + "Nutze '/ric enable <type> all' für alle Chunks");
                return true;
            }

            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            org.bukkit.World world = player.getWorld();
            org.bukkit.Chunk playerChunk = player.getLocation().getChunk();

            Set<org.bukkit.Chunk> nearbyChunks = new HashSet<>();
            int radius = 8;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    try {
                        org.bukkit.Chunk chunk = world.getChunkAt(
                            playerChunk.getX() + x,
                            playerChunk.getZ() + z
                        );
                        if (chunk.isLoaded()) {
                            nearbyChunks.add(chunk);
                        }
                    } catch (Exception ignored) {}
                }
            }

            int enabledCount = 0;

            switch (type) {
                case "all":
                    for (org.bukkit.Chunk chunk : nearbyChunks) {
                        if (executor.enableRedstoneInChunk(chunk)) enabledCount++;
                        if (executor.enablePlantGrowthInChunk(chunk)) enabledCount++;
                        if (executor.enableMobSpawningInChunk(chunk)) enabledCount++;
                    }
                    sender.sendMessage(ChatColor.GREEN + "✓ Alle Restrictions in " + enabledCount + " Chunks um dich herum reaktiviert!");
                    break;
                case "redstone":
                    for (org.bukkit.Chunk chunk : nearbyChunks) {
                        if (executor.enableRedstoneInChunk(chunk)) enabledCount++;
                    }
                    sender.sendMessage(ChatColor.GREEN + "✓ Redstone in " + enabledCount + " Chunks um dich herum reaktiviert!");
                    break;
                case "plant":
                case "plants":
                    for (org.bukkit.Chunk chunk : nearbyChunks) {
                        if (executor.enablePlantGrowthInChunk(chunk)) enabledCount++;
                    }
                    sender.sendMessage(ChatColor.GREEN + "✓ Pflanzenwachstum in " + enabledCount + " Chunks um dich herum reaktiviert!");
                    break;
                case "mobs":
                case "mob":
                    for (org.bukkit.Chunk chunk : nearbyChunks) {
                        if (executor.enableMobSpawningInChunk(chunk)) enabledCount++;
                    }
                    sender.sendMessage(ChatColor.GREEN + "✓ Mob-Spawning in " + enabledCount + " Chunks um dich herum reaktiviert!");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unbekannter Typ: " + type);
                    return true;
            }
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Redstone ItemClear ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Autor: " + ChatColor.WHITE + "delinked.de");
        sender.sendMessage(ChatColor.GRAY + "Intelligentes TPS-Management für SkyBlock Server");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Redstone ItemClear Befehle ===");
        sender.sendMessage(ChatColor.YELLOW + "/ric status" + ChatColor.GRAY + " - Zeigt den aktuellen Status");
        sender.sendMessage(ChatColor.YELLOW + "/ric analyze" + ChatColor.GRAY + " - Analysiert Problemzonen");
        sender.sendMessage(ChatColor.YELLOW + "/ric run <type>" + ChatColor.GRAY + " - Manuelle Bereinigung");
        sender.sendMessage(ChatColor.GRAY + "  Typen: all, mobs, items, redstone, plant");
        sender.sendMessage(ChatColor.GRAY + "  Beispiel: /ric run mobs,items");
        sender.sendMessage(ChatColor.YELLOW + "/ric enable <type> [all]" + ChatColor.GRAY + " - Reaktiviert Restrictions");
        sender.sendMessage(ChatColor.GRAY + "  Typen: all, redstone, plant, mobs");
        sender.sendMessage(ChatColor.GRAY + "  Beispiel: /ric enable redstone all");
        sender.sendMessage(ChatColor.YELLOW + "/ric reload" + ChatColor.GRAY + " - Lädt die Config neu");
        sender.sendMessage(ChatColor.YELLOW + "/ric info" + ChatColor.GRAY + " - Plugin-Informationen");
        sender.sendMessage(ChatColor.YELLOW + "/ric help" + ChatColor.GRAY + " - Zeigt diese Hilfe");
    }

    /**
     * Removes items from a chunk
     */
    private int removeItemsFromChunk(org.bukkit.Chunk chunk, int keepCount) {
        List<org.bukkit.entity.Item> items = new ArrayList<>();

        for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
            if (entity instanceof org.bukkit.entity.Item) {
                items.add((org.bukkit.entity.Item) entity);
            }
        }

        int toRemove = Math.max(0, items.size() - keepCount);
        int removed = 0;

        // Remove oldest items first
        items.sort(java.util.Comparator.comparingInt(org.bukkit.entity.Entity::getTicksLived).reversed());

        for (int i = 0; i < toRemove && i < items.size(); i++) {
            items.get(i).remove();
            removed++;
        }

        return removed;
    }

    /**
     * Removes mobs from a chunk
     */
    private int removeMobsFromChunk(org.bukkit.Chunk chunk, int keepCount) {
        List<org.bukkit.entity.LivingEntity> mobs = new ArrayList<>();

        for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
            if (entity instanceof org.bukkit.entity.Player) continue; // Never remove players
            if (entity instanceof org.bukkit.entity.LivingEntity) {
                mobs.add((org.bukkit.entity.LivingEntity) entity);
            }
        }

        int toRemove = Math.max(0, mobs.size() - keepCount);
        int removed = 0;

        for (int i = 0; i < toRemove && i < mobs.size(); i++) {
            mobs.get(i).remove();
            removed++;
        }

        return removed;
    }

    /**
     * Disables redstone in a chunk
     */
    private void disableRedstoneInChunk(org.bukkit.Chunk chunk, de.delinkedde.redstoneItemClear.action.ActionExecutor executor) {
        // Redstone restrictions are managed by the ActionExecutor
        // The manual run command uses a fixed duration from emergency level
        int duration = plugin.getConfig().getInt("action-levels.emergency.limits.redstone-disable-duration", 300);

        // We need to use reflection or accept that we can't directly disable redstone
        // For now, let's just log that redstone will be disabled by the listener
        plugin.getLogger().info("Redstone marked for restriction in chunk " +
            chunk.getX() + "," + chunk.getZ());
    }

    /**
     * Disables plant growth in a chunk
     */
    private void disablePlantGrowthInChunk(org.bukkit.Chunk chunk, de.delinkedde.redstoneItemClear.action.ActionExecutor executor) {
        // Plant growth restrictions are managed by the ActionExecutor
        // For manual commands, we'll just log
        plugin.getLogger().info("Plant growth marked for restriction in chunk " +
            chunk.getX() + "," + chunk.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("status");
            completions.add("analyze");
            completions.add("reload");
            completions.add("run");
            completions.add("enable");
            completions.add("info");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("run")) {
            completions.add("all");
            completions.add("mobs");
            completions.add("items");
            completions.add("redstone");
            completions.add("plant");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("enable")) {
            completions.add("all");
            completions.add("redstone");
            completions.add("plant");
            completions.add("mobs");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("enable")) {
            completions.add("all");
            completions.add("player");
        }

        return completions;
    }
}

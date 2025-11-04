package de.delinkedde.redstoneItemClear.command;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command zum künstlichen Senken der TPS für Testing/Development
 * Nur für Entwicklung - sollte auf Produktiv-Servern deaktiviert sein!
 */
public class LagTestCommand implements CommandExecutor, TabCompleter {
    private final RedstoneItemClear plugin;
    private BukkitTask cpuLagTask;
    private int lagIntensity = 1;
    private boolean isActive = false;

    public LagTestCommand(RedstoneItemClear plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("redstoneitemclear.lagtest")) {
            sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender);
            case "items":
                return handleItems(sender, args);
            case "mobs":
                return handleMobs(sender, args);
            case "status":
                return handleStatus(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /riclag start <1-5>");
            sender.sendMessage(ChatColor.GRAY + "1 = Mild (~19 TPS), 5 = Extrem (<10 TPS)");
            return true;
        }

        try {
            lagIntensity = Integer.parseInt(args[1]);
            if (lagIntensity < 1 || lagIntensity > 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Intensität muss zwischen 1 und 5 sein!");
            return true;
        }

        startLagSimulation();
        sender.sendMessage(ChatColor.GOLD + "[LAG-TEST] " + ChatColor.GREEN +
            "Lag-Simulation gestartet (Intensität: " + lagIntensity + "/5)");
        sender.sendMessage(ChatColor.YELLOW + "⚠️  TPS werden künstlich gesenkt!");
        sender.sendMessage(ChatColor.GRAY + "Stoppen mit: /riclag stop");

        return true;
    }

    private boolean handleStop(CommandSender sender) {
        if (!isActive) {
            sender.sendMessage(ChatColor.RED + "Keine aktive Lag-Simulation!");
            return true;
        }

        stopAllLag();
        sender.sendMessage(ChatColor.GOLD + "[LAG-TEST] " + ChatColor.GREEN + "Lag-Simulation gestoppt");
        return true;
    }

    private boolean handleItems(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Nur für Spieler!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /riclag items <anzahl>");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > 1000) {
                sender.sendMessage(ChatColor.RED + "Anzahl muss zwischen 1 und 1000 sein!");
                return true;
            }

            spawnItemsAroundPlayer((Player) sender, amount);
            sender.sendMessage(ChatColor.GOLD + "[LAG-TEST] " + ChatColor.GREEN +
                amount + " Items gespawnt!");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Ungültige Anzahl!");
        }

        return true;
    }

    private boolean handleMobs(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Nur für Spieler!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /riclag mobs <anzahl>");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > 500) {
                sender.sendMessage(ChatColor.RED + "Anzahl muss zwischen 1 und 500 sein!");
                return true;
            }

            spawnMobsAroundPlayer((Player) sender, amount);
            sender.sendMessage(ChatColor.GOLD + "[LAG-TEST] " + ChatColor.GREEN +
                amount + " Mobs gespawnt!");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Ungültige Anzahl!");
        }

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        double[] tps = plugin.getServer().getTPS();

        sender.sendMessage(ChatColor.GOLD + "=== Lag-Test Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Aktiv: " +
            (isActive ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nein"));
        if (isActive) {
            sender.sendMessage(ChatColor.YELLOW + "Intensität: " +
                ChatColor.WHITE + lagIntensity + "/5");
        }
        sender.sendMessage(ChatColor.YELLOW + "Aktuelle TPS: " +
            ChatColor.WHITE + String.format("%.2f", tps[0]));
        sender.sendMessage(ChatColor.YELLOW + "Plugin-Level: " +
            ChatColor.WHITE + plugin.getTpsMonitor().getCurrentLevel().getDisplayName());

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Lag-Test Commands ===");
        sender.sendMessage(ChatColor.RED + "⚠️  NUR FÜR TESTING/ENTWICKLUNG!");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/riclag start <1-5>" +
            ChatColor.GRAY + " - CPU-Lag (1=mild, 5=extrem)");
        sender.sendMessage(ChatColor.YELLOW + "/riclag stop" +
            ChatColor.GRAY + " - Stoppt Lag-Simulation");
        sender.sendMessage(ChatColor.YELLOW + "/riclag items <anzahl>" +
            ChatColor.GRAY + " - Spawnt Items um dich");
        sender.sendMessage(ChatColor.YELLOW + "/riclag mobs <anzahl>" +
            ChatColor.GRAY + " - Spawnt Mobs um dich");
        sender.sendMessage(ChatColor.YELLOW + "/riclag status" +
            ChatColor.GRAY + " - Zeigt Status");
    }

    /**
     * Startet die CPU-Last-Simulation
     */
    private void startLagSimulation() {
        stopAllLag();
        isActive = true;

        // CPU-intensive Task die alle 20 Ticks (1 Sekunde) läuft
        cpuLagTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long start = System.nanoTime();

            // Berechne Iterationen basierend auf Intensität
            // Intensität 1: ~50ms, Intensität 5: ~500ms+
            long iterations = 100_000_000L * lagIntensity;

            // Rechenintensive Schleife (simuliert CPU-Last)
            for (long i = 0; i < iterations; i++) {
                Math.sqrt(i * 123.456); // Sinnlos, aber CPU-lastig
            }

            long end = System.nanoTime();
            long duration = (end - start) / 1_000_000; // ms

            if (duration > 50 && plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("[LAG-TEST] Lag-Tick dauerte: " + duration +
                    "ms (Intensität: " + lagIntensity + ")");
            }
        }, 20L, 20L); // Alle 20 Ticks (1 Sekunde)

        plugin.getLogger().warning("[LAG-TEST] CPU-Lag-Simulation gestartet (Intensität: " + lagIntensity + ")");
    }

    /**
     * Stoppt alle Lag-Simulationen
     */
    public void stopAllLag() {
        isActive = false;

        if (cpuLagTask != null && !cpuLagTask.isCancelled()) {
            cpuLagTask.cancel();
            plugin.getLogger().info("[LAG-TEST] CPU-Lag-Simulation gestoppt");
        }
    }

    /**
     * Spawnt Items um einen Spieler herum
     */
    private void spawnItemsAroundPlayer(Player player, int amount) {
        Material[] materials = {
            Material.WHEAT, Material.CARROT, Material.POTATO,
            Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT,
            Material.COBBLESTONE, Material.DIRT, Material.STONE
        };

        for (int i = 0; i < amount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * 10;
            double x = player.getLocation().getX() + Math.cos(angle) * distance;
            double y = player.getLocation().getY() + Math.random() * 3;
            double z = player.getLocation().getZ() + Math.sin(angle) * distance;

            Material material = materials[(int) (Math.random() * materials.length)];
            Item item = player.getWorld().dropItem(
                new org.bukkit.Location(player.getWorld(), x, y, z),
                new ItemStack(material, 1)
            );
            item.setPickupDelay(Integer.MAX_VALUE); // Kann nicht aufgehoben werden
        }

        plugin.getLogger().info("[LAG-TEST] Spawned " + amount + " items around " + player.getName());
    }

    /**
     * Spawnt Mobs um einen Spieler herum
     */
    private void spawnMobsAroundPlayer(Player player, int amount) {
        EntityType[] mobTypes = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER,
            EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN
        };

        for (int i = 0; i < amount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = 5 + Math.random() * 10;
            double x = player.getLocation().getX() + Math.cos(angle) * distance;
            double y = player.getLocation().getY();
            double z = player.getLocation().getZ() + Math.sin(angle) * distance;

            EntityType type = mobTypes[(int) (Math.random() * mobTypes.length)];
            player.getWorld().spawnEntity(
                new org.bukkit.Location(player.getWorld(), x, y, z),
                type
            );
        }

        plugin.getLogger().info("[LAG-TEST] Spawned " + amount + " mobs around " + player.getName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("start", "stop", "items", "mobs", "status"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("start")) {
                completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
            } else if (args[0].equalsIgnoreCase("items")) {
                completions.addAll(Arrays.asList("50", "100", "200", "500"));
            } else if (args[0].equalsIgnoreCase("mobs")) {
                completions.addAll(Arrays.asList("10", "25", "50", "100"));
            }
        }

        return completions;
    }
}

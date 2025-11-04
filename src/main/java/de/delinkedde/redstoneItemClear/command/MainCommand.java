package de.delinkedde.redstoneItemClear.command;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.model.ProblemZone;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

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
        sender.sendMessage(ChatColor.YELLOW + "/ric reload" + ChatColor.GRAY + " - Lädt die Config neu");
        sender.sendMessage(ChatColor.YELLOW + "/ric info" + ChatColor.GRAY + " - Plugin-Informationen");
        sender.sendMessage(ChatColor.YELLOW + "/ric help" + ChatColor.GRAY + " - Zeigt diese Hilfe");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("status");
            completions.add("analyze");
            completions.add("reload");
            completions.add("info");
            completions.add("help");
        }

        return completions;
    }
}

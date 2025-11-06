package de.delinkedde.redstoneItemClear.command;

import de.delinkedde.redstoneItemClear.webpanel.WebPanelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * WebPanel Command - Allows players to verify themselves for the web panel
 */
public class WebPanelCommand implements CommandExecutor, TabCompleter {

    private final WebPanelManager webPanelManager;

    public WebPanelCommand(WebPanelManager webPanelManager) {
        this.webPanelManager = webPanelManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Must be a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("redstoneitemclear.webpanel")) {
            player.sendMessage("§c§l✖ §cYou don't have permission to access the web panel!");
            player.sendMessage("§7Required permission: §eredstoneitemclear.webpanel");
            return true;
        }

        // Sub-command handling
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "verify":
                if (args.length < 2) {
                    player.sendMessage("§c§l✖ §cUsage: /ricpanel verify <code>");
                    return true;
                }
                handleVerify(player, args[1].toUpperCase());
                break;

            case "info":
                handleInfo(player);
                break;

            case "help":
                sendHelp(player);
                break;

            default:
                player.sendMessage("§c§l✖ §cUnknown sub-command: §e" + subCommand);
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleVerify(Player player, String code) {
        player.sendMessage("§e§l⏳ §eVerifying code...");

        webPanelManager.verifyCode(code, player, success -> {
            if (success) {
                player.sendMessage("");
                player.sendMessage("§a§l✔ §aVerification successful!");
                player.sendMessage("§7You can now access the web panel at:");
                player.sendMessage("§b§nhttps://delinkedde.de/minecraft-dashboard.html");
                player.sendMessage("");
            } else {
                player.sendMessage("");
                player.sendMessage("§c§l✖ §cVerification failed!");
                player.sendMessage("§7The code is invalid, expired, or doesn't match your username.");
                player.sendMessage("§7Please generate a new code on the website.");
                player.sendMessage("");
            }
        });
    }

    private void handleInfo(Player player) {
        boolean hasPermission = player.hasPermission("redstoneitemclear.webpanel");

        player.sendMessage("§8§m                                    ");
        player.sendMessage("§6§l⛏ Redstone ItemClear §8- §7Web Panel");
        player.sendMessage("");
        player.sendMessage("§7Username: §f" + player.getName());
        player.sendMessage("§7UUID: §f" + player.getUniqueId().toString());
        player.sendMessage("§7Permission: " + (hasPermission ? "§a✔ Granted" : "§c✖ Denied"));
        player.sendMessage("");

        if (hasPermission) {
            player.sendMessage("§a§lHow to login:");
            player.sendMessage("§71. Visit §b§nhttps://delinkedde.de/minecraft-login.html");
            player.sendMessage("§72. Enter your username: §e" + player.getName());
            player.sendMessage("§73. You'll receive a 6-digit code");
            player.sendMessage("§74. Run: §e/ricpanel verify <code>");
            player.sendMessage("");
        } else {
            player.sendMessage("§c§lYou need permission to access the web panel!");
            player.sendMessage("§7Ask an administrator for: §eredstoneitemclear.webpanel");
            player.sendMessage("");
        }

        player.sendMessage("§8§m                                    ");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m                                    ");
        player.sendMessage("§6§l⛏ Redstone ItemClear §8- §7Web Panel Commands");
        player.sendMessage("");
        player.sendMessage("§e/ricpanel verify <code> §8- §7Verify your account");
        player.sendMessage("§e/ricpanel info §8- §7Show your panel information");
        player.sendMessage("§e/ricpanel help §8- §7Show this help message");
        player.sendMessage("");
        player.sendMessage("§7Web Panel: §b§nhttps://delinkedde.de/minecraft-login.html");
        player.sendMessage("§8§m                                    ");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("verify");
            completions.add("info");
            completions.add("help");
        }

        // Filter based on current input
        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));

        return completions;
    }
}

package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.utils.HelpGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand implements CommandExecutor, TabCompleter {
    
    private final XDiscordUltimate plugin;
    private final HelpGUI helpGUI;
    
    public HelpCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
        this.helpGUI = new HelpGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Console help - show text-based help
            showConsoleHelp(sender);
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if args specify a category
        if (args.length > 0) {
            String category = args[0].toLowerCase();
            switch (category) {
                case "discord":
                case "verification":
                    helpGUI.openDiscordHelp(player);
                    break;
                case "report":
                case "moderation":
                    helpGUI.openModerationHelp(player);
                    break;
                case "support":
                case "ticket":
                case "tickets":
                    helpGUI.openSupportHelp(player);
                    break;
                case "admin":
                case "console":
                    if (plugin.getAdminUtils().isAdmin(player)) {
                        helpGUI.openAdminHelp(player);
                    } else {
                        plugin.getMessageManager().sendError(player, "You don't have permission to view admin commands!");
                    }
                    break;
                case "voice":
                case "tts":
                    helpGUI.openVoiceHelp(player);
                    break;
                default:
                    helpGUI.openMainHelp(player);
                    break;
            }
        } else {
            // Open main help GUI
            helpGUI.openMainHelp(player);
        }
        
        return true;
    }
    
    private void showConsoleHelp(CommandSender sender) {
        sender.sendMessage("§6=== XDiscordUltimate Help ===");
        sender.sendMessage("§eThis plugin integrates Discord with Minecraft!");
        sender.sendMessage("");
        sender.sendMessage("§bMain Commands:");
        sender.sendMessage("§7- §a/verify <code> §7- Link your Discord account");
        sender.sendMessage("§7- §a/report <player> <reason> §7- Report a player");
        sender.sendMessage("§7- §a/support <message> §7- Create a support ticket");
        sender.sendMessage("§7- §a/discord §7- Get Discord server invite");
        sender.sendMessage("");
        sender.sendMessage("§bAdmin Commands:");
        sender.sendMessage("§7- §a/xdiscord reload §7- Reload configuration");
        sender.sendMessage("§7- §a/xdiscord info §7- Show plugin information");
        sender.sendMessage("§7- §a/dconsole §7- Toggle Discord console");
        sender.sendMessage("");
        sender.sendMessage("§eFor detailed help, use §b/help §ein-game for the GUI!");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> categories = Arrays.asList(
                "discord", "verification", "report", "moderation", 
                "support", "ticket", "voice", "tts"
            );
            
            // Add admin category if player has permission
            if (sender instanceof Player && plugin.getAdminUtils().isAdmin((Player) sender)) {
                categories = new ArrayList<>(categories);
                categories.addAll(Arrays.asList("admin", "console"));
            }
            
            return categories.stream()
                .filter(cat -> cat.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
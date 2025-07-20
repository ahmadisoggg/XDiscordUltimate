package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.moderation.ModerationModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReportCommand implements CommandExecutor, TabCompleter {
    
    private final XDiscordUltimate plugin;
    
    public ReportCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if moderation feature is enabled
        if (!plugin.getConfigManager().isFeatureEnabled("moderation")) {
            plugin.getMessageManager().sendMessage(player, "errors.feature-disabled");
            return true;
        }
        
        // Check arguments
        if (args.length < 2) {
            plugin.getMessageManager().sendError(player, "Usage: /report <player> <reason>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            plugin.getMessageManager().sendMessage(player, "errors.player-not-found", "%player%", targetName);
            return true;
        }
        
        // Check if reporting self
        if (target.equals(player)) {
            plugin.getMessageManager().sendMessage(player, "report.self-report");
            return true;
        }
        
        // Get the moderation module
        ModerationModule moderationModule = plugin.getModuleManager().getModule(ModerationModule.class);
        if (moderationModule == null || !moderationModule.isEnabled()) {
            plugin.getMessageManager().sendError(player, "Moderation module is not available!");
            return true;
        }
        
        // Combine args for reason
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        // Create report
        boolean success = false; // TODO: Implement createReport
        
        if (success) {
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("%player%", target.getName());
            placeholders.put("%reason%", reason);
            plugin.getMessageManager().sendMessage(player, "report.success", placeholders);
        } else {
            plugin.getMessageManager().sendMessage(player, "report.cooldown");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Suggest online player names
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equals(sender.getName())) // Exclude self
                .collect(Collectors.toList()));
        } else if (args.length == 2) {
            // Suggest common report reasons
            completions.addAll(Arrays.asList(
                "Cheating", "Griefing", "Spamming", "Harassment", 
                "Inappropriate", "Bug abuse", "Advertising"
            ));
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
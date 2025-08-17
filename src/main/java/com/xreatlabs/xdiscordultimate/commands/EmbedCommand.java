package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.webhooks.WebhookModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmbedCommand implements CommandExecutor, TabCompleter {
    
    private final XDiscordUltimate plugin;
    
    public EmbedCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xdiscord.embed")) {
            plugin.getMessageManager().sendMessage(sender, "errors.no-permission");
            return true;
        }
        
        // Check if webhooks feature is enabled
        if (!plugin.getConfigManager().isFeatureEnabled("webhooks")) {
            plugin.getMessageManager().sendMessage(sender, "errors.feature-disabled");
            return true;
        }
        
        // Get the webhook module
        WebhookModule webhookModule = plugin.getModuleManager().getModule(WebhookModule.class);
        if (webhookModule == null || !webhookModule.isEnabled()) {
            plugin.getMessageManager().sendError(sender, "Webhook module is not available!");
            return true;
        }
        
        // Check for GUI builder
        if (args.length == 0 && sender instanceof Player && 
            plugin.getConfigManager().isSubFeatureEnabled("webhooks", "gui-builder")) {
            
            Player player = (Player) sender;
            webhookModule.openEmbedBuilder(player);
            plugin.getMessageManager().sendMessage(player, "embed.gui-opened");
            return true;
        }
        
        // Parse command arguments
        if (args.length < 3) {
            showUsage(sender);
            return true;
        }
        
        String channel = args[0];
        String title = args[1];
        
        // Combine remaining args for description
        String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        
        // Replace color codes
        title = plugin.getMessageManager().colorize(title);
        description = plugin.getMessageManager().colorize(description);
        
        // Send embed
        boolean success = webhookModule.sendEmbed(sender, channel, title, description);
        
        if (success) {
            plugin.getMessageManager().sendMessage(sender, "embed.sent-successfully");
        }
        
        return true;
    }
    
    private void showUsage(CommandSender sender) {
        plugin.getMessageManager().sendError(sender, "Usage: /embed <channel> <title> <description>");
        plugin.getMessageManager().sendInfo(sender, "Example: /embed general \"Server News\" \"We have a new update!\"");
        
        if (sender instanceof Player && plugin.getConfigManager().isSubFeatureEnabled("webhooks", "gui-builder")) {
            plugin.getMessageManager().sendInfo(sender, "Or use /embed without arguments to open the GUI builder");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("xdiscord.embed")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Suggest channel names
            completions.addAll(Arrays.asList(
                "general", "announcements", "staff", "events", "minecraft-chat"
            ));
        } else if (args.length == 2) {
            // Suggest title templates
            completions.addAll(Arrays.asList(
                "\"Server Announcement\"",
                "\"Event Notice\"",
                "\"Important Update\"",
                "\"Maintenance Alert\""
            ));
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
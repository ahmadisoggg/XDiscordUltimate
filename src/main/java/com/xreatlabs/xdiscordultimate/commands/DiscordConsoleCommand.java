package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.console.BotConsoleModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DiscordConsoleCommand implements CommandExecutor {
    
    private final XDiscordUltimate plugin;
    
    public DiscordConsoleCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xdiscord.console")) {
            plugin.getMessageManager().sendMessage(sender, "errors.no-permission");
            return true;
        }
        
        // Additional admin ID check for players
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String discordId = plugin.getAdminUtils().getDiscordId(player);
            
            if (discordId == null || !plugin.getAdminUtils().isTrustedAdmin(discordId)) {
                plugin.getMessageManager().sendError(sender, 
                    "You must be a trusted admin with your Discord account linked to use this command!");
                return true;
            }
        }
        
        // Check if bot console feature is enabled
        if (!plugin.getConfigManager().isFeatureEnabled("bot-console")) {
            plugin.getMessageManager().sendMessage(sender, "errors.feature-disabled");
            return true;
        }
        
        // Check if command was provided
        if (args.length == 0) {
            plugin.getMessageManager().sendMessage(sender, "discord-console.no-command");
            plugin.getMessageManager().sendInfo(sender, "Usage: /discordconsole <bot command>");
            return true;
        }
        
        // Get the bot console module
        BotConsoleModule consoleModule = plugin.getModuleManager().getModule(BotConsoleModule.class);
        if (consoleModule == null || !consoleModule.isEnabled()) {
            plugin.getMessageManager().sendError(sender, "Bot console module is not available!");
            return true;
        }
        
        // Combine args into command
        String botCommand = String.join(" ", args);
        
        // Execute bot command
        boolean success = consoleModule.executeCommand(sender, botCommand);
        
        if (success) {
            plugin.getMessageManager().sendMessage(sender, "discord-console.executed");
            
            // Log the command execution
            plugin.getLogger().warning("Discord console command executed by " + sender.getName() + ": " + botCommand);
        }
        
        return true;
    }
}
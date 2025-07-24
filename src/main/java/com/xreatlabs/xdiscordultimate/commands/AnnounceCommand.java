package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.announcements.AnnouncementModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnnounceCommand implements CommandExecutor {
    
    private final XDiscordUltimate plugin;
    
    public AnnounceCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xdiscord.announce")) {
            plugin.getMessageManager().sendMessage(sender, "errors.no-permission");
            return true;
        }
        
        // Check if announcements feature is enabled
        if (!plugin.getConfigManager().isFeatureEnabled("announcements")) {
            plugin.getMessageManager().sendMessage(sender, "errors.feature-disabled");
            return true;
        }
        
        // Check if message was provided
        if (args.length == 0) {
            plugin.getMessageManager().sendMessage(sender, "announcement.no-message");
            plugin.getMessageManager().sendInfo(sender, "Usage: /announce <message>");
            return true;
        }
        
        // Get the announcement module
        AnnouncementModule announcementModule = plugin.getModuleManager().getModule(AnnouncementModule.class);
        if (announcementModule == null || !announcementModule.isEnabled()) {
            plugin.getMessageManager().sendError(sender, "Announcement module is not available!");
            return true;
        }
        
        // Combine args into message
        String message = String.join(" ", args);
        
        // Make announcement
        if (sender instanceof Player) {
            announcementModule.makeAnnouncement((Player) sender, "Announcement", message);
        } else {
            announcementModule.makeAnnouncement(null, "Announcement", message);
        }
        
        plugin.getMessageManager().sendMessage(sender, "announcement.sent");
        
        return true;
    }
}
package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.tickets.TicketModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SupportCommand implements CommandExecutor {
    
    private final XDiscordUltimate plugin;
    
    public SupportCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if tickets feature is enabled
        if (!plugin.getConfigManager().isFeatureEnabled("tickets")) {
            plugin.getMessageManager().sendMessage(player, "errors.feature-disabled");
            return true;
        }
        
        // Check if player is verified
        if (!plugin.getAdminUtils().isVerified(player)) {
            plugin.getMessageManager().sendMessage(player, "errors.discord-not-linked");
            return true;
        }
        
        // Check if player provided a message
        if (args.length == 0) {
            plugin.getMessageManager().sendMessage(player, "support.no-message");
            plugin.getMessageManager().sendInfo(player, "Usage: /support <your message>");
            return true;
        }
        
        // Get the ticket module
        TicketModule ticketModule = plugin.getModuleManager().getModule(TicketModule.class);
        if (ticketModule == null || !ticketModule.isEnabled()) {
            plugin.getMessageManager().sendError(player, "Ticket module is not available!");
            return true;
        }
        
        // Combine args into message
        String message = String.join(" ", args);
        
        // Create ticket
        ticketModule.createTicket(player, message);
        
        return true;
    }
}
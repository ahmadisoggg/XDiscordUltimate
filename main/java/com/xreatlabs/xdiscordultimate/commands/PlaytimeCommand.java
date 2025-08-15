package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand implements CommandExecutor {
    
    private final XDiscordUltimate plugin;
    
    public PlaytimeCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Show own playtime
            showPlaytime(player, player);
        } else if (args.length == 1) {
            // Show other player's playtime (requires permission)
            if (!sender.hasPermission("xdiscord.playtime.others")) {
                sender.sendMessage("§cYou don't have permission to check other players' playtime!");
                return true;
            }
            
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer " + args[0] + " is not online!");
                return true;
            }
            
            showPlaytime(player, target);
        } else {
            sender.sendMessage("§cUsage: /playtime [player]");
        }
        
        return true;
    }
    
    private void showPlaytime(Player sender, Player target) {
        long sessionTime = plugin.getPlaytimeTracker().getCurrentSessionTime(target);
        long totalTime = plugin.getPlaytimeTracker().getTotalPlaytime(target);
        
        String sessionPlaytime = plugin.getPlaytimeTracker().formatSessionTime(sessionTime);
        String totalPlaytime = plugin.getPlaytimeTracker().formatPlaytime(totalTime);
        
        if (sender.equals(target)) {
            sender.sendMessage("§6=== Your Playtime ===");
            sender.sendMessage("§eSession: §f" + sessionPlaytime);
            sender.sendMessage("§eTotal: §f" + totalPlaytime);
        } else {
            sender.sendMessage("§6=== " + target.getName() + "'s Playtime ===");
            sender.sendMessage("§eSession: §f" + sessionPlaytime);
            sender.sendMessage("§eTotal: §f" + totalPlaytime);
        }
    }
}
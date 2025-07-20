package com.xreatlabs.xdiscordultimate.listeners;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    
    private final XDiscordUltimate plugin;
    private final List<String> filterWords;
    private final Pattern urlPattern;
    
    public ChatListener(XDiscordUltimate plugin) {
        this.plugin = plugin;
        this.filterWords = plugin.getConfig().getStringList("features.moderation.filter-words");
        this.urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Update message count in stats
        plugin.getDatabaseManager().updatePlayerStats(player.getUniqueId(), "messages_sent", 1);
        
        // Check for auto-moderation if enabled
        if (plugin.getConfigManager().isSubFeatureEnabled("moderation", "auto-moderate")) {
            if (shouldFilterMessage(message, player)) {
                event.setCancelled(true);
                plugin.getMessageManager().sendError(player, "Your message was blocked by the chat filter.");
                
                // Log the filtered message
                plugin.getLogger().warning("Filtered message from " + player.getName() + ": " + message);
                return;
            }
        }
        
        // Process Discord mentions (convert @username to Discord mention format)
        if (message.contains("@")) {
            message = processDiscordMentions(message);
            event.setMessage(message);
        }
    }
    
    /**
     * Check if a message should be filtered
     */
    private boolean shouldFilterMessage(String message, Player player) {
        // Bypass filter for players with permission
        if (player.hasPermission("xdiscord.bypass.filter")) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        
        // Check filter words
        for (String word : filterWords) {
            if (word.startsWith("regex:")) {
                // Regex pattern
                String pattern = word.substring(6);
                if (message.matches(pattern)) {
                    return true;
                }
            } else {
                // Simple word check
                if (lowerMessage.contains(word.toLowerCase())) {
                    return true;
                }
            }
        }
        
        // Check for spam (repeated characters)
        if (containsSpam(message)) {
            return true;
        }
        
        // Check for excessive caps
        if (isExcessiveCaps(message) && message.length() > 5) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if message contains spam (repeated characters)
     */
    private boolean containsSpam(String message) {
        // Check for repeated characters (e.g., "aaaaaaa")
        return message.matches(".*([a-zA-Z])\\1{5,}.*");
    }
    
    /**
     * Check if message has excessive caps
     */
    private boolean isExcessiveCaps(String message) {
        if (message.length() < 5) {
            return false;
        }
        
        int capsCount = 0;
        int letterCount = 0;
        
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                letterCount++;
                if (Character.isUpperCase(c)) {
                    capsCount++;
                }
            }
        }
        
        if (letterCount == 0) {
            return false;
        }
        
        double capsPercentage = (double) capsCount / letterCount;
        return capsPercentage > 0.7; // More than 70% caps
    }
    
    /**
     * Process Discord mentions in the message
     */
    private String processDiscordMentions(String message) {
        // This would convert @username to proper Discord mention format
        // For now, just return the original message
        // In a full implementation, this would look up Discord IDs from usernames
        return message;
    }
}
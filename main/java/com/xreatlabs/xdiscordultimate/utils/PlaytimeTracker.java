package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaytimeTracker {
    
    private final XDiscordUltimate plugin;
    private final Map<UUID, Long> joinTimes = new HashMap<>();
    private final Map<UUID, Long> totalPlaytime = new HashMap<>();
    
    public PlaytimeTracker(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Record when a player joins
     */
    public void recordJoin(Player player) {
        joinTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Record when a player leaves and return their session playtime
     */
    public long recordLeave(Player player) {
        UUID playerId = player.getUniqueId();
        Long joinTime = joinTimes.remove(playerId);
        
        if (joinTime != null) {
            long sessionTime = System.currentTimeMillis() - joinTime;
            long currentTotal = totalPlaytime.getOrDefault(playerId, 0L);
            totalPlaytime.put(playerId, currentTotal + sessionTime);
            return sessionTime;
        }
        
        return 0L;
    }
    
    /**
     * Get the current session playtime for a player
     */
    public long getCurrentSessionTime(Player player) {
        Long joinTime = joinTimes.get(player.getUniqueId());
        if (joinTime != null) {
            return System.currentTimeMillis() - joinTime;
        }
        return 0L;
    }
    
    /**
     * Get total playtime for a player
     */
    public long getTotalPlaytime(Player player) {
        long total = totalPlaytime.getOrDefault(player.getUniqueId(), 0L);
        total += getCurrentSessionTime(player);
        return total;
    }
    
    /**
     * Format playtime in a human-readable format
     */
    public String formatPlaytime(long milliseconds) {
        if (milliseconds <= 0) {
            return "0 minutes";
        }
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutes", minutes);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
    
    /**
     * Format session playtime (shorter format)
     */
    public String formatSessionTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "0m";
        }
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm", minutes);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Get player statistics for Discord embeds
     */
    public String getPlayerStats(Player player) {
        long sessionTime = getCurrentSessionTime(player);
        long totalTime = getTotalPlaytime(player);
        
        return String.format("**Session:** %s\n**Total:** %s", 
            formatSessionTime(sessionTime), 
            formatPlaytime(totalTime));
    }
    
    /**
     * Clear data for a player (useful for data cleanup)
     */
    public void clearPlayerData(UUID playerId) {
        joinTimes.remove(playerId);
        totalPlaytime.remove(playerId);
    }
    
    /**
     * Get all tracked players
     */
    public Map<UUID, Long> getJoinTimes() {
        return new HashMap<>(joinTimes);
    }
    
    /**
     * Get all total playtime data
     */
    public Map<UUID, Long> getTotalPlaytime() {
        return new HashMap<>(totalPlaytime);
    }
}
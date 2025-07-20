package com.xreatlabs.xdiscordultimate.listeners;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.verification.VerificationModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    
    private final XDiscordUltimate plugin;
    private final Map<UUID, Long> joinTimes;
    
    public PlayerListener(XDiscordUltimate plugin) {
        this.plugin = plugin;
        this.joinTimes = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Track join time for playtime calculation
        joinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Update player stats
        plugin.getDatabaseManager().updatePlayerStats(player.getUniqueId(), "joins", 1);
        
        // Check verification requirement
        if (plugin.getConfigManager().isFeatureEnabled("verification")) {
            VerificationModule verificationModule = plugin.getModuleManager().getModule(VerificationModule.class);
            if (verificationModule != null) {
                verificationModule.handlePlayerJoin(player);
            }
        }
        
        // Log first join
        if (!player.hasPlayedBefore()) {
            plugin.getLogger().info("First time player joined: " + player.getName());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Calculate and update playtime
        if (joinTimes.containsKey(uuid)) {
            long joinTime = joinTimes.remove(uuid);
            long playtime = (System.currentTimeMillis() - joinTime) / 60000; // Convert to minutes
            
            if (playtime > 0) {
                plugin.getDatabaseManager().updatePlayerStats(uuid, "playtime_minutes", (int) playtime);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Update death count
        plugin.getDatabaseManager().updatePlayerStats(player.getUniqueId(), "deaths", 1);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        // Handle advancement/achievement events
        Player player = event.getPlayer();
        
        // Only process significant advancements (not recipes)
        if (event.getAdvancement().getKey().getKey().contains("recipes/")) {
            return;
        }
        
        plugin.getLogger().info(player.getName() + " earned advancement: " + 
                              event.getAdvancement().getKey().getKey());
    }
}
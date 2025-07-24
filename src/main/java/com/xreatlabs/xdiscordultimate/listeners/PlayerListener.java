package com.xreatlabs.xdiscordultimate.listeners;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.chatbridge.ChatBridgeModule;
import com.xreatlabs.xdiscordultimate.modules.verification.VerificationModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
        boolean isFirstJoin = !player.hasPlayedBefore();
        
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
        
        // Send join message to Discord (with delay to ensure modules are ready)
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatBridgeModule chatBridge = plugin.getModuleManager().getModule(ChatBridgeModule.class);
                if (chatBridge != null) {
                    chatBridge.sendJoinMessage(player);
                }
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
        
        // Log first join
        if (isFirstJoin) {
            plugin.getLogger().info("First time player joined: " + player.getName());
            
            // Update first join stats
            plugin.getDatabaseManager().updatePlayerStats(player.getUniqueId(), "first_join", 1);
        }
        
        // Welcome message for new players
        if (isFirstJoin) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.sendMessage("§6§l=== Welcome to the Server! ===");
                        player.sendMessage("§eThank you for joining us!");
                        player.sendMessage("§aUse §e/verify §ato link your Discord account");
                        player.sendMessage("§aUse §e/help §afor more information");
                        player.sendMessage("§6§l==============================");
                    }
                }
            }.runTaskLater(plugin, 60L); // 3 second delay
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
        
        // Send leave message to Discord
        ChatBridgeModule chatBridge = plugin.getModuleManager().getModule(ChatBridgeModule.class);
        if (chatBridge != null) {
            chatBridge.sendLeaveMessage(player);
        }
        
        // Update last seen
        plugin.getDatabaseManager().updatePlayerStats(uuid, "last_seen", (int) (System.currentTimeMillis() / 1000));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        
        // Update death count
        plugin.getDatabaseManager().updatePlayerStats(player.getUniqueId(), "deaths", 1);
        
        // Send death message to Discord
        if (deathMessage != null && !deathMessage.isEmpty()) {
            ChatBridgeModule chatBridge = plugin.getModuleManager().getModule(ChatBridgeModule.class);
            if (chatBridge != null) {
                chatBridge.sendDeathMessage(player, deathMessage);
            }
        }
        
        plugin.getLogger().info("Player death: " + deathMessage);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        String advancementKey = event.getAdvancement().getKey().getKey();
        
        // Only process significant advancements (not recipes)
        if (advancementKey.contains("recipes/")) {
            return;
        }
        
        // Update advancement count
        plugin.getDatabaseManager().updatePlayerStats(player.getUniqueId(), "advancements", 1);
        
        plugin.getLogger().info(player.getName() + " earned advancement: " + advancementKey);
        
        // Send advancement notification to Discord
        String advancementName = formatAdvancementName(advancementKey);
        ChatBridgeModule chatBridge = plugin.getModuleManager().getModule(ChatBridgeModule.class);
        if (chatBridge != null) {
            String message = "🏆 **" + player.getName() + "** has made the advancement **[" + advancementName + "]**";
            chatBridge.sendSystemMessage(message, java.awt.Color.YELLOW);
        }
    }
    
    private String formatAdvancementName(String key) {
        // Convert advancement key to readable name
        String[] parts = key.split("/");
        String name = parts[parts.length - 1];
        
        // Replace underscores with spaces and capitalize
        name = name.replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase());
        }
        
        return formatted.toString();
    }
}
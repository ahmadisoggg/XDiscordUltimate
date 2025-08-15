package com.xreatlabs.xdiscordultimate.modules.playerevents;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEventsModule extends Module implements Listener {
    
    private String eventsChannelName;
    private boolean showJoinLeave;
    private boolean showDeaths;
    private boolean showAdvancements;
    private boolean showFirstJoin;
    private boolean useEmbeds;
    private boolean showPlaytime;
    private boolean showLocation;
    private boolean showPing;
    
    // Colors for different event types
    private Color joinColor;
    private Color leaveColor;
    private Color deathColor;
    private Color advancementColor;
    private Color firstJoinColor;
    
    // Track first joins
    private final Map<UUID, Long> firstJoinTimes = new HashMap<>();
    
    public PlayerEventsModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "PlayerEvents";
    }
    
    @Override
    public String getDescription() {
        return "Sends player events (join, leave, death, achievements) to Discord";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        info("Player events module enabled");
    }
    
    @Override
    protected void onDisable() {
        // Unregister events
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerAdvancementDoneEvent.getHandlerList().unregister(this);
        
        info("Player events module disabled");
    }
    
    private void loadConfiguration() {
        eventsChannelName = getConfig().getString("event-channel", "player-events");
        showJoinLeave = getConfig().getBoolean("events.join", true) && getConfig().getBoolean("events.leave", true);
        showDeaths = getConfig().getBoolean("events.death", true);
        showAdvancements = getConfig().getBoolean("events.advancement", true);
        showFirstJoin = getConfig().getBoolean("events.first-join", true);
        useEmbeds = getConfig().getBoolean("use-embeds", true);
        showPlaytime = getConfig().getBoolean("show-playtime", true);
        showLocation = getConfig().getBoolean("show-location", true);
        showPing = getConfig().getBoolean("show-ping", true);
        
        // Load colors
        joinColor = parseColor(getConfig().getString("colors.join", "#00FF00"));
        leaveColor = parseColor(getConfig().getString("colors.leave", "#FF0000"));
        deathColor = parseColor(getConfig().getString("colors.death", "#FFA500"));
        advancementColor = parseColor(getConfig().getString("colors.advancement", "#FFD700"));
        firstJoinColor = parseColor(getConfig().getString("colors.first-join", "#FF69B4"));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!showJoinLeave) return;
        
        Player player = event.getPlayer();
        boolean isFirstJoin = !player.hasPlayedBefore();
        
        // Record join time for playtime tracking
        plugin.getPlaytimeTracker().recordJoin(player);
        
        if (isFirstJoin && showFirstJoin) {
            firstJoinTimes.put(player.getUniqueId(), System.currentTimeMillis());
            sendFirstJoinMessage(player);
        } else {
            sendJoinMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!showJoinLeave) return;
        
        Player player = event.getPlayer();
        
        // Record leave and get session playtime
        long sessionTime = plugin.getPlaytimeTracker().recordLeave(player);
        
        sendLeaveMessage(player, sessionTime);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!showDeaths) return;
        
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        
        if (deathMessage != null) {
            sendDeathMessage(player, deathMessage);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (!showAdvancements) return;
        
        Player player = event.getPlayer();
        String advancementKey = event.getAdvancement().getKey().getKey();
        
        // Skip recipe advancements
        if (advancementKey.startsWith("recipes/")) {
            return;
        }
        
        // Get advancement display name
        String advancementName = formatAdvancementName(advancementKey);
        sendAdvancementMessage(player, advancementName);
    }
    
    private void sendJoinMessage(Player player) {
        TextChannel channel = getEventsChannel();
        if (channel == null) return;
        
        if (useEmbeds) {
            String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay";
            
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(joinColor)
                .setAuthor("➕ " + player.getName() + " joined the server", 
                    null, 
                    avatarUrl)
                .setDescription("**" + player.getName() + "** has joined the server!")
                .addField("Players Online", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), true);
            
            if (showLocation) {
                embed.addField("Location", formatLocation(player), true);
            }
            if (showPing) {
                embed.addField("Ping", player.getPing() + "ms", true);
            }
            
            embed.setTimestamp(Instant.now())
                .setFooter("Player Join • " + plugin.getServer().getName(), null);
            
            channel.sendMessageEmbeds(embed.build()).queue(
                success -> plugin.getLogger().info("Sent join embed for " + player.getName()),
                error -> plugin.getLogger().warning("Failed to send join embed: " + error.getMessage())
            );
        } else {
            channel.sendMessage("**➕ " + player.getName() + " joined the server**").queue();
        }
    }
    
    private void sendLeaveMessage(Player player, long sessionTime) {
        TextChannel channel = getEventsChannel();
        if (channel == null) return;
        
        if (useEmbeds) {
            String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay";
            String sessionPlaytime = plugin.getPlaytimeTracker().formatSessionTime(sessionTime);
            String totalPlaytime = plugin.getPlaytimeTracker().formatPlaytime(plugin.getPlaytimeTracker().getTotalPlaytime(player));
            
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(leaveColor)
                .setAuthor("➖ " + player.getName() + " left the server", 
                    null, 
                    avatarUrl)
                .setDescription("**" + player.getName() + "** has left the server!")
                .addField("Players Online", (Bukkit.getOnlinePlayers().size() - 1) + "/" + Bukkit.getMaxPlayers(), true);
            
            if (showPlaytime) {
                embed.addField("Session Time", sessionPlaytime, true);
                embed.addField("Total Playtime", totalPlaytime, true);
            }
            if (showLocation) {
                embed.addField("Last Location", formatLocation(player), false);
            }
            
            embed.setTimestamp(Instant.now())
                .setFooter("Player Leave • " + plugin.getServer().getName(), null);
            
            channel.sendMessageEmbeds(embed.build()).queue(
                success -> plugin.getLogger().info("Sent leave embed for " + player.getName() + " (Session: " + sessionPlaytime + ")"),
                error -> plugin.getLogger().warning("Failed to send leave embed: " + error.getMessage())
            );
        } else {
            String sessionPlaytime = plugin.getPlaytimeTracker().formatSessionTime(sessionTime);
            channel.sendMessage("**➖ " + player.getName() + " left the server** (Played for " + sessionPlaytime + ")").queue();
        }
    }
    
    private void sendFirstJoinMessage(Player player) {
        TextChannel channel = getEventsChannel();
        if (channel == null) return;
        
        if (useEmbeds) {
            String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay";
            
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(firstJoinColor)
                .setAuthor("🎉 Welcome " + player.getName() + "!", 
                    null, 
                    avatarUrl)
                .setDescription("🎉 **" + player.getName() + "** has joined for the first time! 🎉")
                .addField("Total Players", String.valueOf(Bukkit.getOfflinePlayers().length), true)
                .addField("Players Online", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), true);
            
            if (showLocation) {
                embed.addField("Location", formatLocation(player), true);
            }
            if (showPing) {
                embed.addField("Ping", player.getPing() + "ms", true);
            }
            
            embed.setTimestamp(Instant.now())
                .setFooter("First Join • " + plugin.getServer().getName(), null);
            
            channel.sendMessageEmbeds(embed.build()).queue(
                success -> plugin.getLogger().info("Sent first join embed for " + player.getName()),
                error -> plugin.getLogger().warning("Failed to send first join embed: " + error.getMessage())
            );
        } else {
            channel.sendMessage("**🎉 Welcome " + player.getName() + " to the server for the first time! 🎉**").queue();
        }
    }
    
    private void sendDeathMessage(Player player, String deathMessage) {
        TextChannel channel = getEventsChannel();
        if (channel == null) return;
        
        if (useEmbeds) {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(deathColor)
                .setAuthor(player.getName() + " died", 
                    null, 
                    "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay")
                .setDescription("💀 " + deathMessage)
                .addField("Location", formatLocation(player), true)
                .setTimestamp(Instant.now())
                .setFooter("Player Death", plugin.getServer().getServerIcon() != null ? 
                    "attachment://server-icon.png" : null);
            
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage("**💀 " + deathMessage + "**").queue();
        }
    }
    
    private void sendAdvancementMessage(Player player, String advancementName) {
        TextChannel channel = getEventsChannel();
        if (channel == null) return;
        
        if (useEmbeds) {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(advancementColor)
                .setAuthor(player.getName() + " made an advancement!", 
                    null, 
                    "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay")
                .setDescription("🏆 **" + player.getName() + "** has made the advancement **[" + advancementName + "]**")
                .setTimestamp(Instant.now())
                .setFooter("Advancement", plugin.getServer().getServerIcon() != null ? 
                    "attachment://server-icon.png" : null);
            
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage("**🏆 " + player.getName() + " has made the advancement [" + advancementName + "]**").queue();
        }
    }
    
    private TextChannel getEventsChannel() {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        return plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(eventsChannelName, true)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    private String formatLocation(Player player) {
        return String.format("%s at %d, %d, %d",
            player.getWorld().getName(),
            player.getLocation().getBlockX(),
            player.getLocation().getBlockY(),
            player.getLocation().getBlockZ()
        );
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
    
    private Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.GRAY;
        }
    }
}
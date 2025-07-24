package com.xreatlabs.xdiscordultimate.modules.announcements;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AnnouncementModule extends Module implements Listener {
    
    // Announcement types
    private final Map<String, Announcement> announcements = new ConcurrentHashMap<>();
    private final Map<String, BukkitTask> scheduledAnnouncements = new ConcurrentHashMap<>();
    private final Set<UUID> mutedPlayers = ConcurrentHashMap.newKeySet();
    
    // Configuration
    private String announcementChannel;
    private boolean enableAutoAnnouncements;
    private boolean enableJoinAnnouncements;
    private boolean enableDiscordSync;
    private String announcementPrefix;
    private Sound announcementSound;
    
    public AnnouncementModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Announcements";
    }
    
    @Override
    public String getDescription() {
        return "Comprehensive announcement broadcasting system with scheduling";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        loadAnnouncements();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Start auto announcements
        if (enableAutoAnnouncements) {
            startAutoAnnouncements();
        }
        
        info("Announcements module enabled with " + announcements.size() + " announcements");
    }
    
    @Override
    protected void onDisable() {
        // Cancel all scheduled tasks
        scheduledAnnouncements.values().forEach(BukkitTask::cancel);
        scheduledAnnouncements.clear();
        
        // Save muted players
        saveMutedPlayers();
        
        info("Announcements module disabled");
    }
    
    private void loadConfiguration() {
        announcementChannel = getConfig().getString("discord-channel", "announcements");
        enableAutoAnnouncements = getConfig().getBoolean("enable-auto-announcements", true);
        enableJoinAnnouncements = getConfig().getBoolean("enable-join-announcements", true);
        enableDiscordSync = getConfig().getBoolean("enable-discord-sync", true);
        announcementPrefix = ChatColor.translateAlternateColorCodes('&', 
            getConfig().getString("announcement-prefix", "&6[&eAnnouncement&6] &f"));
        
        String soundName = getConfig().getString("announcement-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            announcementSound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            announcementSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            warning("Invalid sound name: " + soundName + ", using default");
        }
    }
    
    private void loadAnnouncements() {
        ConfigurationSection section = getConfig().getConfigurationSection("announcements");
        if (section == null) {
            // Create default announcements
            createDefaultAnnouncements();
            return;
        }
        
        for (String key : section.getKeys(false)) {
            ConfigurationSection announcementSection = section.getConfigurationSection(key);
            if (announcementSection == null) continue;
            
            Announcement announcement = new Announcement(
                key,
                announcementSection.getString("title", ""),
                announcementSection.getStringList("messages"),
                announcementSection.getInt("interval", 300),
                announcementSection.getBoolean("enabled", true),
                announcementSection.getString("permission", null),
                announcementSection.getBoolean("discord", true),
                announcementSection.getString("discord-color", "#FFD700")
            );
            
            announcements.put(key, announcement);
        }
    }
    
    private void createDefaultAnnouncements() {
        // Server info
        announcements.put("server-info", new Announcement(
            "server-info",
            "Server Information",
            Arrays.asList(
                "&bWelcome to our server!",
                "&eWebsite: &fexample.com",
                "&eDiscord: &fdiscord.gg/example",
                "&eStore: &fstore.example.com"
            ),
            600, // 10 minutes
            true,
            null,
            true,
            "#00BFFF"
        ));
        
        // Vote reminder
        announcements.put("vote", new Announcement(
            "vote",
            "Vote Reminder",
            Arrays.asList(
                "&6Support the server by voting!",
                "&eVote Link 1: &fexample.com/vote1",
                "&eVote Link 2: &fexample.com/vote2",
                "&aYou'll receive rewards for voting!"
            ),
            900, // 15 minutes
            true,
            null,
            true,
            "#FFD700"
        ));
        
        // Discord reminder
        announcements.put("discord", new Announcement(
            "discord",
            "Join our Discord!",
            Arrays.asList(
                "&dJoin our Discord community!",
                "&eLink: &fdiscord.gg/example",
                "&aGet updates, chat with players, and more!"
            ),
            1200, // 20 minutes
            true,
            null,
            true,
            "#7289DA"
        ));
    }
    
    private void startAutoAnnouncements() {
        for (Announcement announcement : announcements.values()) {
            if (!announcement.enabled || announcement.interval <= 0) continue;
            
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    broadcastAnnouncement(announcement);
                }
            }.runTaskTimer(plugin, 20L * announcement.interval, 20L * announcement.interval);
            
            scheduledAnnouncements.put(announcement.id, task);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enableJoinAnnouncements) return;
        
        Player player = event.getPlayer();
        
        // Send welcome announcement after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    Announcement welcome = announcements.get("welcome");
                    if (welcome != null && welcome.enabled) {
                        sendAnnouncementToPlayer(player, welcome);
                    }
                }
            }
        }.runTaskLater(plugin, 60L); // 3 seconds delay
    }
    
    /**
     * Make a custom announcement
     */
    public void makeAnnouncement(Player sender, String title, String message) {
        List<String> messages = Arrays.asList(message.split("\\\\n"));
        
        Announcement customAnnouncement = new Announcement(
            "custom",
            title,
            messages,
            0,
            true,
            null,
            enableDiscordSync,
            "#FF6347"
        );
        
        broadcastAnnouncement(customAnnouncement);
        
        if (sender != null) {
            plugin.getMessageManager().sendSuccess(sender, "Announcement sent!");
        }
    }
    
    /**
     * Broadcast a scheduled announcement
     */
    public void broadcastScheduledAnnouncement(String announcementId) {
        Announcement announcement = announcements.get(announcementId);
        if (announcement == null) {
            warning("Announcement not found: " + announcementId);
            return;
        }
        
        broadcastAnnouncement(announcement);
    }
    
    private void broadcastAnnouncement(Announcement announcement) {
        // Send to Minecraft
        sendMinecraftAnnouncement(announcement);
        
        // Send to Discord
        if (enableDiscordSync && announcement.sendToDiscord) {
            sendDiscordAnnouncement(announcement);
        }
    }
    
    private void sendMinecraftAnnouncement(Announcement announcement) {
        // Build the message
        List<String> formattedMessages = new ArrayList<>();
        
        // Add header
        formattedMessages.add(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        if (!announcement.title.isEmpty()) {
            formattedMessages.add(announcementPrefix + ChatColor.YELLOW + announcement.title);
            formattedMessages.add("");
        }
        
        // Add messages
        for (String message : announcement.messages) {
            formattedMessages.add(ChatColor.translateAlternateColorCodes('&', message));
        }
        
        // Add footer
        formattedMessages.add(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Send to players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check if player has muted announcements
            if (mutedPlayers.contains(player.getUniqueId())) {
                continue;
            }
            
            // Check permission
            if (announcement.permission != null && !player.hasPermission(announcement.permission)) {
                continue;
            }
            
            // Send messages
            for (String line : formattedMessages) {
                player.sendMessage(line);
            }
            
            // Play sound
            if (announcementSound != null) {
                player.playSound(player.getLocation(), announcementSound, 1.0f, 1.0f);
            }
        }
    }
    
    private void sendAnnouncementToPlayer(Player player, Announcement announcement) {
        if (mutedPlayers.contains(player.getUniqueId())) {
            return;
        }
        
        if (announcement.permission != null && !player.hasPermission(announcement.permission)) {
            return;
        }
        
        // Build the message
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        if (!announcement.title.isEmpty()) {
            player.sendMessage(announcementPrefix + ChatColor.YELLOW + announcement.title);
            player.sendMessage("");
        }
        
        for (String message : announcement.messages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        if (announcementSound != null) {
            player.playSound(player.getLocation(), announcementSound, 1.0f, 1.0f);
        }
    }
    
    private void sendDiscordAnnouncement(Announcement announcement) {
        TextChannel channel = getAnnouncementChannel();
        if (channel == null) return;
        
        Color embedColor;
        try {
            embedColor = Color.decode(announcement.discordColor);
        } catch (NumberFormatException e) {
            embedColor = Color.YELLOW;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(embedColor)
            .setTitle("📢 " + (announcement.title.isEmpty() ? "Server Announcement" : announcement.title))
            .setDescription(String.join("\n", announcement.messages)
                .replaceAll("&[0-9a-fk-or]", "")) // Remove color codes
            .setTimestamp(Instant.now())
            .setFooter("Server Announcement", plugin.getServer().getServerIcon() != null ? 
                "attachment://server-icon.png" : null);
        
        // Add buttons for interactive announcements
        List<Button> buttons = new ArrayList<>();
        
        // Check for links in messages
        for (String message : announcement.messages) {
            if (message.contains("discord.gg/")) {
                buttons.add(Button.link(extractUrl(message, "discord.gg/"), "Join Discord")
                    .withEmoji(Emoji.fromUnicode("💬")));
            } else if (message.contains("store.") || message.contains("/store")) {
                buttons.add(Button.link(extractUrl(message, null), "Visit Store")
                    .withEmoji(Emoji.fromUnicode("🛒")));
            } else if (message.contains("/vote")) {
                buttons.add(Button.link(extractUrl(message, null), "Vote Now")
                    .withEmoji(Emoji.fromUnicode("🗳️")));
            }
        }
        
        if (buttons.isEmpty()) {
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessageEmbeds(embed.build())
                .setActionRow(buttons.subList(0, Math.min(buttons.size(), 5)))
                .queue();
        }
    }
    
    private String extractUrl(String message, String contains) {
        String[] words = message.split(" ");
        for (String word : words) {
            if (word.startsWith("http://") || word.startsWith("https://")) {
                if (contains == null || word.contains(contains)) {
                    return word;
                }
            } else if (contains != null && word.contains(contains)) {
                return "https://" + word;
            }
        }
        return "#";
    }
    
    private TextChannel getAnnouncementChannel() {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        return plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(announcementChannel, true)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Toggle announcement mute for a player
     */
    public void toggleAnnouncementMute(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (mutedPlayers.contains(uuid)) {
            mutedPlayers.remove(uuid);
            plugin.getMessageManager().sendSuccess(player, "Announcements unmuted!");
        } else {
            mutedPlayers.add(uuid);
            plugin.getMessageManager().sendSuccess(player, "Announcements muted!");
        }
    }
    
    /**
     * List all announcements
     */
    public void listAnnouncements(Player player) {
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW + "Available Announcements:");
        player.sendMessage("");
        
        for (Announcement announcement : announcements.values()) {
            String status = announcement.enabled ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            String interval = announcement.interval > 0 ? 
                " (every " + announcement.interval + "s)" : "";
            
            player.sendMessage(status + " " + ChatColor.WHITE + announcement.id + 
                ChatColor.GRAY + interval);
            
            if (!announcement.title.isEmpty()) {
                player.sendMessage("  " + ChatColor.GRAY + announcement.title);
            }
        }
        
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    /**
     * Enable or disable an announcement
     */
    public void toggleAnnouncement(String announcementId, boolean enable) {
        Announcement announcement = announcements.get(announcementId);
        if (announcement == null) return;
        
        announcement.enabled = enable;
        
        // Update scheduled task
        if (enable && announcement.interval > 0 && enableAutoAnnouncements) {
            // Cancel existing task if any
            BukkitTask existingTask = scheduledAnnouncements.remove(announcementId);
            if (existingTask != null) {
                existingTask.cancel();
            }
            
            // Create new task
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    broadcastAnnouncement(announcement);
                }
            }.runTaskTimer(plugin, 20L * announcement.interval, 20L * announcement.interval);
            
            scheduledAnnouncements.put(announcementId, task);
        } else {
            // Cancel task
            BukkitTask task = scheduledAnnouncements.remove(announcementId);
            if (task != null) {
                task.cancel();
            }
        }
    }
    
    private void saveMutedPlayers() {
        List<String> mutedList = mutedPlayers.stream()
            .map(UUID::toString)
            .collect(Collectors.toList());
        
        getConfig().set("muted-players", mutedList);
        // TODO: Implement saveConfig()
    }
    
    /**
     * Announcement data class
     */
    private static class Announcement {
        final String id;
        final String title;
        final List<String> messages;
        final int interval;
        boolean enabled;
        final String permission;
        final boolean sendToDiscord;
        final String discordColor;
        
        Announcement(String id, String title, List<String> messages, int interval, 
                    boolean enabled, String permission, boolean sendToDiscord, String discordColor) {
            this.id = id;
            this.title = title;
            this.messages = new ArrayList<>(messages);
            this.interval = interval;
            this.enabled = enabled;
            this.permission = permission;
            this.sendToDiscord = sendToDiscord;
            this.discordColor = discordColor;
        }
    }
}
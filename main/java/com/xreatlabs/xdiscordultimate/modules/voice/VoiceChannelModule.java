package com.xreatlabs.xdiscordultimate.modules.voice;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import com.xreatlabs.xdiscordultimate.utils.TextToSpeechUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VoiceChannelModule extends Module implements Listener {
    
    private VoiceListener voiceListener;
    private TextToSpeechUtils ttsUtils;
    
    // Configuration
    private boolean enableProximityChat;
    private boolean enableWorldChannels;
    private boolean enableRegionChannels;
    private boolean enableTTS;
    private int proximityRange;
    private String voiceCategoryName;
    
    // Voice channel mappings
    private final Map<String, VoiceChannel> worldChannels = new ConcurrentHashMap<>();
    private final Map<String, VoiceChannel> regionChannels = new ConcurrentHashMap<>();
    private final Map<UUID, VoiceChannel> playerChannels = new ConcurrentHashMap<>();
    private final Map<String, Set<UUID>> channelMembers = new ConcurrentHashMap<>();
    
    // Proximity tracking
    private final Map<UUID, Location> playerLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> proximityGroups = new ConcurrentHashMap<>();
    
    public VoiceChannelModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "VoiceChannel";
    }
    
    @Override
    public String getDescription() {
        return "Voice channel integration with proximity chat and world-based channels";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        
        // Initialize TTS utils
        ttsUtils = new TextToSpeechUtils(plugin);
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Register Discord listener
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            voiceListener = new VoiceListener();
            plugin.getDiscordManager().getJDA().addEventListener(voiceListener);
            
            // Create voice channels
            setupVoiceChannels();
            
            // Start proximity tracking
            if (enableProximityChat) {
                startProximityTracking();
            }
        }
        
        info("Voice channel module enabled with TTS support: " + enableTTS);
    }
    
    @Override
    protected void onDisable() {
        // Unregister Discord listener
        if (voiceListener != null && plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getJDA().removeEventListener(voiceListener);
        }
        
        // Clear all temporary channels
        cleanupTemporaryChannels();
        
        info("Voice channel module disabled");
    }
    
    private void loadConfiguration() {
        enableProximityChat = getConfig().getBoolean("enable-proximity-chat", true);
        enableWorldChannels = getConfig().getBoolean("enable-world-channels", true);
        enableRegionChannels = getConfig().getBoolean("enable-region-channels", false);
        enableTTS = getConfig().getBoolean("enable-tts", true);
        proximityRange = getConfig().getInt("proximity-range", 50);
        voiceCategoryName = getConfig().getString("voice-category", "Voice Channels");
    }
    
    private void setupVoiceChannels() {
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild == null) return;
        
        // Get or create voice category
        Category category = guild.getCategoriesByName(voiceCategoryName, true).stream()
            .findFirst()
            .orElseGet(() -> {
                try {
                    return guild.createCategory(voiceCategoryName).complete();
                } catch (Exception e) {
                    error("Failed to create voice category", e);
                    return null;
                }
            });
        
        if (category == null) return;
        
        // Create world channels
        if (enableWorldChannels) {
            for (World world : Bukkit.getWorlds()) {
                createWorldChannel(category, world);
            }
        }
        
        // Create general voice channel
        createGeneralChannel(category);
        
        // Create AFK channel
        createAfkChannel(category);
    }
    
    private void createWorldChannel(Category category, World world) {
        String channelName = "🌍 " + formatWorldName(world.getName());
        
        VoiceChannel channel = category.getGuild().getVoiceChannelsByName(channelName, true)
            .stream()
            .filter(vc -> vc.getParentCategory() != null && vc.getParentCategory().equals(category))
            .findFirst()
            .orElseGet(() -> {
                try {
                    return category.createVoiceChannel(channelName)
                        .setUserlimit(0)
                        .complete();
                } catch (Exception e) {
                    error("Failed to create world voice channel", e);
                    return null;
                }
            });
        
        if (channel != null) {
            worldChannels.put(world.getName(), channel);
            debug("Created/found world voice channel: " + channelName);
        }
    }
    
    private void createGeneralChannel(Category category) {
        String channelName = "🎤 General Voice";
        
        category.getGuild().getVoiceChannelsByName(channelName, true)
            .stream()
            .filter(vc -> vc.getParentCategory() != null && vc.getParentCategory().equals(category))
            .findFirst()
            .orElseGet(() -> {
                try {
                    return category.createVoiceChannel(channelName)
                        .setUserlimit(0)
                        .complete();
                } catch (Exception e) {
                    error("Failed to create general voice channel", e);
                    return null;
                }
            });
    }
    
    private void createAfkChannel(Category category) {
        String channelName = "💤 AFK";
        
        VoiceChannel afkChannel = category.getGuild().getVoiceChannelsByName(channelName, true)
            .stream()
            .filter(vc -> vc.getParentCategory() != null && vc.getParentCategory().equals(category))
            .findFirst()
            .orElseGet(() -> {
                try {
                    return category.createVoiceChannel(channelName)
                        .setUserlimit(0)
                        .complete();
                } catch (Exception e) {
                    error("Failed to create AFK voice channel", e);
                    return null;
                }
            });
        
        // Set as guild AFK channel if possible
        if (afkChannel != null) {
            try {
                category.getGuild().getManager().setAfkChannel(afkChannel).queue();
            } catch (Exception e) {
                debug("Could not set AFK channel: " + e.getMessage());
            }
        }
    }
    
    private void startProximityTracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateProximityGroups();
                updateVoiceChannels();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Every second
    }
    
    private void updateProximityGroups() {
        // Clear old groups
        proximityGroups.clear();
        
        // Get all online players with voice
        List<Player> voicePlayers = Bukkit.getOnlinePlayers().stream()
            .filter(p -> isInVoice(p))
            .collect(Collectors.toList());
        
        // Group players by proximity
        for (Player player : voicePlayers) {
            UUID playerId = player.getUniqueId();
            Location loc = player.getLocation();
            playerLocations.put(playerId, loc);
            
            Set<UUID> nearbyPlayers = new HashSet<>();
            nearbyPlayers.add(playerId);
            
            for (Player other : voicePlayers) {
                if (player.equals(other)) continue;
                
                if (isNearby(player, other)) {
                    nearbyPlayers.add(other.getUniqueId());
                }
            }
            
            proximityGroups.put(playerId, nearbyPlayers);
        }
    }
    
    private void updateVoiceChannels() {
        if (!enableProximityChat) return;
        
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild == null) return;
        
        // Process proximity groups
        Set<Set<UUID>> uniqueGroups = new HashSet<>();
        Set<UUID> processed = new HashSet<>();
        
        for (Map.Entry<UUID, Set<UUID>> entry : proximityGroups.entrySet()) {
            UUID playerId = entry.getKey();
            if (processed.contains(playerId)) continue;
            
            Set<UUID> group = entry.getValue();
            if (group.size() > 1) {
                uniqueGroups.add(new HashSet<>(group));
                processed.addAll(group);
            }
        }
        
        // Create/update proximity channels
        for (Set<UUID> group : uniqueGroups) {
            createProximityChannel(group);
        }
        
        // Move solo players to world channels
        for (UUID playerId : proximityGroups.keySet()) {
            if (!processed.contains(playerId)) {
                moveToWorldChannel(playerId);
            }
        }
    }
    
    private void createProximityChannel(Set<UUID> group) {
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild == null) return;
        
        Category category = guild.getCategoriesByName(voiceCategoryName, true).stream()
            .findFirst().orElse(null);
        if (category == null) return;
        
        // Generate channel name
        List<String> names = group.stream()
            .map(uuid -> Bukkit.getPlayer(uuid))
            .filter(Objects::nonNull)
            .map(Player::getName)
            .limit(3)
            .collect(Collectors.toList());
        
        String channelName = "🎯 " + String.join(", ", names);
        if (group.size() > 3) {
            channelName += " +" + (group.size() - 3);
        }
        
        // Create temporary voice channel
        try {
            VoiceChannel channel = category.createVoiceChannel(channelName)
                .setUserlimit(group.size())
                .complete();
            
            // Move members to channel
            for (UUID playerId : group) {
                movePlayerToChannel(playerId, channel);
            }
            
            // Schedule cleanup
            scheduleChannelCleanup(channel);
            
        } catch (Exception e) {
            error("Failed to create proximity channel", e);
        }
    }
    
    private void moveToWorldChannel(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        
        VoiceChannel worldChannel = worldChannels.get(player.getWorld().getName());
        if (worldChannel != null) {
            movePlayerToChannel(playerId, worldChannel);
        }
    }
    
    private void movePlayerToChannel(UUID playerId, VoiceChannel channel) {
        String discordId = plugin.getDatabaseManager().getDiscordId(playerId)
            .join();
        
        if (discordId == null) return;
        
        Guild guild = channel.getGuild();
        Member member = guild.getMemberById(discordId);
        
        if (member != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel()) {
            try {
                guild.moveVoiceMember(member, channel).queue(
                    success -> {
                        playerChannels.put(playerId, channel);
                        channelMembers.computeIfAbsent(channel.getId(), k -> ConcurrentHashMap.newKeySet())
                            .add(playerId);
                    },
                    error -> debug("Failed to move member: " + error.getMessage())
                );
            } catch (Exception e) {
                debug("Error moving member: " + e.getMessage());
            }
        }
    }
    
    private void scheduleChannelCleanup(VoiceChannel channel) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (channel.getMembers().isEmpty()) {
                    channel.delete().queue();
                    channelMembers.remove(channel.getId());
                }
            }
        }.runTaskLater(plugin, 20L * 30); // Check after 30 seconds
    }
    
    private void cleanupTemporaryChannels() {
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild == null) return;
        
        Category category = guild.getCategoriesByName(voiceCategoryName, true).stream()
            .findFirst().orElse(null);
        if (category == null) return;
        
        // Delete all proximity channels
        category.getVoiceChannels().stream()
            .filter(vc -> vc.getName().startsWith("🎯"))
            .forEach(vc -> vc.delete().queue());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // TTS announcement
        if (enableTTS && ttsUtils != null) {
            ttsUtils.announcePlayerEvent(player, "join");
        }
        
        // Check if player is verified and in Discord voice
        String discordId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId())
            .join();
        
        if (discordId != null) {
            Guild guild = plugin.getDiscordManager().getMainGuild();
            if (guild != null) {
                Member member = guild.getMemberById(discordId);
                if (member != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel()) {
                    // Auto-move to appropriate channel
                    if (enableWorldChannels) {
                        moveToWorldChannel(player.getUniqueId());
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // TTS announcement
        if (enableTTS && ttsUtils != null) {
            ttsUtils.announcePlayerEvent(player, "leave");
        }
        
        playerLocations.remove(playerId);
        proximityGroups.remove(playerId);
        playerChannels.remove(playerId);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // TTS announcement
        if (enableTTS && ttsUtils != null) {
            ttsUtils.announcePlayerEvent(player, "death");
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // TTS announcement for chat messages
        if (enableTTS && ttsUtils != null) {
            // Run on main thread since we're in async event
            Bukkit.getScheduler().runTask(plugin, () -> {
                ttsUtils.announceChatMessage(player, message);
            });
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Check if world changed
        if (from.getWorld() != to.getWorld() && enableWorldChannels) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (isInVoice(player)) {
                    moveToWorldChannel(player.getUniqueId());
                }
            }, 10L);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enableProximityChat) return;
        
        // Only track significant movement
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (from.getBlockX() != to.getBlockX() || 
            from.getBlockY() != to.getBlockY() || 
            from.getBlockZ() != to.getBlockZ()) {
            
            playerLocations.put(event.getPlayer().getUniqueId(), to);
        }
    }
    
    private boolean isInVoice(Player player) {
        String discordId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId())
            .join();
        
        if (discordId == null) return false;
        
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild == null) return false;
        
        Member member = guild.getMemberById(discordId);
        return member != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel();
    }
    
    private boolean isNearby(Player player1, Player player2) {
        if (!player1.getWorld().equals(player2.getWorld())) {
            return false;
        }
        
        double distance = player1.getLocation().distance(player2.getLocation());
        return distance <= proximityRange;
    }
    
    private String formatWorldName(String worldName) {
        switch (worldName.toLowerCase()) {
            case "world":
                return "Overworld";
            case "world_nether":
                return "Nether";
            case "world_the_end":
                return "The End";
            default:
                return worldName.substring(0, 1).toUpperCase() + worldName.substring(1);
        }
    }
    
    private class VoiceListener extends ListenerAdapter {
        @Override
        public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
            Member member = event.getMember();
            User user = member.getUser();
            
            // Get Minecraft UUID
            UUID minecraftUuid = plugin.getDatabaseManager()
                .getMinecraftUuid(user.getId())
                .join();
            
            if (minecraftUuid == null) return;
            
            Player player = Bukkit.getPlayer(minecraftUuid);
            if (player == null) return;
            
            // Handle voice channel join
            if (event.getChannelJoined() != null) {
                plugin.getMessageManager().sendInfo(player,
                    "Connected to voice channel: " + event.getChannelJoined().getName());
                
                // Auto-move to appropriate channel
                if (enableWorldChannels) {
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                        moveToWorldChannel(minecraftUuid), 20L);
                }
            }
            
            // Handle voice channel leave
            if (event.getChannelLeft() != null) {
                playerChannels.remove(minecraftUuid);
                Set<UUID> members = channelMembers.get(event.getChannelLeft().getId());
                if (members != null) {
                    members.remove(minecraftUuid);
                }
                
                plugin.getMessageManager().sendInfo(player,
                    "Disconnected from voice channel");
            }
        }
        
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            
            // Check if message is in a voice channel's text channel
            if (event.getChannel().getName().contains("voice") ||
                event.getChannel().getName().contains("general")) {
                
                // Find associated voice channel
                VoiceChannel voiceChannel = findAssociatedVoiceChannel(event);
                if (voiceChannel != null && enableTTS && ttsUtils != null) {
                    String username = event.getAuthor().getName();
                    String message = event.getMessage().getContentRaw();
                    
                    // Announce Discord message in voice channel
                    ttsUtils.announceDiscordMessage(username, message, voiceChannel);
                }
            }
        }
        
        private VoiceChannel findAssociatedVoiceChannel(MessageReceivedEvent event) {
            // Try to find a voice channel with similar name or in same category
            Guild guild = event.getGuild();
            String channelName = event.getChannel().getName();
            
            // Look for voice channels in the same category
            if (event.getChannel().asTextChannel().getParentCategory() != null) {
                Category category = event.getChannel().asTextChannel().getParentCategory();
                return category.getVoiceChannels().stream()
                    .filter(vc -> vc.getMembers().size() > 0) // Has members
                    .findFirst()
                    .orElse(null);
            }
            
            // Fallback to general voice channel
            return guild.getVoiceChannelsByName("general voice", true)
                .stream()
                .findFirst()
                .orElse(null);
        }
    }
    
    /**
     * Get the current voice channel of a player
     */
    public VoiceChannel getPlayerVoiceChannel(Player player) {
        return playerChannels.get(player.getUniqueId());
    }
    
    /**
     * Get all players in a voice channel
     */
    public Set<Player> getChannelPlayers(VoiceChannel channel) {
        Set<UUID> memberIds = channelMembers.get(channel.getId());
        if (memberIds == null) return new HashSet<>();
        
        return memberIds.stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    
    /**
     * Check if proximity chat is enabled
     */
    public boolean isProximityChatEnabled() {
        return enableProximityChat;
    }
    
    /**
     * Get the proximity range
     */
    public int getProximityRange() {
        return proximityRange;
    }
    
    /**
     * Handle user joining voice channel from Discord
     */
    public void onUserJoinVoice(Member member, VoiceChannel channel) {
        if (!enableTTS || ttsUtils == null) return;
        
        // Get Minecraft player
        UUID minecraftUuid = plugin.getDatabaseManager()
            .getMinecraftUuid(member.getId())
            .join();
        
        if (minecraftUuid != null) {
            Player player = Bukkit.getPlayer(minecraftUuid);
            if (player != null) {
                // Announce voice join
                ttsUtils.announceVoiceEvent(player, "joined voice channel " + channel.getName());
            }
        }
    }
    
    /**
     * Handle user leaving voice channel from Discord
     */
    public void onUserLeaveVoice(Member member, VoiceChannel channel) {
        if (!enableTTS || ttsUtils == null) return;
        
        // Get Minecraft player
        UUID minecraftUuid = plugin.getDatabaseManager()
            .getMinecraftUuid(member.getId())
            .join();
        
        if (minecraftUuid != null) {
            Player player = Bukkit.getPlayer(minecraftUuid);
            if (player != null) {
                // Announce voice leave
                ttsUtils.announceVoiceEvent(player, "left voice channel " + channel.getName());
            }
        }
    }
    
    /**
     * Announce Discord message in voice channels
     */
    public void announceDiscordMessage(String username, String message) {
        if (!enableTTS || ttsUtils == null) return;
        
        // Announce in all active voice channels
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild != null) {
            for (VoiceChannel channel : guild.getVoiceChannels()) {
                if (!channel.getMembers().isEmpty()) {
                    ttsUtils.announceDiscordMessage(username, message, channel);
                }
            }
        }
    }
}
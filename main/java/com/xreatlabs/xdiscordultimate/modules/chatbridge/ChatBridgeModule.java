package com.xreatlabs.xdiscordultimate.modules.chatbridge;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import com.xreatlabs.xdiscordultimate.utils.imagemessage.ImageChar;
import com.xreatlabs.xdiscordultimate.utils.imagemessage.ImageMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.configuration.ConfigurationSection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChatBridgeModule extends Module implements Listener {

    private ChatBridgeListener discordListener;
    private String chatChannelName;
    private Map<String, String> worldChannels;
    private boolean enableMinecraftToDiscord;
    private boolean enableDiscordToMinecraft;
    private boolean useEmbeds;
    private boolean filterBotMessages;
    private String minecraftFormat;
    private String discordFormat;
    private String discordReplyFormat;
    
    // Webhook URL pattern for validation
    private static final Pattern WEBHOOK_PATTERN = Pattern.compile("https://discord(?:app)?\\.com/api/webhooks/\\d+/[\\w-]+");
    
    private boolean skinsRestorerEnabled;
    
    public ChatBridgeModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "ChatBridge";
    }
    
    @Override
    public String getDescription() {
        return "Bridges chat between Minecraft and Discord";
    }

    @Override
    protected ConfigurationSection getConfig() {
        return plugin.getConfigManager().getFeatureConfig("chat-bridge");
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        
        // Check for SkinsRestorer
        skinsRestorerEnabled = Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer");
        
        // Register Minecraft events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        info("Chat bridge module enabled");
    }
    
    @Override
    protected void onDisable() {
        // Unregister Discord listener
        if (discordListener != null && plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getJDA().removeEventListener(discordListener);
        }
        
        // Unregister Minecraft events
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        
        info("Chat bridge module disabled");
    }
    
    @Override
    public void onDiscordReady() {
        // Register Discord listener when Discord is ready
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            discordListener = new ChatBridgeListener();
            plugin.getDiscordManager().getJDA().addEventListener(discordListener);
            info("Discord chat listener registered");
        }
    }
    
    private void loadConfiguration() {
        chatChannelName = getConfig().getString("channel-id");
        if (chatChannelName == null || chatChannelName.isEmpty()) {
            chatChannelName = getConfig().getString("channel-name");
        }

        if (chatChannelName == null || chatChannelName.isEmpty()) {
            chatChannelName = "minecraft-chat";
            warning("ChatBridge: 'channel-id' or 'channel-name' not found in config, defaulting to 'minecraft-chat'.");
        }

        enableMinecraftToDiscord = getConfig().getBoolean("minecraft-to-discord", true);
        enableDiscordToMinecraft = getConfig().getBoolean("discord-to-minecraft", true);
        useEmbeds = getConfig().getBoolean("use-embeds", false);
        filterBotMessages = getConfig().getBoolean("filter-bot-messages", true);
        
        minecraftFormat = getConfig().getString("minecraft-format", "**%player%**: %message%");
        discordFormat = getConfig().getString("discord-format", "&7[&9Discord&7] &b%user%&7: &f%message%");
        discordReplyFormat = getConfig().getString("discord-reply-format", "&7[&9Discord&7] &b%user% &7(replying to &b%replied_user%&7): &f%message%");

        ConfigurationSection worldChannelsSection = getConfig().getConfigurationSection("world-channels");
        if (worldChannelsSection != null) {
            worldChannels = new HashMap<>();
            for (String worldName : worldChannelsSection.getKeys(false)) {
                worldChannels.put(worldName, worldChannelsSection.getString(worldName));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        info("onPlayerChat event triggered for player: " + event.getPlayer().getName());
        if (!enableMinecraftToDiscord) {
            info("Minecraft to Discord chat is disabled.");
            return;
        }
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Get world-specific channel, or fall back to default
        String worldName = player.getWorld().getName();
        String targetChannelName = worldChannels != null ? worldChannels.getOrDefault(worldName, chatChannelName) : chatChannelName;

        // Send to Discord
        info("Attempting to send message to Discord...");
        sendToDiscord(player, message, targetChannelName);
    }
    
    private void sendToDiscord(Player player, String message, String channelName) {
        info("sendToDiscord called for player: " + player.getName() + ", message: " + message);

        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            warning("Discord bot is not ready, unable to send chat message from " + player.getName());
            return;
        }
        info("Discord manager is ready.");

        TextChannel channel = getChatChannel(channelName);
        if (channel == null) {
            warning("Chat channel not found, Minecraft chat will not be sent to Discord. Please check your 'chat-channel-id' or 'chat-channel' configuration.");
            return;
        }
        info("Found chat channel: " + channel.getName() + " (" + channel.getId() + ")");

        String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay";

        if (skinsRestorerEnabled) {
            try {
                Class<?> skinsRestorerApi = Class.forName("net.skinsrestorer.api.SkinsRestorerAPI");
                Object api = skinsRestorerApi.getMethod("getApi").invoke(null);
                Object playerStorage = api.getClass().getMethod("getPlayerStorage").invoke(api);
                Object skin = playerStorage.getClass().getMethod("getSkinForPlayer", String.class).invoke(playerStorage, player.getName());

                if (skin != null) {
                    String skinUrl = (String) skin.getClass().getMethod("getUrl").invoke(skin);
                    if (skinUrl != null && !skinUrl.isEmpty()) {
                        // Crafatar can generate a render from the skin URL
                        avatarUrl = "https://crafatar.com/renders/head/" + skinUrl.substring(skinUrl.lastIndexOf('/') + 1) + "?overlay";
                    }
                }
            } catch (Exception e) {
                warning("Could not get skin from SkinsRestorer: " + e.getMessage());
            }
        }
        
        if (useEmbeds) {
            info("Using embeds to send message.");
            // Send as embed
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(0, 255, 0))
                .setAuthor(player.getName(), 
                    null, 
                    avatarUrl)
                .setDescription(message)
                .setTimestamp(Instant.now())
                .setFooter("Minecraft Chat", null);
            
            channel.sendMessageEmbeds(embed.build()).queue(
                success -> info("Successfully sent embed message to Discord for player: " + player.getName()),
                error -> warning("Failed to send embed message to Discord: " + error.getMessage())
            );
        } else {
            info("Using plain text to send message.");
            // Send as plain message
            String formattedMessage = minecraftFormat
                .replace("%player%", player.getName())
                .replace("%message%", message);
            
            info("Formatted message: " + formattedMessage);
            
            channel.sendMessage(formattedMessage).queue(
                success -> info("Successfully sent plain text message to Discord for player: " + player.getName()),
                error -> warning("Failed to send plain text message to Discord: " + error.getMessage())
            );
        }
    }
    
    private class ChatBridgeListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            // Check if it's from the correct channel (by ID first, then by name)
            String channelId = event.getChannel().getId();
            String channelName = event.getChannel().getName();

            boolean isWorldChannel = worldChannels != null && worldChannels.containsValue(channelId);
            
            // If we have a channel ID configured, use that
            if (!chatChannelName.isEmpty() && chatChannelName.matches("\\d+")) {
                if (!channelId.equals(chatChannelName) && !isWorldChannel) {
                    return;
                }
            } else {
                // Fallback to name comparison
                boolean isDefaultChannel = channelName.equalsIgnoreCase(chatChannelName);
                boolean isWorldChannelByName = worldChannels != null && worldChannels.values().stream().anyMatch(name -> name.equalsIgnoreCase(channelName));
                if (!isDefaultChannel && !isWorldChannelByName) {
                    return;
                }
            }
            
            // Ignore bot messages if configured
            if (filterBotMessages && event.getAuthor().isBot()) {
                return;
            }
            
            // Ignore if Discord to Minecraft is disabled
            if (!enableDiscordToMinecraft) {
                return;
            }
            
            User author = event.getAuthor();
            String message = event.getMessage().getContentDisplay();
            
            if (message.trim().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
                return;
            }

            // Check for replies
            Message referencedMessage = event.getMessage().getReferencedMessage();
            if (referencedMessage != null) {
                User repliedUser = referencedMessage.getAuthor();
                String repliedUserName = referencedMessage.getMember() != null ? referencedMessage.getMember().getEffectiveName() : repliedUser.getName();
                sendToMinecraft(author, message, event.getMember(), repliedUserName);
            } else {
                sendToMinecraft(author, message, event.getMember(), null);
            }
        }
    }
    
    private void sendToMinecraft(User author, String message, Member member, String repliedToUser) {
        // Get display name (nickname if available, otherwise username)
        String displayName = member != null && member.getNickname() != null ?
            member.getNickname() : author.getName();

        // Convert markdown to Minecraft format codes
        message = convertMarkdownToMinecraft(message);

        // Format the message
        String formattedMessage;
        if (repliedToUser != null) {
            formattedMessage = discordReplyFormat
                .replace("%user%", displayName)
                .replace("%replied_user%", repliedToUser)
                .replace("%message%", message);
        } else {
            formattedMessage = discordFormat
                .replace("%user%", displayName)
                .replace("%message%", message);
        }

        // Apply color codes
        formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);

        // Get the user's avatar
        String avatarUrl = author.getEffectiveAvatarUrl();

        try {
            BufferedImage image = ImageIO.read(new URL(avatarUrl));
            ImageMessage imageMessage = new ImageMessage(image, 8, ImageChar.BLOCK.getChar())
                .appendText(formattedMessage);

            for (Player player : Bukkit.getOnlinePlayers()) {
                imageMessage.sendToPlayer(player);
            }
        } catch (IOException e) {
            Bukkit.broadcastMessage(formattedMessage);
            warning("Failed to load avatar for " + author.getName() + ": " + e.getMessage());
        }

        // Relay Discord message to other servers
        if (plugin.getNetworkManager() != null) {
            plugin.getNetworkManager().send(
                com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.discordChat(
                    plugin.getConfig().getString("network.server_id", plugin.getServer().getName()),
                    displayName,
                    message
                )
            );
        }

        debug("Relayed Discord message to Minecraft: " + displayName);
    }

    private String convertMarkdownToMinecraft(String message) {
        return message
                .replaceAll("\\*\\*(.*?)\\*\\*", "§l$1§r") // Bold
                .replaceAll("\\*(.*?)\\*", "§o$1§r")     // Italics
                .replaceAll("__(.*?)__", "§n$1§r")     // Underline
                .replaceAll("~~(.*?)~~", "§m$1§r")   // Strikethrough
                .replaceAll("`(.*?)`", "§7$1§r");      // Code
    }
    
    private TextChannel getChatChannel(String channelName) {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }

        // Safeguard against a null or empty channel name, though loadConfiguration should prevent this.
        if (channelName == null || channelName.isEmpty()) {
            debug("ChatBridge: chatChannelName is not configured.");
            return null;
        }
        
        // First, try to resolve the channel by ID if the name is numeric.
        if (channelName.matches("\\d+")) {
            TextChannel channel = plugin.getDiscordManager().getTextChannelById(channelName);
            if (channel != null) {
                return channel;
            }
        }
        
        // Finally, fall back to looking up the channel by name.
        return plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(channelName, true)
            .stream()
            .findFirst()
            .orElseGet(() -> {
                debug("ChatBridge: Could not find channel with ID or name: " + channelName);
                return null;
            });
    }
    
    /**
     * Send a system message to Discord (like join/leave notifications)
     */
    public void sendSystemMessage(String message, Color color, String channelId) {
        TextChannel channel = null;
        if (channelId != null && !channelId.isEmpty()) {
            channel = plugin.getDiscordManager().getTextChannelById(channelId);
        }

        if (channel == null) {
            channel = getChatChannel(chatChannelName);
        }

        if (channel == null) return;

        if (useEmbeds) {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(color != null ? color : Color.GRAY)
                .setDescription(message)
                .setTimestamp(Instant.now());
            
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage(message).queue();
        }
    }

    public void sendSystemMessage(String message, Color color) {
        sendSystemMessage(message, color, null);
    }
    
    /**
     * Send a player join message to Discord
     */
    public void sendJoinMessage(Player player) {
        TextChannel channel = getChatChannel(chatChannelName);
        if (channel == null) return;
        
        String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay";
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(new Color(0, 255, 0))
            .setAuthor("➕ " + player.getName() + " joined the server", 
                null, 
                avatarUrl)
            .setDescription("**" + player.getName() + "** has joined the server!")
            .addField("Players Online", plugin.getServer().getOnlinePlayers().size() + "/" + plugin.getServer().getMaxPlayers(), true)
            .addField("Location", formatLocation(player), true)
            .addField("Ping", player.getPing() + "ms", true)
            .setTimestamp(Instant.now())
            .setFooter("Player Join • " + plugin.getServer().getName(), null);
        
        channel.sendMessageEmbeds(embed.build()).queue(
            success -> info("Sent join embed for " + player.getName()),
            error -> warning("Failed to send join embed: " + error.getMessage())
        );
    }
    
    /**
     * Send a player leave message to Discord
     */
    public void sendLeaveMessage(Player player) {
        TextChannel channel = getChatChannel(chatChannelName);
        if (channel == null) return;
        
        // Get playtime information
        long sessionTime = plugin.getPlaytimeTracker().getCurrentSessionTime(player);
        long totalTime = plugin.getPlaytimeTracker().getTotalPlaytime(player);
        String sessionPlaytime = plugin.getPlaytimeTracker().formatSessionTime(sessionTime);
        String totalPlaytime = plugin.getPlaytimeTracker().formatPlaytime(totalTime);
        
        String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay";
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(new Color(255, 0, 0))
            .setAuthor("➖ " + player.getName() + " left the server", 
                null, 
                avatarUrl)
            .setDescription("**" + player.getName() + "** has left the server!")
            .addField("Players Online", (plugin.getServer().getOnlinePlayers().size() - 1) + "/" + plugin.getServer().getMaxPlayers(), true)
            .addField("Session Time", sessionPlaytime, true)
            .addField("Total Playtime", totalPlaytime, true)
            .addField("Last Location", formatLocation(player), false)
            .setTimestamp(Instant.now())
            .setFooter("Player Leave • " + plugin.getServer().getName(), null);
        
        channel.sendMessageEmbeds(embed.build()).queue(
            success -> info("Sent leave embed for " + player.getName() + " (Session: " + sessionPlaytime + ")"),
            error -> warning("Failed to send leave embed: " + error.getMessage())
        );
    }
    
    /**
     * Send a death message to Discord
     */
    public void sendDeathMessage(Player player, String deathMessage) {
        String message = plugin.getConfig().getString("messages.death-message", 
            ":skull: **%player%** %death_message%");
        message = message.replace("%player%", player.getName())
                        .replace("%death_message%", deathMessage);
        
        sendSystemMessage(message, new Color(255, 165, 0));
    }
    
    /**
     * Format player location for Discord embeds
     */
    private String formatLocation(Player player) {
        return String.format("%s at %d, %d, %d",
            player.getWorld().getName(),
            player.getLocation().getBlockX(),
            player.getLocation().getBlockY(),
            player.getLocation().getBlockZ()
        );
    }
}

package com.xreatlabs.xdiscordultimate.modules.chatbridge;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
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

import java.awt.*;
import java.time.Instant;
import java.util.regex.Pattern;

public class ChatBridgeModule extends Module implements Listener {
    
    private ChatBridgeListener discordListener;
    private String chatChannelName;
    private boolean enableMinecraftToDiscord;
    private boolean enableDiscordToMinecraft;
    private boolean useEmbeds;
    private boolean filterBotMessages;
    private String minecraftFormat;
    private String discordFormat;
    
    // Webhook URL pattern for validation
    private static final Pattern WEBHOOK_PATTERN = Pattern.compile("https://discord(?:app)?\\.com/api/webhooks/\\d+/[\\w-]+");
    
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
    protected void onEnable() {
        loadConfiguration();
        
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
        chatChannelName = getConfig().getString("chat-channel-id", "");
        if (chatChannelName.isEmpty()) {
            // Fallback to name-based lookup
            chatChannelName = getConfig().getString("chat-channel", "minecraft-chat");
        }
        enableMinecraftToDiscord = getConfig().getBoolean("minecraft-to-discord", true);
        enableDiscordToMinecraft = getConfig().getBoolean("discord-to-minecraft", true);
        useEmbeds = getConfig().getBoolean("use-embeds", false);
        filterBotMessages = getConfig().getBoolean("filter-bot-messages", true);
        
        minecraftFormat = getConfig().getString("minecraft-format", "**%player%**: %message%");
        discordFormat = getConfig().getString("discord-format", "&7[&9Discord&7] &b%user%&7: &f%message%");
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!enableMinecraftToDiscord) return;
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Send to Discord
        sendToDiscord(player, message);
    }
    
    private void sendToDiscord(Player player, String message) {
        TextChannel channel = getChatChannel();
        if (channel == null) {
            debug("Chat channel not found: " + chatChannelName);
            return;
        }
        
        if (useEmbeds) {
            // Send as embed
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(0, 255, 0))
                .setAuthor(player.getName(), 
                    null, 
                    "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay")
                .setDescription(message)
                .setTimestamp(Instant.now())
                .setFooter("Minecraft Chat", null);
            
            channel.sendMessageEmbeds(embed.build()).queue(
                success -> debug("Sent chat message to Discord: " + player.getName()),
                error -> warning("Failed to send chat message to Discord: " + error.getMessage())
            );
        } else {
            // Send as plain message
            String formattedMessage = minecraftFormat
                .replace("%player%", player.getName())
                .replace("%message%", message);
            
            channel.sendMessage(formattedMessage).queue(
                success -> debug("Sent chat message to Discord: " + player.getName()),
                error -> warning("Failed to send chat message to Discord: " + error.getMessage())
            );
        }
    }
    
    private class ChatBridgeListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            // Check if it's from the correct channel (by ID first, then by name)
            String channelId = event.getChannel().getId();
            String channelName = event.getChannel().getName();
            
            // If we have a channel ID configured, use that
            if (!chatChannelName.isEmpty() && chatChannelName.matches("\\d+")) {
                if (!channelId.equals(chatChannelName)) {
                    return;
                }
            } else {
                // Fallback to name comparison
                if (!channelName.equalsIgnoreCase(chatChannelName)) {
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
            
            // Don't relay empty messages
            if (message.trim().isEmpty()) {
                return;
            }
            
            // Format and send to Minecraft
            sendToMinecraft(author, message, event.getMember());
        }
    }
    
    private void sendToMinecraft(User author, String message, Member member) {
        // Get display name (nickname if available, otherwise username)
        String displayName = member != null && member.getNickname() != null ? 
            member.getNickname() : author.getName();
        
        // Format the message
        String formattedMessage = discordFormat
            .replace("%user%", displayName)
            .replace("%message%", message);
        
        // Apply color codes
        formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);
        
        // Broadcast to all players
        Bukkit.broadcastMessage(formattedMessage);
        
        debug("Relayed Discord message to Minecraft: " + displayName);
    }
    
    private TextChannel getChatChannel() {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        // First try the module's configured channel ID
        if (!chatChannelName.isEmpty() && chatChannelName.matches("\\d+")) {
            TextChannel channel = plugin.getDiscordManager().getTextChannelById(chatChannelName);
            if (channel != null) {
                return channel;
            }
        }
        
        // Then try to get by ID from main config
        String channelId = plugin.getConfig().getString("discord.channels.chat");
        if (channelId != null && !channelId.isEmpty() && !channelId.equals("YOUR_CHAT_CHANNEL_ID")) {
            TextChannel channel = plugin.getDiscordManager().getTextChannelById(channelId);
            if (channel != null) {
                return channel;
            }
        }
        
        // Fall back to name lookup
        return plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(chatChannelName, true)
            .stream()
            .findFirst()
            .orElse(null);
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
            channel = getChatChannel();
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
        String message = plugin.getConfig().getString("messages.join-message", 
            ":arrow_right: **%player%** joined the server!");
        message = message.replace("%player%", player.getName());
        
        sendSystemMessage(message, new Color(0, 255, 0));
    }
    
    /**
     * Send a player leave message to Discord
     */
    public void sendLeaveMessage(Player player) {
        String message = plugin.getConfig().getString("messages.leave-message", 
            ":arrow_left: **%player%** left the server!");
        message = message.replace("%player%", player.getName());
        
        sendSystemMessage(message, new Color(255, 0, 0));
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
}
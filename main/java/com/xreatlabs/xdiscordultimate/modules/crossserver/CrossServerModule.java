package com.xreatlabs.xdiscordultimate.modules.crossserver;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.awt.Color;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrossServerModule extends Module implements Listener {
    
    private CrossServerListener discordListener;
    private String globalChatChannel;
    private String serverName;
    private String chatFormat;
    private boolean enableGlobalChat;
    private boolean enablePrivateMessages;
    private boolean enableMentions;
    private boolean showServerInChat;
    
    // Private message tracking
    private final Map<UUID, String> lastMessageTarget = new ConcurrentHashMap<>();
    private final Map<String, UUID> discordToMinecraft = new ConcurrentHashMap<>();
    
    // Mention pattern
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]{3,16})");
    
    public CrossServerModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "CrossServer";
    }
    
    @Override
    public String getDescription() {
        return "Cross-server chat and private messaging system";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Register Discord listener
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            discordListener = new CrossServerListener();
            plugin.getDiscordManager().getJDA().addEventListener(discordListener);
        }
        
        // Load Discord-Minecraft mappings
        loadUserMappings();
        
        info("Cross-server module enabled");
    }
    
    @Override
    protected void onDisable() {
        // Unregister Discord listener
        if (discordListener != null && plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getJDA().removeEventListener(discordListener);
        }
        
        // Unregister events
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
        
        info("Cross-server module disabled");
    }
    
    private void loadConfiguration() {
        globalChatChannel = getConfig().getString("global-chat-channel", "global-chat");
        serverName = getConfig().getString("server-name", plugin.getServer().getName());
        chatFormat = getConfig().getString("chat-format", "[%server%] %player%: %message%");
        enableGlobalChat = getConfig().getBoolean("enable-global-chat", true);
        enablePrivateMessages = getConfig().getBoolean("enable-private-messages", true);
        enableMentions = getConfig().getBoolean("enable-mentions", true);
        showServerInChat = getConfig().getBoolean("show-server-in-chat", true);
    }
    
    private void loadUserMappings() {
        // Load verified users for quick Discord ID to Minecraft UUID mapping
        // TODO: Implement proper user loading from database
        debug("User mappings loading not implemented");
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!enableGlobalChat) return;
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Check if message starts with global chat prefix
        if (!message.startsWith("!")) return;
        
        // Remove prefix
        message = message.substring(1).trim();
        if (message.isEmpty()) return;
        
        // Cancel original message
        event.setCancelled(true);
        
        // Process mentions
        if (enableMentions) {
            message = processMentions(message, player);
        }
        
        // Send to Discord
        sendToDiscord(player, message);
        
        // Send to local server with formatting
        String formattedMessage = formatGlobalMessage(serverName, player.getName(), message, true);
        Bukkit.broadcastMessage(formattedMessage);
    }
    
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!enablePrivateMessages) return;
        
        String[] args = event.getMessage().split(" ");
        String command = args[0].toLowerCase();
        
        if (command.equals("/msg") || command.equals("/tell") || command.equals("/w")) {
            if (args.length < 3) return;
            
            String target = args[1];
            
            // Check if target starts with @ (Discord user)
            if (target.startsWith("@")) {
                event.setCancelled(true);
                
                Player player = event.getPlayer();
                String discordTarget = target.substring(1);
                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                
                sendPrivateMessage(player, discordTarget, message);
            }
        } else if (command.equals("/r") || command.equals("/reply")) {
            Player player = event.getPlayer();
            String lastTarget = lastMessageTarget.get(player.getUniqueId());
            
            if (lastTarget != null && lastTarget.startsWith("@")) {
                event.setCancelled(true);
                
                if (args.length < 2) {
                    plugin.getMessageManager().sendError(player, "Usage: /r <message>");
                    return;
                }
                
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                sendPrivateMessage(player, lastTarget.substring(1), message);
            }
        }
    }
    
    private void sendToDiscord(Player player, String message) {
        TextChannel channel = getGlobalChatChannel();
        if (channel == null) return;
        
        // Get player's Discord info if verified
        String discordId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId())
            .join();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(new Color(0, 255, 0))
            .setAuthor(player.getName() + " @ " + serverName, 
                null, 
                "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay")
            .setDescription(message)
            .setTimestamp(Instant.now())
            .setFooter(serverName, null);
        
        // Add verified badge if player is verified
        if (discordId != null) {
            Member member = channel.getGuild().getMemberById(discordId);
            if (member != null) {
                embed.setAuthor(player.getName() + " @ " + serverName + " ✓", 
                    null, 
                    member.getUser().getAvatarUrl());
            }
        }
        
        channel.sendMessageEmbeds(embed.build()).queue();

        // Relay to other servers via network chat packet
        if (plugin.getNetworkManager() != null) {
            plugin.getNetworkManager().send(
                com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.chat(
                    plugin.getConfig().getString("network.server_id", plugin.getServer().getName()),
                    player.getName(), message
                )
            );
        }
    }
    
    private void sendPrivateMessage(Player sender, String targetDiscord, String message) {
        // Find Discord user
        User targetUser = findDiscordUser(targetDiscord);
        
        if (targetUser == null) {
            plugin.getMessageManager().sendError(sender, "Discord user not found: @" + targetDiscord);
            return;
        }
        
        // Get sender's Discord info
        String senderDiscordId = plugin.getDatabaseManager().getDiscordId(sender.getUniqueId())
            .join();
        
        // Build message
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(new Color(128, 0, 255))
            .setTitle("Private Message from Minecraft")
            .setAuthor(sender.getName() + " @ " + serverName, 
                null, 
                "https://crafatar.com/avatars/" + sender.getUniqueId() + "?overlay")
            .setDescription(message)
            .setTimestamp(Instant.now())
            .setFooter("Reply with /msg " + sender.getName() + " <message>", null);
        
        // Send DM
        if (targetUser != null) {
            targetUser.openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(embed.build()).queue(
                    success -> {
                        plugin.getMessageManager().sendSuccess(sender, 
                            "Message sent to @" + targetUser.getName());
                        lastMessageTarget.put(sender.getUniqueId(), "@" + targetUser.getName());
                    },
                    error -> plugin.getMessageManager().sendError(sender, 
                        "Failed to send message: " + error.getMessage())
                );
            });
        }
    }
    
    private class CrossServerListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            // Ignore bot messages
            if (event.getAuthor().isBot()) return;
            
            // Handle global chat
            if (enableGlobalChat && event.getChannel().getName().equalsIgnoreCase(globalChatChannel)) {
                handleGlobalChatMessage(event);
            }
            
            // Handle private messages
            if (enablePrivateMessages && event.getChannel().getType() == net.dv8tion.jda.api.entities.channel.ChannelType.PRIVATE) {
                handlePrivateMessage(event);
            }
        }
    }
    
    private void handleGlobalChatMessage(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String message = event.getMessage().getContentDisplay();
        
        // Get server name from message if available
        final String[] sourceServer = {"Discord"};
        Member member = event.getMember();
        if (member != null) {
            // Check if user has a server role/nickname that indicates their server
            member.getRoles().stream()
                .filter(role -> role.getName().startsWith("Server:"))
                .findFirst()
                .ifPresent(role -> {
                    String serverFromRole = role.getName().substring(7).trim();
                    if (!serverFromRole.isEmpty()) {
                        sourceServer[0] = serverFromRole;
                    }
                });
        }
        
        // Don't relay our own messages back
        if (sourceServer[0].equals(serverName)) return;
        
        // Format and broadcast to Minecraft
        String formattedMessage = formatGlobalMessage(sourceServer[0], author.getName(), message, false);
        Bukkit.broadcastMessage(formattedMessage);
    }
    
    private void handlePrivateMessage(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String message = event.getMessage().getContentDisplay();
        
        // Parse command
        if (!message.startsWith("/msg ") && !message.startsWith("/tell ") && !message.startsWith("/w ")) {
            return;
        }
        
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            event.getChannel().sendMessage("Usage: /msg <player> <message>").queue();
            return;
        }
        
        String targetName = parts[1];
        String privateMessage = parts[2];
        
        // Find target player
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            event.getChannel().sendMessage("Player not found: " + targetName).queue();
            return;
        }
        
        // Get sender's Minecraft name if verified
        UUID senderUuid = discordToMinecraft.get(author.getId());
        String senderName = senderUuid != null ?
            Bukkit.getOfflinePlayer(senderUuid).getName() :
            author.getName();
        
        // Send message to player
        target.sendMessage(ChatColor.LIGHT_PURPLE + "[Discord PM] " +
            ChatColor.GRAY + senderName + ": " +
            ChatColor.WHITE + privateMessage);
        
        // Store for reply
        lastMessageTarget.put(target.getUniqueId(), "@" + author.getName());
        
        // Confirm to Discord
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(new Color(0, 255, 0))
            .setTitle("Message Sent")
            .setDescription("Your message was delivered to " + targetName)
            .setTimestamp(Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    private String processMentions(String message, Player sender) {
        if (!enableMentions) return message;
        
        Matcher matcher = MENTION_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String username = matcher.group(1);
            Player mentioned = Bukkit.getPlayer(username);
            
            if (mentioned != null && mentioned.isOnline()) {
                // Notify mentioned player
                mentioned.sendMessage(ChatColor.YELLOW + sender.getName() +
                    " mentioned you in global chat!");
                
                // Play sound
                mentioned.playSound(mentioned.getLocation(),
                    org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                
                // Highlight mention
                matcher.appendReplacement(result,
                    ChatColor.YELLOW + "@" + username + ChatColor.RESET);
            } else {
                matcher.appendReplacement(result, "@" + username);
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    private String formatGlobalMessage(String server, String player, String message, boolean isLocal) {
        String formatted = chatFormat
            .replace("%server%", server)
            .replace("%player%", player)
            .replace("%message%", message);
        
        // Add color coding
        if (isLocal) {
            formatted = ChatColor.GREEN + "[G] " + ChatColor.RESET + formatted;
        } else {
            formatted = ChatColor.AQUA + "[G] " + ChatColor.GRAY + formatted;
        }
        
        return formatted;
    }
    
    private TextChannel getGlobalChatChannel() {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        return plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(globalChatChannel, true)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    private User findDiscordUser(String username) {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        // Try to find by username
        return plugin.getDiscordManager().getJDA().getUsers().stream()
            .filter(user -> user.getName().equalsIgnoreCase(username))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Send a cross-server message programmatically
     */
    public void sendCrossServerMessage(String server, String sender, String message) {
        // Send to Discord
        TextChannel channel = getGlobalChatChannel();
        if (channel != null) {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(0, 123, 255))
                .setAuthor(sender + " @ " + server, null, null)
                .setDescription(message)
                .setTimestamp(Instant.now())
                .setFooter(server, null);
            
            channel.sendMessageEmbeds(embed.build()).queue();
        }
        
        // Broadcast locally
        String formatted = formatGlobalMessage(server, sender, message, false);
        Bukkit.broadcastMessage(formatted);
    }
    
    /**
     * Get list of online servers (from Discord roles)
     */
    public List<String> getOnlineServers() {
        List<String> servers = new ArrayList<>();
        servers.add(serverName); // Add self
        
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getMainGuild().getRoles().stream()
                .filter(role -> role.getName().startsWith("Server:"))
                .map(role -> role.getName().substring(7).trim())
                .filter(name -> !name.isEmpty() && !name.equals(serverName))
                .forEach(servers::add);
        }
        
        return servers;
    }
}
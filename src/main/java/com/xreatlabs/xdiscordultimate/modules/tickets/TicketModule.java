package com.xreatlabs.xdiscordultimate.modules.tickets;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TicketModule extends Module {
    
    private TicketListener listener;
    private String ticketCategoryName;
    private String ticketChannelName;
    private String supportRoleName;
    private int maxOpenTickets;
    private boolean autoCloseInactive;
    private int inactiveHours;
    
    // Active tickets
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private final Map<String, String> channelToTicket = new ConcurrentHashMap<>();
    private final Map<String, Integer> userTicketCount = new ConcurrentHashMap<>();
    
    public TicketModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Tickets";
    }
    
    @Override
    public String getDescription() {
        return "Support ticket system with Discord integration";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        
        // Register Discord listener
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            listener = new TicketListener();
            plugin.getDiscordManager().getJDA().addEventListener(listener);
            
            // Create ticket panel if needed
            createTicketPanel();
            
            // Start auto-close task
            if (autoCloseInactive) {
                startAutoCloseTask();
            }
        }
        
        info("Ticket module enabled");
    }
    
    @Override
    protected void onDisable() {
        // Unregister Discord listener
        if (listener != null && plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getJDA().removeEventListener(listener);
        }
        
        // Save active tickets
        saveActiveTickets();
        
        info("Ticket module disabled");
    }
    
    private void loadConfiguration() {
        ticketCategoryName = getConfig().getString("category-name", "Support Tickets");
        ticketChannelName = getConfig().getString("panel-channel", "create-ticket");
        supportRoleName = getConfig().getString("support-role", "Support");
        maxOpenTickets = getConfig().getInt("max-open-tickets", 3);
        autoCloseInactive = getConfig().getBoolean("auto-close-inactive", true);
        inactiveHours = getConfig().getInt("inactive-hours", 48);
    }
    
    /**
     * Create a new ticket
     */
    public void createTicket(Player player, String subject) {
        String userId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId())
            .join();
        
        if (userId == null) {
            plugin.getMessageManager().sendError(player, "You must verify your Discord account first!");
            return;
        }
        
        // Check ticket limit
        int currentTickets = userTicketCount.getOrDefault(userId, 0);
        if (currentTickets >= maxOpenTickets) {
            plugin.getMessageManager().sendError(player, 
                "You have reached the maximum number of open tickets (" + maxOpenTickets + ")");
            return;
        }
        
        // Create ticket in Discord
        createDiscordTicket(userId, player.getName(), subject);
        
        plugin.getMessageManager().sendSuccess(player, "Your ticket has been created!");
    }
    
    private void createTicketPanel() {
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild == null) return;
        
        TextChannel panelChannel = guild.getTextChannelsByName(ticketChannelName, true)
            .stream().findFirst().orElse(null);
        
        if (panelChannel == null) {
            // Create the channel
            Category category = getOrCreateCategory(guild);
            panelChannel = guild.createTextChannel(ticketChannelName)
                .setParent(category)
                .setTopic("Click the button below to create a support ticket")
                .complete();
        }
        
        // Make panelChannel final for lambda
        final TextChannel finalPanelChannel = panelChannel;
        
        // Clear old messages
        finalPanelChannel.getHistory().retrievePast(10).queue(messages -> {
            for (Message msg : messages) {
                if (msg.getAuthor().equals(plugin.getDiscordManager().getJDA().getSelfUser())) {
                    msg.delete().queue();
                }
            }
            
            // Send new panel
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(0, 123, 255))
                .setTitle("🎫 Support Tickets")
                .setDescription("Need help? Click the button below to create a support ticket!\n\n" +
                    "**Before creating a ticket:**\n" +
                    "• Check our FAQ and documentation\n" +
                    "• Make sure you have verified your account\n" +
                    "• Provide as much detail as possible\n\n" +
                    "**Ticket Rules:**\n" +
                    "• Maximum " + maxOpenTickets + " open tickets per user\n" +
                    "• Tickets auto-close after " + inactiveHours + " hours of inactivity\n" +
                    "• Be respectful to support staff")
                .setFooter("Click below to create a ticket", null)
                .setTimestamp(Instant.now());
            
            finalPanelChannel.sendMessageEmbeds(embed.build())
                .setActionRow(
                    Button.primary("create_ticket", "Create Ticket")
                        .withEmoji(Emoji.fromUnicode("🎫"))
                )
                .queue();
        });
    }
    
    private void createDiscordTicket(String userId, String minecraftName, String subject) {
        Guild guild = plugin.getDiscordManager().getMainGuild();
        if (guild == null) return;
        
        Member member = guild.getMemberById(userId);
        if (member == null) return;
        
        // Generate ticket ID
        String ticketId = generateTicketId();
        
        // Create ticket channel
        Category category = getOrCreateCategory(guild);
        TextChannel ticketChannel = guild.createTextChannel("ticket-" + ticketId)
            .setParent(category)
            .setTopic("Support ticket for " + member.getEffectiveName() + " | MC: " + minecraftName)
            .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
            .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
            .complete();
        
        // Add support role permissions
        Role supportRole = guild.getRolesByName(supportRoleName, true).stream().findFirst().orElse(null);
        if (supportRole != null) {
            ticketChannel.upsertPermissionOverride(supportRole)
                .setAllowed(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_MANAGE)
                .queue();
        }
        
        // Create ticket object
        Ticket ticket = new Ticket(ticketId, userId, minecraftName, subject, ticketChannel.getId());
        activeTickets.put(ticketId, ticket);
        channelToTicket.put(ticketChannel.getId(), ticketId);
        userTicketCount.merge(userId, 1, Integer::sum);
        
        // Send welcome message
        EmbedBuilder welcomeEmbed = new EmbedBuilder()
            .setColor(new Color(0, 255, 0))
            .setTitle("🎫 Ticket #" + ticketId)
            .setDescription("Welcome " + member.getAsMention() + "!\n\n" +
                "**Subject:** " + subject + "\n" +
                "**Minecraft Name:** " + minecraftName + "\n\n" +
                "A support team member will be with you shortly.\n" +
                "Please describe your issue in detail.")
            .addField("Ticket Information", 
                "Created: <t:" + (System.currentTimeMillis() / 1000) + ":R>\n" +
                "Status: Open", false)
            .setFooter("Ticket System", null)
            .setTimestamp(Instant.now());
        
        ticketChannel.sendMessageEmbeds(welcomeEmbed.build())
            .setActionRow(
                Button.danger("close_ticket", "Close Ticket")
                    .withEmoji(Emoji.fromUnicode("🔒")),
                Button.secondary("claim_ticket", "Claim Ticket")
                    .withEmoji(Emoji.fromUnicode("✋"))
            )
            .queue();
        
        // Notify support role
        if (supportRole != null) {
            ticketChannel.sendMessage(supportRole.getAsMention() + " - New ticket created!").queue();
        }
        
        // Log ticket creation
        logTicketAction(ticketId, "created", member.getEffectiveName());
    }
    
    private class TicketListener extends ListenerAdapter {
        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            String buttonId = event.getComponentId();
            Member member = event.getMember();
            
            if (member == null) return;
            
            switch (buttonId) {
                case "create_ticket":
                    handleCreateTicket(event);
                    break;
                case "close_ticket":
                    handleCloseTicket(event);
                    break;
                case "claim_ticket":
                    handleClaimTicket(event);
                    break;
            }
        }
        
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            
            String channelId = event.getChannel().getId();
            String ticketId = channelToTicket.get(channelId);
            
            if (ticketId != null) {
                Ticket ticket = activeTickets.get(ticketId);
                if (ticket != null) {
                    ticket.lastActivity = System.currentTimeMillis();
                    
                    // Log message to transcript
                    ticket.addMessage(event.getAuthor().getName(), event.getMessage().getContentRaw());
                }
            }
        }
    }
    
    private void handleCreateTicket(ButtonInteractionEvent event) {
        Member member = event.getMember();
        String userId = member.getId();
        
        // Check ticket limit
        int currentTickets = userTicketCount.getOrDefault(userId, 0);
        if (currentTickets >= maxOpenTickets) {
            event.reply("❌ You have reached the maximum number of open tickets (" + maxOpenTickets + ")")
                .setEphemeral(true).queue();
            return;
        }
        
        // Check if verified
        String minecraftName = null; // TODO: Implement getMinecraftName
        if (minecraftName == null) {
            event.reply("❌ You must verify your Minecraft account first! Use `/verify` in-game.")
                .setEphemeral(true).queue();
            return;
        }
        
        event.reply("✅ Creating your ticket...").setEphemeral(true).queue();
        createDiscordTicket(userId, minecraftName, "General Support");
    }
    
    private void handleCloseTicket(ButtonInteractionEvent event) {
        String channelId = event.getChannel().getId();
        String ticketId = channelToTicket.get(channelId);
        
        if (ticketId == null) {
            event.reply("❌ This is not a valid ticket channel!").setEphemeral(true).queue();
            return;
        }
        
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) return;
        
        // Check permissions
        Member member = event.getMember();
        boolean canClose = member.getId().equals(ticket.userId) || 
            member.hasPermission(Permission.MANAGE_CHANNEL) ||
            member.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase(supportRoleName));
        
        if (!canClose) {
            event.reply("❌ You don't have permission to close this ticket!").setEphemeral(true).queue();
            return;
        }
        
        event.reply("🔒 Closing ticket...").queue();
        closeTicket(ticketId, member.getEffectiveName());
    }
    
    private void handleClaimTicket(ButtonInteractionEvent event) {
        Member member = event.getMember();
        
        // Check if member has support role
        boolean isSupport = member.getRoles().stream()
            .anyMatch(role -> role.getName().equalsIgnoreCase(supportRoleName));
        
        if (!isSupport) {
            event.reply("❌ Only support staff can claim tickets!").setEphemeral(true).queue();
            return;
        }
        
        String channelId = event.getChannel().getId();
        String ticketId = channelToTicket.get(channelId);
        
        if (ticketId == null) return;
        
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) return;
        
        if (ticket.claimedBy != null) {
            event.reply("❌ This ticket is already claimed by " + ticket.claimedBy).setEphemeral(true).queue();
            return;
        }
        
        ticket.claimedBy = member.getEffectiveName();
        ticket.claimedById = member.getId();
        
        event.reply("✅ You have claimed this ticket!").queue();
        
        // Update channel topic
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel != null) {
            channel.getManager().setTopic(channel.getTopic() + " | Claimed by " + member.getEffectiveName()).queue();
        }
        
        logTicketAction(ticketId, "claimed", member.getEffectiveName());
    }
    
    private void closeTicket(String ticketId, String closedBy) {
        Ticket ticket = activeTickets.remove(ticketId);
        if (ticket == null) return;
        
        channelToTicket.remove(ticket.channelId);
        userTicketCount.merge(ticket.userId, -1, Integer::sum);
        
        Guild guild = plugin.getDiscordManager().getMainGuild();
        TextChannel channel = guild.getTextChannelById(ticket.channelId);
        
        if (channel != null) {
            // Generate transcript
            String transcript = ticket.generateTranscript();
            
            // Send closing message
            EmbedBuilder closeEmbed = new EmbedBuilder()
                .setColor(new Color(255, 0, 0))
                .setTitle("🔒 Ticket Closed")
                .setDescription("This ticket has been closed by " + closedBy)
                .addField("Ticket ID", ticket.id, true)
                .addField("Duration", formatDuration(System.currentTimeMillis() - ticket.createdAt), true)
                .setTimestamp(Instant.now());
            
            channel.sendMessageEmbeds(closeEmbed.build()).queue();
            
            // Delete channel after delay
            channel.delete().queueAfter(10, TimeUnit.SECONDS);
        }
        
        // Save transcript
        // TODO: Implement transcript saving
        logTicketAction(ticketId, "closed", closedBy);
    }
    
    private void startAutoCloseTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long inactiveMillis = inactiveHours * 60 * 60 * 1000L;
            long currentTime = System.currentTimeMillis();
            
            activeTickets.values().stream()
                .filter(ticket -> currentTime - ticket.lastActivity > inactiveMillis)
                .forEach(ticket -> {
                    Bukkit.getScheduler().runTask(plugin, () -> 
                        closeTicket(ticket.id, "System (Inactivity)"));
                });
        }, 20L * 60 * 30, 20L * 60 * 30); // Check every 30 minutes
    }
    
    private Category getOrCreateCategory(Guild guild) {
        return guild.getCategoriesByName(ticketCategoryName, true).stream()
            .findFirst()
            .orElseGet(() -> guild.createCategory(ticketCategoryName).complete());
    }
    
    private String generateTicketId() {
        return String.format("%04d", new Random().nextInt(10000));
    }
    
    private void logTicketAction(String ticketId, String action, String user) {
        info("Ticket #" + ticketId + " " + action + " by " + user);
    }
    
    private void saveTranscript(Ticket ticket, String transcript) {
        // TODO: Save to file or database
        debug("Saved transcript for ticket #" + ticket.id);
    }
    
    private void saveActiveTickets() {
        // TODO: Save active tickets to database for persistence
    }
    
    private String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        return String.format("%d hours, %d minutes", hours, minutes);
    }
    
    /**
     * Ticket data class
     */
    private static class Ticket {
        final String id;
        final String userId;
        final String minecraftName;
        final String subject;
        final String channelId;
        final long createdAt;
        long lastActivity;
        String claimedBy;
        String claimedById;
        final List<TicketMessage> messages;
        
        Ticket(String id, String userId, String minecraftName, String subject, String channelId) {
            this.id = id;
            this.userId = userId;
            this.minecraftName = minecraftName;
            this.subject = subject;
            this.channelId = channelId;
            this.createdAt = System.currentTimeMillis();
            this.lastActivity = createdAt;
            this.messages = new ArrayList<>();
        }
        
        void addMessage(String author, String content) {
            messages.add(new TicketMessage(author, content, System.currentTimeMillis()));
        }
        
        String generateTranscript() {
            StringBuilder transcript = new StringBuilder();
            transcript.append("Ticket #").append(id).append(" - ").append(subject).append("\n");
            transcript.append("User: ").append(minecraftName).append("\n");
            transcript.append("Created: ").append(new Date(createdAt)).append("\n");
            transcript.append("Claimed by: ").append(claimedBy != null ? claimedBy : "None").append("\n");
            transcript.append("\n--- Messages ---\n\n");
            
            for (TicketMessage msg : messages) {
                transcript.append("[").append(new Date(msg.timestamp)).append("] ");
                transcript.append(msg.author).append(": ").append(msg.content).append("\n");
            }
            
            return transcript.toString();
        }
    }
    
    private static class TicketMessage {
        final String author;
        final String content;
        final long timestamp;
        
        TicketMessage(String author, String content, long timestamp) {
            this.author = author;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}
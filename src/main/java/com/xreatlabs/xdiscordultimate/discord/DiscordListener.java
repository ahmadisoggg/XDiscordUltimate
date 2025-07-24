package com.xreatlabs.xdiscordultimate.discord;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.Bukkit;

import java.awt.*;
import java.time.Instant;

public class DiscordListener implements EventListener {
    
    private final XDiscordUltimate plugin;
    
    public DiscordListener(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            onReady((ReadyEvent) event);
        } else if (event instanceof MessageReceivedEvent) {
            onMessageReceived((MessageReceivedEvent) event);
        } else if (event instanceof GuildMemberJoinEvent) {
            onGuildMemberJoin((GuildMemberJoinEvent) event);
        } else if (event instanceof GuildMemberRemoveEvent) {
            onGuildMemberRemove((GuildMemberRemoveEvent) event);
        } else if (event instanceof SlashCommandInteractionEvent) {
            onSlashCommand((SlashCommandInteractionEvent) event);
        }
    }
    
    private void onReady(ReadyEvent event) {
        plugin.getLogger().info("Discord bot is ready!");
        plugin.getLogger().info("Connected to guild: " + event.getJDA().getGuilds().get(0).getName());
        plugin.getLogger().info("Bot user: " + event.getJDA().getSelfUser().getAsTag());
        
        // Update bot activity
        plugin.getDiscordManager().updateActivity();
        
        // Register slash commands
        registerSlashCommands(event);
        
        // Notify modules that Discord is ready
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (plugin.getModuleManager() != null) {
                plugin.getModuleManager().onDiscordReady();
            }
        });
    }
    
    private void registerSlashCommands(ReadyEvent event) {
        try {
            var guild = plugin.getDiscordManager().getMainGuild();
            if (guild != null) {
                // Register basic bot commands
                guild.upsertCommand(Commands.slash("help", "Show bot commands and information")).queue();
                guild.upsertCommand(Commands.slash("status", "Show server status")).queue();
                guild.upsertCommand(Commands.slash("players", "List online players")).queue();
                guild.upsertCommand(Commands.slash("server", "Show server information")).queue();
                guild.upsertCommand(Commands.slash("verify", "Get a verification code to link your Minecraft account")).queue();
                
                plugin.getLogger().info("Registered slash commands successfully");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register slash commands: " + e.getMessage());
        }
    }
    
    private void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) return;
        
        // Handle DMs for support/verification
        if (!event.isFromGuild()) {
            handleDirectMessage(event);
            return;
        }
        
        // Log message for debugging
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Discord message from " + event.getAuthor().getAsTag() +
                " in #" + event.getChannel().getName() + ": " + event.getMessage().getContentDisplay());
        }
        
        // Handle bot commands
        String message = event.getMessage().getContentDisplay().trim();
        if (message.startsWith("!") || message.startsWith("/")) {
            handleBotCommand(event);
        }
    }
    
    private void handleDirectMessage(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String message = event.getMessage().getContentDisplay().trim();
        
        plugin.getLogger().info("Received DM from " + author.getAsTag() + ": " + message);
        
        // Handle verification codes
        if (message.matches("\\d{6}")) {
            // This looks like a verification code
            var verificationCode = plugin.getDatabaseManager().getVerificationCode(message);
            if (verificationCode != null && !verificationCode.isExpired()) {
                // Valid verification code
                event.getChannel().sendMessage("✅ Verification code accepted! You can now use `/verify " + message + "` in-game.").queue();
            } else {
                event.getChannel().sendMessage("❌ Invalid or expired verification code.").queue();
            }
        } else if (message.toLowerCase().startsWith("help")) {
            // Send help message
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.BLUE)
                .setTitle("XDiscordUltimate Help")
                .setDescription("Available commands:")
                .addField("Verification", "Use `/verify` in-game to link your accounts", false)
                .addField("Support", "Use `/support <message>` in-game to create a ticket", false)
                .addField("Commands", "Use `!help` in the server for bot commands", false)
                .setTimestamp(Instant.now());
            
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
    
    private void handleBotCommand(MessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay().trim();
        String command = message.substring(1).toLowerCase();
        String[] args = command.split(" ");
        
        switch (args[0]) {
            case "help":
                sendHelpMessage(event);
                break;
            case "status":
                sendStatusMessage(event);
                break;
            case "players":
            case "list":
                sendPlayerList(event);
                break;
            case "server":
                sendServerInfo(event);
                break;
            default:
                // Unknown command - let modules handle it
                break;
        }
    }
    
    private void sendHelpMessage(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("🤖 Bot Commands")
            .setDescription("Available Discord bot commands:")
            .addField("!help", "Show this help message", false)
            .addField("!status", "Show server status", false)
            .addField("!players", "List online players", false)
            .addField("!server", "Show server information", false)
            .setTimestamp(Instant.now())
            .setFooter("XDiscordUltimate", null);
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    private void sendStatusMessage(MessageReceivedEvent event) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String version = Bukkit.getVersion();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.GREEN)
            .setTitle("🖥️ Server Status")
            .addField("Status", "🟢 Online", true)
            .addField("Players", onlinePlayers + "/" + maxPlayers, true)
            .addField("Version", version, true)
            .setTimestamp(Instant.now())
            .setFooter("Last updated", null);
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    private void sendPlayerList(MessageReceivedEvent event) {
        var players = Bukkit.getOnlinePlayers();
        
        if (players.isEmpty()) {
            event.getChannel().sendMessage("No players are currently online.").queue();
            return;
        }
        
        StringBuilder playerList = new StringBuilder();
        for (var player : players) {
            if (playerList.length() > 0) {
                playerList.append(", ");
            }
            playerList.append(player.getName());
        }
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.CYAN)
            .setTitle("👥 Online Players (" + players.size() + ")")
            .setDescription(playerList.toString())
            .setTimestamp(Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    private void sendServerInfo(MessageReceivedEvent event) {
        String serverName = Bukkit.getServerName();
        String version = Bukkit.getVersion();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.ORANGE)
            .setTitle("ℹ️ Server Information")
            .addField("Server Name", serverName, true)
            .addField("Version", version, true)
            .addField("Players", onlinePlayers + "/" + maxPlayers, true)
            .setTimestamp(Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    private void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        plugin.getLogger().info("Discord member joined: " + member.getEffectiveName());
        
        // Send welcome message if configured
        String welcomeChannelId = plugin.getConfig().getString("discord.channels.welcome");
        if (welcomeChannelId != null && !welcomeChannelId.isEmpty() && !welcomeChannelId.equals("YOUR_WELCOME_CHANNEL_ID")) {
            TextChannel welcomeChannel = plugin.getDiscordManager().getTextChannelById(welcomeChannelId);
            if (welcomeChannel != null) {
                sendWelcomeMessage(welcomeChannel, member);
            }
        }
    }
    
    private void sendWelcomeMessage(TextChannel channel, Member member) {
        User user = member.getUser();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.GREEN)
            .setTitle("🎉 Welcome to the server!")
            .setDescription("Welcome " + user.getAsMention() + " to our Discord server!")
            .addField("Getting Started",
                "• Link your Minecraft account with `/verify` in-game\n" +
                "• Check out our rules and information\n" +
                "• Say hello in the chat!", false)
            .setThumbnail(user.getAvatarUrl())
            .setTimestamp(Instant.now())
            .setFooter("Member #" + channel.getGuild().getMemberCount(), null);
        
        channel.sendMessageEmbeds(embed.build()).queue();
    }
    
    private void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        User user = event.getUser();
        plugin.getLogger().info("Discord member left: " + user.getAsTag());
        
        // Log to logs channel if configured
        String logsChannelId = plugin.getConfig().getString("discord.channels.logs");
        if (logsChannelId != null && !logsChannelId.isEmpty() && !logsChannelId.equals("YOUR_LOGS_CHANNEL_ID")) {
            TextChannel logsChannel = plugin.getDiscordManager().getTextChannelById(logsChannelId);
            if (logsChannel != null) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("📤 Member Left")
                    .setDescription(user.getAsTag() + " has left the server")
                    .setTimestamp(Instant.now());
                
                logsChannel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
    
    private void onSlashCommand(SlashCommandInteractionEvent event) {
        String command = event.getName();
        
        switch (command) {
            case "help":
                sendSlashHelpMessage(event);
                break;
            case "status":
                sendSlashStatusMessage(event);
                break;
            case "players":
                sendSlashPlayerList(event);
                break;
            case "server":
                sendSlashServerInfo(event);
                break;
            case "verify":
                // Let the verification module handle this
                break;
            default:
                event.reply("Unknown command!").setEphemeral(true).queue();
                break;
        }
    }
    
    private void sendSlashHelpMessage(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("🤖 Bot Commands")
            .setDescription("Available Discord bot commands:")
            .addField("/help", "Show this help message", false)
            .addField("/status", "Show server status", false)
            .addField("/players", "List online players", false)
            .addField("/server", "Show server information", false)
            .addField("/verify", "Get verification code to link accounts", false)
            .setTimestamp(Instant.now())
            .setFooter("XDiscordUltimate", null);
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void sendSlashStatusMessage(SlashCommandInteractionEvent event) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String version = Bukkit.getVersion();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.GREEN)
            .setTitle("🖥️ Server Status")
            .addField("Status", "🟢 Online", true)
            .addField("Players", onlinePlayers + "/" + maxPlayers, true)
            .addField("Version", version, true)
            .setTimestamp(Instant.now())
            .setFooter("Last updated", null);
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void sendSlashPlayerList(SlashCommandInteractionEvent event) {
        var players = Bukkit.getOnlinePlayers();
        
        if (players.isEmpty()) {
            event.reply("No players are currently online.").queue();
            return;
        }
        
        StringBuilder playerList = new StringBuilder();
        for (var player : players) {
            if (playerList.length() > 0) {
                playerList.append(", ");
            }
            playerList.append(player.getName());
        }
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.CYAN)
            .setTitle("👥 Online Players (" + players.size() + ")")
            .setDescription(playerList.toString())
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void sendSlashServerInfo(SlashCommandInteractionEvent event) {
        String serverName = Bukkit.getServerName();
        String version = Bukkit.getVersion();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.ORANGE)
            .setTitle("ℹ️ Server Information")
            .addField("Server Name", serverName, true)
            .addField("Version", version, true)
            .addField("Players", onlinePlayers + "/" + maxPlayers, true)
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }
}
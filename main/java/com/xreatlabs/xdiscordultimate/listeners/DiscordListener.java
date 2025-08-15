package com.xreatlabs.xdiscordultimate.listeners;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.voice.VoiceChannelModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class DiscordListener extends ListenerAdapter {
    
    private final XDiscordUltimate plugin;
    
    public DiscordListener(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onReady(ReadyEvent event) {
        plugin.getLogger().info("Discord bot is ready! Logged in as: " + event.getJDA().getSelfUser().getAsTag());
        
        // Register slash commands
        registerSlashCommands(event.getJDA());
        
        // Update activity
        plugin.getDiscordManager().updateActivity();
    }
    
    private void registerSlashCommands(net.dv8tion.jda.api.JDA jda) {
        // Get the main guild for faster command registration
        String guildId = plugin.getConfigManager().getGuildId();
        
        plugin.getLogger().info("Attempting to register slash commands...");
        plugin.getLogger().info("Guild ID from config: " + guildId);
        
        if (guildId != null && !guildId.isEmpty() && !guildId.equals("YOUR_GUILD_ID")) {
            // Register guild-specific commands (instant)
            Guild guild = jda.getGuildById(guildId);
            plugin.getLogger().info("Found guild: " + (guild != null ? guild.getName() : "null"));
            if (guild != null) {
                plugin.getLogger().info("Registering guild-specific commands for guild: " + guild.getName());
                guild.updateCommands().addCommands(
                    // Verification command
                    Commands.slash("verify", "Get verification code to link your Minecraft account")
                        .addOption(OptionType.STRING, "username", "Your Minecraft username", false),
                    
                    // Console command for admins
                    Commands.slash("console", "Execute a Minecraft console command")
                        .addOptions(new OptionData(OptionType.STRING, "command", "The command to execute", true)),
                    
                    // Server status command
                    Commands.slash("status", "Check Minecraft server status"),
                    
                    // Player list command
                    Commands.slash("players", "List online players"),
                    
                    // Server info command
                    Commands.slash("serverinfo", "Get detailed server information"),
                    
                    // Help command
                    Commands.slash("help", "Show available commands"),
                    
                    // Whitelist management
                    Commands.slash("whitelist", "Manage server whitelist")
                        .addOptions(
                            new OptionData(OptionType.STRING, "action", "Action to perform", true)
                                .addChoice("add", "add")
                                .addChoice("remove", "remove")
                                .addChoice("list", "list"),
                            new OptionData(OptionType.STRING, "player", "Player name", false)
                        ),
                    
                    // Ban management
                    Commands.slash("ban", "Ban a player from the server")
                        .addOptions(
                            new OptionData(OptionType.STRING, "player", "Player to ban", true),
                            new OptionData(OptionType.STRING, "reason", "Ban reason", false)
                        ),
                    
                    // Unban command
                    Commands.slash("unban", "Unban a player from the server")
                        .addOptions(new OptionData(OptionType.STRING, "player", "Player to unban", true)),
                    
                    // Kick command
                    Commands.slash("kick", "Kick a player from the server")
                        .addOptions(
                            new OptionData(OptionType.STRING, "player", "Player to kick", true),
                            new OptionData(OptionType.STRING, "reason", "Kick reason", false)
                        )
                ).queue(
                    success -> plugin.getLogger().info("Successfully registered " + success.size() + " guild slash commands!"),
                    error -> plugin.getLogger().severe("Failed to register guild slash commands: " + error.getMessage())
                );
            } else {
                plugin.getLogger().warning("Could not find guild with ID: " + guildId + " - commands will not be registered!");
            }
        } else {
            // Fallback to global commands if no guild ID is configured
            plugin.getLogger().warning("No valid guild ID configured! Registering global commands (may take up to 1 hour to appear)...");
            plugin.getLogger().warning("Guild ID value: '" + guildId + "'");
            plugin.getLogger().info("Registering global commands...");
            jda.updateCommands().addCommands(
                // Verification command
                Commands.slash("verify", "Get verification code to link your Minecraft account")
                    .addOption(OptionType.STRING, "username", "Your Minecraft username", false),
                
                // Console command for admins
                Commands.slash("console", "Execute a Minecraft console command")
                    .addOptions(new OptionData(OptionType.STRING, "command", "The command to execute", true)),
                
                // Server status command
                Commands.slash("status", "Check Minecraft server status"),
                
                // Player list command
                Commands.slash("players", "List online players"),
                
                // Server info command
                Commands.slash("serverinfo", "Get detailed server information"),
                
                // Help command
                Commands.slash("help", "Show available commands"),
                
                // Whitelist management
                Commands.slash("whitelist", "Manage server whitelist")
                    .addOptions(
                        new OptionData(OptionType.STRING, "action", "Action to perform", true)
                            .addChoice("add", "add")
                            .addChoice("remove", "remove")
                            .addChoice("list", "list"),
                        new OptionData(OptionType.STRING, "player", "Player name", false)
                    ),
                
                // Ban management
                Commands.slash("ban", "Ban a player from the server")
                    .addOptions(
                        new OptionData(OptionType.STRING, "player", "Player to ban", true),
                        new OptionData(OptionType.STRING, "reason", "Ban reason", false)
                    ),
                
                // Unban command
                Commands.slash("unban", "Unban a player from the server")
                    .addOptions(new OptionData(OptionType.STRING, "player", "Player to unban", true)),
                
                // Kick command
                Commands.slash("kick", "Kick a player from the server")
                    .addOptions(
                        new OptionData(OptionType.STRING, "player", "Player to kick", true),
                        new OptionData(OptionType.STRING, "reason", "Kick reason", false)
                    )
            ).queue(
                success -> plugin.getLogger().info("Successfully registered " + success.size() + " global slash commands!"),
                error -> plugin.getLogger().severe("Failed to register global slash commands: " + error.getMessage())
            );
        }
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        
        switch (command) {
            case "verify":
                handleVerifyCommand(event);
                break;
            case "console":
                handleConsoleCommand(event);
                break;
            case "status":
                handleStatusCommand(event);
                break;
            case "players":
                handlePlayersCommand(event);
                break;
            case "serverinfo":
                handleServerInfoCommand(event);
                break;
            case "help":
                handleHelpCommand(event);
                break;
            case "whitelist":
                handleWhitelistCommand(event);
                break;
            case "ban":
                handleBanCommand(event);
                break;
            case "unban":
                handleUnbanCommand(event);
                break;
            case "kick":
                handleKickCommand(event);
                break;
            default:
                event.reply("Unknown command!").setEphemeral(true).queue();
        }
    }
    
    private void handleVerifyCommand(SlashCommandInteractionEvent event) {
        String username = event.getOption("username") != null ? 
            event.getOption("username").getAsString() : null;
        
        // Check if user is already verified
        String discordId = event.getUser().getId();
        if (plugin.getAdminUtils().isVerifiedByDiscordId(discordId)) {
            EmbedBuilder alreadyVerifiedEmbed = new EmbedBuilder()
                .setTitle("✅ Already Verified")
                .setDescription("Your Discord account is already linked to a Minecraft account!")
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now())
                .setFooter("XDiscordUltimate", event.getJDA().getSelfUser().getAvatarUrl());
            
            event.replyEmbeds(alreadyVerifiedEmbed.build()).setEphemeral(true).queue();
            return;
        }
        
        // Generate verification code
        String code = generateVerificationCode();
        
        // Store verification attempt
        plugin.getDatabaseManager().storeVerificationCode(discordId, code, username);
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🔗 Account Verification")
            .setDescription("Use this code to link your Discord and Minecraft accounts!")
            .addField("🎯 Verification Code", "`" + code + "`", false)
            .addField("📋 How to verify", "1. Join the Minecraft server\n2. Type: `/verify " + code + "`\n3. Your accounts will be linked!", false)
            .addField("⏰ Expires", "This code expires in 10 minutes", false)
            .addField("⚠️ Important", "• Keep this code private\n• Use it only once\n• Get a new code if it expires", false)
            .setColor(Color.BLUE)
            .setTimestamp(Instant.now())
            .setFooter("XDiscordUltimate", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        
        // Log verification code generation
        plugin.getLogger().info("Verification code generated for Discord user " + event.getUser().getAsTag() + ": " + code);
    }
    
    private void handleConsoleCommand(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        
        // Check if user is admin
        if (!plugin.getAdminUtils().isAdminByDiscordId(discordId)) {
            event.reply("❌ You don't have permission to use console commands!").setEphemeral(true).queue();
            return;
        }
        
        String command = event.getOption("command").getAsString();
        
        // Create a list to capture command output
        List<String> commandOutput = new ArrayList<>();
        
        // Create a custom appender to capture console output
        final AbstractAppender appender = new AbstractAppender("DiscordConsole-" + event.getUser().getId() + "-" + System.currentTimeMillis(), null,
                PatternLayout.createDefaultLayout(), false, null) {
            @Override
            public void append(LogEvent event) {
                final String message = event.getMessage().getFormattedMessage();
                // Capture all console output during command execution
                commandOutput.add(message);
            }
        };
        appender.start();
        
        final Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addAppender(appender);
        
        // Execute command on main thread
        CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    
                    // Wait a bit for output to be captured
                    Thread.sleep(500);
                    
                    // Remove the appender
                    rootLogger.removeAppender(appender);
                    appender.stop();
                    
                    // Build the response embed
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(success ? "✅ Command Executed" : "❌ Command Failed")
                        .addField("Command", "`" + command + "`", false)
                        .addField("Executor", event.getUser().getAsMention(), true)
                        .setColor(success ? Color.GREEN : Color.RED)
                        .setTimestamp(Instant.now());
                    
                    // Add command output if available
                    if (!commandOutput.isEmpty()) {
                        StringBuilder output = new StringBuilder();
                        int lineCount = 0;
                        
                        for (String line : commandOutput) {
                            // Skip empty lines and some system messages
                            if (line.trim().isEmpty() || line.contains("Starting minecraft server")) {
                                continue;
                            }
                            
                            // Limit to reasonable number of lines
                            if (lineCount >= 20) {
                                output.append("\n... (output truncated - too many lines)");
                                break;
                            }
                            
                            if (output.length() + line.length() > 1000) {
                                output.append("\n... (output truncated - too long)");
                                break;
                            }
                            
                            output.append(line).append("\n");
                            lineCount++;
                        }
                        
                        String finalOutput = output.toString().trim();
                        if (!finalOutput.isEmpty()) {
                            embed.addField("Output", "```" + finalOutput + "```", false);
                        } else {
                            embed.addField("Output", "Command executed successfully (no output)", false);
                        }
                    } else {
                        embed.addField("Output", "Command executed successfully (no output)", false);
                    }
                    
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                    
                    // Log to console
                    plugin.getLogger().info("Discord console command executed by " + event.getUser().getAsTag() + ": " + command);
                    
                } catch (Exception e) {
                    // Remove the appender on error
                    rootLogger.removeAppender(appender);
                    appender.stop();
                    
                    EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle("❌ Command Error")
                        .setDescription("An error occurred while executing the command")
                        .addField("Command", "`" + command + "`", false)
                        .addField("Error", e.getMessage(), false)
                        .setColor(Color.RED)
                        .setTimestamp(Instant.now());
                    
                    event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
                }
            });
        });
        
        event.reply("⏳ Executing command...").queue();
    }
    
    private void handleStatusCommand(SlashCommandInteractionEvent event) {
        boolean online = !Bukkit.getOnlinePlayers().isEmpty() || Bukkit.getServer() != null;
        int playerCount = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🖥️ Server Status")
            .addField("Status", online ? "🟢 Online" : "🔴 Offline", true)
            .addField("Players", playerCount + "/" + maxPlayers, true)
            .addField("Version", Bukkit.getVersion(), true)
            .addField("TPS", getTPS(), true)
            .addField("Uptime", getUptime(), true)
            .addField("Memory", getMemoryUsage(), true)
            .setColor(online ? Color.GREEN : Color.RED)
            .setTimestamp(Instant.now())
            .setFooter("Last updated", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void handlePlayersCommand(SlashCommandInteractionEvent event) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("👥 Online Players (" + players.size() + "/" + Bukkit.getMaxPlayers() + ")")
            .setColor(Color.BLUE)
            .setTimestamp(Instant.now());
        
        if (players.isEmpty()) {
            embed.setDescription("No players are currently online.");
        } else {
            StringBuilder playerList = new StringBuilder();
            for (Player player : players) {
                playerList.append("• ").append(player.getName()).append("\n");
            }
            embed.setDescription(playerList.toString());
        }
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void handleServerInfoCommand(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("📊 Server Information")
            .addField("Version", Bukkit.getVersion(), true)
            .addField("Bukkit Version", Bukkit.getBukkitVersion(), true)
            .addField("Online Mode", Bukkit.getOnlineMode() ? "Yes" : "No", true)
            .addField("Max Players", String.valueOf(Bukkit.getMaxPlayers()), true)
            .addField("Online Players", String.valueOf(Bukkit.getOnlinePlayers().size()), true)
            .addField("Worlds", String.valueOf(Bukkit.getWorlds().size()), true)
            .addField("Plugins", String.valueOf(Bukkit.getPluginManager().getPlugins().length), true)
            .setColor(Color.CYAN)
            .setTimestamp(Instant.now())
            .setFooter("XDiscordUltimate", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void handleHelpCommand(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🤖 Bot Commands")
            .setDescription("Available slash commands:")
            .addField("/verify", "Link your Discord and Minecraft accounts", false)
            .addField("/status", "Check server status", false)
            .addField("/players", "List online players", false)
            .addField("/serverinfo", "Get detailed server information", false)
            .addField("/help", "Show this help message", false)
            .setColor(Color.ORANGE)
            .setTimestamp(Instant.now());
        
        // Add admin commands if user is admin
        if (plugin.getAdminUtils().isAdminByDiscordId(event.getUser().getId())) {
            embed.addField("**Admin Commands**", "", false)
                .addField("/console", "Execute console commands", false)
                .addField("/whitelist", "Manage server whitelist", false)
                .addField("/ban", "Ban players", false)
                .addField("/unban", "Unban players", false)
                .addField("/kick", "Kick players", false);
        }
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
    
    private void handleWhitelistCommand(SlashCommandInteractionEvent event) {
        if (!plugin.getAdminUtils().isAdminByDiscordId(event.getUser().getId())) {
            event.reply("❌ You don't have permission to manage the whitelist!").setEphemeral(true).queue();
            return;
        }
        
        String action = event.getOption("action").getAsString();
        String playerName = event.getOption("player") != null ? event.getOption("player").getAsString() : null;
        
        CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    String command;
                    String result;
                    
                    switch (action) {
                        case "add":
                            if (playerName == null) {
                                event.getHook().editOriginal("❌ Player name is required for add action!").queue();
                                return;
                            }
                            command = "whitelist add " + playerName;
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            result = "✅ Added " + playerName + " to whitelist";
                            break;
                        case "remove":
                            if (playerName == null) {
                                event.getHook().editOriginal("❌ Player name is required for remove action!").queue();
                                return;
                            }
                            command = "whitelist remove " + playerName;
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            result = "✅ Removed " + playerName + " from whitelist";
                            break;
                        case "list":
                            // This would need custom implementation to get whitelist
                            result = "📋 Check console for whitelist";
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist list");
                            break;
                        default:
                            result = "❌ Invalid action!";
                    }
                    
                    event.getHook().editOriginal(result).queue();
                    
                } catch (Exception e) {
                    event.getHook().editOriginal("❌ Error executing whitelist command: " + e.getMessage()).queue();
                }
            });
        });
        
        event.reply("⏳ Processing whitelist command...").queue();
    }
    
    private void handleBanCommand(SlashCommandInteractionEvent event) {
        if (!plugin.getAdminUtils().isAdminByDiscordId(event.getUser().getId())) {
            event.reply("❌ You don't have permission to ban players!").setEphemeral(true).queue();
            return;
        }
        
        String playerName = event.getOption("player").getAsString();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Banned by Discord admin";
        
        CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    String command = "ban " + playerName + " " + reason;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🔨 Player Banned")
                        .addField("Player", playerName, true)
                        .addField("Reason", reason, true)
                        .addField("Admin", event.getUser().getAsMention(), true)
                        .setColor(Color.RED)
                        .setTimestamp(Instant.now());
                    
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                    
                } catch (Exception e) {
                    event.getHook().editOriginal("❌ Error banning player: " + e.getMessage()).queue();
                }
            });
        });
        
        event.reply("⏳ Banning player...").queue();
    }
    
    private void handleUnbanCommand(SlashCommandInteractionEvent event) {
        if (!plugin.getAdminUtils().isAdminByDiscordId(event.getUser().getId())) {
            event.reply("❌ You don't have permission to unban players!").setEphemeral(true).queue();
            return;
        }
        
        String playerName = event.getOption("player").getAsString();
        
        CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    String command = "pardon " + playerName;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("✅ Player Unbanned")
                        .addField("Player", playerName, true)
                        .addField("Admin", event.getUser().getAsMention(), true)
                        .setColor(Color.GREEN)
                        .setTimestamp(Instant.now());
                    
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                    
                } catch (Exception e) {
                    event.getHook().editOriginal("❌ Error unbanning player: " + e.getMessage()).queue();
                }
            });
        });
        
        event.reply("⏳ Unbanning player...").queue();
    }
    
    private void handleKickCommand(SlashCommandInteractionEvent event) {
        if (!plugin.getAdminUtils().isAdminByDiscordId(event.getUser().getId())) {
            event.reply("❌ You don't have permission to kick players!").setEphemeral(true).queue();
            return;
        }
        
        String playerName = event.getOption("player").getAsString();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Kicked by Discord admin";
        
        CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    String command = "kick " + playerName + " " + reason;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("👢 Player Kicked")
                        .addField("Player", playerName, true)
                        .addField("Reason", reason, true)
                        .addField("Admin", event.getUser().getAsMention(), true)
                        .setColor(Color.ORANGE)
                        .setTimestamp(Instant.now());
                    
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                    
                } catch (Exception e) {
                    event.getHook().editOriginal("❌ Error kicking player: " + e.getMessage()).queue();
                }
            });
        });
        
        event.reply("⏳ Kicking player...").queue();
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        // Send welcome message
        String welcomeChannelId = plugin.getConfig().getString("discord.channels.welcome");
        if (welcomeChannelId != null) {
            TextChannel channel = plugin.getDiscordManager().getTextChannelById(welcomeChannelId);
            if (channel != null) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("👋 Welcome!")
                    .setDescription("Welcome to the server, " + event.getUser().getAsMention() + "!")
                    .addField("Getting Started", "Use `/verify` to link your Minecraft account", false)
                    .addField("Need Help?", "Use `/help` to see available commands", false)
                    .setColor(Color.GREEN)
                    .setTimestamp(Instant.now())
                    .setThumbnail(event.getUser().getAvatarUrl());
                
                channel.sendMessageEmbeds(embed.build())
                    .addActionRow(
                        Button.primary("verify_button", "🔗 Verify Account"),
                        Button.secondary("help_button", "❓ Get Help")
                    ).queue();
            }
        }
    }
    
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        // Log member leave
        String logChannelId = plugin.getConfig().getString("discord.channels.logs");
        if (logChannelId != null) {
            TextChannel channel = plugin.getDiscordManager().getTextChannelById(logChannelId);
            if (channel != null) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("👋 Member Left")
                    .setDescription(event.getUser().getAsTag() + " left the server")
                    .setColor(Color.ORANGE)
                    .setTimestamp(Instant.now())
                    .setThumbnail(event.getUser().getAvatarUrl());
                
                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
    
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        // Handle voice channel events for TTS
        VoiceChannel joinedChannel = event.getChannelJoined() != null ? event.getChannelJoined().asVoiceChannel() : null;
        VoiceChannel leftChannel = event.getChannelLeft() != null ? event.getChannelLeft().asVoiceChannel() : null;
        
        if (joinedChannel != null) {
            // User joined voice channel - notify TTS system
            VoiceChannelModule voiceModule = plugin.getModuleManager().getModule(VoiceChannelModule.class);
            if (voiceModule != null && voiceModule.isEnabled()) {
                voiceModule.onUserJoinVoice(event.getMember(), joinedChannel);
            }
        }
        
        if (leftChannel != null) {
            // User left voice channel - notify TTS system
            VoiceChannelModule voiceModule = plugin.getModuleManager().getModule(VoiceChannelModule.class);
            if (voiceModule != null && voiceModule.isEnabled()) {
                voiceModule.onUserLeaveVoice(event.getMember(), leftChannel);
            }
        }
    }
    
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        
        switch (buttonId) {
            case "verify_button":
                // Handle verify button click
                handleVerifyCommand(event);
                break;
            case "help_button":
                // Handle help button click
                handleHelpCommand(event);
                break;
            default:
                event.reply("Unknown button interaction!").setEphemeral(true).queue();
        }
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) return;
        
        // Handle cross-chat if enabled
        String chatChannelId = plugin.getConfig().getString("discord.channels.chat");
        if (chatChannelId != null && event.getChannel().getId().equals(chatChannelId)) {
            // Send Discord message to Minecraft
            String message = "§7[§9Discord§7] §b" + event.getAuthor().getName() + "§7: §f" + event.getMessage().getContentDisplay();
            Bukkit.broadcastMessage(message);
            
            // TTS announcement if enabled
            if (plugin.getConfig().getBoolean("voice.tts.discord-messages", true)) {
                VoiceChannelModule voiceModule = plugin.getModuleManager().getModule(VoiceChannelModule.class);
                if (voiceModule != null && voiceModule.isEnabled()) {
                    voiceModule.announceDiscordMessage(
                        event.getAuthor().getName(),
                        event.getMessage().getContentDisplay()
                    );
                }
            }
        }
    }
    
    // Helper methods
    private String generateVerificationCode() {
        // Generate a 6-character alphanumeric code (easier to type)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }
    
    private String getTPS() {
        try {
            // Try to get TPS using reflection for compatibility
            Object minecraftServer = Bukkit.getServer().getClass()
                .getMethod("getServer").invoke(Bukkit.getServer());
            
            double[] recentTps = (double[]) minecraftServer.getClass()
                .getField("recentTps").get(minecraftServer);
            
            return String.format("%.2f", recentTps[0]);
        } catch (Exception e) {
            return "20.0"; // Default TPS value
        }
    }
    
    private String getUptime() {
        try {
            // Try to get server start time using reflection
            long startTime = (Long) plugin.getServer().getClass()
                .getMethod("getStartTime").invoke(plugin.getServer());
            long uptime = System.currentTimeMillis() - startTime;
            long hours = TimeUnit.MILLISECONDS.toHours(uptime);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) % 60;
            return hours + "h " + minutes + "m";
        } catch (Exception e) {
            // Fallback - use plugin enable time
            long uptime = System.currentTimeMillis() - plugin.getStartTime();
            long hours = TimeUnit.MILLISECONDS.toHours(uptime);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) % 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long total = runtime.totalMemory();
        return String.format("%.1f/%.1f MB", used / 1024.0 / 1024.0, total / 1024.0 / 1024.0);
    }
    
    // Convert SlashCommandInteractionEvent to ButtonInteractionEvent for verify button
    private void handleVerifyCommand(ButtonInteractionEvent event) {
        // Check if user is already verified
        String discordId = event.getUser().getId();
        if (plugin.getAdminUtils().isVerifiedByDiscordId(discordId)) {
            EmbedBuilder alreadyVerifiedEmbed = new EmbedBuilder()
                .setTitle("✅ Already Verified")
                .setDescription("Your Discord account is already linked to a Minecraft account!")
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now())
                .setFooter("XDiscordUltimate", event.getJDA().getSelfUser().getAvatarUrl());
            
            event.replyEmbeds(alreadyVerifiedEmbed.build()).setEphemeral(true).queue();
            return;
        }
        
        // Generate verification code
        String code = generateVerificationCode();
        
        // Store verification attempt
        plugin.getDatabaseManager().storeVerificationCode(discordId, code, null);
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🔗 Account Verification")
            .setDescription("Use this code to link your Discord and Minecraft accounts!")
            .addField("🎯 Verification Code", "`" + code + "`", false)
            .addField("📋 How to verify", "1. Join the Minecraft server\n2. Type: `/verify " + code + "`\n3. Your accounts will be linked!", false)
            .addField("⏰ Expires", "This code expires in 10 minutes", false)
            .addField("⚠️ Important", "• Keep this code private\n• Use it only once\n• Get a new code if it expires", false)
            .setColor(Color.BLUE)
            .setTimestamp(Instant.now())
            .setFooter("XDiscordUltimate", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        
        // Log verification code generation
        plugin.getLogger().info("Verification code generated for Discord user " + event.getUser().getAsTag() + ": " + code);
    }
    
    // Convert SlashCommandInteractionEvent to ButtonInteractionEvent for help button
    private void handleHelpCommand(ButtonInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🤖 Bot Commands")
            .setDescription("Available slash commands:")
            .addField("/verify", "Link your Discord and Minecraft accounts", false)
            .addField("/status", "Check server status", false)
            .addField("/players", "List online players", false)
            .addField("/serverinfo", "Get detailed server information", false)
            .addField("/help", "Show this help message", false)
            .setColor(Color.ORANGE)
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
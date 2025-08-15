package com.xreatlabs.xdiscordultimate.modules.console;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class BotConsoleModule extends Module {
    
    private ConsoleListener consoleListener;
    private boolean consoleEnabled;
    private String consoleChannelId;
    private boolean showPlayerChat;
    private boolean showCommands;
    private boolean showErrors;
    private boolean showWarnings;
    private boolean showInfo;
    
    public BotConsoleModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Bot Console";
    }
    
    @Override
    public String getDescription() {
        return "Enhanced Discord bot console with real-time chat and command execution";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        
        // Register Discord listener
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            consoleListener = new ConsoleListener();
            plugin.getDiscordManager().getJDA().addEventListener(consoleListener);
        }
        
        info("Enhanced Bot Console module enabled");
    }
    
    @Override
    protected void onDisable() {
        if (consoleListener != null && plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getJDA().removeEventListener(consoleListener);
        }
        
        info("Bot Console module disabled");
    }
    
    private void loadConfiguration() {
        consoleEnabled = getConfig().getBoolean("enabled", true);
        consoleChannelId = getConfig().getString("console-channel", "");
        showPlayerChat = getConfig().getBoolean("show-player-chat", true);
        showCommands = getConfig().getBoolean("show-commands", true);
        showErrors = getConfig().getBoolean("show-errors", true);
        showWarnings = getConfig().getBoolean("show-warnings", true);
        showInfo = getConfig().getBoolean("show-info", false);
    }
    
    public void sendConsoleMessage(String message, String type) {
        if (!consoleEnabled || consoleChannelId.isEmpty()) return;
        
        TextChannel channel = plugin.getDiscordManager().getTextChannelById(consoleChannelId);
        if (channel == null) return;
        
        // Determine if we should send this message type
        boolean shouldSend = false;
        Color color = Color.GRAY;
        
        switch (type.toLowerCase()) {
            case "chat":
                shouldSend = showPlayerChat;
                color = Color.BLUE;
                break;
            case "command":
                shouldSend = showCommands;
                color = Color.GREEN;
                break;
            case "error":
                shouldSend = showErrors;
                color = Color.RED;
                break;
            case "warning":
                shouldSend = showWarnings;
                color = Color.ORANGE;
                break;
            case "info":
                shouldSend = showInfo;
                color = Color.CYAN;
                break;
        }
        
        if (!shouldSend) return;
        
        // Truncate message if too long
        if (message.length() > 1900) {
            message = message.substring(0, 1900) + "...";
        }
        
        EmbedBuilder embed = new EmbedBuilder()
            .setDescription("```" + message + "```")
            .setColor(color)
            .setTimestamp(Instant.now())
            .setFooter("Console • " + type.toUpperCase(), null);
        
        channel.sendMessageEmbeds(embed.build()).queue(
            success -> {},
            error -> plugin.getLogger().warning("Failed to send console message to Discord: " + error.getMessage())
        );
    }
    
    public boolean executeCommand(CommandSender sender, String command) {
        if (!plugin.getAdminUtils().isAdmin(sender)) {
            return false;
        }
        
        CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    
                    // Send result to Discord
                    String result = success ? "✅ Command executed successfully" : "❌ Command failed";
                    sendConsoleMessage("Command: " + command + "\nResult: " + result, "command");
                    
                } catch (Exception e) {
                    sendConsoleMessage("Command: " + command + "\nError: " + e.getMessage(), "error");
                }
            });
        });
        
        return true;
    }
    
    private class ConsoleListener extends ListenerAdapter {
        
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (!consoleEnabled || consoleChannelId.isEmpty()) return;
            if (!event.getChannel().getId().equals(consoleChannelId)) return;
            if (event.getAuthor().isBot()) return;
            
            String message = event.getMessage().getContentDisplay();
            
            // Check if it's a command (starts with /)
            if (message.startsWith("/")) {
                handleConsoleCommand(event, message.substring(1));
            } else {
                // Send as chat message to Minecraft
                String formattedMessage = "§7[§9Discord§7] §b" + event.getAuthor().getName() + "§7: §f" + message;
                Bukkit.broadcastMessage(formattedMessage);
                
                // Send back to Discord console
                sendConsoleMessage("Discord → Minecraft: " + message, "chat");
            }
        }
        
        private void handleConsoleCommand(MessageReceivedEvent event, String command) {
            // Check if user has permission
            if (!plugin.getAdminUtils().isAdminByDiscordId(event.getAuthor().getId())) {
                event.getChannel().sendMessage("❌ You don't have permission to execute console commands!").queue();
                return;
            }
            
            // Execute command
            executeCommand(Bukkit.getConsoleSender(), command);
            
            // Send confirmation
            event.getChannel().sendMessage("⏳ Executing command: `" + command + "`").queue();
        }
    }
}
package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class ConsoleAppender extends AbstractAppender {

    private final XDiscordUltimate plugin;

    public ConsoleAppender(XDiscordUltimate plugin) {
        super("XDiscordUltimateConsoleAppender", null,
                PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss} %level]: %msg%n").build(), false, null);
        this.plugin = plugin;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void append(LogEvent event) {
        if (!isStarted()) {
            return;
        }
        
        String message = event.getMessage().getFormattedMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        
        // Skip certain messages to avoid spam
        if (message.contains("Starting minecraft server") || 
            message.contains("Loading properties") ||
            message.contains("Default game type") ||
            message.contains("This server is running") ||
            message.contains("Preparing spawn area") ||
            message.contains("Skipping bad entity")) {
            return;
        }
        
        // Determine message type for better formatting
        String type = "info";
        if (event.getLevel().name().equals("ERROR")) {
            type = "error";
        } else if (event.getLevel().name().equals("WARN")) {
            type = "warning";
        } else if (message.contains("issued server command")) {
            type = "command";
        } else if (message.contains("<") && message.contains(">")) {
            type = "chat";
        }
        
        // Send to enhanced console module if available
        try {
            var consoleModule = plugin.getModuleManager().getModule("Bot Console");
            if (consoleModule != null) {
                consoleModule.getClass()
                    .getMethod("sendConsoleMessage", String.class, String.class)
                    .invoke(consoleModule, message, type);
                return;
            }
        } catch (Exception e) {
            // Fallback to old method
        }
        
        // Fallback: Send to Discord channel directly
        try {
            if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
                TextChannel consoleChannel = plugin.getDiscordManager().getConsoleChannel();
                if (consoleChannel != null) {
                    // Limit message length to avoid Discord limits
                    if (message.length() > 1900) {
                        message = message.substring(0, 1900) + "...";
                    }
                    consoleChannel.sendMessage("```" + message + "```")
                        .queue(success -> {}, error -> {
                            // Silently ignore Discord errors to avoid spam
                        });
                }
            }
        } catch (Exception e) {
            // Silently ignore errors to prevent console spam
        }
    }
}
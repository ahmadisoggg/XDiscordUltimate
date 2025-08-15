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
        
        try {
            if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
                TextChannel consoleChannel = plugin.getDiscordManager().getConsoleChannel();
                if (consoleChannel != null) {
                    String message = event.getMessage().getFormattedMessage();
                    if (message != null && !message.isEmpty()) {
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
            }
        } catch (Exception e) {
            // Silently ignore errors to prevent console spam
        }
    }
}
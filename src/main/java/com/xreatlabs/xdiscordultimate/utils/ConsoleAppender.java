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
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            TextChannel consoleChannel = plugin.getDiscordManager().getConsoleChannel();
            if (consoleChannel != null) {
                consoleChannel.sendMessage("```" + event.getMessage().getFormattedMessage() + "```").queue();
            }
        }
    }
}
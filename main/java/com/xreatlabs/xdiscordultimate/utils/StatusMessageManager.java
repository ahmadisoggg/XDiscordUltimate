package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.scheduler.BukkitRunnable;

public class StatusMessageManager {

    private final XDiscordUltimate plugin;
    private String messageId;
    private String channelId;

    public StatusMessageManager(XDiscordUltimate plugin) {
        this.plugin = plugin;
        this.channelId = plugin.getConfig().getString("features.server-status.channel-id");
        this.messageId = plugin.getConfig().getString("features.server-status.message-id");
    }

    public void start() {
        if (channelId == null || channelId.isEmpty()) {
            plugin.getLogger().warning("Server status channel ID not configured. Status message will not be sent.");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                updateStatusMessage();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20 * 60);
    }

    private void updateStatusMessage() {
        TextChannel channel = plugin.getDiscordManager().getTextChannelById(channelId);
        if (channel == null) {
            plugin.getLogger().warning("Could not find status channel with ID: " + channelId);
            return;
        }

        if (messageId == null || messageId.isEmpty()) {
            channel.sendMessageEmbeds(plugin.getEmbedUtils().createServerStatusEmbed()).queue(this::setMessageId);
        } else {
            channel.retrieveMessageById(messageId).queue(message -> {
                message.editMessageEmbeds(plugin.getEmbedUtils().createServerStatusEmbed()).queue();
            }, throwable -> {
                // Message not found, create a new one
                channel.sendMessageEmbeds(plugin.getEmbedUtils().createServerStatusEmbed()).queue(this::setMessageId);
            });
        }
    }

    private void setMessageId(Message message) {
        this.messageId = message.getId();
        plugin.getConfig().set("features.server-status.message-id", this.messageId);
        plugin.saveConfig();
    }
}

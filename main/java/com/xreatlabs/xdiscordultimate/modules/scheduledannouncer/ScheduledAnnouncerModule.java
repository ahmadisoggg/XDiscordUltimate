package com.xreatlabs.xdiscordultimate.modules.scheduledannouncer;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledAnnouncerModule extends Module {

    private ScheduledExecutorService scheduler;

    public ScheduledAnnouncerModule(XDiscordUltimate plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Scheduled Announcer";
    }

    @Override
    public String getDescription() {
        return "Sends scheduled announcements to a Discord channel.";
    }

    @Override
    protected void onEnable() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduleAnnouncements();
    }

    @Override
    protected void onDisable() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void scheduleAnnouncements() {
        List<String> announcements = getConfig().getStringList("announcements");
        String channelId = getConfig().getString("channel-id");
        long interval = getConfig().getLong("interval", 3600); // Default to 1 hour

        if (channelId == null || channelId.isEmpty()) {
            warning("The announcement channel ID is not set.");
            return;
        }

        TextChannel channel = plugin.getDiscordManager().getJDA().getTextChannelById(channelId);
        if (channel == null) {
            warning("The announcement channel with ID '" + channelId + "' was not found.");
            return;
        }

        scheduler.scheduleAtFixedRate(() -> {
            for (String announcement : announcements) {
                channel.sendMessage(announcement).queue();
            }
        }, 0, interval, TimeUnit.SECONDS);
    }
}

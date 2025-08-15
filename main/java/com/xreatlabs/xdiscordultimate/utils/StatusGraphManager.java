package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatusGraphManager {

    private final XDiscordUltimate plugin;
    private final Map<String, BukkitTask> activeGraphs = new ConcurrentHashMap<>();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final DecimalFormat df = new DecimalFormat("#.##");

    public StatusGraphManager(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }

    public void startGraph(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Live Server Status")
                .setColor(Color.GREEN)
                .setDescription("Initializing...");

        channel.sendMessageEmbeds(embed.build())
                .setActionRow(Button.danger("stop-graph", "Stop"))
                .queue(message -> {
                    BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                            () -> updateGraph(message), 0L, 10 * 20L); // 10 seconds
                    activeGraphs.put(message.getId(), task);
                });
    }

    public void stopGraph(String messageId) {
        BukkitTask task = activeGraphs.remove(messageId);
        if (task != null) {
            task.cancel();
        }
    }

    private void updateGraph(Message message) {
        double tps = plugin.getTpsManager().getTPS();
        double ramUsage = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
        double maxRam = (double) Runtime.getRuntime().maxMemory() / 1024 / 1024;
        double cpuUsage = osBean.getSystemLoadAverage();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Live Server Status")
                .setColor(Color.GREEN)
                .addField("TPS", formatBar(tps, 20.0, 20) + " " + df.format(tps), true)
                .addField("RAM", formatBar(ramUsage, maxRam, 10) + " " + df.format(ramUsage) + "/" + df.format(maxRam) + "MB", true)
                .addField("CPU", formatBar(cpuUsage, osBean.getAvailableProcessors(), 10) + " " + df.format(cpuUsage * 100) + "%", true);

        message.editMessageEmbeds(embed.build()).queue(null, error -> stopGraph(message.getId()));
    }

    private String formatBar(double value, double max, int length) {
        int filled = (int) Math.round((value / max) * length);
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("=");
            } else {
                bar.append("-");
            }
        }
        bar.append("]");
        return bar.toString();
    }
}

package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.scheduler.BukkitRunnable;

public class TPSManager implements Runnable {

    private long lastTick = 0L;
    private long tickCount = 0L;
    private double tps = 20.0;
    private final XDiscordUltimate plugin;

    public TPSManager(XDiscordUltimate plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (lastTick == 0) {
                    lastTick = currentTime;
                    return;
                }
                long diff = currentTime - lastTick;
                if (diff <= 0) {
                    diff = 1; // Prevent division by zero
                }
                // Calculate TPS: ticks per second
                double seconds = diff / 1000.0;
                if (seconds > 0) {
                    tps = Math.min(20.0, Math.max(0.0, (double) tickCount / seconds));
                }
                tickCount = 0;
                lastTick = currentTime;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 60); // Update every minute
    }

    @Override
    public void run() {
        tickCount++;
    }

    public double getTPS() {
        return tps;
    }
}

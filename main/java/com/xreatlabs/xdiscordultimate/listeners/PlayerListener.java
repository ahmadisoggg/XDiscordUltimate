package com.xreatlabs.xdiscordultimate.listeners;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.logging.ServerLoggingModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.Color;

public class PlayerListener implements Listener {

    private final XDiscordUltimate plugin;

    public PlayerListener(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Broadcast join to network
        if (plugin.getNetworkManager() != null) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("player", player.getName());
            plugin.getNetworkManager().send(
                com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.event(
                    plugin.getConfig().getString("network.server_id", plugin.getServer().getName()),
                    com.xreatlabs.xdiscordultimate.network.PacketType.EVENT_JOIN,
                    map
                )
            );
        }
        ServerLoggingModule loggingModule = plugin.getModuleManager().getModule(ServerLoggingModule.class);
        if (loggingModule != null && loggingModule.isEnabled()) {
            plugin.getDatabaseManager().getDiscordId(player.getUniqueId()).thenAccept(discordId -> {
                String discordInfo = discordId != null ? "Linked to <@" + discordId + ">" : "Not linked to Discord";
                loggingModule.logServerEvent(
                        "Player Join",
                        "**" + player.getName() + "** joined the server." 
                                + "\n**IP:** " + player.getAddress().getAddress().getHostAddress()
                                + "\n**UUID:** " + player.getUniqueId()
                                + "\n**Discord:** " + discordInfo,
                        Color.GREEN
                );
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getNetworkManager() != null) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("player", player.getName());
            plugin.getNetworkManager().send(
                com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.event(
                    plugin.getConfig().getString("network.server_id", plugin.getServer().getName()),
                    com.xreatlabs.xdiscordultimate.network.PacketType.EVENT_LEAVE,
                    map
                )
            );
        }
        ServerLoggingModule loggingModule = plugin.getModuleManager().getModule(ServerLoggingModule.class);
        if (loggingModule != null && loggingModule.isEnabled()) {
            plugin.getDatabaseManager().getDiscordId(player.getUniqueId()).thenAccept(discordId -> {
                String discordInfo = discordId != null ? "Linked to <@" + discordId + ">" : "Not linked to Discord";
                loggingModule.logServerEvent(
                        "Player Leave",
                        "**" + player.getName() + "** left the server." 
                                + "\n**UUID:** " + player.getUniqueId()
                                + "\n**Discord:** " + discordInfo,
                        Color.RED
                );
            });
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (plugin.getNetworkManager() != null) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("player", player.getName());
            map.put("cause", event.getDeathMessage());
            plugin.getNetworkManager().send(
                com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.event(
                    plugin.getConfig().getString("network.server_id", plugin.getServer().getName()),
                    com.xreatlabs.xdiscordultimate.network.PacketType.EVENT_DEATH,
                    map
                )
            );
        }
        ServerLoggingModule loggingModule = plugin.getModuleManager().getModule(ServerLoggingModule.class);
        if (loggingModule != null && loggingModule.isEnabled()) {
            String deathMessage = event.getDeathMessage();
            String location = String.format("x: %.2f, y: %.2f, z: %.2f in %s",
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ(),
                    player.getWorld().getName());
            String teleportCommand = String.format("/teleport %s %.2f %.2f %.2f",
                    player.getName(),
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ());

            loggingModule.logServerEvent(
                    "Player Death",
                    "**" + player.getName() + "** died." 
                            + "\n**Cause:** " + deathMessage
                            + "\n**Location:** " + location
                            + "\n**Teleport:** `" + teleportCommand + "`",
                    Color.ORANGE
            );
        }
    }
}

package com.xreatlabs.xdiscordultimate.network;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.network.NetworkManager.RemotePacketEvent;
import com.xreatlabs.xdiscordultimate.network.packet.NetworkPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public class NetworkPacketListener implements Listener {
    private final XDiscordUltimate plugin;

    public NetworkPacketListener(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRemotePacket(RemotePacketEvent event) {
        NetworkPacket p = event.getPacket();
        switch (p.type) {
            case CHAT: {
                Map<?,?> map = (Map<?,?>) p.payload;
                String player = String.valueOf(map.get("player"));
                String message = String.valueOf(map.get("message"));
                Bukkit.broadcastMessage(ChatColor.GRAY + "[" + p.serverId + "] " + ChatColor.RESET + player + ": " + message);
                break;
            }
            case DISCORD_CHAT: {
                Map<?,?> map = (Map<?,?>) p.payload;
                String user = String.valueOf(map.get("user"));
                String message = String.valueOf(map.get("message"));
                Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord] " + ChatColor.GRAY + "[" + p.serverId + "] " + ChatColor.RESET + user + ": " + message);
                break;
            }
            case COMMAND: {
                Map<?,?> map = (Map<?,?>) p.payload;
                String cmd = String.valueOf(map.get("command"));
                plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                break;
            }
            case EVENT_JOIN: {
                Map<?,?> map = (Map<?,?>) p.payload;
                String player = String.valueOf(map.get("player"));
                Bukkit.broadcastMessage(ChatColor.GREEN + "+ " + ChatColor.GRAY + "[" + p.serverId + "] " + ChatColor.RESET + player + " joined");
                break;
            }
            case EVENT_LEAVE: {
                Map<?,?> map = (Map<?,?>) p.payload;
                String player = String.valueOf(map.get("player"));
                Bukkit.broadcastMessage(ChatColor.RED + "- " + ChatColor.GRAY + "[" + p.serverId + "] " + ChatColor.RESET + player + " left");
                break;
            }
            case EVENT_DEATH: {
                Map<?,?> map = (Map<?,?>) p.payload;
                String player = String.valueOf(map.get("player"));
                String cause = String.valueOf(map.get("cause"));
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "✖ " + ChatColor.GRAY + "[" + p.serverId + "] " + ChatColor.RESET + player + " died: " + cause);
                break;
            }
            case PLAYER_LIST_SNAPSHOT: {
                Map<?,?> map = (Map<?,?>) p.payload;
                @SuppressWarnings("unchecked") List<String> players = (List<String>) map.get("players");
                // no-op here; used by the /network list command aggregator
                break;
            }
            default:
                break;
        }
    }
}

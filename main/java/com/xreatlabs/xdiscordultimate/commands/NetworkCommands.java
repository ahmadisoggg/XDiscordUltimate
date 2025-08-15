package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.command.CommandSender;

public class NetworkCommands {
    private final XDiscordUltimate plugin;

    public NetworkCommands(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }

    public void printNetworkKey(CommandSender sender) {
        String key = plugin.getConfig().getString("network.key", "");
        sender.sendMessage("Current network key: " + (key == null ? "" : key));
    }
}

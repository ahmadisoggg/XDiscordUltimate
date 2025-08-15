package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.utils.StatusGraphManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StatusGraphCommand implements CommandExecutor {

    private final XDiscordUltimate plugin;
    private final StatusGraphManager statusGraphManager;

    public StatusGraphCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
        this.statusGraphManager = new StatusGraphManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
            if (args.length > 1) {
                String messageId = args[1];
                statusGraphManager.stopGraph(messageId);
                sender.sendMessage("Stopped status graph for message ID: " + messageId);
            } else {
                sender.sendMessage("Usage: /statusgraph stop <message_id>");
            }
            return true;
        }

        // This command is primarily for slash commands, but we can handle direct invocation too.
        // For now, we'll just send a message to use the slash command.
        sender.sendMessage("Please use the /statusgraph slash command in Discord to start a live graph.");

        return true;
    }

    public StatusGraphManager getStatusGraphManager() {
        return statusGraphManager;
    }
}

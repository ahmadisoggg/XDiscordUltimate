package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class DropdownManager {

    private final XDiscordUltimate plugin;

    public DropdownManager(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }

    public StringSelectMenu createChannelSelectMenu() {
        StringSelectMenu.Builder menu = StringSelectMenu.create("channel-select")
                .setPlaceholder("Select a channel")
                .setRequiredRange(1, 1);

        plugin.getDiscordManager().getJda().getTextChannels().forEach(channel -> 
                menu.addOption(channel.getName(), channel.getId())
        );

        return menu.build();
    }

    public StringSelectMenu createRoleSelectMenu() {
        StringSelectMenu.Builder menu = StringSelectMenu.create("role-select")
                .setPlaceholder("Select a role")
                .setRequiredRange(1, 1);

        plugin.getDiscordManager().getJda().getRoles().forEach(role ->
                menu.addOption(role.getName(), role.getId())
        );

        return menu.build();
    }

    public StringSelectMenu createServerActionSelectMenu() {
        return StringSelectMenu.create("server-action-select")
                .setPlaceholder("Select a server action")
                .addOption("Restart", "restart", "Restart the server")
                .addOption("Stop", "stop", "Stop the server")
                .addOption("List Players", "list", "List online players")
                .build();
    }
}

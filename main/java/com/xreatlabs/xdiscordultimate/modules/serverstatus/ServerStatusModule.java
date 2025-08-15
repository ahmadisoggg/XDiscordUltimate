package com.xreatlabs.xdiscordultimate.modules.serverstatus;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ServerStatusModule extends Module {

    public ServerStatusModule(XDiscordUltimate plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Server Status";
    }

    @Override
    public String getDescription() {
        return "Provides a command to check the server status from Discord.";
    }

    @Override
    protected void onEnable() {
        plugin.getDiscordManager().getJDA().addEventListener(new DiscordListener());
    }

    @Override
    protected void onDisable() {
        // Listeners are automatically removed by JDA when the bot is shut down.
    }

    private class DiscordListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;

            String message = event.getMessage().getContentRaw();
            if (message.equalsIgnoreCase("!status")) {
                event.getChannel().sendMessage("The server is online!").queue();
            }
        }
    }
}

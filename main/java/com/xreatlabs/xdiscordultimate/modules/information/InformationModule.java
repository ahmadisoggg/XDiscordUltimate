package com.xreatlabs.xdiscordultimate.modules.information;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class InformationModule extends Module {

    public InformationModule(XDiscordUltimate plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Information";
    }

    @Override
    public String getDescription() {
        return "Provides commands to get information about the server.";
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
            if (message.equalsIgnoreCase("!info")) {
                sendInfo((TextChannel) event.getChannel());
            } else if (message.equalsIgnoreCase("!vote")) {
                sendVoteInfo((TextChannel) event.getChannel());
            } else if (message.equalsIgnoreCase("!discord")) {
                sendDiscordInfo((TextChannel) event.getChannel());
            }
        }
    }

    private void sendInfo(TextChannel channel) {
        List<String> messages = Arrays.asList(
            "&bWelcome to our server!",
            "&eWebsite: &fexample.com",
            "&eDiscord: &fdiscord.gg/example",
            "&eStore: &fstore.example.com"
        );

        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.decode("#00BFFF"))
            .setTitle("Server Information")
            .setDescription(String.join("\n", messages).replaceAll("&[0-9a-fk-or]", ""));

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private void sendVoteInfo(TextChannel channel) {
        List<String> messages = Arrays.asList(
            "&6Support the server by voting!",
            "&eVote Link 1: &fexample.com/vote1",
            "&eVote Link 2: &fexample.com/vote2",
            "&aYou'll receive rewards for voting!"
        );

        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.decode("#FFD700"))
            .setTitle("Vote Reminder")
            .setDescription(String.join("\n", messages).replaceAll("&[0-9a-fk-or]", ""));

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private void sendDiscordInfo(TextChannel channel) {
        List<String> messages = Arrays.asList(
            "&dJoin our Discord community!",
            "&eLink: &fdiscord.gg/example",
            "&aGet updates, chat with players, and more!"
        );

        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.decode("#7289DA"))
            .setTitle("Join our Discord!")
            .setDescription(String.join("\n", messages).replaceAll("&[0-9a-fk-or]", ""));

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}

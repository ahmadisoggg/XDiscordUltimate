package com.xreatlabs.xdiscordultimate.discord;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.bukkit.Bukkit;

public class DiscordListener implements EventListener {
    
    private final XDiscordUltimate plugin;
    
    public DiscordListener(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            onReady((ReadyEvent) event);
        } else if (event instanceof MessageReceivedEvent) {
            onMessageReceived((MessageReceivedEvent) event);
        } else if (event instanceof GuildMemberJoinEvent) {
            onGuildMemberJoin((GuildMemberJoinEvent) event);
        } else if (event instanceof GuildMemberRemoveEvent) {
            onGuildMemberRemove((GuildMemberRemoveEvent) event);
        }
    }
    
    private void onReady(ReadyEvent event) {
        plugin.getLogger().info("Discord bot is ready!");
        
        // Notify modules that Discord is ready
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (plugin.getModuleManager() != null) {
                plugin.getModuleManager().onDiscordReady();
            }
        });
    }
    
    private void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) return;
        
        // Ignore DMs for now
        if (!event.isFromGuild()) return;
        
        // Pass to modules for processing
        // This will be handled by individual modules
    }
    
    private void onGuildMemberJoin(GuildMemberJoinEvent event) {
        // Handle member join events
        plugin.getLogger().info("Discord member joined: " + event.getMember().getEffectiveName());
    }
    
    private void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        // Handle member leave events
        plugin.getLogger().info("Discord member left: " + event.getMember().getEffectiveName());
    }
}
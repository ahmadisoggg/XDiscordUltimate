package com.xreatlabs.xdiscordultimate.discord;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DiscordManager {
    
    private final XDiscordUltimate plugin;
    private JDA jda;
    private Guild mainGuild;
    private boolean ready = false;
    
    public DiscordManager(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize the Discord bot
     */
    public CompletableFuture<Boolean> initialize() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        String token = plugin.getConfigManager().getBotToken();
        if (token == null || token.isEmpty() || token.equals("YOUR_BOT_TOKEN")) {
            plugin.getLogger().severe("Discord bot token not configured!");
            future.complete(false);
            return future;
        }
        
        try {
            // Configure intents
            EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.DIRECT_MESSAGES
            );
            
            // Build JDA - only enable cache flags that have corresponding intents
            JDABuilder builder = JDABuilder.createDefault(token)
                .setActivity(Activity.playing("Minecraft"))
                .setEnabledIntents(intents)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.ROLE_TAGS)
                .disableCache(CacheFlag.SCHEDULED_EVENTS); // Explicitly disable scheduled events cache
            
            // Add the listener and pass the future to it
            builder.addEventListeners(new DiscordListener(plugin, this, future));

            jda = builder.build();
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize Discord bot", e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Shutdown the Discord bot
     */
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            ready = false;
            plugin.getLogger().info("Discord bot disconnected");
        }
    }
    
    /**
     * Update bot activity
     */
    public void updateActivity() {
        if (jda == null) return;
        
        String type = plugin.getConfig().getString("discord.activity.type", "PLAYING");
        String text = plugin.getConfig().getString("discord.activity.text", "Minecraft");
        
        // Replace placeholders
        text = text.replace("%players%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        text = text.replace("%max_players%", String.valueOf(plugin.getServer().getMaxPlayers()));
        text = text.replace("%tps%", String.format("%.2f", plugin.getTpsManager().getTPS()));
        text = text.replace("%version%", plugin.getServer().getVersion());
        text = text.replace("%motd%", plugin.getServer().getMotd());
        
        long uptimeMillis = System.currentTimeMillis() - plugin.getStartTime();
        long uptimeSeconds = uptimeMillis / 1000;
        long uptimeMinutes = uptimeSeconds / 60;
        long uptimeHours = uptimeMinutes / 60;
        long uptimeDays = uptimeHours / 24;
        String uptime = String.format("%dd %dh %dm", uptimeDays, uptimeHours % 24, uptimeMinutes % 60);
        text = text.replace("%uptime%", uptime);
        
        Activity activity;
        switch (type.toUpperCase()) {
            case "WATCHING":
                activity = Activity.watching(text);
                break;
            case "LISTENING":
                activity = Activity.listening(text);
                break;
            case "COMPETING":
                activity = Activity.competing(text);
                break;
            default:
                activity = Activity.playing(text);
                break;
        }
        
        jda.getPresence().setActivity(activity);
    }
    
    /**
     * Get JDA instance
     */
    public JDA getJDA() {
        return jda;
    }
    
    /**
     * Get main guild
     */
    public Guild getMainGuild() {
        return mainGuild;
    }
    
    /**
     * Check if bot is ready
     */
    public boolean isReady() {
        return ready && jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setMainGuild(JDA jda) {
        String guildId = plugin.getConfigManager().getGuildId();
        if (guildId != null && !guildId.isEmpty()) {
            this.mainGuild = jda.getGuildById(guildId);
            if (this.mainGuild == null) {
                plugin.getLogger().warning("Could not find guild with ID: " + guildId + ". Some features may not work.");
            }
        } else {
            plugin.getLogger().warning("Main guild ID not configured. Some features may not work.");
        }
    }
    
    /**
     * Get text channel by name
     */
    public TextChannel getTextChannel(String name) {
        if (mainGuild == null) return null;
        
        return mainGuild.getTextChannelsByName(name, true).stream()
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get text channel by ID
     */
    public TextChannel getTextChannelById(String id) {
        if (jda == null || id == null || id.isEmpty() || id.startsWith("YOUR_")) {
            return null;
        }
        try {
            return jda.getTextChannelById(id);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("'" + id + "' is not a valid Discord channel ID. Please check your config.yml.");
            return null;
        }
    }

    public JDA getJda() {
        return jda;
    }

    public TextChannel getConsoleChannel() {
        String channelId = plugin.getConfig().getString("discord-console.channel-id");
        if (channelId == null || channelId.isEmpty()) {
            return null;
        }
        return getTextChannelById(channelId);
    }

    public void sendTestDropdowns(org.bukkit.entity.Player player) {
        String discordId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId()).join();
        if (discordId == null) {
            player.sendMessage("You need to verify your account first. Use /verify");
            return;
        }

        jda.openPrivateChannelById(discordId).queue(channel -> {
            net.dv8tion.jda.api.EmbedBuilder embed = plugin.getEmbedUtils().createThemedEmbed("info", "Test Dropdowns");
            embed.setDescription("Please select an option from the dropdowns below.");

            channel.sendMessageEmbeds(embed.build())
                    .addActionRow(plugin.getDropdownManager().createChannelSelectMenu())
                    .addActionRow(plugin.getDropdownManager().createRoleSelectMenu())
                    .addActionRow(plugin.getDropdownManager().createServerActionSelectMenu())
                    .queue();
        });
    }
}
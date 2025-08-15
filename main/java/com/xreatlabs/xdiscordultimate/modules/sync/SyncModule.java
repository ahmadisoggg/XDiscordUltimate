package com.xreatlabs.xdiscordultimate.modules.sync;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SyncModule extends Module implements Listener {

    private boolean rankSync;
    private boolean nicknameSync;
    private Map<String, String> rankMappings;
    private SyncListener syncListener;

    public SyncModule(XDiscordUltimate plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Sync";
    }

    @Override
    public String getDescription() {
        return "Syncs ranks and nicknames between Minecraft and Discord.";
    }

    @Override
    protected void onEnable() {
        rankSync = getConfig().getBoolean("rank-sync", true);
        nicknameSync = getConfig().getBoolean("nickname-sync", true);
        rankMappings = new java.util.HashMap<>();
        org.bukkit.configuration.ConfigurationSection section = getConfig().getConfigurationSection("rank-mappings");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                rankMappings.put(key, section.getString(key));
            }
        }

        if (rankSync || nicknameSync) {
            syncListener = new SyncListener();
            plugin.getDiscordManager().getJDA().addEventListener(syncListener);
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        info("Sync module enabled.");
    }

    @Override
    protected void onDisable() {
        if (syncListener != null) {
            plugin.getDiscordManager().getJDA().removeEventListener(syncListener);
        }
        info("Sync module disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (rankSync || nicknameSync) {
            Player player = event.getPlayer();
            plugin.getDatabaseManager().getDiscordId(player.getUniqueId()).thenAccept(discordId -> {
                if (discordId != null) {
                    Member member = plugin.getDiscordManager().getMainGuild().getMemberById(discordId);
                    if (member != null) {
                        if (rankSync) {
                            syncRank(player, member);
                        }
                        if (nicknameSync) {
                            syncNickname(player, member);
                        }
                    }
                }
            });
        }
    }

    public void syncRank(Player player, Member member) {
        if (plugin.isLuckPermsEnabled()) {
            try {
                Object luckPerms = plugin.getLuckPerms();
                if (luckPerms == null) return;

                Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
                Object userFuture = userManager.getClass().getMethod("loadUser", UUID.class).invoke(userManager, player.getUniqueId());
                Object user = ((java.util.concurrent.CompletableFuture<?>) userFuture).get();

                // Clear existing groups
                Object primaryGroup = user.getClass().getMethod("getPrimaryGroup").invoke(user);
                if (primaryGroup != null) {
                    Object data = user.getClass().getMethod("data").invoke(user);
                    Object node = Class.forName("net.luckperms.api.node.Node").getMethod("builder", String.class).invoke(null, "group." + primaryGroup).getClass().getMethod("build").invoke(null);
                    data.getClass().getMethod("remove", Class.forName("net.luckperms.api.node.Node")).invoke(data, node);
                }


                // Add new groups
                for (Role role : member.getRoles()) {
                    String group = rankMappings.get(role.getName());
                    if (group != null) {
                        Object data = user.getClass().getMethod("data").invoke(user);
                        Object node = Class.forName("net.luckperms.api.node.Node").getMethod("builder", String.class).invoke(null, "group." + group).getClass().getMethod("build").invoke(null);
                        data.getClass().getMethod("add", Class.forName("net.luckperms.api.node.Node")).invoke(data, node);
                    }
                }
                userManager.getClass().getMethod("saveUser", user.getClass()).invoke(userManager, user);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void syncNickname(Player player, Member member) {
        if (member.getNickname() != null) {
            player.setDisplayName(member.getNickname());
            player.setPlayerListName(member.getNickname());
        }
    }

    private class SyncListener extends ListenerAdapter {
        @Override
        public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
            if (rankSync) {
                plugin.getDatabaseManager().getMinecraftUuid(event.getMember().getId()).thenAccept(uuid -> {
                    if (uuid != null) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            syncRank(player, event.getMember());
                        }
                    }
                });
            }
        }

        @Override
        public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
            if (rankSync) {
                plugin.getDatabaseManager().getMinecraftUuid(event.getMember().getId()).thenAccept(uuid -> {
                    if (uuid != null) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            syncRank(player, event.getMember());
                        }
                    }
                });
            }
        }

        @Override
        public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
            if (nicknameSync) {
                plugin.getDatabaseManager().getMinecraftUuid(event.getMember().getId()).thenAccept(uuid -> {
                    if (uuid != null) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            syncNickname(player, event.getMember());
                        }
                    }
                });
            }
        }
    }
}

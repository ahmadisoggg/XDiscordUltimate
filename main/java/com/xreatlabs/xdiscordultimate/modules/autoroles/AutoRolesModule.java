package com.xreatlabs.xdiscordultimate.modules.autoroles;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class AutoRolesModule extends Module {

    public AutoRolesModule(XDiscordUltimate plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Auto Roles";
    }

    @Override
    public String getDescription() {
        return "Automatically assigns a role to new members.";
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
        public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
            String roleId = getConfig().getString("role-id");
            if (roleId == null || roleId.isEmpty()) {
                return;
            }

            Role role = event.getGuild().getRoleById(roleId);
            if (role == null) {
                warning("The role with ID '" + roleId + "' was not found.");
                return;
            }

            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
    }
}

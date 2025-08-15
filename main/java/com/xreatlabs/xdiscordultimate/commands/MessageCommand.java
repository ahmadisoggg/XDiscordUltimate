package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.discord.DiscordManager;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand implements CommandExecutor {

    private final XDiscordUltimate plugin;

    public MessageCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /msg <discord_user> <message>");
            return true;
        }

        Player player = (Player) sender;
        String discordUsername = args[0];
        String message = String.join(" ", args).substring(discordUsername.length() + 1);

        DiscordManager discordManager = plugin.getDiscordManager();
        if (discordManager == null || !discordManager.isReady()) {
            player.sendMessage("The Discord bot is not ready yet. Please try again later.");
            return true;
        }

        User discordUser = discordManager.getJDA().getUsersByName(discordUsername, true).stream().findFirst().orElse(null);
        if (discordUser == null) {
            player.sendMessage("Could not find a Discord user with that name.");
            return true;
        }

        discordUser.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("**" + player.getName() + "**: " + message).queue(
                success -> player.sendMessage("Message sent to " + discordUser.getName()),
                error -> player.sendMessage("Failed to send message. The user may have DMs disabled.")
            );
        });

        return true;
    }
}

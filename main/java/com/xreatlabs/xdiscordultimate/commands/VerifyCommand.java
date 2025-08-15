package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VerifyCommand implements CommandExecutor {

    private final XDiscordUltimate plugin;

    public VerifyCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            plugin.getMessageManager().sendError(player, plugin.getMessageManager().getMessage("verification.code-usage"));
            plugin.getMessageManager().sendInfo(player, plugin.getMessageManager().getMessage("verification.code-instructions"));
            return true;
        }

        String code = args[0].toUpperCase();

        // Use the verification module to process the verification
        if (plugin.getModuleManager().getModule("Verification") != null) {
            try {
                plugin.getModuleManager().getModule("Verification").getClass()
                    .getMethod("processMinecraftVerification", Player.class, String.class)
                    .invoke(plugin.getModuleManager().getModule("Verification"), player, code);
            } catch (Exception e) {
                plugin.getMessageManager().sendError(player, "Failed to process verification: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Fallback to direct verification processing
            processVerification(player, code);
        }

        return true;
    }

    private void processVerification(Player player, String code) {
        // Check if player is already verified
        if (plugin.getAdminUtils().isVerified(player)) {
            plugin.getMessageManager().sendError(player, plugin.getMessageManager().getMessage("verification.code-already-verified"));
            return;
        }

        // Get verification code from database
        var dbCode = plugin.getDatabaseManager().getVerificationCode(code);
        
        if (dbCode == null || dbCode.isExpired()) {
            plugin.getMessageManager().sendError(player, plugin.getMessageManager().getMessage("verification.code-invalid"));
            plugin.getMessageManager().sendInfo(player, plugin.getMessageManager().getMessage("verification.code-get-new"));
            return;
        }

        // Complete verification
        plugin.getDatabaseManager().linkAccounts(dbCode.getDiscordId(), player.getUniqueId(), player.getName());
        
        // Remove the used code
        plugin.getDatabaseManager().removeVerificationCode(code);

        // Send success message
        plugin.getMessageManager().sendSuccess(player, plugin.getMessageManager().getMessage("verification.code-success"));
        plugin.getMessageManager().sendInfo(player, plugin.getMessageManager().getMessage("verification.code-linked"));

        // Apply verified role/group if configured
        applyVerifiedRole(player);

        // Log verification
        plugin.getLogger().info("Player " + player.getName() + " verified with Discord ID: " + dbCode.getDiscordId());
    }

    private void applyVerifiedRole(Player player) {
        // This would integrate with permission plugins like LuckPerms
        // For now, just send a message
        plugin.getMessageManager().sendInfo(player, plugin.getMessageManager().getMessage("verification.code-features"));
    }
}
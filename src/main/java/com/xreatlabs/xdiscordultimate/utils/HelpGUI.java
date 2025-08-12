package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HelpGUI implements Listener {
    
    private final XDiscordUltimate plugin;
    private final Map<UUID, String> openGUIs = new HashMap<>();
    
    public HelpGUI(XDiscordUltimate plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void openMainHelp(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6XDiscordUltimate Help");
        
        // Discord/Verification section
        inv.setItem(10, createItem(Material.ENDER_PEARL, "§bDiscord & Verification",
            "§7Click to view Discord commands",
            "§7• Link your account",
            "§7• Get server invite",
            "§7• Verification status"));
        
        // Moderation/Reports section
        inv.setItem(11, createItem(Material.SHIELD, "§cModeration & Reports", 
            "§7Click to view report commands",
            "§7• Report players",
            "§7• View report status",
            "§7• Moderation tools"));
        
        // Support/Tickets section
        inv.setItem(12, createItem(Material.WRITABLE_BOOK, "§eSupport & Tickets", 
            "§7Click to view support commands",
            "§7• Create tickets",
            "§7• Get help from staff",
            "§7• Track ticket status"));
        
        // Voice/TTS section
        inv.setItem(13, createItem(Material.NOTE_BLOCK, "§dVoice & TTS", 
            "§7Click to view voice features",
            "§7• Text-to-speech",
            "§7• Voice announcements",
            "§7• Discord voice integration"));
        
        // Admin section (only if admin)
        if (plugin.getAdminUtils().isAdmin(player)) {
            inv.setItem(15, createItem(Material.COMMAND_BLOCK, "§4Admin Commands", 
                "§7Click to view admin commands",
                "§7• Console access",
                "§7• Plugin management",
                "§7• Advanced features"));
        }
        
        // Info item
        inv.setItem(22, createItem(Material.BOOK, "§6Plugin Information", 
            "§7XDiscordUltimate v" + plugin.getDescription().getVersion(),
            "§7Integrates Discord with Minecraft",
            "§7Created by XreaLabs",
            "",
            "§eClick for detailed info"));
        
        openGUIs.put(player.getUniqueId(), "main");
        player.openInventory(inv);
    }
    
    public void openDiscordHelp(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§bDiscord & Verification Help");
        
        // Back button
        inv.setItem(0, createItem(Material.ARROW, "§7← Back to Main Help", "§7Click to go back"));
        
        // Verify command
        inv.setItem(10, createItem(Material.EMERALD, "§a/verify <code>", 
            "§7Link your Discord account",
            "§7Usage: §f/verify ABC123",
            "",
            "§7Get your verification code from",
            "§7the Discord bot by typing §f/verify",
            "§7in any Discord channel"));
        
        // Discord command
        inv.setItem(11, createItem(Material.COMPASS, "§a/discord", 
            "§7Get Discord server invite",
            "§7Usage: §f/discord",
            "",
            "§7Shows the Discord server invite",
            "§7link and connection status"));
        
        // Status info
        inv.setItem(13, createItem(Material.PLAYER_HEAD, "§eVerification Status", 
            "§7Check if you're verified",
            "",
            "§7Verified players can:",
            "§7• Create support tickets",
            "§7• Submit reports",
            "§7• Access special features"));
        
        // Benefits
        inv.setItem(15, createItem(Material.DIAMOND, "§bVerification Benefits", 
            "§7What you get when verified:",
            "",
            "§a✓ §7Create support tickets",
            "§a✓ §7Report rule breakers",
            "§a✓ §7Access to Discord channels",
            "§a✓ §7Real-time notifications",
            "§a✓ §7Cross-platform chat"));
        
        openGUIs.put(player.getUniqueId(), "discord");
        player.openInventory(inv);
    }
    
    public void openModerationHelp(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§cModeration & Reports Help");
        
        // Back button
        inv.setItem(0, createItem(Material.ARROW, "§7← Back to Main Help", "§7Click to go back"));
        
        // Report command
        inv.setItem(10, createItem(Material.REDSTONE, "§a/report <player> <reason>", 
            "§7Report a rule-breaking player",
            "§7Usage: §f/report PlayerName griefing",
            "",
            "§7Examples:",
            "§f/report BadPlayer cheating",
            "§f/report Griefer destroyed my house",
            "",
            "§c⚠ §7Requires Discord verification"));
        
        // Report guidelines
        inv.setItem(12, createItem(Material.PAPER, "§eReport Guidelines", 
            "§7How to make good reports:",
            "",
            "§7• Be specific about the issue",
            "§7• Include evidence if possible",
            "§7• Don't spam reports",
            "§7• Use appropriate language",
            "",
            "§7Reports are reviewed by staff"));
        
        // Report status
        inv.setItem(14, createItem(Material.CLOCK, "§bReport Status", 
            "§7Track your reports:",
            "",
            "§7• Reports are logged to Discord",
            "§7• Staff receive notifications",
            "§7• You'll get updates on actions",
            "§7• Cooldown: 5 minutes between reports"));
        
        // What to report
        inv.setItem(16, createItem(Material.OAK_SIGN, "§cWhat to Report",
            "§7Report these violations:",
            "",
            "§c• Cheating/Hacking",
            "§c• Griefing",
            "§c• Inappropriate chat",
            "§c• Harassment",
            "§c• Rule breaking",
            "",
            "§7Don't report minor issues"));
        
        openGUIs.put(player.getUniqueId(), "moderation");
        player.openInventory(inv);
    }
    
    public void openSupportHelp(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§eSupport & Tickets Help");
        
        // Back button
        inv.setItem(0, createItem(Material.ARROW, "§7← Back to Main Help", "§7Click to go back"));
        
        // Support command
        inv.setItem(10, createItem(Material.WRITABLE_BOOK, "§a/support <message>", 
            "§7Create a support ticket",
            "§7Usage: §f/support I need help with...",
            "",
            "§7Examples:",
            "§f/support Lost my items in a glitch",
            "§f/support Can't access my base",
            "",
            "§c⚠ §7Requires Discord verification"));
        
        // Ticket process
        inv.setItem(12, createItem(Material.BOOK, "§bTicket Process", 
            "§7How tickets work:",
            "",
            "§71. Create ticket with §f/support",
            "§72. Discord channel is created",
            "§73. Staff will respond there",
            "§74. Ticket auto-closes after 24h",
            "§75. You get notifications"));
        
        // Ticket limits
        inv.setItem(14, createItem(Material.BARRIER, "§cTicket Limits", 
            "§7Ticket restrictions:",
            "",
            "§7• Max 3 open tickets per player",
            "§7• Don't spam tickets",
            "§7• Be patient for responses",
            "§7• Provide clear descriptions"));
        
        // When to use
        inv.setItem(16, createItem(Material.MAP, "§eWhen to Use Tickets",
            "§7Create tickets for:",
            "",
            "§a• Technical issues",
            "§a• Lost items/progress",
            "§a• Account problems",
            "§a• General questions",
            "§a• Feature requests",
            "",
            "§7For urgent issues, contact staff directly"));
        
        openGUIs.put(player.getUniqueId(), "support");
        player.openInventory(inv);
    }
    
    public void openVoiceHelp(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§dVoice & TTS Help");
        
        // Back button
        inv.setItem(0, createItem(Material.ARROW, "§7← Back to Main Help", "§7Click to go back"));
        
        // TTS Features
        inv.setItem(10, createItem(Material.NOTE_BLOCK, "§dText-to-Speech", 
            "§7Voice announcements:",
            "",
            "§7• Player join/leave messages",
            "§7• Chat messages read aloud",
            "§7• Discord messages in voice",
            "§7• Important notifications",
            "",
            "§7Automatic in Discord voice channels"));
        
        // Voice integration
        inv.setItem(12, createItem(Material.JUKEBOX, "§bVoice Integration", 
            "§7Discord voice features:",
            "",
            "§7• Real-time chat reading",
            "§7• Player event announcements",
            "§7• Cross-platform communication",
            "§7• Smart message filtering"));
        
        // Settings
        inv.setItem(14, createItem(Material.REDSTONE_TORCH, "§eVoice Settings", 
            "§7Configurable options:",
            "",
            "§7• Enable/disable TTS",
            "§7• Voice speed control",
            "§7• Message filtering",
            "§7• Channel restrictions",
            "",
            "§7Contact admin to adjust"));
        
        // Requirements
        inv.setItem(16, createItem(Material.DIAMOND, "§aRequirements",
            "§7To use voice features:",
            "",
            "§a✓ §7Discord verification required",
            "§a✓ §7Join Discord voice channel",
            "§a✓ §7Bot needs voice permissions",
            "",
            "§7Features work automatically"));
        
        openGUIs.put(player.getUniqueId(), "voice");
        player.openInventory(inv);
    }
    
    public void openAdminHelp(Player player) {
        if (!plugin.getAdminUtils().isAdmin(player)) {
            plugin.getMessageManager().sendError(player, "You don't have permission to view admin commands!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 36, "§4Admin Commands Help");
        
        // Back button
        inv.setItem(0, createItem(Material.ARROW, "§7← Back to Main Help", "§7Click to go back"));
        
        // Console command
        inv.setItem(10, createItem(Material.COMMAND_BLOCK, "§a/dconsole", 
            "§7Toggle Discord console access",
            "§7Usage: §f/dconsole",
            "",
            "§7Enables/disables console commands",
            "§7from Discord bot for admins",
            "",
            "§c⚠ §7Admin permission required"));
        
        // XDiscord commands
        inv.setItem(11, createItem(Material.REDSTONE_BLOCK, "§a/xdiscord <action>", 
            "§7Plugin management commands",
            "§7Usage: §f/xdiscord reload",
            "",
            "§7Available actions:",
            "§f• reload §7- Reload config",
            "§f• info §7- Show plugin info",
            "§f• status §7- Check bot status"));
        
        // Discord console
        inv.setItem(13, createItem(Material.REPEATING_COMMAND_BLOCK, "§bDiscord Console", 
            "§7Execute commands from Discord:",
            "",
            "§7Use §f/console <command> §7in Discord",
            "§7Examples:",
            "§f/console gamemode creative Player",
            "§f/console tp Player1 Player2",
            "",
            "§c⚠ §7Only for verified admins"));
        
        // Admin verification
        inv.setItem(15, createItem(Material.GOLDEN_APPLE, "§eAdmin Verification", 
            "§7How admin access works:",
            "",
            "§7• Discord ID must be in config.yml",
            "§7• Must be verified in-game",
            "§7• Real-time permission checks",
            "§7• Secure command execution",
            "",
            "§7Contact server owner to add IDs"));
        
        // Security info
        inv.setItem(16, createItem(Material.SHIELD, "§cSecurity Features", 
            "§7Admin security measures:",
            "",
            "§7• Multi-layer verification",
            "§7• Command logging",
            "§7• Permission validation",
            "§7• Rate limiting",
            "§7• Audit trails"));
        
        openGUIs.put(player.getUniqueId(), "admin");
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String guiType = openGUIs.get(player.getUniqueId());
        
        if (guiType == null) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;
        
        String displayName = meta.getDisplayName();
        
        // Handle back button
        if (displayName.contains("← Back to Main Help")) {
            openMainHelp(player);
            return;
        }
        
        // Handle main menu navigation
        if (guiType.equals("main")) {
            if (displayName.contains("Discord & Verification")) {
                openDiscordHelp(player);
            } else if (displayName.contains("Moderation & Reports")) {
                openModerationHelp(player);
            } else if (displayName.contains("Support & Tickets")) {
                openSupportHelp(player);
            } else if (displayName.contains("Voice & TTS")) {
                openVoiceHelp(player);
            } else if (displayName.contains("Admin Commands")) {
                openAdminHelp(player);
            } else if (displayName.contains("Plugin Information")) {
                showPluginInfo(player);
            }
        }
    }
    
    private void showPluginInfo(Player player) {
        player.closeInventory();
        openGUIs.remove(player.getUniqueId());
        
        plugin.getMessageManager().sendInfo(player, "=== XDiscordUltimate Information ===");
        plugin.getMessageManager().sendInfo(player, "Version: " + plugin.getDescription().getVersion());
        plugin.getMessageManager().sendInfo(player, "Author: XreaLabs");
        plugin.getMessageManager().sendInfo(player, "Description: " + plugin.getDescription().getDescription());
        plugin.getMessageManager().sendInfo(player, "");
        plugin.getMessageManager().sendInfo(player, "Features:");
        plugin.getMessageManager().sendInfo(player, "• Discord-Minecraft integration");
        plugin.getMessageManager().sendInfo(player, "• Player verification system");
        plugin.getMessageManager().sendInfo(player, "• Report and ticket system");
        plugin.getMessageManager().sendInfo(player, "• Voice/TTS features");
        plugin.getMessageManager().sendInfo(player, "• Admin console access");
        plugin.getMessageManager().sendInfo(player, "");
        plugin.getMessageManager().sendInfo(player, "Use §b/help §7to open the help GUI again!");
    }
    
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void cleanup() {
        openGUIs.clear();
    }
}
package com.xreatlabs.xdiscordultimate.modules.webhooks;
import java.io.IOException;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.Color;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebhookModule extends Module implements Listener {
    
    // Embed builders in progress
    private final Map<UUID, EmbedBuilderSession> embedSessions = new ConcurrentHashMap<>();
    private final Map<UUID, String> awaitingInput = new ConcurrentHashMap<>();
    
    // Webhook cache
    private final Map<String, Webhook> webhookCache = new ConcurrentHashMap<>();
    
    public WebhookModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Webhooks";
    }
    
    @Override
    public String getDescription() {
        return "Interactive webhook and embed system for Discord";
    }
    
    @Override
    protected void onEnable() {
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        info("Webhook module enabled");
    }
    
    @Override
    protected void onDisable() {
        // Clear sessions
        embedSessions.clear();
        awaitingInput.clear();
        webhookCache.clear();
        
        // Unregister events
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        
        info("Webhook module disabled");
    }
    
    /**
     * Send an embed to Discord
     */
    public boolean sendEmbed(CommandSender sender, String channelName, String title, String description) {
        try {
            TextChannel channel = getChannelByName(channelName);
            if (channel == null) {
                plugin.getMessageManager().sendError(sender, "Channel not found: " + channelName);
                return false;
            }
            
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(title);
            embed.setDescription(description);
            embed.setColor(Color.BLUE);
            embed.setTimestamp(Instant.now());
            
            if (sender instanceof Player) {
                Player player = (Player) sender;
                embed.setAuthor(player.getName(), null, 
                    "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay");
                embed.setFooter("Sent by " + player.getName(), null);
            }
            
            channel.sendMessageEmbeds(embed.build()).queue();
            plugin.getMessageManager().sendSuccess(sender, "Embed sent successfully!");
            return true;
            
        } catch (Exception e) {
            error("Failed to send embed", e);
            return false;
        }
    }
    
    /**
     * Send a webhook message
     */
    public boolean sendWebhook(String channelName, String username, String avatarUrl, String content, MessageEmbed embed) {
        try {
            TextChannel channel = getChannelByName(channelName);
            if (channel == null) {
                return false;
            }
            
            // Get or create webhook
            Webhook webhook = getOrCreateWebhook(channel);
            if (webhook == null) {
                return false;
            }
            
            // Build webhook message
            net.dv8tion.jda.api.utils.messages.MessageCreateBuilder builder = 
                new net.dv8tion.jda.api.utils.messages.MessageCreateBuilder();
            
            if (content != null && !content.isEmpty()) {
                builder.setContent(content);
            }
            
            if (embed != null) {
                builder.setEmbeds(embed);
            }
            
            // Send via webhook
            webhook.sendMessage(builder.build())
                .setUsername(username)
                .setAvatarUrl(avatarUrl)
                .queue();
            
            return true;
            
        } catch (Exception e) {
            error("Failed to send webhook", e);
            return false;
        }
    }
    
    /**
     * Open embed builder GUI
     */
    public void openEmbedBuilder(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Create new session
        EmbedBuilderSession session = new EmbedBuilderSession();
        embedSessions.put(uuid, session);
        
        // Create GUI
        Inventory gui = Bukkit.createInventory(null, 54, "§6Embed Builder");
        
        // Title
        ItemStack titleItem = createGuiItem(Material.NAME_TAG, "§e§lSet Title", 
            "§7Current: §f" + (session.title != null ? session.title : "None"),
            "", "§aClick to set title");
        gui.setItem(10, titleItem);
        
        // Description
        ItemStack descItem = createGuiItem(Material.BOOK, "§e§lSet Description", 
            "§7Current: §f" + (session.description != null ? 
                (session.description.length() > 30 ? session.description.substring(0, 30) + "..." : session.description) 
                : "None"),
            "", "§aClick to set description");
        gui.setItem(11, descItem);
        
        // Color
        ItemStack colorItem = createGuiItem(Material.YELLOW_DYE, "§e§lSet Color", 
            "§7Current: §f" + session.colorName,
            "", "§aClick to change color");
        gui.setItem(12, colorItem);
        
        // Author
        ItemStack authorItem = createGuiItem(Material.PLAYER_HEAD, "§e§lSet Author", 
            "§7Current: §f" + (session.author != null ? session.author : player.getName()),
            "", "§aClick to set author");
        gui.setItem(13, authorItem);
        
        // Footer
        ItemStack footerItem = createGuiItem(Material.IRON_BOOTS, "§e§lSet Footer", 
            "§7Current: §f" + (session.footer != null ? session.footer : "None"),
            "", "§aClick to set footer");
        gui.setItem(14, footerItem);
        
        // Thumbnail
        ItemStack thumbnailItem = createGuiItem(Material.PAINTING, "§e§lSet Thumbnail", 
            "§7Current: §f" + (session.thumbnailUrl != null ? "Set" : "None"),
            "", "§aClick to set thumbnail URL");
        gui.setItem(15, thumbnailItem);
        
        // Image
        ItemStack imageItem = createGuiItem(Material.ITEM_FRAME, "§e§lSet Image", 
            "§7Current: §f" + (session.imageUrl != null ? "Set" : "None"),
            "", "§aClick to set image URL");
        gui.setItem(16, imageItem);
        
        // Add field
        ItemStack fieldItem = createGuiItem(Material.WRITABLE_BOOK, "§e§lAdd Field", 
            "§7Fields: §f" + session.fields.size(),
            "", "§aClick to add a field");
        gui.setItem(28, fieldItem);
        
        // Timestamp
        ItemStack timestampItem = createGuiItem(Material.CLOCK, "§e§lToggle Timestamp", 
            "§7Current: §f" + (session.timestamp ? "Enabled" : "Disabled"),
            "", "§aClick to toggle");
        gui.setItem(29, timestampItem);
        
        // Preview
        ItemStack previewItem = createGuiItem(Material.ENDER_EYE, "§d§lPreview", 
            "§7See how your embed will look",
            "", "§aClick to preview in Discord");
        gui.setItem(31, previewItem);
        
        // Channel selection
        ItemStack channelItem = createGuiItem(Material.COMPASS, "§e§lSelect Channel", 
            "§7Current: §f" + (session.targetChannel != null ? session.targetChannel : "None"),
            "", "§aClick to select channel");
        gui.setItem(32, channelItem);
        
        // Send
        ItemStack sendItem = createGuiItem(Material.EMERALD, "§a§lSend Embed", 
            "§7Send the embed to Discord",
            "", "§aClick to send");
        gui.setItem(34, sendItem);
        
        // Cancel
        ItemStack cancelItem = createGuiItem(Material.BARRIER, "§c§lCancel", 
            "§7Close without sending");
        gui.setItem(49, cancelItem);
        
        // Color options (bottom row)
        gui.setItem(45, createColorItem(Material.RED_DYE, "Red", Color.RED));
        gui.setItem(46, createColorItem(Material.BLUE_DYE, "Blue", Color.BLUE));
        gui.setItem(47, createColorItem(Material.LIME_DYE, "Green", Color.GREEN));
        gui.setItem(48, createColorItem(Material.YELLOW_DYE, "Yellow", Color.YELLOW));
        gui.setItem(50, createColorItem(Material.PURPLE_DYE, "Purple", new Color(128, 0, 128)));
        gui.setItem(51, createColorItem(Material.ORANGE_DYE, "Orange", Color.ORANGE));
        gui.setItem(52, createColorItem(Material.PINK_DYE, "Pink", Color.PINK));
        gui.setItem(53, createColorItem(Material.GRAY_DYE, "Gray", Color.GRAY));
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Embed Builder")) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        EmbedBuilderSession session = embedSessions.get(uuid);
        
        if (session == null) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String itemName = clicked.getItemMeta().getDisplayName();
        
        // Handle color selection
        if (event.getSlot() >= 45 && event.getSlot() <= 53 && event.getSlot() != 49) {
            handleColorSelection(player, session, clicked);
            openEmbedBuilder(player); // Refresh
            return;
        }
        
        switch (event.getSlot()) {
            case 10: // Title
                awaitingInput.put(uuid, "title");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the embed title in chat (or 'cancel' to cancel):");
                break;
                
            case 11: // Description
                awaitingInput.put(uuid, "description");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the embed description in chat (or 'cancel' to cancel):");
                break;
                
            case 13: // Author
                awaitingInput.put(uuid, "author");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the author name in chat (or 'cancel' to cancel):");
                break;
                
            case 14: // Footer
                awaitingInput.put(uuid, "footer");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the footer text in chat (or 'cancel' to cancel):");
                break;
                
            case 15: // Thumbnail
                awaitingInput.put(uuid, "thumbnail");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the thumbnail URL in chat (or 'cancel' to cancel):");
                break;
                
            case 16: // Image
                awaitingInput.put(uuid, "image");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the image URL in chat (or 'cancel' to cancel):");
                break;
                
            case 28: // Add field
                awaitingInput.put(uuid, "field_name");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the field name in chat (or 'cancel' to cancel):");
                break;
                
            case 29: // Toggle timestamp
                session.timestamp = !session.timestamp;
                openEmbedBuilder(player); // Refresh
                break;
                
            case 31: // Preview
                sendPreview(player, session);
                break;
                
            case 32: // Channel selection
                awaitingInput.put(uuid, "channel");
                player.closeInventory();
                plugin.getMessageManager().sendInfo(player, "Type the channel name in chat (or 'cancel' to cancel):");
                break;
                
            case 34: // Send
                if (session.targetChannel == null) {
                    plugin.getMessageManager().sendError(player, "Please select a channel first!");
                } else {
                    sendFinalEmbed(player, session);
                    player.closeInventory();
                    embedSessions.remove(uuid);
                }
                break;
                
            case 49: // Cancel
                player.closeInventory();
                embedSessions.remove(uuid);
                plugin.getMessageManager().sendInfo(player, "Embed builder cancelled.");
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("§6Embed Builder")) return;
        
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Clean up if not awaiting input
        if (!awaitingInput.containsKey(uuid)) {
            embedSessions.remove(uuid);
        }
    }
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        String inputType = awaitingInput.get(uuid);
        if (inputType == null) return;
        
        event.setCancelled(true);
        String message = event.getMessage();
        
        if (message.equalsIgnoreCase("cancel")) {
            awaitingInput.remove(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getMessageManager().sendInfo(player, "Input cancelled.");
                openEmbedBuilder(player);
            });
            return;
        }
        
        EmbedBuilderSession session = embedSessions.get(uuid);
        if (session == null) {
            awaitingInput.remove(uuid);
            return;
        }
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            switch (inputType) {
                case "title":
                    session.title = message;
                    break;
                case "description":
                    session.description = message;
                    break;
                case "author":
                    session.author = message;
                    break;
                case "footer":
                    session.footer = message;
                    break;
                case "thumbnail":
                    if (isValidUrl(message)) {
                        session.thumbnailUrl = message;
                    } else {
                        plugin.getMessageManager().sendError(player, "Invalid URL!");
                    }
                    break;
                case "image":
                    if (isValidUrl(message)) {
                        session.imageUrl = message;
                    } else {
                        plugin.getMessageManager().sendError(player, "Invalid URL!");
                    }
                    break;
                case "field_name":
                    session.tempFieldName = message;
                    awaitingInput.put(uuid, "field_value");
                    plugin.getMessageManager().sendInfo(player, "Now type the field value:");
                    return;
                case "field_value":
                    if (session.tempFieldName != null) {
                        session.fields.add(new EmbedField(session.tempFieldName, message, false));
                        session.tempFieldName = null;
                    }
                    break;
                case "channel":
                    session.targetChannel = message;
                    break;
            }
            
            awaitingInput.remove(uuid);
            openEmbedBuilder(player);
        });
    }
    
    private void handleColorSelection(Player player, EmbedBuilderSession session, ItemStack item) {
        String colorName = item.getItemMeta().getDisplayName().replace("§r", "");
        Color color = getColorFromItem(item.getType());
        
        session.color = color;
        session.colorName = colorName;
    }
    
    private Color getColorFromItem(Material material) {
        switch (material) {
            case RED_DYE: return Color.RED;
            case BLUE_DYE: return Color.BLUE;
            case LIME_DYE: return Color.GREEN;
            case YELLOW_DYE: return Color.YELLOW;
            case PURPLE_DYE: return new Color(128, 0, 128);
            case ORANGE_DYE: return Color.ORANGE;
            case PINK_DYE: return Color.PINK;
            case GRAY_DYE: return Color.GRAY;
            default: return Color.BLUE;
        }
    }
    
    private void sendPreview(Player player, EmbedBuilderSession session) {
        String previewChannel = getConfig().getString("preview-channel", "bot-commands");
        TextChannel channel = getChannelByName(previewChannel);
        
        if (channel == null) {
            plugin.getMessageManager().sendError(player, "Preview channel not found!");
            return;
        }
        
        EmbedBuilder embed = buildEmbed(session, player);
        
        channel.sendMessage(player.getName() + " is previewing an embed:")
            .setEmbeds(embed.build())
            .setActionRow(
                Button.success("approve_" + player.getUniqueId(), "Looks Good!")
                    .withEmoji(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("✅")),
                Button.danger("reject_" + player.getUniqueId(), "Needs Changes")
                    .withEmoji(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("❌"))
            )
            .queue();
        
        plugin.getMessageManager().sendSuccess(player, "Preview sent to #" + previewChannel);
    }
    
    private void sendFinalEmbed(Player player, EmbedBuilderSession session) {
        TextChannel channel = getChannelByName(session.targetChannel);
        
        if (channel == null) {
            plugin.getMessageManager().sendError(player, "Channel not found: " + session.targetChannel);
            return;
        }
        
        EmbedBuilder embed = buildEmbed(session, player);
        
        channel.sendMessageEmbeds(embed.build()).queue(
            success -> plugin.getMessageManager().sendSuccess(player, "Embed sent successfully!"),
            error -> plugin.getMessageManager().sendError(player, "Failed to send embed: " + error.getMessage())
        );
    }
    
    private EmbedBuilder buildEmbed(EmbedBuilderSession session, Player player) {
        EmbedBuilder embed = new EmbedBuilder();
        
        if (session.title != null) embed.setTitle(session.title);
        if (session.description != null) embed.setDescription(session.description);
        if (session.color != null) embed.setColor(session.color);
        if (session.author != null) {
            embed.setAuthor(session.author, null, 
                "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay");
        }
        if (session.footer != null) embed.setFooter(session.footer, null);
        if (session.thumbnailUrl != null) embed.setThumbnail(session.thumbnailUrl);
        if (session.imageUrl != null) embed.setImage(session.imageUrl);
        if (session.timestamp) embed.setTimestamp(Instant.now());
        
        for (EmbedField field : session.fields) {
            embed.addField(field.name, field.value, field.inline);
        }
        
        return embed;
    }
    
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createColorItem(Material material, String colorName, Color color) {
        return createGuiItem(material, "§r" + colorName, "§7Click to select this color");
    }
    
    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
    
    private TextChannel getChannelByName(String name) {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        return plugin.getDiscordManager().getTextChannel(name);
    }
    
    private Webhook getOrCreateWebhook(TextChannel channel) {
        String channelId = channel.getId();
        
        // Check cache
        Webhook cached = webhookCache.get(channelId);
        if (cached != null) {
            return cached;
        }
        
        // Get existing webhooks
        List<Webhook> webhooks = channel.retrieveWebhooks().complete();
        
        // Find our webhook
        Webhook webhook = webhooks.stream()
            .filter(w -> w.getName().equals("XDiscordUltimate"))
            .findFirst()
            .orElse(null);
        
        // Create if not exists
        if (webhook == null) {
            try {
                webhook = channel.createWebhook("XDiscordUltimate")
                    .setAvatar(net.dv8tion.jda.api.entities.Icon.from(
                        getClass().getResourceAsStream("/icon.png")))
                    .complete();
            } catch (IOException e) {
                // Create without avatar if icon loading fails
                webhook = channel.createWebhook("XDiscordUltimate").complete();
                plugin.getLogger().warning("Failed to set webhook avatar: " + e.getMessage());
            }
        }
        
        // Cache it
        webhookCache.put(channelId, webhook);
        return webhook;
    }
    
    /**
     * Embed builder session data
     */
    private static class EmbedBuilderSession {
        String title;
        String description;
        Color color = Color.BLUE;
        String colorName = "Blue";
        String author;
        String footer;
        String thumbnailUrl;
        String imageUrl;
        boolean timestamp = true;
        String targetChannel;
        List<EmbedField> fields = new ArrayList<>();
        String tempFieldName;
    }
    
    private static class EmbedField {
        final String name;
        final String value;
        final boolean inline;
        
        EmbedField(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }
}
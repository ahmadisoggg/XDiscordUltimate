package com.xreatlabs.xdiscordultimate.modules.emojireactions;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmojiReactionModule extends Module implements Listener {
    
    private EmojiListener emojiListener;
    
    // Emoji mappings
    private final Map<String, String> emojiMap = new ConcurrentHashMap<>();
    private final Map<String, String> customEmojis = new ConcurrentHashMap<>();
    private final Map<String, ReactionRole> reactionRoles = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> playerFavorites = new ConcurrentHashMap<>();
    
    // Configuration
    private boolean enableEmojiChat;
    private boolean enableReactionRoles;
    private boolean enableCustomEmojis;
    private boolean enableEmojiGUI;
    private String emojiPrefix;
    private String emojiSuffix;
    
    // Patterns
    private static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+):");
    private static final Pattern DISCORD_EMOJI_PATTERN = Pattern.compile("<a?:([a-zA-Z0-9_]+):(\\d+)>");
    
    public EmojiReactionModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "EmojiReactions";
    }
    
    @Override
    public String getDescription() {
        return "Emoji reactions, custom emojis, and reaction roles system";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        loadEmojis();
        loadReactionRoles();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Register Discord listener
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            emojiListener = new EmojiListener();
            plugin.getDiscordManager().getJDA().addEventListener(emojiListener);
        }
        
        info("Emoji reactions module enabled with " + emojiMap.size() + " emojis");
    }
    
    @Override
    protected void onDisable() {
        // Unregister Discord listener
        if (emojiListener != null && plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getJDA().removeEventListener(emojiListener);
        }
        
        // Save player favorites
        savePlayerFavorites();
        
        info("Emoji reactions module disabled");
    }
    
    private void loadConfiguration() {
        enableEmojiChat = getConfig().getBoolean("enable-emoji-chat", true);
        enableReactionRoles = getConfig().getBoolean("enable-reaction-roles", true);
        enableCustomEmojis = getConfig().getBoolean("enable-custom-emojis", true);
        enableEmojiGUI = getConfig().getBoolean("enable-emoji-gui", true);
        emojiPrefix = getConfig().getString("emoji-prefix", ":");
        emojiSuffix = getConfig().getString("emoji-suffix", ":");
    }
    
    private void loadEmojis() {
        // Load default emojis
        loadDefaultEmojis();
        
        // Load custom emojis
        if (enableCustomEmojis) {
            ConfigurationSection section = getConfig().getConfigurationSection("custom-emojis");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    String value = section.getString(key);
                    if (value != null) {
                        customEmojis.put(key.toLowerCase(), value);
                        emojiMap.put(key.toLowerCase(), value);
                    }
                }
            }
        }
    }
    
    private void loadDefaultEmojis() {
        // Common emojis
        emojiMap.put("smile", "😊");
        emojiMap.put("sad", "😢");
        emojiMap.put("heart", "❤️");
        emojiMap.put("fire", "🔥");
        emojiMap.put("star", "⭐");
        emojiMap.put("thumbsup", "👍");
        emojiMap.put("thumbsdown", "👎");
        emojiMap.put("wave", "👋");
        emojiMap.put("clap", "👏");
        emojiMap.put("eyes", "👀");
        emojiMap.put("thinking", "🤔");
        emojiMap.put("joy", "😂");
        emojiMap.put("cry", "😭");
        emojiMap.put("angry", "😠");
        emojiMap.put("love", "😍");
        emojiMap.put("cool", "😎");
        emojiMap.put("wink", "😉");
        emojiMap.put("kiss", "😘");
        emojiMap.put("sick", "🤢");
        emojiMap.put("party", "🎉");
        emojiMap.put("100", "💯");
        emojiMap.put("ok", "👌");
        emojiMap.put("no", "❌");
        emojiMap.put("yes", "✅");
        emojiMap.put("warning", "⚠️");
        emojiMap.put("info", "ℹ️");
        emojiMap.put("question", "❓");
        emojiMap.put("exclamation", "❗");
        emojiMap.put("zzz", "💤");
        emojiMap.put("boom", "💥");
        
        // Minecraft related
        emojiMap.put("creeper", "🟢");
        emojiMap.put("diamond", "💎");
        emojiMap.put("sword", "⚔️");
        emojiMap.put("pickaxe", "⛏️");
        emojiMap.put("tnt", "🧨");
        emojiMap.put("chest", "📦");
        emojiMap.put("potion", "🧪");
        emojiMap.put("apple", "🍎");
        emojiMap.put("cake", "🎂");
        emojiMap.put("bed", "🛏️");
    }
    
    private void loadReactionRoles() {
        if (!enableReactionRoles) return;
        
        ConfigurationSection section = getConfig().getConfigurationSection("reaction-roles");
        if (section == null) return;
        
        for (String key : section.getKeys(false)) {
            ConfigurationSection roleSection = section.getConfigurationSection(key);
            if (roleSection == null) continue;
            
            ReactionRole reactionRole = new ReactionRole(
                roleSection.getString("message-id", ""),
                roleSection.getString("emoji", ""),
                roleSection.getString("role", ""),
                roleSection.getString("description", "")
            );
            
            if (!reactionRole.messageId.isEmpty()) {
                reactionRoles.put(reactionRole.messageId + ":" + reactionRole.emoji, reactionRole);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!enableEmojiChat) return;
        
        String message = event.getMessage();
        String processed = processEmojis(message);
        
        if (!message.equals(processed)) {
            event.setMessage(processed);
        }
    }
    
    /**
     * Process emoji codes in text
     */
    public String processEmojis(String text) {
        if (!enableEmojiChat) return text;
        
        // Replace :emoji: patterns
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String emojiName = matcher.group(1).toLowerCase();
            String emoji = emojiMap.get(emojiName);
            
            if (emoji != null) {
                matcher.appendReplacement(result, emoji);
            } else {
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Open emoji GUI for player
     */
    public void openEmojiGUI(Player player) {
        if (!enableEmojiGUI) {
            plugin.getMessageManager().sendError(player, "Emoji GUI is disabled!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lEmoji Selector");
        
        // Get player favorites
        Set<String> favorites = playerFavorites.getOrDefault(player.getUniqueId(), new HashSet<>());
        
        // Add favorite emojis first
        int slot = 0;
        for (String favName : favorites) {
            String emoji = emojiMap.get(favName);
            if (emoji != null && slot < 9) {
                gui.setItem(slot, createEmojiItem(favName, emoji, true));
                slot++;
            }
        }
        
        // Add separator
        for (int i = 9; i < 18; i++) {
            ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = separator.getItemMeta();
            meta.setDisplayName(" ");
            separator.setItemMeta(meta);
            gui.setItem(i, separator);
        }
        
        // Add all emojis
        slot = 18;
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            if (slot >= 54) break;
            
            gui.setItem(slot, createEmojiItem(entry.getKey(), entry.getValue(), 
                favorites.contains(entry.getKey())));
            slot++;
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6§lEmoji Selector")) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        
        List<String> lore = meta.getLore();
        if (lore.size() < 2) return;
        
        // Extract emoji name from lore
        String nameLine = ChatColor.stripColor(lore.get(0));
        if (nameLine.startsWith("Name: ")) {
            String emojiName = nameLine.substring(6);
            String emoji = emojiMap.get(emojiName);
            
            if (emoji != null) {
                if (event.isRightClick()) {
                    // Toggle favorite
                    toggleFavorite(player, emojiName);
                    openEmojiGUI(player); // Refresh
                } else {
                    // Send emoji to chat
                    player.closeInventory();
                    player.chat(emoji);
                }
            }
        }
    }
    
    private ItemStack createEmojiItem(String name, String emoji, boolean isFavorite) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(emoji + " " + (isFavorite ? "§e⭐" : ""));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Name: §f" + name);
        lore.add("§7Code: §f:" + name + ":");
        lore.add("");
        lore.add("§aLeft-click to send");
        lore.add("§eRight-click to " + (isFavorite ? "unfavorite" : "favorite"));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private void toggleFavorite(Player player, String emojiName) {
        UUID uuid = player.getUniqueId();
        Set<String> favorites = playerFavorites.computeIfAbsent(uuid, k -> new HashSet<>());
        
        if (favorites.contains(emojiName)) {
            favorites.remove(emojiName);
            plugin.getMessageManager().sendInfo(player, "Removed " + emojiName + " from favorites");
        } else {
            if (favorites.size() >= 9) {
                plugin.getMessageManager().sendError(player, "You can only have 9 favorite emojis!");
                return;
            }
            favorites.add(emojiName);
            plugin.getMessageManager().sendSuccess(player, "Added " + emojiName + " to favorites");
        }
    }
    
    private class EmojiListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            
            // Process custom Discord emojis to Unicode
            String content = event.getMessage().getContentDisplay();
            String processed = processDiscordEmojis(content);
            
            // If message contains custom emojis, relay to Minecraft with replacements
            if (!content.equals(processed) && null != null) {
                // This would integrate with your chat sync system
                debug("Processed Discord emojis in message");
            }
        }
        
        @Override
        public void onMessageReactionAdd(MessageReactionAddEvent event) {
            if (!enableReactionRoles) return;
            if (event.getUser() == null || event.getUser().isBot()) return;
            
            handleReactionRole(event.getMessageId(), event.getEmoji(), event.getUser(), true);
        }
        
        @Override
        public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
            if (!enableReactionRoles) return;
            if (event.getUser() == null || event.getUser().isBot()) return;
            
            handleReactionRole(event.getMessageId(), event.getEmoji(), event.getUser(), false);
        }
    }
    
    private void handleReactionRole(String messageId, EmojiUnion emoji, User user, boolean add) {
        String emojiKey = emoji.getType() == Emoji.Type.UNICODE ? 
            emoji.asUnicode().getName() : emoji.asCustom().getName();
        
        ReactionRole reactionRole = reactionRoles.get(messageId + ":" + emojiKey);
        if (reactionRole == null) return;
        
        // Get member
        plugin.getDiscordManager().getMainGuild().retrieveMemberById(user.getId()).queue(member -> {
            if (member == null) return;
            
            // Get role
            plugin.getDiscordManager().getMainGuild().getRolesByName(reactionRole.roleName, true)
                .stream()
                .findFirst()
                .ifPresent(role -> {
                    if (add) {
                        plugin.getDiscordManager().getMainGuild()
                            .addRoleToMember(member, role)
                            .reason("Reaction role")
                            .queue(
                                success -> debug("Added role " + role.getName() + " to " + user.getName()),
                                error -> error("Failed to add reaction role: " + error.getMessage())
                            );
                    } else {
                        plugin.getDiscordManager().getMainGuild()
                            .removeRoleFromMember(member, role)
                            .reason("Reaction role removed")
                            .queue(
                                success -> debug("Removed role " + role.getName() + " from " + user.getName()),
                                error -> error("Failed to remove reaction role: " + error.getMessage())
                            );
                    }
                });
        });
    }
    
    private String processDiscordEmojis(String text) {
        // Convert Discord custom emojis to text representation
        Matcher matcher = DISCORD_EMOJI_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String emojiName = matcher.group(1);
            matcher.appendReplacement(result, ":" + emojiName + ":");
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Create a reaction role message
     */
    public void createReactionRoleMessage(String channelName, String title, Map<String, String> emojiRoles) {
        TextChannel channel = plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(channelName, true)
            .stream()
            .findFirst()
            .orElse(null);
        
        if (channel == null) {
            warning("Channel not found for reaction roles: " + channelName);
            return;
        }
        
        StringBuilder description = new StringBuilder();
        description.append("React to this message to get your roles!\n\n");
        
        for (Map.Entry<String, String> entry : emojiRoles.entrySet()) {
            String emoji = entry.getKey();
            String roleName = entry.getValue();
            description.append(emoji).append(" - **").append(roleName).append("**\n");
        }
        
        net.dv8tion.jda.api.EmbedBuilder embed = new net.dv8tion.jda.api.EmbedBuilder()
            .setTitle(title)
            .setDescription(description.toString())
            .setColor(java.awt.Color.BLUE)
            .setFooter("React to get roles • Remove reaction to remove roles", null);
        
        channel.sendMessageEmbeds(embed.build()).queue(message -> {
            // Add reactions
            for (String emoji : emojiRoles.keySet()) {
                message.addReaction(Emoji.fromUnicode(emoji)).queue();
            }
            
            // Save reaction roles
            String messageId = message.getId();
            for (Map.Entry<String, String> entry : emojiRoles.entrySet()) {
                ReactionRole reactionRole = new ReactionRole(
                    messageId,
                    entry.getKey(),
                    entry.getValue(),
                    ""
                );
                reactionRoles.put(messageId + ":" + entry.getKey(), reactionRole);
            }
            
            // Save to config
            saveReactionRoles();
            
            info("Created reaction role message with " + emojiRoles.size() + " roles");
        });
    }
    
    private void savePlayerFavorites() {
        ConfigurationSection section = getConfig().createSection("player-favorites");
        
        for (Map.Entry<UUID, Set<String>> entry : playerFavorites.entrySet()) {
            section.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }
        
        // TODO: Implement saveConfig()
    }
    
    private void saveReactionRoles() {
        ConfigurationSection section = getConfig().createSection("reaction-roles");
        
        int index = 0;
        for (ReactionRole role : reactionRoles.values()) {
            ConfigurationSection roleSection = section.createSection("role" + index);
            roleSection.set("message-id", role.messageId);
            roleSection.set("emoji", role.emoji);
            roleSection.set("role", role.roleName);
            roleSection.set("description", role.description);
            index++;
        }
        
        // TODO: Implement saveConfig()
    }
    
    /**
     * Get all available emojis
     */
    public Map<String, String> getEmojis() {
        return new HashMap<>(emojiMap);
    }
    
    /**
     * Add custom emoji
     */
    public void addCustomEmoji(String name, String emoji) {
        customEmojis.put(name.toLowerCase(), emoji);
        emojiMap.put(name.toLowerCase(), emoji);
        
        // Save to config
        getConfig().set("custom-emojis." + name.toLowerCase(), emoji);
        // TODO: Implement saveConfig()
    }
    
    /**
     * Remove custom emoji
     */
    public void removeCustomEmoji(String name) {
        customEmojis.remove(name.toLowerCase());
        emojiMap.remove(name.toLowerCase());
        
        // Remove from config
        getConfig().set("custom-emojis." + name.toLowerCase(), null);
        // TODO: Implement saveConfig()
    }
    
    /**
     * Reaction role data
     */
    private static class ReactionRole {
        final String messageId;
        final String emoji;
        final String roleName;
        final String description;
        
        ReactionRole(String messageId, String emoji, String roleName, String description) {
            this.messageId = messageId;
            this.emoji = emoji;
            this.roleName = roleName;
            this.description = description;
        }
    }
}
package com.xreatlabs.xdiscordultimate.modules.leaderboards;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import me.clip.placeholderapi.PlaceholderAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LeaderboardModule extends Module implements Listener {
    
    // Leaderboard configurations
    private final Map<String, LeaderboardConfig> leaderboards = new ConcurrentHashMap<>();
    private final Map<String, List<LeaderboardEntry>> cachedLeaderboards = new ConcurrentHashMap<>();
    private final Map<String, Message> discordMessages = new ConcurrentHashMap<>();
    
    // Configuration
    private String leaderboardChannel;
    private int updateInterval;
    private int maxEntries;
    private boolean enableDiscordSync;
    private boolean enableGUI;
    
    public LeaderboardModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Leaderboards";
    }
    
    @Override
    public String getDescription() {
        return "Dynamic leaderboards with PlaceholderAPI integration";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        loadLeaderboards();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Start update task
        startUpdateTask();
        
        info("Leaderboards module enabled with " + leaderboards.size() + " leaderboards");
    }
    
    @Override
    protected void onDisable() {
        // Clear caches
        cachedLeaderboards.clear();
        discordMessages.clear();
        
        info("Leaderboards module disabled");
    }
    
    private void loadConfiguration() {
        leaderboardChannel = getConfig().getString("discord-channel", "leaderboards");
        updateInterval = getConfig().getInt("update-interval", 300); // 5 minutes
        maxEntries = getConfig().getInt("max-entries", 10);
        enableDiscordSync = getConfig().getBoolean("enable-discord-sync", true);
        enableGUI = getConfig().getBoolean("enable-gui", true);
    }
    
    private void loadLeaderboards() {
        ConfigurationSection section = getConfig().getConfigurationSection("leaderboards");
        if (section == null) {
            createDefaultLeaderboards();
            return;
        }
        
        for (String key : section.getKeys(false)) {
            ConfigurationSection lbSection = section.getConfigurationSection(key);
            if (lbSection == null) continue;
            
            LeaderboardConfig config = new LeaderboardConfig(
                key,
                lbSection.getString("title", key),
                lbSection.getString("type", "placeholder"),
                lbSection.getString("value", ""),
                lbSection.getString("format", "&e%position%. &f%player% &7- &a%value%"),
                lbSection.getBoolean("enabled", true),
                lbSection.getString("icon", "DIAMOND"),
                lbSection.getString("discord-color", "#FFD700")
            );
            
            leaderboards.put(key, config);
        }
    }
    
    private void createDefaultLeaderboards() {
        // Playtime leaderboard
        leaderboards.put("playtime", new LeaderboardConfig(
            "playtime",
            "Top Playtime",
            "statistic",
            "PLAY_ONE_MINUTE",
            "&e%position%. &f%player% &7- &a%value%",
            true,
            "CLOCK",
            "#00CED1"
        ));
        
        // Balance leaderboard (if economy plugin exists)
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            leaderboards.put("balance", new LeaderboardConfig(
                "balance",
                "Richest Players",
                "placeholder",
                "%vault_eco_balance%",
                "&e%position%. &f%player% &7- &a$%value%",
                true,
                "EMERALD",
                "#32CD32"
            ));
        }
        
        // Kills leaderboard
        leaderboards.put("kills", new LeaderboardConfig(
            "kills",
            "Top Killers",
            "statistic",
            "PLAYER_KILLS",
            "&e%position%. &f%player% &7- &c%value% kills",
            true,
            "DIAMOND_SWORD",
            "#DC143C"
        ));
        
        // Deaths leaderboard
        leaderboards.put("deaths", new LeaderboardConfig(
            "deaths",
            "Most Deaths",
            "statistic",
            "DEATHS",
            "&e%position%. &f%player% &7- &c%value% deaths",
            true,
            "SKELETON_SKULL",
            "#8B0000"
        ));
        
        // Blocks mined
        leaderboards.put("blocks_mined", new LeaderboardConfig(
            "blocks_mined",
            "Top Miners",
            "statistic",
            "MINE_BLOCK",
            "&e%position%. &f%player% &7- &a%value% blocks",
            true,
            "DIAMOND_PICKAXE",
            "#4169E1"
        ));
    }
    
    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllLeaderboards();
            }
        }.runTaskTimer(plugin, 20L, 20L * updateInterval);
    }
    
    private void updateAllLeaderboards() {
        for (LeaderboardConfig config : leaderboards.values()) {
            if (!config.enabled) continue;
            
            List<LeaderboardEntry> entries = generateLeaderboard(config);
            cachedLeaderboards.put(config.id, entries);
            
            // Update Discord
            if (enableDiscordSync) {
                updateDiscordLeaderboard(config, entries);
            }
        }
    }
    
    private List<LeaderboardEntry> generateLeaderboard(LeaderboardConfig config) {
        Map<OfflinePlayer, Double> values = new HashMap<>();
        
        // Collect values for all players
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.getName() == null) continue;
            
            double value = getValue(player, config);
            if (value > 0) {
                values.put(player, value);
            }
        }
        
        // Sort and create entries
        return values.entrySet().stream()
            .sorted(Map.Entry.<OfflinePlayer, Double>comparingByValue().reversed())
            .limit(maxEntries)
            .map(entry -> new LeaderboardEntry(
                entry.getKey().getName(),
                entry.getKey().getUniqueId(),
                entry.getValue()
            ))
            .collect(Collectors.toList());
    }
    
    private double getValue(OfflinePlayer player, LeaderboardConfig config) {
        switch (config.type.toLowerCase()) {
            case "statistic":
                try {
                    Statistic stat = Statistic.valueOf(config.value.toUpperCase());
                    return player.getStatistic(stat);
                } catch (Exception e) {
                    return 0;
                }
                
            case "placeholder":
                if (null != null && player.isOnline()) {
                    String result = PlaceholderAPI.setPlaceholders(player.getPlayer(), config.value);
                    try {
                        return Double.parseDouble(result.replaceAll("[^0-9.]", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
                return 0;
                
            default:
                return 0;
        }
    }
    
    /**
     * Show leaderboard GUI to player
     */
    public void showLeaderboardGUI(Player player) {
        if (!enableGUI) {
            plugin.getMessageManager().sendError(player, "Leaderboard GUI is disabled!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lLeaderboards");
        
        int slot = 10;
        for (LeaderboardConfig config : leaderboards.values()) {
            if (!config.enabled) continue;
            
            ItemStack item = createLeaderboardItem(config);
            gui.setItem(slot, item);
            
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip to next row
            if (slot >= 44) break; // Max items
        }
        
        // Close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§c§lClose");
        closeButton.setItemMeta(closeMeta);
        gui.setItem(49, closeButton);
        
        player.openInventory(gui);
    }
    
    /**
     * Show specific leaderboard to player
     */
    public void showLeaderboard(Player player, String leaderboardId) {
        LeaderboardConfig config = leaderboards.get(leaderboardId);
        if (config == null) {
            plugin.getMessageManager().sendError(player, "Leaderboard not found: " + leaderboardId);
            return;
        }
        
        List<LeaderboardEntry> entries = cachedLeaderboards.get(leaderboardId);
        if (entries == null || entries.isEmpty()) {
            plugin.getMessageManager().sendError(player, "No data available for this leaderboard!");
            return;
        }
        
        // Send header
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW + "§l" + config.title);
        player.sendMessage("");
        
        // Send entries
        int position = 1;
        for (LeaderboardEntry entry : entries) {
            String formatted = config.format
                .replace("%position%", String.valueOf(position))
                .replace("%player%", entry.playerName)
                .replace("%value%", formatValue(entry.value, config));
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', formatted));
            position++;
        }
        
        // Send player's position if not in top
        OfflinePlayer offlinePlayer = player;
        double playerValue = getValue(offlinePlayer, config);
        boolean inTop = entries.stream().anyMatch(e -> e.playerName.equals(player.getName()));
        
        if (!inTop && playerValue > 0) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "Your position: " + ChatColor.WHITE + "Not in top " + maxEntries);
            player.sendMessage(ChatColor.GRAY + "Your value: " + ChatColor.GREEN + formatValue(playerValue, config));
        }
        
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6§lLeaderboards")) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Handle close button
        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        
        // Find leaderboard by item name
        String displayName = clicked.getItemMeta().getDisplayName();
        for (LeaderboardConfig config : leaderboards.values()) {
            if (displayName.contains(config.title)) {
                player.closeInventory();
                showLeaderboard(player, config.id);
                break;
            }
        }
    }
    
    private ItemStack createLeaderboardItem(LeaderboardConfig config) {
        Material material;
        try {
            material = Material.valueOf(config.icon.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.PAPER;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e§l" + config.title);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Click to view this leaderboard");
        lore.add("");
        
        List<LeaderboardEntry> entries = cachedLeaderboards.get(config.id);
        if (entries != null && !entries.isEmpty()) {
            lore.add("§6Top 3:");
            for (int i = 0; i < Math.min(3, entries.size()); i++) {
                LeaderboardEntry entry = entries.get(i);
                lore.add("§e" + (i + 1) + ". §f" + entry.playerName + " §7- §a" + 
                    formatValue(entry.value, config));
            }
        } else {
            lore.add("§cNo data available");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private void updateDiscordLeaderboard(LeaderboardConfig config, List<LeaderboardEntry> entries) {
        TextChannel channel = getLeaderboardChannel();
        if (channel == null) return;
        
        Color embedColor;
        try {
            embedColor = Color.decode(config.discordColor);
        } catch (NumberFormatException e) {
            embedColor = Color.YELLOW;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(embedColor)
            .setTitle("🏆 " + config.title)
            .setTimestamp(Instant.now())
            .setFooter("Updates every " + updateInterval + " seconds", null);
        
        if (entries.isEmpty()) {
            embed.setDescription("No data available");
        } else {
            StringBuilder description = new StringBuilder();
            
            for (int i = 0; i < entries.size(); i++) {
                LeaderboardEntry entry = entries.get(i);
                String medal = getMedal(i + 1);
                
                description.append(medal).append(" **")
                    .append(entry.playerName).append("** - ")
                    .append(formatValue(entry.value, config)).append("\n");
            }
            
            embed.setDescription(description.toString());
        }
        
        // Update or create message
        Message existingMessage = discordMessages.get(config.id);
        if (existingMessage != null) {
            existingMessage.editMessageEmbeds(embed.build()).queue(
                success -> {},
                error -> {
                    // Message was deleted, create new one
                    discordMessages.remove(config.id);
                    createNewDiscordMessage(config, embed);
                }
            );
        } else {
            createNewDiscordMessage(config, embed);
        }
    }
    
    private void createNewDiscordMessage(LeaderboardConfig config, EmbedBuilder embed) {
        TextChannel channel = getLeaderboardChannel();
        if (channel == null) return;
        
        channel.sendMessageEmbeds(embed.build()).queue(message -> {
            discordMessages.put(config.id, message);
        });
    }
    
    private String getMedal(int position) {
        switch (position) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "**" + position + ".**";
        }
    }
    
    private String formatValue(double value, LeaderboardConfig config) {
        if (config.type.equals("statistic") && config.value.equals("PLAY_ONE_MINUTE")) {
            // Convert ticks to hours
            long hours = (long) (value / 20 / 60 / 60);
            long minutes = (long) ((value / 20 / 60) % 60);
            return hours + "h " + minutes + "m";
        } else if (config.value.contains("balance") || config.value.contains("money")) {
            return String.format("%.2f", value);
        } else {
            return String.valueOf((int) value);
        }
    }
    
    private TextChannel getLeaderboardChannel() {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        return plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(leaderboardChannel, true)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Force update all leaderboards
     */
    public void forceUpdate() {
        updateAllLeaderboards();
    }
    
    /**
     * Get leaderboard data
     */
    public List<LeaderboardEntry> getLeaderboard(String leaderboardId) {
        return cachedLeaderboards.getOrDefault(leaderboardId, new ArrayList<>());
    }
    
    /**
     * Leaderboard configuration
     */
    private static class LeaderboardConfig {
        final String id;
        final String title;
        final String type;
        final String value;
        final String format;
        boolean enabled;
        final String icon;
        final String discordColor;
        
        LeaderboardConfig(String id, String title, String type, String value, 
                         String format, boolean enabled, String icon, String discordColor) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.value = value;
            this.format = format;
            this.enabled = enabled;
            this.icon = icon;
            this.discordColor = discordColor;
        }
    }
    
    /**
     * Leaderboard entry
     */
    public static class LeaderboardEntry {
        public final String playerName;
        public final UUID playerUuid;
        public final double value;
        
        LeaderboardEntry(String playerName, UUID playerUuid, double value) {
            this.playerName = playerName;
            this.playerUuid = playerUuid;
            this.value = value;
        }
    }
}
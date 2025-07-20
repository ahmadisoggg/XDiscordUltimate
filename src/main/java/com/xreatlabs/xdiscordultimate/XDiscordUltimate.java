package com.xreatlabs.xdiscordultimate;

import com.xreatlabs.xdiscordultimate.commands.*;
import com.xreatlabs.xdiscordultimate.listeners.*;
import com.xreatlabs.xdiscordultimate.modules.ModuleManager;
import com.xreatlabs.xdiscordultimate.utils.*;
import com.xreatlabs.xdiscordultimate.database.DatabaseManager;
import com.xreatlabs.xdiscordultimate.discord.DiscordManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class XDiscordUltimate extends JavaPlugin {
    
    private static XDiscordUltimate instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private AdminUtils adminUtils;
    private EmbedUtils embedUtils;
    private DatabaseManager databaseManager;
    private ModuleManager moduleManager;
    private DiscordManager discordManager;
    private Object luckPerms;
    private boolean placeholderAPIEnabled;
    private boolean luckPermsEnabled;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("XDiscordUltimate starting up...");
        
        // Load runtime dependencies first
        try {
            LibraryManager libraryManager = new LibraryManager(this);
            libraryManager.loadAllLibraries();
        } catch (Exception e) {
            getLogger().severe("Failed to load dependencies! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize configuration
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        
        // Initialize utilities
        adminUtils = new AdminUtils(this);
        embedUtils = new EmbedUtils(this);
        
        // Check optional dependencies
        checkOptionalDependencies();
        
        // Initialize Discord manager
        discordManager = new DiscordManager(this);
        discordManager.initialize().thenAccept(success -> {
            if (!success) {
                getLogger().severe("Failed to initialize Discord bot!");
            }
        });
        
        // Initialize database
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize module manager
        moduleManager = new ModuleManager(this);
        moduleManager.loadModules();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Discord events are now handled by DiscordListener
        
        // Start metrics
        if (getConfig().getBoolean("advanced.metrics.enabled", true)) {
            int pluginId = getConfig().getInt("advanced.metrics.bstats-id", 12345);
            new Metrics(this, pluginId);
        }
        
        getLogger().info("XDiscordUltimate v" + getDescription().getVersion() + " has been enabled!");
        
        // Check for updates
        if (getConfig().getBoolean("general.check-updates", true)) {
            new UpdateChecker(this).checkForUpdates();
        }
    }
    
    @Override
    public void onDisable() {
        // Shutdown Discord bot
        if (discordManager != null) {
            discordManager.shutdown();
        }
        
        // Disable modules
        if (moduleManager != null) {
            moduleManager.disableModules();
        }
        
        // Close database
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("XDiscordUltimate has been disabled!");
    }
    
    
    private void registerCommands() {
        // Register main command
        getCommand("xdiscord").setExecutor(new XDiscordCommand(this));
        
        // Register feature commands
        getCommand("verify").setExecutor(new VerifyCommand(this));
        getCommand("support").setExecutor(new SupportCommand(this));
        getCommand("embed").setExecutor(new EmbedCommand(this));
        getCommand("announce").setExecutor(new AnnounceCommand(this));
        getCommand("discordconsole").setExecutor(new DiscordConsoleCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
    }
    
    private void registerListeners() {
        // Register Bukkit listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
    }
    
    
    public void reload() {
        reloadConfig();
        configManager.reload();
        messageManager.reload();
        moduleManager.reloadModules();
        getLogger().info("Configuration reloaded!");
    }
    
    private void checkOptionalDependencies() {
        // Check for LuckPerms (optional)
        try {
            if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(luckPermsClass);
                if (provider != null) {
                    luckPerms = provider.getProvider();
                    luckPermsEnabled = true;
                    getLogger().info("LuckPerms found and hooked!");
                }
            }
        } catch (ClassNotFoundException e) {
            // LuckPerms not available
        }
        
        if (!luckPermsEnabled) {
            getLogger().warning("LuckPerms not found. Some features will be disabled.");
        }
        
        // Check for PlaceholderAPI (optional)
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI found and hooked!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Some features will be disabled.");
        }
    }
    
    // Getters
    public static XDiscordUltimate getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public AdminUtils getAdminUtils() {
        return adminUtils;
    }
    
    public EmbedUtils getEmbedUtils() {
        return embedUtils;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    public DiscordManager getDiscordManager() {
        return discordManager;
    }
    
    public Object getLuckPerms() {
        return luckPerms;
    }
    
    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled;
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
    
    public String parsePlaceholders(String text, org.bukkit.entity.Player player) {
        if (placeholderAPIEnabled && player != null) {
            try {
                Class<?> placeholderAPIClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                java.lang.reflect.Method setPlaceholders = placeholderAPIClass.getMethod("setPlaceholders", org.bukkit.entity.Player.class, String.class);
                return (String) setPlaceholders.invoke(null, player, text);
            } catch (Exception e) {
                // PlaceholderAPI not available or error occurred
            }
        }
        return text;
    }
}
package com.xreatlabs.xdiscordultimate;

import com.xreatlabs.xdiscordultimate.commands.*;
import com.xreatlabs.xdiscordultimate.listeners.*;
import com.xreatlabs.xdiscordultimate.modules.Module;
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
    private HelpGUI helpGUI;
    private DropdownManager dropdownManager;
    private ConsoleAppender consoleAppender;
    private long startTime;
    private TPSManager tpsManager;
    private StatusMessageManager statusMessageManager;
    private com.xreatlabs.xdiscordultimate.network.NetworkManager networkManager;
    private PlaytimeTracker playtimeTracker;

    private Object luckPerms;
    private boolean luckPermsEnabled;
    private boolean placeholderAPIEnabled;
    
    @Override
    public void onEnable() {
        instance = this;
        startTime = System.currentTimeMillis();
        
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
        dropdownManager = new DropdownManager(this);
        tpsManager = new TPSManager(this);
        getServer().getScheduler().runTaskTimer(this, tpsManager, 0L, 1L);
        statusMessageManager = new StatusMessageManager(this);
        playtimeTracker = new PlaytimeTracker(this);
        
        // Check optional dependencies
        checkOptionalDependencies();
        
        // Initialize database
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize Discord manager
        discordManager = new DiscordManager(this);
        discordManager.initialize().thenAccept(success -> {
            if (success) {
                getLogger().info("Discord bot initialized successfully. Loading modules...");

                // Initialize module manager
                moduleManager = new ModuleManager(this);
                moduleManager.loadModules();
                
                // Initialize help GUI
                helpGUI = new HelpGUI(this);
                
                // Register commands
                registerCommands();
                
                // Register listeners
                registerListeners();
                
                // Attach console appender now that the bot is ready
                if (getConfig().getBoolean("discord-console.enabled", true)) {
                    consoleAppender = new ConsoleAppender(this);
                    consoleAppender.start();
                    org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager.getRootLogger();
                    rootLogger.addAppender(consoleAppender);
                }

                // Initialize network sync after listeners
                initializeNetwork();

                // Start metrics
                if (getConfig().getBoolean("advanced.metrics.enabled", true)) {
                    int pluginId = getConfig().getInt("advanced.metrics.bstats-id", 12345);
                    new Metrics(this, pluginId);
                }
                
                // Log server startup
                if (moduleManager != null) {
                    Module loggingModule = moduleManager.getModule("server-logging");
                    if (loggingModule != null && loggingModule.isEnabled()) {
                        ((com.xreatlabs.xdiscordultimate.modules.logging.ServerLoggingModule) loggingModule).logServerStartup();
                    }
                }
                
                getLogger().info("XDiscordUltimate v" + getDescription().getVersion() + " has been enabled!");
                
                // Check for updates
                if (getConfig().getBoolean("general.check-updates", true)) {
                    new UpdateChecker(this).checkForUpdates();
                }

                // Start activity updater
                getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                    if (discordManager.isReady()) {
                        discordManager.updateActivity();
                    }
                }, 20L * 10, 20L * 15); // Start after 10s, repeat every 15s

                // Start status message manager
                if (getConfig().getBoolean("features.server-status.enabled", true)) {
                    statusMessageManager.start();
                }

            } else {
                getLogger().severe("Failed to initialize Discord bot! Most features will be disabled.");
            }
        });
    }
    
    @Override
    public void onDisable() {
        // Shutdown network
        if (networkManager != null) {
            networkManager.shutdown();
        }
        // Detach console appender
        if (consoleAppender != null) {
            consoleAppender.stop();
            org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager.getRootLogger();
            rootLogger.removeAppender(consoleAppender);
        }

        // Log server shutdown
        if (moduleManager != null) {
            Module loggingModule = moduleManager.getModule("server-logging");
            if (loggingModule != null && loggingModule.isEnabled()) {
                ((com.xreatlabs.xdiscordultimate.modules.logging.ServerLoggingModule) loggingModule).logServerShutdown();
            }
        }
        
        // Cleanup help GUI
        if (helpGUI != null) {
            helpGUI.cleanup();
        }
        
        // Disable modules
        if (moduleManager != null) {
            moduleManager.disableModules();
        }
        
        // Shutdown Discord bot
        if (discordManager != null) {
            discordManager.shutdown();
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
        getCommand("help").setExecutor(new HelpCommand(this));
        getCommand("msg").setExecutor(new MessageCommand(this));
        getCommand("statusgraph").setExecutor(new StatusGraphCommand(this));
        getCommand("playtime").setExecutor(new PlaytimeCommand(this));
    }
    
    private void registerListeners() {
        // Register Bukkit listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
        // Network incoming packets listener
        getServer().getPluginManager().registerEvents(new com.xreatlabs.xdiscordultimate.network.NetworkPacketListener(this), this);
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
    
    public HelpGUI getHelpGUI() {
        return helpGUI;
    }

    public DropdownManager getDropdownManager() {
        return dropdownManager;
    }

    public TPSManager getTpsManager() {
        return tpsManager;
    }

    public long getStartTime() {
        return startTime;
    }

    public StatusMessageManager getStatusMessageManager() {
        return statusMessageManager;
    }

    public com.xreatlabs.xdiscordultimate.network.NetworkManager getNetworkManager() {
        return networkManager;
    }

    public PlaytimeTracker getPlaytimeTracker() {
        return playtimeTracker;
    }

    private void initializeNetwork() {
        try {
            org.bukkit.configuration.ConfigurationSection section = getConfig().getConfigurationSection("network");
            if (section == null) {
                getLogger().info("No network section in config.yml; skipping network sync");
                return;
            }
            
            // Auto-generate key if missing
            String key = section.getString("key", "");
            if (key == null || key.isEmpty()) {
                String newKey = java.util.UUID.randomUUID().toString();
                getConfig().set("network.key", newKey);
                saveConfig();
                getLogger().info("[XDiscordUltimate] Network key generated automatically for proxy instance.");
            }
            
            com.xreatlabs.xdiscordultimate.network.NetworkConfig cfg = new com.xreatlabs.xdiscordultimate.network.NetworkConfig(getConfig().getConfigurationSection("network"));
            networkManager = new com.xreatlabs.xdiscordultimate.network.NetworkManager(this);
            networkManager.initialize(cfg);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize network sync", e);
        }
    }
}
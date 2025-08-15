package com.xreatlabs.xdiscordultimate.modules;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Base interface for all XDiscordUltimate modules
 */
public abstract class Module {
    
    protected final XDiscordUltimate plugin;
    private boolean enabled = false;
    private String moduleKey;
    
    public Module(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Get the module name
     */
    public abstract String getName();
    
    /**
     * Get the module description
     */
    public abstract String getDescription();
    
    /**
     * Enable the module
     */
    public void enable(String moduleKey) {
        if (enabled) {
            return;
        }
        
        this.moduleKey = moduleKey;
        
        try {
            onEnable();
            enabled = true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to enable module " + getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Disable the module
     */
    public void disable() {
        if (!enabled) {
            return;
        }
        
        try {
            onDisable();
            enabled = false;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to disable module " + getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Called when the module is enabled
     */
    protected abstract void onEnable();
    
    /**
     * Called when the module is disabled
     */
    protected abstract void onDisable();
    
    /**
     * Called when Discord is ready
     */
    public void onDiscordReady() {
        // Override in modules that need Discord functionality
    }
    
    /**
     * Reload the module configuration
     */
    public void reload() {
        if (enabled) {
            disable();
            enable(this.moduleKey);
        }
    }
    
    /**
     * Check if the module is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Get the module's configuration section
     */
    protected ConfigurationSection getConfig() {
        return plugin.getConfigManager().getFeatureConfig(this.moduleKey);
    }
    
    /**
     * Check if a sub-feature is enabled
     */
    protected boolean isSubFeatureEnabled(String subFeature) {
        String configPath = getName().toLowerCase().replace(" ", "-");
        return plugin.getConfigManager().isSubFeatureEnabled(configPath, subFeature);
    }
    
    /**
     * Log a debug message
     */
    protected void debug(String message) {
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("[" + getName() + "] " + message);
        }
    }
    
    /**
     * Log an info message
     */
    protected void info(String message) {
        plugin.getLogger().info("[" + getName() + "] " + message);
    }
    
    /**
     * Log a warning message
     */
    protected void warning(String message) {
        plugin.getLogger().warning("[" + getName() + "] " + message);
    }
    
    /**
     * Log an error message
     */
    protected void error(String message) {
        plugin.getLogger().severe("[" + getName() + "] " + message);
    }
    
    /**
     * Log an error message with exception
     */
    protected void error(String message, Throwable throwable) {
        plugin.getLogger().severe("[" + getName() + "] " + message);
        throwable.printStackTrace();
    }
    
    /**
     * Check if a message starts with a command prefix (! or /)
     * @param message The message to check
     * @return The message without the prefix, or null if no valid prefix
     */
    protected String getCommandFromMessage(String message) {
        if (message.startsWith("!")) {
            return message.substring(1);
        } else if (message.startsWith("/")) {
            return message.substring(1);
        }
        return null;
    }
    
    /**
     * Check if a message is a command (starts with ! or /)
     * @param message The message to check
     * @return true if the message is a command
     */
    protected boolean isCommand(String message) {
        return message.startsWith("!") || message.startsWith("/");
    }
}
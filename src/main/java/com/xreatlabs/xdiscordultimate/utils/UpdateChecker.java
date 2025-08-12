package com.xreatlabs.xdiscordultimate.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class UpdateChecker {
    
    private final XDiscordUltimate plugin;
    private final String currentVersion;
    private final OkHttpClient httpClient;
    
    // GitHub API URL for releases
    private static final String GITHUB_API_URL = "https://api.github.com/repos/xreatlabs/xdiscordultimate/releases/latest";
    // Spigot resource ID (change this to your actual resource ID when published)
    private static final int SPIGOT_RESOURCE_ID = 12345;
    private static final String SPIGOT_API_URL = "https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOT_RESOURCE_ID;
    
    public UpdateChecker(XDiscordUltimate plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.httpClient = new OkHttpClient();
    }
    
    /**
     * Check for updates asynchronously
     */
    public void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                String latestVersion = getLatestVersion();
                
                if (latestVersion != null && isNewerVersion(latestVersion)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLogger().warning("========================================");
                        plugin.getLogger().warning("A new version of XDiscordUltimate is available!");
                        plugin.getLogger().warning("Current version: " + currentVersion);
                        plugin.getLogger().warning("Latest version: " + latestVersion);
                        plugin.getLogger().warning("Download at: https://github.com/xreatlabs/xdiscordultimate/releases");
                        plugin.getLogger().warning("========================================");
                        
                        // Notify admins in-game
                        Bukkit.getOnlinePlayers().stream()
                            .filter(player -> player.hasPermission("xdiscord.admin"))
                            .forEach(player -> {
                                plugin.getMessageManager().sendWarning(player, 
                                    "A new version of XDiscordUltimate is available: v" + latestVersion);
                            });
                    });
                } else {
                    plugin.getLogger().info("XDiscordUltimate is up to date!");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
            }
        });
    }
    
    /**
     * Get the latest version from GitHub
     */
    private String getLatestVersion() {
        try {
            Request request = new Request.Builder()
                .url(GITHUB_API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                    String tagName = jsonObject.get("tag_name").getAsString();
                    
                    // Remove 'v' prefix if present
                    if (tagName.startsWith("v")) {
                        tagName = tagName.substring(1);
                    }
                    
                    return tagName;
                }
            }
        } catch (IOException e) {
            // Try Spigot API as fallback
            return getLatestVersionFromSpigot();
        }
        
        return null;
    }
    
    /**
     * Get the latest version from Spigot (fallback)
     */
    private String getLatestVersionFromSpigot() {
        try {
            Request request = new Request.Builder()
                .url(SPIGOT_API_URL)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string().trim();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check Spigot for updates", e);
        }
        
        return null;
    }
    
    /**
     * Check if a version is newer than the current version
     */
    private boolean isNewerVersion(String latestVersion) {
        try {
            Version current = new Version(currentVersion);
            Version latest = new Version(latestVersion);
            return latest.compareTo(current) > 0;
        } catch (Exception e) {
            // If version parsing fails, do simple string comparison
            return !currentVersion.equals(latestVersion);
        }
    }
    
    /**
     * Version comparison class
     */
    private static class Version implements Comparable<Version> {
        private final int major;
        private final int minor;
        private final int patch;
        
        public Version(String version) {
            String[] parts = version.split("\\.");
            this.major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            this.minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            this.patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        }
        
        @Override
        public int compareTo(Version other) {
            if (this.major != other.major) {
                return Integer.compare(this.major, other.major);
            }
            if (this.minor != other.minor) {
                return Integer.compare(this.minor, other.minor);
            }
            return Integer.compare(this.patch, other.patch);
        }
    }
}
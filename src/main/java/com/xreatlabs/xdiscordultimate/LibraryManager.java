package com.xreatlabs.xdiscordultimate;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.logging.LogLevel;
import org.bukkit.plugin.java.JavaPlugin;

public class LibraryManager {
    
    private final JavaPlugin plugin;
    private final BukkitLibraryManager libraryManager;
    
    public LibraryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.libraryManager = new BukkitLibraryManager(plugin);
        this.libraryManager.addMavenCentral();
        this.libraryManager.setLogLevel(LogLevel.INFO);
    }
    
    public void loadAllLibraries() {
        plugin.getLogger().info("Loading runtime dependencies with Libby...");
        
        try {
            // Kotlin runtime (required by JDA)
            loadLibrary("org.jetbrains.kotlin", "kotlin-stdlib", "1.8.10");
            loadLibrary("org.jetbrains.kotlin", "kotlin-stdlib-common", "1.8.10");
            loadLibrary("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.8.10");
            
            // JDA (Discord API)
            loadLibrary("net.dv8tion", "JDA", "5.0.0-beta.18");
            
            // Okio (required by OkHttp3) - using okio-jvm for OkHttp 4.10.0 compatibility
            loadLibrary("com.squareup.okio", "okio-jvm", "3.0.0");
            
            // OkHttp3 (HTTP client)
            loadLibrary("com.squareup.okhttp3", "okhttp", "4.10.0");
            
            // Gson (JSON parsing)
            loadLibrary("com.google.code.gson", "gson", "2.10.1");
            
            // HikariCP (Database connection pooling)
            loadLibrary("com.zaxxer", "HikariCP", "5.0.1");
            
            // Database drivers
            loadLibrary("org.xerial", "sqlite-jdbc", "3.42.0.0");
            loadLibrary("com.mysql", "mysql-connector-j", "8.0.33");
            loadLibrary("org.postgresql", "postgresql", "42.6.0");
            
            // Logging
            loadLibrary("org.slf4j", "slf4j-api", "2.0.9");
            loadLibrary("ch.qos.logback", "logback-classic", "1.4.11");
            loadLibrary("org.apache.logging.log4j", "log4j-core", "2.17.1");
            loadLibrary("org.apache.logging.log4j", "log4j-api", "2.17.1");
            
            plugin.getLogger().info("All runtime dependencies loaded successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load runtime dependencies: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Critical dependency loading failure", e);
        }
    }
    
    private void loadLibrary(String groupId, String artifactId, String version) {
        plugin.getLogger().info("Loading " + groupId + ":" + artifactId + ":" + version);
        
        Library library = Library.builder()
            .groupId(groupId)
            .artifactId(artifactId)
            .version(version)
            .build();
        
        libraryManager.loadLibrary(library);
    }
}
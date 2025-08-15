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
            // GNU Trove (required by JDA)
            loadLibrary("net.sf.trove4j", "trove4j", "3.0.3");
            
            // Apache Commons Collections (required by JDA)
            loadLibrary("org.apache.commons", "commons-collections4", "4.4");
            
            // WebSocket client (required by JDA)
            loadLibrary("com.neovisionaries", "nv-websocket-client", "2.14");
            
            // Jackson (JSON processing, required by JDA)
            loadLibrary("com.fasterxml.jackson.core", "jackson-core", "2.15.2");
            loadLibrary("com.fasterxml.jackson.core", "jackson-databind", "2.15.2");
            loadLibrary("com.fasterxml.jackson.core", "jackson-annotations", "2.15.2");
            
            // Kotlin runtime (required by JDA)
            loadLibrary("org.jetbrains.kotlin", "kotlin-stdlib", "1.8.10");
            loadLibrary("org.jetbrains.kotlin", "kotlin-stdlib-common", "1.8.10");
            loadLibrary("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.8.10");
            
            // Okio (required by OkHttp3) - using okio-jvm for OkHttp 4.10.0 compatibility
            loadLibrary("com.squareup.okio", "okio-jvm", "3.0.0");
            
            // OkHttp3 (HTTP client)
            loadLibrary("com.squareup.okhttp3", "okhttp", "4.10.0");
            
            // JDA (Discord API) - Load after dependencies
            loadLibrary("net.dv8tion", "JDA", "5.0.0-beta.18");
            
            // Gson (JSON parsing)
            loadLibrary("com.google.code.gson", "gson", "2.10.1");

            // Netty (for TCP networking)
            loadLibrary("io.netty", "netty-transport", "4.1.110.Final");
            loadLibrary("io.netty", "netty-buffer", "4.1.110.Final");
            loadLibrary("io.netty", "netty-common", "4.1.110.Final");
            loadLibrary("io.netty", "netty-codec", "4.1.110.Final");
            loadLibrary("io.netty", "netty-handler", "4.1.110.Final");
            loadLibrary("io.netty", "netty-resolver", "4.1.110.Final");
            loadLibrary("io.netty", "netty-transport-native-epoll", "4.1.110.Final");
            
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
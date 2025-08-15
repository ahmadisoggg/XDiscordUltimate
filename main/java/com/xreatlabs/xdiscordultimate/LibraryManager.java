package com.xreatlabs.xdiscordultimate;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class LibraryManager {
    
    private final JavaPlugin plugin;
    private final File libsFolder;
    private final List<URL> loadedUrls = new ArrayList<>();
    
    public LibraryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.libsFolder = new File(plugin.getDataFolder(), "libs");
        if (!libsFolder.exists()) {
            libsFolder.mkdirs();
        }
    }
    
    public void loadAllLibraries() {
        plugin.getLogger().info("Loading runtime dependencies...");
        
        try {
            // Core dependencies that are essential
            loadLibrary("JDA", "https://repo1.maven.org/maven2/net/dv8tion/JDA/5.0.0-beta.20/JDA-5.0.0-beta.20.jar");
            loadLibrary("gson", "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar");
            loadLibrary("hikaricp", "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar");
            loadLibrary("slf4j-api", "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar");
            loadLibrary("log4j-core", "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar");
            loadLibrary("okhttp", "https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.11.0/okhttp-4.11.0.jar");
            
            // Add loaded URLs to classpath
            addToClasspath();
            
            plugin.getLogger().info("All runtime dependencies loaded successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load runtime dependencies: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Critical dependency loading failure", e);
        }
    }
    
    private void loadLibrary(String name, String url) throws IOException {
        File libFile = new File(libsFolder, name + ".jar");
        
        if (!libFile.exists()) {
            plugin.getLogger().info("Downloading " + name + " from " + url);
            downloadFile(url, libFile);
        } else {
            plugin.getLogger().info("Using cached " + name + ".jar");
        }
        
        loadedUrls.add(libFile.toURI().toURL());
    }
    
    private void downloadFile(String urlString, File destination) throws IOException {
        URL url = new URL(urlString);
        
        try (InputStream in = url.openStream();
             ReadableByteChannel rbc = Channels.newChannel(in);
             FileOutputStream fos = new FileOutputStream(destination)) {
            
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }
    
    private void addToClasspath() {
        try {
            // Get the current class loader
            ClassLoader currentClassLoader = plugin.getClass().getClassLoader();
            
            if (currentClassLoader instanceof URLClassLoader) {
                // Use reflection to add URLs to the classpath
                URLClassLoader urlClassLoader = (URLClassLoader) currentClassLoader;
                
                // Get the addURL method
                java.lang.reflect.Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURLMethod.setAccessible(true);
                
                // Add each library URL to the classpath
                for (URL url : loadedUrls) {
                    addURLMethod.invoke(urlClassLoader, url);
                }
                
                plugin.getLogger().info("Added " + loadedUrls.size() + " libraries to classpath");
            } else {
                plugin.getLogger().warning("Cannot add libraries to classpath - unsupported class loader type");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to add libraries to classpath: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public File getLibsFolder() {
        return libsFolder;
    }
    
    public List<URL> getLoadedUrls() {
        return new ArrayList<>(loadedUrls);
    }
}
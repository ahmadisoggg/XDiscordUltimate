package com.xreatlabs.xdiscordultimate.network;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SecurityLogger {
    private final File file;

    public SecurityLogger(XDiscordUltimate plugin) {
        this.file = new File(plugin.getDataFolder(), "security.log");
    }

    public synchronized void log(String ip, String reason) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = String.format("[%s] %s - %s%n", timestamp, ip, reason);
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(line);
        } catch (IOException ignored) { }
    }
}

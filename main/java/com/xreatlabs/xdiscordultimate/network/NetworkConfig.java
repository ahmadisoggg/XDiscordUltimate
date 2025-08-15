package com.xreatlabs.xdiscordultimate.network;

import org.bukkit.configuration.ConfigurationSection;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkConfig {
    public final boolean enabled;
    public final String key;
    public final int port;
    public final boolean encryption;
    public final Set<String> allowedIps;
    public final List<RemoteServer> servers;
    public final String serverId;

    public static class RemoteServer {
        public final String id;
        public final String host;
        public final int port;

        public RemoteServer(String id, String host, int port) {
            this.id = id;
            this.host = host;
            this.port = port;
        }

        public InetSocketAddress toAddress() {
            return new InetSocketAddress(host, port);
        }
    }

    public NetworkConfig(ConfigurationSection section) {
        this.enabled = section.getBoolean("enabled", false);
        this.key = section.getString("key", "");
        this.port = section.getInt("port", 25101);
        this.encryption = section.getBoolean("encryption", true);
        this.serverId = section.getString("server_id", "server");

        this.allowedIps = new HashSet<>(section.getStringList("allowed_ips"));

        this.servers = new ArrayList<>();
        List<?> entries = section.getList("servers");
        if (entries != null) {
            for (Object item : entries) {
                if (item instanceof ConfigurationSection) {
                    ConfigurationSection s = (ConfigurationSection) item;
                    String id = s.getString("id", "");
                    String address = s.getString("address", "");
                    if (id.isEmpty() || address.isEmpty()) continue;
                    String[] parts = address.split(":");
                    if (parts.length != 2) continue;
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    servers.add(new RemoteServer(id, host, port));
                }
            }
        }
    }
}

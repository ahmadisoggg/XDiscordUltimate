package com.xreatlabs.xdiscordultimate.network;

import com.google.gson.Gson;
import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.network.crypto.AesGcm;
import com.xreatlabs.xdiscordultimate.network.packet.NetworkPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class NetworkManager {
    private final XDiscordUltimate plugin;
    private final Logger logger;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final EventLoopGroup clientGroup = new NioEventLoopGroup();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, Channel> serverIdToChannel = new ConcurrentHashMap<>();
    private final Map<Channel, String> channelToServerId = new ConcurrentHashMap<>();

    private NetworkConfig config;
    private AesGcm aes;

    public NetworkManager(XDiscordUltimate plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public Logger getLogger() { return logger; }

    public void initialize(NetworkConfig config) {
        this.config = config;
        SecretKey key = AesGcm.deriveKeyFromUuid(config.key);
        this.aes = new AesGcm(key);

        if (!config.enabled) {
            logger.info("Network sync disabled");
            return;
        }

        startServer(config.port);
        scheduleOutgoingConnections();
    }

    public void shutdown() {
        try { bossGroup.shutdownGracefully(); } catch (Exception ignored) {}
        try { workerGroup.shutdownGracefully(); } catch (Exception ignored) {}
        try { clientGroup.shutdownGracefully(); } catch (Exception ignored) {}
        scheduler.shutdownNow();
        serverIdToChannel.clear();
        channelToServerId.clear();
    }

    private void startServer(int port) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new NetChannelInitializer(this, true))
            .childOption(ChannelOption.SO_KEEPALIVE, true);
        b.bind(new InetSocketAddress(port)).addListener(f -> {
            if (f.isSuccess()) {
                logger.info("Network listener started on port " + port);
            } else {
                logger.severe("Failed to bind network port " + port + ": " + f.cause());
            }
        });
    }

    private void scheduleOutgoingConnections() {
        scheduler.scheduleWithFixedDelay(this::connectAll, 2, 10, TimeUnit.SECONDS);
    }

    private void connectAll() {
        for (NetworkConfig.RemoteServer rs : config.servers) {
            if (serverIdToChannel.containsKey(rs.id)) continue;
            connectTo(rs);
        }
    }

    private void connectTo(NetworkConfig.RemoteServer rs) {
        Bootstrap b = new Bootstrap();
        b.group(clientGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new NetChannelInitializer(this, false));

        b.connect(rs.host, rs.port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Channel ch = future.channel();
                logger.info("Connected to server " + rs.id + " at " + rs.host + ":" + rs.port);
                // send handshake
                Map<String, Object> payload = new HashMap<>();
                payload.put("key", config.key);
                payload.put("server_id", config.serverId);
                payload.put("time", Instant.now().toEpochMilli());
                send(ch, PacketBuilder.handshake(config.serverId, payload));
            } else {
                logger.warning("Failed to connect to " + rs.id + ": " + future.cause().getMessage());
            }
        });
    }

    public void onChannelActive(Channel channel) {
        // server side will wait for handshake; client side already sent one in connectTo
    }

    public void onChannelInactive(Channel channel) {
        String serverId = channelToServerId.remove(channel);
        if (serverId != null) {
            serverIdToChannel.remove(serverId);
            logger.info("Disconnected from server " + serverId);
        }
    }

    public void handlePacket(Channel channel, NetworkPacket packet) {
        switch (packet.type) {
            case HANDSHAKE:
                if (packet.payload instanceof Map) {
                    // unchecked cast is fine for our own serialization
                    @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) packet.payload;
                    acceptHandshake(channel, map);
                } else {
                    logger.warning("Handshake payload invalid");
                    channel.close();
                }
                break;
            case HANDSHAKE_OK:
                // incoming handshake OK after we sent handshake
                if (packet.serverId != null) {
                    serverIdToChannel.put(packet.serverId, channel);
                    channelToServerId.put(channel, packet.serverId);
                }
                break;
            case CHAT:
            case DISCORD_CHAT:
            case COMMAND:
            case EVENT_JOIN:
            case EVENT_LEAVE:
            case EVENT_DEATH:
            case PLAYER_LIST_SNAPSHOT:
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new RemotePacketEvent(packet)));
                break;
            case PLAYER_LIST_REQUEST:
                // respond with local snapshot
                java.util.List<String> players = new java.util.ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(p -> players.add(p.getName()));
                send(channel, PacketBuilder.playerList(config.serverId, players));
                break;
            default:
                break;
        }
    }

    public void acceptHandshake(Channel channel, Map<String, Object> payload) {
        Object keyObj = payload.get("key");
        Object idObj = payload.get("server_id");
        if (keyObj == null || idObj == null) {
            logger.warning("Handshake missing key or server_id; closing");
            channel.close();
            return;
        }
        String key = String.valueOf(keyObj);
        String serverId = String.valueOf(idObj);
        if (!config.key.equals(key)) {
            logger.warning("Rejected connection from serverId=" + serverId + ": invalid key");
            channel.close();
            return;
        }
        serverIdToChannel.put(serverId, channel);
        channelToServerId.put(channel, serverId);
        send(channel, PacketBuilder.handshakeOk(config.serverId));
    }

    public void broadcastExcept(String excludeServerId, NetworkPacket packet) {
        for (Map.Entry<String, Channel> e : serverIdToChannel.entrySet()) {
            if (excludeServerId != null && excludeServerId.equals(e.getKey())) continue;
            send(e.getValue(), packet);
        }
    }

    public void sendTo(String serverId, NetworkPacket packet) {
        Channel ch = serverIdToChannel.get(serverId);
        if (ch != null) send(ch, packet);
    }

    public void send(NetworkPacket packet) {
        broadcastExcept(null, packet);
    }

    public void send(Channel ch, NetworkPacket packet) {
        try {
            String json = NetworkPacket.toJson(packet);
            String enc = encrypt(json);
            ch.writeAndFlush(enc);
        } catch (Exception e) {
            logger.warning("Failed to send packet: " + e.getMessage());
        }
    }

    public String encrypt(String json) {
        if (!config.encryption) return json;
        return Base64.getEncoder().encodeToString(aes.encrypt(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    public String decrypt(String data) {
        if (!config.encryption) return data;
        byte[] plain = aes.decrypt(Base64.getDecoder().decode(data));
        return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
    }

    // Simple builder helpers
    public static class PacketBuilder {
        public static NetworkPacket handshake(String serverId, Map<String, Object> payload) {
            NetworkPacket p = new NetworkPacket();
            p.type = PacketType.HANDSHAKE;
            p.serverId = serverId;
            p.payload = payload;
            return p;
        }
        public static NetworkPacket handshakeOk(String serverId) {
            NetworkPacket p = new NetworkPacket();
            p.type = PacketType.HANDSHAKE_OK;
            p.serverId = serverId;
            p.payload = null;
            return p;
        }
        public static NetworkPacket chat(String serverId, String player, String message) {
            Map<String, Object> map = new HashMap<>();
            map.put("player", player);
            map.put("message", message);
            NetworkPacket p = new NetworkPacket();
            p.type = PacketType.CHAT;
            p.serverId = serverId;
            p.payload = map;
            return p;
        }
        public static NetworkPacket discordChat(String serverId, String user, String message) {
            Map<String, Object> map = new HashMap<>();
            map.put("user", user);
            map.put("message", message);
            NetworkPacket p = new NetworkPacket();
            p.type = PacketType.DISCORD_CHAT;
            p.serverId = serverId;
            p.payload = map;
            return p;
        }
        public static NetworkPacket command(String serverId, String command) {
            Map<String, Object> map = new HashMap<>();
            map.put("command", command);
            NetworkPacket p = new NetworkPacket();
            p.type = PacketType.COMMAND;
            p.serverId = serverId;
            p.payload = map;
            return p;
        }
        public static NetworkPacket event(String serverId, PacketType eventType, Map<String, Object> data) {
            NetworkPacket p = new NetworkPacket();
            p.type = eventType;
            p.serverId = serverId;
            p.payload = data;
            return p;
        }
        public static NetworkPacket playerList(String serverId, List<String> players) {
            Map<String, Object> map = new HashMap<>();
            map.put("players", players);
            NetworkPacket p = new NetworkPacket();
            p.type = PacketType.PLAYER_LIST_SNAPSHOT;
            p.serverId = serverId;
            p.payload = map;
            return p;
        }
        public static NetworkPacket playerListRequest(String serverId) {
            NetworkPacket p = new NetworkPacket();
            p.type = PacketType.PLAYER_LIST_REQUEST;
            p.serverId = serverId;
            p.payload = null;
            return p;
        }
    }

    // Bukkit event fired when remote packet arrives
    public static class RemotePacketEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final NetworkPacket packet;
        public RemotePacketEvent(NetworkPacket packet) { this.packet = packet; }
        public NetworkPacket getPacket() { return packet; }
        @Override public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }
}

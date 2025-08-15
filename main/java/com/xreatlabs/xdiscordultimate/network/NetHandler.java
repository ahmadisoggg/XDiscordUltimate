package com.xreatlabs.xdiscordultimate.network;

import com.xreatlabs.xdiscordultimate.network.crypto.AesGcm;
import com.xreatlabs.xdiscordultimate.network.packet.NetworkPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

class NetHandler extends SimpleChannelInboundHandler<String> {
    private final NetworkManager manager;
    private final boolean serverSide;

    NetHandler(NetworkManager manager, boolean serverSide) {
        this.manager = manager;
        this.serverSide = serverSide;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        manager.onChannelInactive(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        try {
            String decrypted = manager.decrypt(msg);
            NetworkPacket packet = NetworkPacket.fromJson(decrypted);
            manager.handlePacket(ctx.channel(), packet);
        } catch (Exception e) {
            manager.getLogger().warning("Failed to process network packet: " + e.getMessage());
            ctx.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        manager.onChannelActive(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        manager.getLogger().warning("Network error: " + cause.getMessage());
        ctx.close();
    }
}

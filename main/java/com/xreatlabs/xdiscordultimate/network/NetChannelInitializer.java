package com.xreatlabs.xdiscordultimate.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.nio.charset.StandardCharsets;

class NetChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final NetworkManager manager;
    private final boolean server;

    NetChannelInitializer(NetworkManager manager, boolean server) {
        this.manager = manager;
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
        p.addLast(new LengthFieldPrepender(4));
        p.addLast(new StringDecoder(StandardCharsets.UTF_8));
        p.addLast(new StringEncoder(StandardCharsets.UTF_8));
        p.addLast(new NetHandler(manager, server));
    }
}

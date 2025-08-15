package com.xreatlabs.xdiscordultimate.network;

public enum PacketType {
    HANDSHAKE,
    HANDSHAKE_OK,
    CHAT,
    DISCORD_CHAT,
    COMMAND,
    EVENT_JOIN,
    EVENT_LEAVE,
    EVENT_DEATH,
    PLAYER_LIST_REQUEST,
    PLAYER_LIST_SNAPSHOT,
    HEARTBEAT
}

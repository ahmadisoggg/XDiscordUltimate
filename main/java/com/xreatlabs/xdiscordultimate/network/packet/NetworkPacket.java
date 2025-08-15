package com.xreatlabs.xdiscordultimate.network.packet;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.xreatlabs.xdiscordultimate.network.PacketType;

public class NetworkPacket {
    private static final Gson GSON = new Gson();

    @SerializedName("t")
    public PacketType type;

    @SerializedName("sid")
    public String serverId; // sender id

    @SerializedName("p")
    public Object payload;

    public static String toJson(NetworkPacket packet) {
        return GSON.toJson(packet);
    }

    public static NetworkPacket fromJson(String json) {
        return GSON.fromJson(json, NetworkPacket.class);
    }
}

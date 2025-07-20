package com.xreatlabs.xdiscordultimate.modules.events;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;

public class PlayerEventsModule extends Module {
    
    public PlayerEventsModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Player Events";
    }
    
    @Override
    public String getDescription() {
        return "Player event feeds and notifications";
    }
    
    @Override
    protected void onEnable() {
        info("Player events module enabled");
    }
    
    @Override
    protected void onDisable() {
        info("Player events module disabled");
    }
}
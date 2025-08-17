package com.xreatlabs.xdiscordultimate.modules.reactions;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;

public class EmojiReactionModule extends Module {
    
    public EmojiReactionModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Emoji Reactions";
    }
    
    @Override
    public String getDescription() {
        return "Custom emoji reactions for events";
    }
    
    @Override
    protected void onEnable() {
        info("Emoji Reactions module enabled");
    }
    
    @Override
    protected void onDisable() {
        info("Emoji Reactions module disabled");
    }
}
package com.xreatlabs.xdiscordultimate.modules.console;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import org.bukkit.command.CommandSender;

public class BotConsoleModule extends Module {
    
    public BotConsoleModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Bot Console";
    }
    
    @Override
    public String getDescription() {
        return "Discord bot console commands";
    }
    
    @Override
    protected void onEnable() {
        info("Bot Console module enabled");
    }
    
    @Override
    protected void onDisable() {
        info("Bot Console module disabled");
    }
    
    public boolean executeCommand(CommandSender sender, String command) {
        // TODO: Implement bot command execution
        return true;
    }
}
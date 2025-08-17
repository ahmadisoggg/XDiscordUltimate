package com.xreatlabs.xdiscordultimate.modules.alerts;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;

public class AdminAlertsModule extends Module {
    
    public AdminAlertsModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "Admin Alerts";
    }
    
    @Override
    public String getDescription() {
        return "Server monitoring and admin notifications";
    }
    
    @Override
    protected void onEnable() {
        info("Admin Alerts module enabled");
    }
    
    @Override
    protected void onDisable() {
        info("Admin Alerts module disabled");
    }
}
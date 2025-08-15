package com.xreatlabs.xdiscordultimate.commands;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XDiscordCommand implements CommandExecutor, TabCompleter {
    
    private final XDiscordUltimate plugin;
    
    public XDiscordCommand(XDiscordUltimate plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xdiscord.admin")) {
            plugin.getMessageManager().sendMessage(sender, "errors.no-permission");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
                
            case "status":
                handleStatus(sender);
                break;
                
            case "help":
                if (args.length > 1) {
                    showCommandHelp(sender, args[1]);
                } else {
                    showHelp(sender);
                }
                break;
                
            case "modules":
                handleModules(sender);
                break;
                
            case "debug":
                handleDebug(sender, args);
                break;
                
            case "admin":
                handleAdmin(sender, args);
                break;
            
            case "menu":
                handleMenu(sender);
                break;

            case "key":
                handleKey(sender, args);
                break;

            case "network":
                handleNetwork(sender, args);
                break;

            case "run":
                handleRun(sender, args);
                break;
                
            default:
                plugin.getMessageManager().sendError(sender, "Unknown subcommand: " + subCommand);
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getMessageManager().sendInfo(sender, "Reloading configuration...");
        
        try {
            plugin.reload();
            plugin.getMessageManager().sendMessage(sender, "general.reload-success");
        } catch (Exception e) {
            plugin.getMessageManager().sendMessage(sender, "general.reload-failed");
            e.printStackTrace();
        }
    }
    
    private void handleStatus(CommandSender sender) {
        plugin.getMessageManager().sendInfo(sender, "=== XDiscordUltimate Status ===");
        
        // Plugin version
        sender.sendMessage("§7Version: §e" + plugin.getDescription().getVersion());
        
        // Discord connection status
        boolean discordConnected = plugin.getDiscordManager() != null &&
                                 plugin.getDiscordManager().getJDA() != null &&
                                 plugin.getDiscordManager().getJDA().getStatus().name().equals("CONNECTED");
        sender.sendMessage("§7Discord: " + (discordConnected ? "§aConnected" : "§cDisconnected"));
        
        // Database status
        boolean databaseConnected = plugin.getDatabaseManager() != null;
        sender.sendMessage("§7Database: " + (databaseConnected ? "§aConnected" : "§cDisconnected"));
        
        // Module status
        Map<String, Module> modules = plugin.getModuleManager().getModules();
        long enabledModules = modules.values().stream().filter(Module::isEnabled).count();
        sender.sendMessage("§7Modules: §e" + enabledModules + "/" + modules.size() + " §7enabled");
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        sender.sendMessage("§7Memory: §e" + usedMemory + "MB / " + maxMemory + "MB");
        
        // Player stats
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        sender.sendMessage("§7Online Players: §e" + onlinePlayers);
    }
    
    private void handleModules(CommandSender sender) {
        plugin.getMessageManager().sendInfo(sender, "=== XDiscordUltimate Modules ===");
        
        Map<String, Module> modules = plugin.getModuleManager().getModules();
        
        for (Map.Entry<String, Module> entry : modules.entrySet()) {
            String key = entry.getKey();
            Module module = entry.getValue();
            
            String status = module.isEnabled() ? "§aEnabled" : "§cDisabled";
            sender.sendMessage("§7" + module.getName() + ": " + status);
            sender.sendMessage("  §8" + module.getDescription());
        }
    }
    
    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            boolean debugEnabled = plugin.getConfigManager().isDebugEnabled();
            sender.sendMessage("§7Debug mode is currently: " + 
                             (debugEnabled ? "§aEnabled" : "§cDisabled"));
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("on") || action.equals("enable")) {
            plugin.getConfigManager().set("general.debug", true);
            plugin.getMessageManager().sendSuccess(sender, "Debug mode enabled");
        } else if (action.equals("off") || action.equals("disable")) {
            plugin.getConfigManager().set("general.debug", false);
            plugin.getMessageManager().sendSuccess(sender, "Debug mode disabled");
        } else {
            plugin.getMessageManager().sendError(sender, "Usage: /xdiscord debug <on|off>");
        }
    }
    
    private void handleAdmin(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessageManager().sendError(sender, "Usage: /xdiscord admin <add|remove> <discord-id>");
            return;
        }
        
        String action = args[1].toLowerCase();
        String discordId = args[2];
        
        // Validate Discord ID format
        if (!discordId.matches("\\d{17,20}")) {
            plugin.getMessageManager().sendError(sender, "Invalid Discord ID format!");
            return;
        }
        
        if (action.equals("add")) {
            plugin.getAdminUtils().addAdmin(discordId);
            plugin.getMessageManager().sendSuccess(sender, "Added Discord ID " + discordId + " as admin");
        } else if (action.equals("remove")) {
            plugin.getAdminUtils().removeAdmin(discordId);
            plugin.getMessageManager().sendSuccess(sender, "Removed Discord ID " + discordId + " from admins");
        } else {
            plugin.getMessageManager().sendError(sender, "Usage: /xdiscord admin <add|remove> <discord-id>");
        }
    }

    private void handleMenu(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendError(sender, "This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        plugin.getDiscordManager().sendTestDropdowns(player);
        plugin.getMessageManager().sendSuccess(sender, "A message with dropdowns has been sent to your Discord DMs.");
    }
    
    private void showHelp(CommandSender sender) {
        plugin.getMessageManager().sendMessageList(sender, "help.header");
        
        sender.sendMessage("§e/xdiscord reload §7- Reload the plugin configuration");
        sender.sendMessage("§e/xdiscord status §7- Show plugin status");
        sender.sendMessage("§e/xdiscord modules §7- List all modules and their status");
        sender.sendMessage("§e/xdiscord debug <on|off> §7- Toggle debug mode");
        sender.sendMessage("§e/xdiscord admin <add|remove> <id> §7- Manage admin IDs");
        sender.sendMessage("§e/xdiscord help <command> §7- Show help for a specific command");
        
        plugin.getMessageManager().sendMessageList(sender, "help.footer");
    }
    
    private void showCommandHelp(CommandSender sender, String command) {
        // Show detailed help for specific commands
        switch (command.toLowerCase()) {
            case "verify":
                sender.sendMessage("§e/verify §7- " + 
                    plugin.getMessageManager().getMessage("commands.verify"));
                sender.sendMessage("§7Links your Minecraft account with Discord for enhanced features.");
                break;
                
            case "support":
                sender.sendMessage("§e/support <message> §7- " + 
                    plugin.getMessageManager().getMessage("commands.support"));
                sender.sendMessage("§7Creates a support ticket that staff can respond to via Discord.");
                break;
                
            case "embed":
                sender.sendMessage("§e/embed <channel> <title> <description> §7- " + 
                    plugin.getMessageManager().getMessage("commands.embed"));
                sender.sendMessage("§7Sends a custom embed message to the specified Discord channel.");
                sender.sendMessage("§7Requires: §exdiscord.embed");
                break;
                
            case "announce":
                sender.sendMessage("§e/announce <message> §7- " + 
                    plugin.getMessageManager().getMessage("commands.announce"));
                sender.sendMessage("§7Broadcasts an announcement to both Discord and in-game.");
                sender.sendMessage("§7Requires: §exdiscord.announce");
                break;
                
            default:
                plugin.getMessageManager().sendError(sender, "No help available for: " + command);
                break;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("xdiscord.admin")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "status", "modules", "debug", "admin", "help", "menu", "key", "network", "run"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "debug":
                    completions.addAll(Arrays.asList("on", "off", "enable", "disable"));
                    break;
                case "admin":
                    completions.addAll(Arrays.asList("add", "remove"));
                    break;
                case "help":
                    completions.addAll(Arrays.asList("verify", "support", "embed", "announce", 
                                                    "discordconsole", "report"));
                    break;
                case "network":
                    completions.addAll(Arrays.asList("list", "networkkey"));
                    break;
                case "key":
                    completions.addAll(Arrays.asList("generate"));
                    break;
            }
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }

    private void handleKey(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("generate")) {
            plugin.getMessageManager().sendError(sender, "Usage: /xdiscord key generate");
            return;
        }
        // Generate UUID-like key
        String key = java.util.UUID.randomUUID().toString();
        plugin.getConfigManager().set("network.key", key);
        plugin.getMessageManager().sendSuccess(sender, "Generated new network key. Saved to config.yml");
    }

    private void handleNetwork(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendError(sender, "Usage: /xdiscord network <list|networkkey>");
            return;
        }
        String sub = args[1].toLowerCase();
        if (sub.equals("networkkey")) {
            // Console-only as requested
            if (!(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
                plugin.getMessageManager().sendError(sender, "This command can only be used from the console.");
                return;
            }
            String key = plugin.getConfig().getString("network.key", "");
            sender.sendMessage("Current network key: " + key);
            return;
        }
        if (sub.equals("list")) {
            // Request player lists from peers and display with local
            String thisId = plugin.getConfig().getString("network.server_id", plugin.getServer().getName());
            if (thisId == null || thisId.isEmpty()) {
                thisId = plugin.getServer().getName();
            }
            
            java.util.Map<String, java.util.List<String>> map = new java.util.LinkedHashMap<>();
            java.util.List<String> local = plugin.getServer().getOnlinePlayers().stream()
                .map(org.bukkit.entity.Player::getName)
                .collect(java.util.stream.Collectors.toList());
            map.put(thisId, local);

            if (plugin.getNetworkManager() != null) {
                try {
                    // Send request
                    plugin.getNetworkManager().send(
                        com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.playerListRequest(thisId)
                    );
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to send player list request: " + e.getMessage());
                }
            }

            sender.sendMessage("Players by server (local view):");
            map.forEach((id, players) -> sender.sendMessage(id + ": " + (players.isEmpty() ? "(none)" : String.join(", ", players))));
            sender.sendMessage("Note: remote servers will reply asynchronously to their locals.");
            return;
        }
        plugin.getMessageManager().sendError(sender, "Usage: /xdiscord network <list|networkkey>");
    }

    private void handleRun(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessageManager().sendError(sender, "Usage: /xdiscord run <server_id|all> <command>");
            return;
        }
        String target = args[1];
        String cmd = java.util.Arrays.stream(args).skip(2).collect(java.util.stream.Collectors.joining(" "));
        if (target.equalsIgnoreCase("all")) {
            if (plugin.getNetworkManager() != null) {
                plugin.getNetworkManager().send(
                    com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.command(
                        plugin.getConfig().getString("network.server_id", plugin.getServer().getName()), cmd));
            }
            // Also run locally
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            plugin.getMessageManager().sendSuccess(sender, "Executed on all servers.");
        } else {
            if (plugin.getNetworkManager() == null) {
                plugin.getMessageManager().sendError(sender, "Network manager not initialized.");
                return;
            }
            plugin.getNetworkManager().sendTo(target,
                com.xreatlabs.xdiscordultimate.network.NetworkManager.PacketBuilder.command(
                    plugin.getConfig().getString("network.server_id", plugin.getServer().getName()), cmd));
            plugin.getMessageManager().sendSuccess(sender, "Command sent to " + target);
        }
    }
}
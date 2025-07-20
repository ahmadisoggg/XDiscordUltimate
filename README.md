# XDiscordUltimate

The most comprehensive Discord integration plugin for Minecraft servers, featuring 17+ modules for complete Discord-Minecraft synchronization.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.8--1.21.8-green)
![Discord](https://img.shields.io/badge/discord-JDA_5.0-7289da)
![License](https://img.shields.io/badge/license-MIT-orange)

## 🌟 Features

### Core Features
- **OAuth2 Verification System** - Secure Discord account linking with web-based verification
- **Modular Architecture** - Enable/disable features as needed
- **Multi-Database Support** - SQLite, MySQL, and PostgreSQL
- **PlaceholderAPI Integration** - Full placeholder support
- **LuckPerms Integration** - Permission and group synchronization
- **Admin Protection** - Discord ID-based admin verification

### 📦 Modules

#### 1. **Verification Module**
- OAuth2-based account linking
- Automatic role assignment
- Whitelist mode with kick timer
- Custom verification messages

#### 2. **Interactive Webhooks & Embeds**
- GUI-based embed builder
- Custom webhook management
- Preview system
- Template support

#### 3. **Voice Channel Integration**
- Proximity voice chat
- World-based channels
- Auto-channel creation
- AFK channel support

#### 4. **Server Control**
- Remote server management
- Command execution
- Server statistics
- Admin-only commands

#### 5. **Player Events & Feeds**
- Join/leave notifications
- Death messages
- Achievement announcements
- First join celebrations

#### 6. **Admin Alerts & Monitoring**
- Suspicious command detection
- Resource usage alerts
- Multiple account detection
- Block monitoring

#### 7. **Ticket System**
- Discord-based support tickets
- Auto-close inactive tickets
- Transcript generation
- Staff claiming system

#### 8. **Cross-Server Communication**
- Global chat system
- Private messaging
- Server-specific channels
- Mention support

#### 9. **Mini-Games & Polls**
- Interactive polls
- Trivia games
- Math challenges
- Word scrambles
- Reaction games

#### 10. **Moderation Sync**
- Ban/mute synchronization
- Warning system
- Moderation logs
- Audit trail

#### 11. **Bot Console**
- Remote console access
- Log streaming
- Command execution
- Session management

#### 12. **Announcement System**
- Scheduled announcements
- Custom formatting
- Discord sync
- Player-specific messages

#### 13. **Leaderboards**
- Dynamic leaderboards
- PlaceholderAPI support
- GUI interface
- Discord updates

#### 14. **Emoji Reactions**
- Custom emoji support
- Reaction roles
- Emoji GUI
- Chat integration

#### 15. **And More!**
- Update checker
- Metrics collection
- Multi-language support
- Extensive configuration

## 📋 Requirements

- **Minecraft Server**: 1.8 - 1.21.8
- **Java**: 8 or higher
- **Discord Bot**: With appropriate permissions
- **Optional Dependencies**:
  - PlaceholderAPI
  - LuckPerms
  - Vault (for economy features)

## 🚀 Installation

1. **Download** the latest release from [Releases](https://github.com/yourusername/XDiscordUltimate/releases)
2. **Place** the JAR file in your server's `plugins` folder
3. **Start** your server to generate configuration files
4. **Stop** your server
5. **Configure** the plugin (see Configuration section)
6. **Start** your server again

## ⚙️ Configuration

### Quick Setup

1. **Create Discord Bot**
   ```
   1. Go to https://discord.com/developers/applications
   2. Create New Application
   3. Go to Bot section
   4. Create Bot and copy token
   5. Enable all Privileged Gateway Intents
   ```

2. **Configure config.yml**
   ```yaml
   discord:
     bot-token: "YOUR_BOT_TOKEN"
     guild-id: "YOUR_GUILD_ID"
     
   database:
     type: "sqlite" # or mysql, postgresql
     
   modules:
     verification: true
     webhooks: true
     voice-channel: true
     # ... enable/disable modules as needed
   ```

3. **Set up OAuth2 (for verification)**
   ```yaml
   verification:
     oauth2:
       client-id: "YOUR_CLIENT_ID"
       client-secret: "YOUR_CLIENT_SECRET"
       redirect-uri: "http://yourserver.com:8080/callback"
   ```

### Module Configuration

Each module has its own configuration section. Example:

```yaml
announcements:
  enable-auto-announcements: true
  discord-channel: "announcements"
  announcements:
    welcome:
      title: "Welcome!"
      messages:
        - "&bWelcome to our server!"
        - "&eType /help for commands"
      interval: 600 # seconds
      enabled: true
```

## 📝 Commands

### Player Commands
- `/verify` - Link your Discord account
- `/discord` - Show Discord invite
- `/report <player> <reason>` - Report a player
- `/support <message>` - Create support ticket
- `/announce <message>` - Make announcement (requires permission)
- `/embed` - Open embed builder GUI
- `/poll create` - Create a poll
- `/game <type>` - Start a mini-game
- `/emoji` - Open emoji selector

### Admin Commands
- `/xdiscord reload` - Reload configuration
- `/xdiscord module <enable|disable> <module>` - Manage modules
- `/xdiscord info` - Show plugin information
- `/dconsole` - Toggle console output to Discord

## 🔑 Permissions

### Basic Permissions
- `xdiscord.verify` - Use verification system
- `xdiscord.report` - Report players
- `xdiscord.support` - Create support tickets
- `xdiscord.emoji` - Use emoji features

### Admin Permissions
- `xdiscord.admin` - Access admin commands
- `xdiscord.announce` - Make announcements
- `xdiscord.console` - Use Discord console
- `xdiscord.bypass.verification` - Bypass verification requirement

### Module-Specific Permissions
- `xdiscord.embed.create` - Create embeds
- `xdiscord.poll.create` - Create polls
- `xdiscord.game.start` - Start mini-games
- `xdiscord.webhook.manage` - Manage webhooks

## 🔧 PlaceholderAPI Placeholders

- `%xdiscord_verified%` - Shows if player is verified
- `%xdiscord_discord_name%` - Player's Discord name
- `%xdiscord_discord_id%` - Player's Discord ID
- `%xdiscord_tickets_open%` - Number of open tickets
- `%xdiscord_warnings%` - Number of warnings

## 🌐 API Usage

### Maven
```xml
<repository>
    <id>xreatlabs-repo</id>
    <url>https://repo.xreatlabs.com/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>com.xreatlabs</groupId>
    <artifactId>xdiscordultimate</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle
```gradle
repositories {
    maven { url 'https://repo.xreatlabs.com/repository/maven-public/' }
}

dependencies {
    compileOnly 'com.xreatlabs:xdiscordultimate:1.0.0'
}
```

### Example API Usage
```java
XDiscordUltimate api = XDiscordUltimate.getInstance();

// Check if player is verified
boolean isVerified = api.getAdminUtils().isVerified(player);

// Send Discord message
api.getDiscordManager().sendMessage("channel-name", "Hello from Minecraft!");

// Get player's Discord ID
api.getDatabaseManager().getDiscordId(player.getUniqueId())
    .thenAccept(discordId -> {
        if (discordId.isPresent()) {
            // Player is linked
        }
    });
```

## 🐛 Troubleshooting

### Common Issues

1. **Bot not connecting**
   - Check bot token is correct
   - Ensure bot has required permissions
   - Verify guild ID is correct

2. **Verification not working**
   - Check OAuth2 credentials
   - Ensure redirect URI matches configuration
   - Verify port 8080 is open

3. **Database errors**
   - Check database credentials
   - Ensure database exists
   - Verify connection URL

4. **Module not loading**
   - Check for errors in console
   - Verify dependencies are installed
   - Ensure module is enabled in config

## 📊 Metrics

This plugin uses bStats to collect anonymous usage data. You can opt-out in the configuration:

```yaml
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
- **Contributors**: See [contributors](https://github.com/yourusername/XDiscordUltimate/contributors)
- **Libraries Used**:
  - [JDA](https://github.com/DV8FromTheWorld/JDA) - Discord API
  - [HikariCP](https://github.com/brettwooldridge/HikariCP) - Database pooling
  - [OkHttp](https://github.com/square/okhttp) - HTTP client
  - [Gson](https://github.com/google/gson) - JSON parsing

## 📞 Support

- **Discord**: [Join our Discord](https://discord.gg/yourinvite)
- **Issues**: [GitHub Issues](https://github.com/yourusername/XDiscordUltimate/issues)
- **Wiki**: [GitHub Wiki](https://github.com/yourusername/XDiscordUltimate/wiki)
- **Email**: support@xreatlabs.com

## 🔄 Update History

### Version 1.0.0 (Current)
- Initial release
- 17 fully functional modules
- Multi-version support (1.8-1.21.8)
- Comprehensive API
- Full documentation

---

Made with ❤️ by XReatLabs Team
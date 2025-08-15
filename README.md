# XDiscordUltimate

Advanced Discord integration for Minecraft servers with enhanced chat bridge, comprehensive player event tracking, and powerful server management features.

## 🚀 Features

### Core Integration
- **Real-time Chat Bridge**: Seamless communication between Minecraft and Discord
- **Player Event Tracking**: Join/leave notifications with detailed playtime statistics
- **Account Verification**: Link Discord accounts with Minecraft players
- **Support Ticket System**: In-game support with Discord integration
- **Server Control**: Discord-based server management and monitoring
- **Cross-Server Communication**: Multi-server synchronization and messaging

### Enhanced Player Events
- **Rich Discord Embeds**: Beautiful, customizable embeds for all player events
- **Playtime Tracking**: Session and total playtime with detailed statistics
- **Player Statistics**: Real-time location, ping, and online player count
- **Avatar Integration**: Player avatars from Crafatar/SkinsRestorer
- **First Join Celebrations**: Special welcome messages for new players
- **Death Notifications**: Player death alerts with location details
- **Advancement Tracking**: Achievement and advancement notifications

### Advanced Chat Bridge
- **Embed Support**: Rich Discord embeds for all messages
- **Message Filtering**: Configurable message filtering and formatting
- **Cross-Platform Sync**: Bidirectional chat between platforms
- **Custom Styling**: Personalized message colors and formatting
- **Player Context**: Enhanced messages with player information

### Server Management
- **Status Monitoring**: Real-time server status with TPS and memory usage
- **Console Integration**: Discord console access and logging
- **Player Management**: Kick, ban, and player information commands
- **Server Control**: Start, stop, and restart server commands
- **Performance Metrics**: Detailed server performance tracking

### Security & Permissions
- **Role-Based Access**: Discord role integration with permissions
- **Secure Verification**: Encrypted account linking system
- **Permission Management**: Granular permission control
- **Audit Logging**: Comprehensive activity logging

## 📋 Requirements

- **Minecraft Server**: 1.16.5+ (Paper/Spigot recommended)
- **Java**: 8 or higher
- **Discord Bot**: Bot token with appropriate permissions
- **Optional Dependencies**: LuckPerms, PlaceholderAPI, SkinsRestorer

## 🔧 Installation

### 1. Download
Download the latest release from the GitHub releases page or build from source.

### 2. Discord Bot Setup
1. Create a Discord application at [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a bot and copy the token
3. Invite the bot to your server with required permissions:
   - Send Messages
   - Embed Links
   - Use Slash Commands
   - Manage Messages
   - Read Message History

### 3. Plugin Configuration
1. Place the JAR file in your `plugins` folder
2. Start the server once to generate configuration files
3. Edit `plugins/XDiscordUltimate/config.yml` with your Discord settings
4. Configure channels and permissions as needed
5. Restart the server

## 🛠️ Building from Source

### Prerequisites
- Java 8 or higher
- Gradle (wrapper included)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/Xreatlabs/XDiscordUltimate.git
cd XDiscordUltimate

# Build with Gradle
./gradlew build

# Or use the provided build script
./build.sh
```

The compiled JAR will be in the `build/libs/` directory.

## ⚙️ Configuration

### Basic Configuration
```yaml
# Discord Bot Settings
discord:
  bot-token: "YOUR_BOT_TOKEN"
  guild-id: "YOUR_GUILD_ID"
  activity: "Minecraft Server"

# Chat Bridge Settings
features:
  chat-bridge:
    enabled: true
    chat-channel-id: "YOUR_CHANNEL_ID"
    use-embeds: true
    minecraft-to-discord: true
    discord-to-minecraft: true
    webhook-enabled: false

# Player Events Settings
  player-events:
    enabled: true
    event-channel: "minecraft-events"
    use-embeds: true
    show-playtime: true
    show-location: true
    show-ping: true
    colors:
      join: "#00FF00"
      leave: "#FF0000"
      first-join: "#FF69B4"
      death: "#FFA500"
      advancement: "#FFD700"
    emojis:
      join: "➕"
      leave: "➖"
      death: "💀"
      advancement: "🏆"
      first-join: "🎉"

# Verification System
  verification:
    enabled: true
    verification-channel: "verification"
    auto-role: "Verified"
    verification-method: "code"

# Support Tickets
  tickets:
    enabled: true
    ticket-channel: "support-tickets"
    max-tickets: 5
    auto-close: true
```

## 📜 Commands

### Player Commands
- `/playtime [player]` - Check your or another player's playtime
- `/verify` - Link your Discord account
- `/ticket` - Create a support ticket

### Admin Commands
- `/xdiscord reload` - Reload plugin configuration
- `/xdiscord status` - Check plugin status
- `/xdiscord modules` - Manage plugin modules
- `/xdiscord debug` - Enable debug mode
- `/xdiscord admin` - Access admin panel
- `/xdiscord menu` - Open configuration menu

## 🔐 Permissions

### Player Permissions
- `xdiscord.playtime` - Check own playtime
- `xdiscord.playtime.others` - Check others' playtime
- `xdiscord.verify` - Use verification system
- `xdiscord.ticket` - Create support tickets

### Admin Permissions
- `xdiscord.admin` - All admin commands
- `xdiscord.embed` - Send custom embeds
- `xdiscord.announce` - Make announcements
- `xdiscord.console` - Access Discord console
- `xdiscord.modules` - Manage plugin modules

## 🎨 Discord Embed Examples

### Player Join
```
➕ PlayerName joined the server
Players Online: 15/20
Location: world at 100, 64, 200
Ping: 45ms
```

### Player Leave
```
➖ PlayerName left the server
Players Online: 14/20
Session Time: 2h 30m
Total Playtime: 1 day, 5 hours, 30 minutes
Last Location: world at 150, 70, 250
```

### First Join
```
🎉 Welcome PlayerName!
First time joining the server
Location: world at 100, 64, 200
Ping: 45ms
```

## 🔧 Advanced Configuration

### Custom Colors
```yaml
player-events:
  colors:
    join: "#00FF00"
    leave: "#FF0000"
    first-join: "#FF69B4"
    death: "#FFA500"
    advancement: "#FFD700"
```

### Custom Emojis
```yaml
player-events:
  emojis:
    join: "➕"
    leave: "➖"
    death: "💀"
    advancement: "🏆"
    first-join: "🎉"
```

### Network Configuration
```yaml
network:
  enabled: true
  port: 25565
  key: "your-secure-key"
  servers:
    - "server1"
    - "server2"
```

## 🐛 Troubleshooting

### Common Issues
1. **Bot not connecting**: Check bot token and permissions
2. **Messages not sending**: Verify channel IDs and bot permissions
3. **Playtime not tracking**: Ensure player-events module is enabled
4. **Verification not working**: Check verification channel and role permissions

### Debug Mode
Enable debug mode in config:
```yaml
general:
  debug: true
```

### Log Files
Check server logs for detailed error information:
- `logs/latest.log` - General server logs
- `plugins/XDiscordUltimate/` - Plugin-specific logs

## 📝 Changelog

### Version 1.0.0
- Complete Discord integration system
- Enhanced chat bridge with rich embeds
- Comprehensive player event tracking
- Advanced playtime tracking system
- Support ticket system
- Server control and monitoring
- Cross-server communication
- Account verification system
- Performance optimization
- Extensive configuration options

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Discord**: Join our support server
- **Issues**: Report bugs on GitHub
- **Wiki**: Check the [wiki](https://github.com/Xreatlabs/XDiscordUltimate/wiki) for detailed documentation

## 🙏 Acknowledgments

- Discord JDA library for Discord API integration
- Bukkit/Spigot API for Minecraft server integration
- Community contributors and testers
- All plugin dependencies and libraries

---

**XDiscordUltimate** - The ultimate Discord integration for Minecraft servers

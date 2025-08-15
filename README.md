# XDiscordUltimate

Advanced Discord integration for Minecraft servers with enhanced chat bridge, playtime tracking, and comprehensive player event notifications.

## 🚀 Features

### Core Features
- **Chat Bridge**: Real-time chat between Minecraft and Discord
- **Player Events**: Join/leave notifications with playtime tracking
- **Verification System**: Link Discord accounts with Minecraft
- **Support Tickets**: In-game support ticket system
- **Server Control**: Discord-based server management
- **Cross-Server Sync**: Multi-server communication

### Enhanced Chat Bridge
- **Rich Embeds**: Beautiful Discord embeds for all events
- **Playtime Tracking**: Session and total playtime display
- **Player Statistics**: Location, ping, and online player count
- **Avatar Integration**: Player avatars from Crafatar/SkinsRestorer
- **Configurable Messages**: Customizable event messages and colors

### Player Event Features
- **Join Notifications**: Welcome messages with player info
- **Leave Notifications**: Session playtime and total playtime
- **First Join**: Special welcome for new players
- **Death Messages**: Player death notifications with location
- **Advancement Tracking**: Achievement notifications

## 📋 Requirements

- **Minecraft Server**: 1.16.5+ (Paper/Spigot recommended)
- **Java**: 8 or higher
- **Discord Bot**: Bot token and permissions
- **Optional**: LuckPerms, PlaceholderAPI, SkinsRestorer

## 🔧 Installation

### 1. Download
Download the latest release from the releases page or build from source.

### 2. Setup Discord Bot
1. Create a Discord application at [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a bot and copy the token
3. Invite the bot to your server with required permissions

### 3. Configure Plugin
1. Place the JAR file in your `plugins` folder
2. Start the server once to generate config files
3. Edit `config.yml` with your Discord bot token and channel IDs
4. Restart the server

## 🛠️ Building from Source

### Prerequisites
- Java 8 or higher
- Gradle (optional, wrapper included)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/xreatlabs/xdiscordultimate.git
cd xdiscordultimate

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

# Chat Bridge Settings
features:
  chat-bridge:
    enabled: true
    chat-channel-id: "YOUR_CHANNEL_ID"
    use-embeds: true
    minecraft-to-discord: true
    discord-to-minecraft: true

# Player Events Settings
  player-events:
    enabled: true
    event-channel: "minecraft-events"
    use-embeds: true
    show-playtime: true
    show-location: true
    show-ping: true
```

### Playtime Tracking
The plugin automatically tracks:
- **Session Playtime**: Time spent in current session
- **Total Playtime**: Accumulated time across all sessions
- **Player Statistics**: Location, ping, and online status

## 🎮 Commands

### Player Commands
- `/verify` - Link Discord account
- `/support <message>` - Create support ticket
- `/playtime [player]` - Check playtime
- `/help` - Show help menu

### Admin Commands
- `/xdiscord reload` - Reload configuration
- `/xdiscord status` - Show plugin status
- `/xdiscord modules` - List modules
- `/embed <channel> <title> <description>` - Send custom embed
- `/announce <message>` - Broadcast announcement

## 🔐 Permissions

### Player Permissions
- `xdiscord.verify` - Use verification command
- `xdiscord.support` - Create support tickets
- `xdiscord.playtime` - Check own playtime
- `xdiscord.playtime.others` - Check others' playtime

### Admin Permissions
- `xdiscord.admin` - All admin commands
- `xdiscord.embed` - Send custom embeds
- `xdiscord.announce` - Make announcements
- `xdiscord.console` - Access Discord console

## 🎨 Discord Embeds

### Join Embed Example
```
➕ PlayerName joined the server
Players Online: 15/20
Location: world at 100, 64, 200
Ping: 45ms
```

### Leave Embed Example
```
➖ PlayerName left the server
Players Online: 14/20
Session Time: 2h 30m
Total Playtime: 1 day, 5 hours, 30 minutes
Last Location: world at 150, 70, 250
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

## 🐛 Troubleshooting

### Common Issues
1. **Bot not connecting**: Check bot token and permissions
2. **Messages not sending**: Verify channel IDs and bot permissions
3. **Playtime not tracking**: Ensure player-events module is enabled

### Debug Mode
Enable debug mode in config:
```yaml
general:
  debug: true
```

## 📝 Changelog

### Version 1.0.0
- Initial release
- Chat bridge functionality
- Player event tracking
- Playtime tracking system
- Enhanced Discord embeds
- Support ticket system
- Server control features

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
- **Wiki**: Check the [wiki](https://github.com/xreatlabs/xdiscordultimate/wiki) for detailed documentation

## 🙏 Acknowledgments

- Discord JDA library
- Bukkit/Spigot API
- Community contributors and testers

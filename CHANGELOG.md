# Changelog

All notable changes to XDiscordUltimate will be documented in this file.

## [1.0.0] - 2024-08-15

### 🚀 Added
- **PlaytimeTracker Utility**: Complete playtime tracking system
  - Session playtime tracking
  - Total accumulated playtime
  - Formatted time display (e.g., "2h 30m", "1 day, 5 hours, 30 minutes")
  - Player statistics for Discord embeds

- **Enhanced Discord Embeds**: Rich, informative player event notifications
  - Player avatars from Crafatar/SkinsRestorer
  - Player location and ping information
  - Online player count
  - Session and total playtime display
  - Customizable colors and emojis

- **New Commands**:
  - `/playtime` - Check your own playtime
  - `/playtime <player>` - Check another player's playtime (requires permission)

- **New Permissions**:
  - `xdiscord.playtime` - Check own playtime (default: true)
  - `xdiscord.playtime.others` - Check other players' playtime (default: op)

- **Build System**:
  - `build.sh` - Simple build script
  - `build-with-deps.sh` - Build script with dependency handling
  - Proper `.gitignore` configuration

### 🔧 Enhanced
- **PlayerEventsModule**: 
  - Enhanced join/leave embeds with playtime information
  - Configurable display options (playtime, location, ping)
  - Better error handling and logging
  - Improved embed formatting

- **ChatBridgeModule**:
  - Enhanced join/leave notifications
  - Better error handling for Discord API calls
  - Improved avatar handling
  - Message length limiting to prevent Discord API errors

- **Configuration System**:
  - New playtime tracking options
  - Customizable embed colors
  - Configurable emoji display
  - Enhanced player event settings

### 🐛 Fixed
- **DiscordManager**: 
  - Added null checks in `setMainGuild()` method
  - Enhanced error handling in `sendTestDropdowns()` method
  - Improved Discord API error handling

- **DatabaseManager**: 
  - Added exception handling in `close()` method
  - Improved resource management

- **ConfigManager**: 
  - Fixed `getKeys()` method to return empty HashSet instead of null
  - Prevented potential NullPointerExceptions

- **NetworkManager**: 
  - Added exception handling in `decrypt()` method
  - Improved error handling for network operations

- **XDiscordCommand**: 
  - Added null checks and error handling in `handleNetwork()` method
  - Improved server ID validation

- **TPSManager**: 
  - Fixed TPS calculation logic
  - Added proper bounds checking
  - Prevented division by zero errors

- **StatusMessageManager**: 
  - Added comprehensive exception handling
  - Improved Discord API error handling

- **ConsoleAppender**: 
  - Added exception handling and message length limiting
  - Prevented console spam from Discord errors

- **DropdownManager**: 
  - Added null checks for Discord manager and JDA
  - Improved error handling

- **XDiscordUltimate**: 
  - Fixed potential null pointer in `initializeNetwork()` method
  - Added PlaytimeTracker integration

### 📝 Documentation
- **Updated README.md**: 
  - Comprehensive feature documentation
  - Installation and configuration guides
  - Command and permission reference
  - Troubleshooting section

- **Enhanced Configuration**: 
  - Added playtime tracking options
  - Customizable embed settings
  - Improved configuration structure

### 🔐 Security
- Enhanced error handling throughout the codebase
- Improved resource management
- Better null safety checks
- Comprehensive logging for debugging

### 📦 Build System
- Added proper build scripts
- Enhanced `.gitignore` configuration
- Improved project structure
- Better dependency management documentation

---

## Migration Guide

### For Existing Users
1. **Backup your configuration**: Copy your current `config.yml` and `messages.yml`
2. **Update the plugin**: Replace the old JAR with the new version
3. **Review new configuration options**: Check the new playtime tracking settings
4. **Test the new features**: Try the `/playtime` command and check the enhanced embeds

### New Configuration Options
```yaml
player-events:
  show-playtime: true      # Show playtime in leave messages
  show-location: true      # Show player location
  show-ping: true          # Show player ping in join messages
  colors:
    join: "#00FF00"
    leave: "#FF0000"
    first-join: "#FF69B4"
  emojis:
    join: "➕"
    leave: "➖"
    death: "💀"
    advancement: "🏆"
    first-join: "🎉"
```

### Breaking Changes
- None - This is a fully backward-compatible update

---

## Technical Details

### Performance Improvements
- Optimized playtime calculations
- Improved memory usage in PlaytimeTracker
- Better resource management in database connections
- Enhanced error handling to prevent crashes

### Code Quality
- Added comprehensive null checks
- Improved exception handling
- Better logging throughout the codebase
- Enhanced code documentation

### Dependencies
- No new external dependencies added
- All existing dependencies remain the same
- Improved dependency management documentation
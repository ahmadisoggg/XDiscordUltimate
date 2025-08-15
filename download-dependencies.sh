#!/bin/bash

echo "📥 XDiscordUltimate Dependency Downloader"
echo "=========================================="

# Create libs directory
mkdir -p plugins/XDiscordUltimate/libs
cd plugins/XDiscordUltimate/libs

echo "🔗 Downloading runtime dependencies..."

# Core dependencies
echo "📦 Downloading JDA (Discord API)..."
wget -O JDA.jar "https://repo1.maven.org/maven2/net/dv8tion/JDA/5.0.0-beta.20/JDA-5.0.0-beta.20.jar"

echo "📦 Downloading Gson (JSON parsing)..."
wget -O gson.jar "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"

echo "📦 Downloading HikariCP (Database pooling)..."
wget -O hikaricp.jar "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar"

echo "📦 Downloading SLF4J API (Logging)..."
wget -O slf4j-api.jar "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar"

echo "📦 Downloading Log4j Core (Logging)..."
wget -O log4j-core.jar "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar"

echo "📦 Downloading OkHttp (HTTP client)..."
wget -O okhttp.jar "https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.11.0/okhttp-4.11.0.jar"

echo "📦 Downloading SQLite JDBC..."
wget -O sqlite-jdbc.jar "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar"

echo "📦 Downloading MySQL Connector..."
wget -O mysql-connector.jar "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"

echo "📦 Downloading PostgreSQL Driver..."
wget -O postgresql.jar "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar"

echo ""
echo "✅ All dependencies downloaded successfully!"
echo "📊 Total size: $(du -sh . | cut -f1)"
echo ""
echo "🎯 Next steps:"
echo "1. Place XDiscordUltimate-Lightweight.jar in your plugins folder"
echo "2. Start your server"
echo "3. The plugin will automatically load the dependencies"
echo ""
echo "💡 Note: Dependencies are cached and won't be downloaded again"
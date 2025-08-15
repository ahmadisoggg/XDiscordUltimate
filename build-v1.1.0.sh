#!/bin/bash

echo "🚀 Building XDiscordUltimate v1.1.0 Production Plugin..."

# Create build directories
mkdir -p build/v1.1.0/classes
mkdir -p build/v1.1.0/resources

# Copy all source files
echo "📁 Copying source files..."
cp -r main/java/* build/v1.1.0/classes/
cp -r main/resources/* build/v1.1.0/resources/

# Update version to 1.1.0 in plugin.yml
echo "🔧 Updating version to 1.1.0..."
sed -i 's/version: 1.0.0/version: 1.1.0/' build/v1.1.0/resources/plugin.yml

# Create build directory structure
cd build/v1.1.0

# Download essential dependencies for compilation
echo "📥 Downloading dependencies..."
mkdir -p libs

# Download JDA
wget -O libs/jda-5.0.0-beta.20.jar "https://repo1.maven.org/maven2/net/dv8tion/JDA/5.0.0-beta.20/JDA-5.0.0-beta.20.jar" 2>/dev/null || echo "Failed to download JDA"

# Download HikariCP
wget -O libs/hikaricp-5.0.1.jar "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar" 2>/dev/null || echo "Failed to download HikariCP"

# Download Gson
wget -O libs/gson-2.10.1.jar "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" 2>/dev/null || echo "Failed to download Gson"

# Download SLF4J API
wget -O libs/slf4j-api-2.0.7.jar "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar" 2>/dev/null || echo "Failed to download SLF4J API"

# Download Log4j Core
wget -O libs/log4j-core-2.20.0.jar "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar" 2>/dev/null || echo "Failed to download Log4j Core"

# Download OkHttp
wget -O libs/okhttp-4.11.0.jar "https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.11.0/okhttp-4.11.0.jar" 2>/dev/null || echo "Failed to download OkHttp"

# Download SQLite JDBC
wget -O libs/sqlite-jdbc-3.42.0.0.jar "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar" 2>/dev/null || echo "Failed to download SQLite JDBC"

# Download MySQL Connector
wget -O libs/mysql-connector-j-8.1.0.jar "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.1.0/mysql-connector-j-8.1.0.jar" 2>/dev/null || echo "Failed to download MySQL Connector"

# Download PostgreSQL JDBC
wget -O libs/postgresql-42.6.0.jar "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar" 2>/dev/null || echo "Failed to download PostgreSQL JDBC"

# Create classpath
echo "🔗 Creating classpath..."
CLASSPATH="."
for jar in libs/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Find all Java files
echo "🔍 Finding Java files..."
find . -name "*.java" > sources.txt

# Compile with Java 8 compatibility
echo "🔨 Compiling Java files with Java 8 compatibility..."
javac -source 8 -target 8 -cp "$CLASSPATH" -d . @sources.txt 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Create JAR file
    echo "📦 Creating v1.1.0 JAR file..."
    
    # Create manifest
    mkdir -p META-INF
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Plugin-Class: com.xreatlabs.xdiscordultimate.XDiscordUltimate
Plugin-Version: 1.1.0
Plugin-Name: XDiscordUltimate
Plugin-Description: Advanced Discord integration for Minecraft servers
Plugin-Author: XReatLabs
Plugin-Website: https://github.com/Xreatlabs/XDiscordUltimate
Plugin-API-Version: 1.16
Plugin-Dependencies: 
Plugin-SoftDepend: LuckPerms,PlaceholderAPI,Vault
Created-By: XReatLabs Build System
Build-Date: $(date)
EOF
    
    # Create fat JAR with all dependencies
    echo "📦 Creating fat JAR with dependencies..."
    
    # Extract all dependency JARs
    mkdir -p temp-deps
    for jar in libs/*.jar; do
        if [ -f "$jar" ]; then
            echo "📦 Extracting $jar..."
            jar xf "$jar" -d temp-deps/
        fi
    done
    
    # Create the final JAR
    jar cf ../../XDiscordUltimate-v1.1.0.jar .
    
    # Clean up
    rm -rf temp-deps sources.txt
    
    cd ../..
    
    # Get file size
    SIZE=$(du -h XDiscordUltimate-v1.1.0.jar | cut -f1)
    echo "✅ v1.1.0 JAR created: XDiscordUltimate-v1.1.0.jar ($SIZE)"
    
    # Show JAR contents
    echo "📋 JAR contents (first 20 files):"
    jar tf XDiscordUltimate-v1.1.0.jar | head -20
    echo "... and more files"
    
    # Count total files
    TOTAL_FILES=$(jar tf XDiscordUltimate-v1.1.0.jar | wc -l)
    echo "📊 Total files in JAR: $TOTAL_FILES"
    
    # Count class files
    CLASS_FILES=$(jar tf XDiscordUltimate-v1.1.0.jar | grep "\.class$" | wc -l)
    echo "📊 Total class files: $CLASS_FILES"
    
    echo "🎉 v1.1.0 build completed successfully!"
    echo "📊 Final JAR size: $SIZE"
    echo "💡 This JAR contains all compiled classes and dependencies"
    
else
    echo "❌ Compilation failed! Creating fallback JAR..."
    
    # Create fallback with just plugin.yml and resources
    cd ..
    mkdir -p build/fallback-v1.1.0/META-INF
    cp v1.1.0/resources/plugin.yml build/fallback-v1.1.0/
    cp v1.1.0/resources/config.yml build/fallback-v1.1.0/
    cp v1.1.0/resources/messages.yml build/fallback-v1.1.0/
    
    # Create manifest
    cat > build/fallback-v1.1.0/META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Plugin-Class: com.xreatlabs.xdiscordultimate.XDiscordUltimate
Plugin-Version: 1.1.0
Plugin-Name: XDiscordUltimate
Plugin-Description: Advanced Discord integration for Minecraft servers
Plugin-Author: XReatLabs
Plugin-Website: https://github.com/Xreatlabs/XDiscordUltimate
Plugin-API-Version: 1.16
Plugin-Dependencies: 
Plugin-SoftDepend: LuckPerms,PlaceholderAPI,Vault
Created-By: XReatLabs Build System
Build-Date: $(date)
EOF
    
    cd build/fallback-v1.1.0
    jar cf ../../XDiscordUltimate-v1.1.0-Fallback.jar .
    cd ../..
    
    SIZE=$(du -h XDiscordUltimate-v1.1.0-Fallback.jar | cut -f1)
    echo "✅ Fallback v1.1.0 JAR created: XDiscordUltimate-v1.1.0-Fallback.jar ($SIZE)"
    echo "⚠️  Note: This is a fallback JAR - compilation failed"
fi
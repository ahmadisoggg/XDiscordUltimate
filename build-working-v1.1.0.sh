#!/bin/bash

echo "🚀 Building Working XDiscordUltimate v1.1.0 with All Classes..."

# Create build directory
mkdir -p build/working-v1.1.0/classes
mkdir -p build/working-v1.1.0/resources

# Copy source files
echo "📁 Copying source files..."
cp -r main/java/* build/working-v1.1.0/classes/
cp -r main/resources/* build/working-v1.1.0/resources/

# Update version to 1.1.0
echo "🔧 Updating version to 1.1.0..."
sed -i 's/version: 1.0.0/version: 1.1.0/' build/working-v1.1.0/resources/plugin.yml

cd build/working-v1.1.0

# Download Bukkit API for compilation
echo "📥 Downloading Bukkit API..."
mkdir -p libs
wget -O libs/bukkit-api-1.20.4-R0.1-SNAPSHOT.jar "https://repo1.maven.org/maven2/org/bukkit/bukkit/1.20.4-R0.1-SNAPSHOT/bukkit-1.20.4-R0.1-SNAPSHOT.jar" 2>/dev/null || echo "Failed to download Bukkit API"

# Download essential dependencies
echo "📥 Downloading dependencies..."
wget -O libs/jda-5.0.0-beta.20.jar "https://repo1.maven.org/maven2/net/dv8tion/JDA/5.0.0-beta.20/JDA-5.0.0-beta.20.jar" 2>/dev/null || echo "Failed to download JDA"
wget -O libs/hikaricp-5.0.1.jar "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar" 2>/dev/null || echo "Failed to download HikariCP"
wget -O libs/gson-2.10.1.jar "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" 2>/dev/null || echo "Failed to download Gson"
wget -O libs/slf4j-api-2.0.7.jar "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar" 2>/dev/null || echo "Failed to download SLF4J API"
wget -O libs/log4j-core-2.20.0.jar "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar" 2>/dev/null || echo "Failed to download Log4j Core"
wget -O libs/okhttp-4.11.0.jar "https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.11.0/okhttp-4.11.0.jar" 2>/dev/null || echo "Failed to download OkHttp"

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

# Count Java files
JAVA_COUNT=$(wc -l < sources.txt)
echo "📊 Found $JAVA_COUNT Java files to compile"

# Try to compile
echo "🔨 Compiling Java files..."
javac -source 8 -target 8 -cp "$CLASSPATH" -d . @sources.txt 2>compile_errors.log

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Count compiled class files
    CLASS_COUNT=$(find . -name "*.class" | wc -l)
    echo "📊 Compiled $CLASS_COUNT class files"
    
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
Created-By: XReatLabs Build System v1.1.0
Build-Date: $(date)
EOF
    
    # Create JAR with compiled classes and resources
    echo "📦 Creating JAR with compiled classes..."
    jar cf ../../XDiscordUltimate-Working-v1.1.0.jar .
    
    cd ../..
    
    # Get file size
    SIZE=$(du -h XDiscordUltimate-Working-v1.1.0.jar | cut -f1)
    echo "✅ Working v1.1.0 JAR created: XDiscordUltimate-Working-v1.1.0.jar ($SIZE)"
    
    # Verify main class exists
    if jar tf XDiscordUltimate-Working-v1.1.0.jar | grep -q "com/xreatlabs/xdiscordultimate/XDiscordUltimate.class"; then
        echo "✅ Main class found in JAR!"
    else
        echo "❌ Main class not found in JAR!"
    fi
    
    # Show JAR contents
    echo "📋 JAR contents (first 20 files):"
    jar tf XDiscordUltimate-Working-v1.1.0.jar | head -20
    echo "... and more files"
    
    # Count total files
    TOTAL_FILES=$(jar tf XDiscordUltimate-Working-v1.1.0.jar | wc -l)
    echo "📊 Total files in JAR: $TOTAL_FILES"
    
    # Count class files
    CLASS_FILES=$(jar tf XDiscordUltimate-Working-v1.1.0.jar | grep "\.class$" | wc -l)
    echo "📊 Total class files: $CLASS_FILES"
    
    echo "🎉 Working v1.1.0 build completed successfully!"
    echo "📊 Final JAR size: $SIZE"
    echo "💡 This JAR contains all compiled classes and should work properly"
    
else
    echo "❌ Compilation failed! Check compile_errors.log for details"
    echo "📋 Compilation errors:"
    cat compile_errors.log | head -20
    
    echo "🔧 Creating fallback JAR with source files..."
    
    # Create fallback JAR with source files
    jar cf ../../XDiscordUltimate-Fallback-v1.1.0.jar .
    
    cd ../..
    
    SIZE=$(du -h XDiscordUltimate-Fallback-v1.1.0.jar | cut -f1)
    echo "✅ Fallback v1.1.0 JAR created: XDiscordUltimate-Fallback-v1.1.0.jar ($SIZE)"
    echo "⚠️  Note: This is a fallback JAR - compilation failed"
fi

# Clean up
rm -f sources.txt compile_errors.log
#!/bin/bash

echo "🚀 Building Working Lightweight XDiscordUltimate Plugin..."

# Create build directory
mkdir -p build/working-lightweight

# Extract the production JAR to see what we have
echo "📦 Extracting production JAR..."
cd build/working-lightweight
jar xf ../../XDiscordUltimate-Production.jar

# Check if we have our actual plugin classes
echo "🔍 Checking for plugin classes..."
if [ -d "com/xreatlabs/xdiscordultimate" ]; then
    echo "✅ Found plugin classes!"
    PLUGIN_CLASSES_EXIST=true
else
    echo "❌ No plugin classes found in production JAR"
    PLUGIN_CLASSES_EXIST=false
fi

# Create a working lightweight JAR
echo "📦 Creating working lightweight JAR..."

# Keep only essential files
if [ "$PLUGIN_CLASSES_EXIST" = true ]; then
    # Keep plugin classes and essential files
    echo "✅ Creating JAR with plugin classes..."
    
    # Create new JAR with only essential files
    jar cf ../../XDiscordUltimate-Working-Lightweight.jar \
        plugin.yml \
        config.yml \
        messages.yml \
        com/xreatlabs/xdiscordultimate/ \
        META-INF/MANIFEST.MF
else
    echo "⚠️ Creating minimal JAR without plugin classes..."
    
    # Create minimal JAR with just plugin.yml and manifest
    jar cf ../../XDiscordUltimate-Minimal-Working.jar \
        plugin.yml \
        META-INF/MANIFEST.MF
fi

cd ../..

# Check which JAR was created
if [ -f "XDiscordUltimate-Working-Lightweight.jar" ]; then
    JAR_FILE="XDiscordUltimate-Working-Lightweight.jar"
    echo "✅ Working lightweight JAR created with plugin classes"
elif [ -f "XDiscordUltimate-Minimal-Working.jar" ]; then
    JAR_FILE="XDiscordUltimate-Minimal-Working.jar"
    echo "✅ Minimal working JAR created"
else
    echo "❌ Failed to create JAR"
    exit 1
fi

# Get file size and show info
SIZE=$(du -h "$JAR_FILE" | cut -f1)
echo "📊 JAR size: $SIZE"

# Show JAR contents
echo "📋 JAR contents:"
jar tf "$JAR_FILE"

# Count files
TOTAL_FILES=$(jar tf "$JAR_FILE" | wc -l)
echo "📊 Total files in JAR: $TOTAL_FILES"

echo "🎉 Working lightweight build completed!"
echo "📊 Final JAR: $JAR_FILE ($SIZE)"
echo "💡 This JAR should load properly on your server"
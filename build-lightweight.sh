#!/bin/bash

echo "🚀 Building XDiscordUltimate Lightweight JAR (Runtime Dependencies)..."

# Create build directories
mkdir -p build/classes

# Find all Java files
find main/java -name "*.java" > sources.txt

echo "🔨 Compiling Java files (plugin code only)..."
javac -cp ".:main/resources" -d build/classes @sources.txt

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Copy resources
    echo "📁 Copying resources..."
    cp -r main/resources/* build/classes/
    
    # Create lightweight JAR file
    echo "📦 Creating lightweight JAR file..."
    cd build/classes
    jar cf ../../XDiscordUltimate-Lightweight.jar .
    cd ../..
    
    # Get file size
    SIZE=$(du -h XDiscordUltimate-Lightweight.jar | cut -f1)
    echo "✅ Lightweight JAR created: XDiscordUltimate-Lightweight.jar ($SIZE)"
    
    # Show JAR contents
    echo "📋 JAR contents (plugin files only):"
    jar tf XDiscordUltimate-Lightweight.jar | head -20
    echo "... and more files"
    
    # Count total files
    TOTAL_FILES=$(jar tf XDiscordUltimate-Lightweight.jar | wc -l)
    echo "📊 Total files in JAR: $TOTAL_FILES"
    
    echo "🎉 Lightweight build completed successfully!"
    echo "📊 Final JAR size: $SIZE"
    echo "💡 Dependencies will be downloaded at runtime to plugins/XDiscordUltimate/libs/"
    
else
    echo "❌ Compilation failed!"
    exit 1
fi

# Clean up
rm sources.txt
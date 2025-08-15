#!/bin/bash

echo "🚀 Building XDiscordUltimate Fat JAR with All Dependencies..."

# Create build directories
mkdir -p build/classes
mkdir -p build/temp

# Build classpath with all dependencies
CLASSPATH=".:main/resources"
for jar in build/deps/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

echo "📦 Classpath: $CLASSPATH"

# Find all Java files
find main/java -name "*.java" > sources.txt

echo "🔨 Compiling Java files with dependencies..."
javac -cp "$CLASSPATH" -d build/classes @sources.txt

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Copy resources
    echo "📁 Copying resources..."
    cp -r main/resources/* build/classes/
    
    # Extract all dependency JARs
    echo "📦 Extracting dependencies..."
    for jar in build/deps/*.jar; do
        if [ -f "$jar" ]; then
            echo "Extracting: $(basename $jar)"
            cd build/temp
            jar xf ../../$jar
            cd ../..
        fi
    done
    
    # Copy dependency classes to main classes
    echo "📋 Merging dependency classes..."
    cp -r build/temp/* build/classes/ 2>/dev/null || true
    
    # Create fat JAR file
    echo "📦 Creating fat JAR file..."
    cd build/classes
    jar cf ../../XDiscordUltimate-Fat.jar .
    cd ../..
    
    # Get file size
    SIZE=$(du -h XDiscordUltimate-Fat.jar | cut -f1)
    echo "✅ Fat JAR created: XDiscordUltimate-Fat.jar ($SIZE)"
    
    # Show JAR contents
    echo "📋 JAR contents (first 20 files):"
    jar tf XDiscordUltimate-Fat.jar | head -20
    echo "... and more files"
    
    # Count total files
    TOTAL_FILES=$(jar tf XDiscordUltimate-Fat.jar | wc -l)
    echo "📊 Total files in JAR: $TOTAL_FILES"
    
    echo "🎉 Fat JAR build completed successfully!"
    echo "📊 Final JAR size: $SIZE"
    
else
    echo "❌ Compilation failed!"
    exit 1
fi

# Clean up
rm sources.txt
rm -rf build/temp
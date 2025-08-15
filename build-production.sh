#!/bin/bash

echo "🚀 Building XDiscordUltimate Production JAR with Dependencies..."

# Create build directories
mkdir -p build/classes
mkdir -p build/libs

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
    
    # Create JAR file
    echo "📦 Creating production JAR file..."
    cd build/classes
    jar cf ../../XDiscordUltimate-Production.jar .
    cd ../..
    
    # Get file size
    SIZE=$(du -h XDiscordUltimate-Production.jar | cut -f1)
    echo "✅ Production JAR created: XDiscordUltimate-Production.jar ($SIZE)"
    
    # Show JAR contents
    echo "📋 JAR contents:"
    jar tf XDiscordUltimate-Production.jar | head -10
    echo "... and more files"
    
    echo "🎉 Production build completed successfully!"
    echo "📊 Final JAR size: $SIZE"
    
else
    echo "❌ Compilation failed!"
    exit 1
fi

# Clean up
rm sources.txt
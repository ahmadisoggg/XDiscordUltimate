#!/bin/bash

echo "Building XDiscordUltimate..."

# Create build directory
mkdir -p build/classes

# Find all Java files
find main/java -name "*.java" > sources.txt

# Compile Java files
echo "Compiling Java files..."
javac -cp ".:main/resources" -d build/classes @sources.txt

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Create JAR file
    echo "Creating JAR file..."
    cd build/classes
    jar cf ../../XDiscordUltimate.jar .
    cd ../..
    
    echo "✅ JAR file created: XDiscordUltimate.jar"
    echo "Build completed successfully!"
else
    echo "❌ Compilation failed!"
    exit 1
fi

# Clean up
rm sources.txt
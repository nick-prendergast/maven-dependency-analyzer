#!/bin/bash

# Maven Dependency Analyzer - Docker Runner Script
# Usage: ./analyze.sh <project-path> [output-file]

set -e

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed or not in PATH"
    echo "   Please install Docker first: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check arguments
if [ $# -eq 0 ]; then
    echo "🔍 Maven Dependency Analyzer"
    echo ""
    echo "Usage: $0 <project-path> [output-file]"
    echo ""
    echo "Examples:"
    echo "  $0 /path/to/maven/project"
    echo "  $0 ../my-spring-app my-app-deps.json"
    echo "  $0 . current-project.json"
    echo ""
    echo "Options:"
    echo "  project-path    Path to Maven project directory (required)"
    echo "  output-file     Output JSON file name (default: dependencies.json)"
    exit 1
fi

PROJECT_PATH="$1"
OUTPUT_FILE="${2:-dependencies.json}"

# Validate project path
if [ ! -d "$PROJECT_PATH" ]; then
    echo "❌ Error: Directory '$PROJECT_PATH' does not exist"
    exit 1
fi

# Check if it's a Maven project
if [ ! -f "$PROJECT_PATH/pom.xml" ]; then
    echo "⚠️  Warning: No pom.xml found in '$PROJECT_PATH'"
    echo "   This may not be a Maven project"
fi

# Convert to absolute path
PROJECT_PATH=$(cd "$PROJECT_PATH" && pwd)

# Create output directory
OUTPUT_DIR="$(pwd)/output"
mkdir -p "$OUTPUT_DIR"

echo "🔍 Analyzing Maven project: $PROJECT_PATH"
echo "📄 Output file: $OUTPUT_DIR/$OUTPUT_FILE"
echo ""

# Build Docker image if it doesn't exist
if ! docker images | grep -q "maven-dependency-analyzer"; then
    echo "🔨 Building Docker image..."
    docker build -t maven-dependency-analyzer .
    echo ""
fi

# Run the analyzer
echo "🚀 Running dependency analysis..."
docker run --rm \
    -v "$HOME/.m2:/root/.m2" \
    -v "$PROJECT_PATH:/project:ro" \
    -v "$OUTPUT_DIR:/output" \
    maven-dependency-analyzer \
    -d /project -o "/output/$OUTPUT_FILE"

echo ""
echo "✅ Analysis complete!"
echo "📁 Output saved to: $OUTPUT_DIR/$OUTPUT_FILE"

# Show file size and preview
if [ -f "$OUTPUT_DIR/$OUTPUT_FILE" ]; then
    FILE_SIZE=$(ls -lh "$OUTPUT_DIR/$OUTPUT_FILE" | awk '{print $5}')
    echo "📊 File size: $FILE_SIZE"
    
    # Show total dependencies count
    if command -v jq &> /dev/null; then
        TOTAL_DEPS=$(jq -r '.totalDependencies // 0' "$OUTPUT_DIR/$OUTPUT_FILE")
        echo "🔗 Total dependencies found: $TOTAL_DEPS"
    fi
fi
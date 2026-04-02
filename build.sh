#!/bin/bash
# Build script for F21AS Stage 2
echo "=== Building Stage 2 classes ==="
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin -sourcepath src @sources.txt
if [ $? -eq 0 ]; then
    echo "=== Compilation successful ==="
    echo "Supported run mode: project-root execution with external data/ and assets/"
    echo "Run from repository root with:"
    echo "  java -cp bin coffeeshop.CoffeeShopApp"
else
    echo "=== Compilation FAILED ==="
    exit 1
fi

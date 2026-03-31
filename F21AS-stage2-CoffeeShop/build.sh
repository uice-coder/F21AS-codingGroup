#!/bin/bash
# Build script for F21AS Stage 2
echo "=== Compiling Stage 2 ==="
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin -sourcepath src @sources.txt
if [ $? -eq 0 ]; then
    echo "=== Compilation successful ==="
    echo "=== Creating JAR ==="
    cp -r data bin/data 2>/dev/null || true
    cp -r assets bin/assets 2>/dev/null || true
    jar cfe CoffeeShop.jar coffeeshop.CoffeeShopApp -C bin .
    echo "=== JAR created: CoffeeShop.jar ==="
    echo "Run with: java -jar CoffeeShop.jar"
else
    echo "=== Compilation FAILED ==="
    exit 1
fi

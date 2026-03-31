@echo off
echo === Compiling Stage 2 ===
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin -sourcepath src @sources.txt
if %ERRORLEVEL% EQU 0 (
    echo === Compilation successful ===
    echo === Creating JAR ===
    xcopy /E /Q data bin\data\ 2>nul
    xcopy /E /Q assets bin\assets\ 2>nul
    jar cfe CoffeeShop.jar coffeeshop.CoffeeShopApp -C bin .
    echo === JAR created: CoffeeShop.jar ===
    echo Run with: java -jar CoffeeShop.jar
) else (
    echo === Compilation FAILED ===
)
pause

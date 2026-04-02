@echo off
setlocal
echo === Building Stage 2 classes ===
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin -sourcepath src @sources.txt
if %ERRORLEVEL% EQU 0 (
    echo === Compilation successful ===
    echo Supported run mode: project-root execution with external data/ and assets/
    echo Run from repository root with:
    echo   java -cp bin coffeeshop.CoffeeShopApp
) else (
    echo === Compilation FAILED ===
    exit /b 1
)
endlocal

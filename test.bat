@echo off
setlocal
cd /d "%~dp0"

if not exist build mkdir build

javac --release 17 -encoding UTF-8 -d build src\*.java test\*.java
if errorlevel 1 (
    echo Compilation failed. Check the Java installation and file paths.
    pause
    exit /b 1
)

java -cp build sortapp.AppTests
if errorlevel 1 (
    echo Tests failed. Read the error above.
    pause
    exit /b 1
)

pause

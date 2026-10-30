@echo off
setlocal
cd /d "%~dp0"

if not exist build mkdir build

javac --release 17 -encoding UTF-8 -d build src\*.java
if errorlevel 1 (
    echo Compilation failed. Check that JDK 17 or newer is installed.
    pause
    exit /b 1
)

java -cp build sortapp.Main
if errorlevel 1 pause

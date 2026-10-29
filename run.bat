@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo Java JDK 17 or newer is required. Install a JDK and add its bin folder to PATH.
  pause
  exit /b 1
)
if not exist build mkdir build
javac --release 17 -encoding UTF-8 -d build src\sortapp\*.java
if errorlevel 1 (
  pause
  exit /b 1
)
java -cp build sortapp.Main
if errorlevel 1 pause

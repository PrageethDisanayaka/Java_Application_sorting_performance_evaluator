@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
javac --release 17 -encoding UTF-8 -d build src\sortapp\*.java tests\sortapp\*.java
if errorlevel 1 exit /b 1
java -cp build sortapp.AppTests
if errorlevel 1 exit /b 1
pause

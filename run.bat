@echo off
chcp 65001 >nul
cd /d "%~dp0"
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
  del sources.txt
  echo.
  echo Compile failed. Check that JDK 17+ is installed: java -version
  pause
  exit /b 1
)
del sources.txt
java -Dfile.encoding=UTF-8 -cp out sharedtrip.Main

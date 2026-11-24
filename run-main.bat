@echo off
cd /d "%~dp0"
title SmartHome Main Driver (8 Skenario PBO)

if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ========================================================
echo Menjalankan Pengujian 8 Skenario PBO...
echo ========================================================

call "%~dp0mvn.cmd" exec:java -Dexec.mainClass="com.smarthome.Main"

pause

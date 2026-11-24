@echo off
cd /d "%~dp0"
title SmartHome REST API Server (Port 8080)

if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ========================================================
echo Memulai SmartHome REST API Server di Port 8080...
echo ========================================================

call "%~dp0mvn.cmd" exec:java -Dexec.mainClass="com.smarthome.api.Server" "-Djava.net.preferIPv4Stack=true"

pause

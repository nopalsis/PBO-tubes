@echo off
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if exist "C:\maven\bin\mvn.cmd" (
    call "C:\maven\bin\mvn.cmd" -Dmaven.repo.local="%~dp0.m2\repository" %*
) else (
    call "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" -Dmaven.repo.local="%~dp0.m2\repository" %*
)

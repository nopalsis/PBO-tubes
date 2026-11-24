if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
}
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

if (Test-Path 'C:\maven\bin\mvn.cmd') {
    & 'C:\maven\bin\mvn.cmd' "-Dmaven.repo.local=$PSScriptRoot\.m2\repository" @args
} else {
    & 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd' "-Dmaven.repo.local=$PSScriptRoot\.m2\repository" @args
}

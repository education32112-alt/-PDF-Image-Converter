@rem Gradle execution script for Windows
@if "%DEBUG%"=="" @echo off
set DIRNAME=%~dp0
if exist "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" (
    java -jar "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %*
) else (
    gradle %*
)

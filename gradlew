#!/usr/bin/env sh
APP_HOME=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
    exec java -jar "$WRAPPER_JAR" "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "Error: Neither gradle-wrapper.jar nor gradle binary found in PATH." >&2
    echo "Please install Gradle 8.9+ or run with Android Studio." >&2
    exit 1
fi

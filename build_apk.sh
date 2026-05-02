#!/usr/bin/env bash
set -e
if command -v ./gradlew >/dev/null 2>&1; then
  ./gradlew assembleDebug
elif command -v gradle >/dev/null 2>&1; then
  gradle assembleDebug
else
  echo "Gradle is not installed. Open this folder in Android Studio and choose Build > Build APK(s)."
  exit 1
fi

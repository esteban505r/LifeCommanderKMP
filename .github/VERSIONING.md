# Versioning Guide

This project uses semantic versioning (MAJOR.MINOR.PATCH) across all platforms.

## Version Files

- **`VERSION`**: Contains current version numbers for all platforms
- **`.github/VERSION_HISTORY.md`**: Tracks release history and notes

## Branch Naming Convention

Release branches follow the pattern: `release/{platform}/{version}`

### Examples:
- `release/android/1.0.0`
- `release/desktop/1.2.3`
- `release/server/2.0.0`

## Version Code Calculation (Android)

Android `versionCode` is calculated automatically from version string:
- Formula: `major * 10000 + minor * 100 + patch`
- Example: `1.2.3` → `1 * 10000 + 2 * 100 + 3 = 10203`
- Supports versions up to `99.99.99`

## Creating a New Release

### 1. Update Version Files

Edit `VERSION` file:
```bash
ANDROID_VERSION=1.1.0
ANDROID_VERSION_CODE=10100
DESKTOP_VERSION=1.1.0
SERVER_VERSION=1.1.0
```

### 2. Create Release Branch

For each platform you want to release:

```bash
# Android
git checkout -b release/android/1.1.0
git push origin release/android/1.1.0

# Desktop
git checkout -b release/desktop/1.1.0
git push origin release/desktop/1.1.0

# Server
git checkout -b release/server/1.1.0
git push origin release/server/1.1.0
```

### 3. CI/CD Automation

Pushing to a release branch automatically triggers:
- **Android**: Builds APK and uploads to Google Drive
- **Desktop**: Builds distributions (Linux, macOS, Windows) and uploads to Google Drive
- **Server**: Builds Docker image and deploys

## Version Format

- Supports formats: `1.0.0`, `v1.0.0`, `1.2.3-beta`
- The 'v' prefix is automatically stripped
- Version is extracted from branch name after `release/{platform}/`

## Environment Variables

Versions can be set via environment variables (used in CI/CD):

- `APP_VERSION_NAME`: Android/Desktop version name
- `APP_VERSION_CODE`: Android version code (integer)
- `SERVER_VERSION`: Server version

These are automatically set by the GitHub Actions workflows based on branch names.

## Manual Version Override

To override versions locally or in CI:

```bash
# Android
APP_VERSION_NAME=1.2.3 APP_VERSION_CODE=10203 ./gradlew :composeApp:assembleRelease

# Desktop
APP_VERSION_NAME=1.2.3 ./gradlew :composeApp:packageReleaseDistributionForMacosX64

# Server
SERVER_VERSION=1.2.3 ./gradlew :server:shadowJar
```

## Current Versions

See `VERSION` file for current version numbers.


# Version History

This document tracks version releases across all platforms.

## Version 1.0.0 - Initial Release

**Release Date:** 2025-11-19

### Android
- Version Name: 1.0.0
- Version Code: 10000
- Branch: `release/android/1.0.0`

### Desktop
- Version: 1.0.0
- Branch: `release/desktop/1.0.0`
- Platforms: Linux, macOS, Windows

### Server
- Version: 1.0.0
- Branch: `release/server/1.0.0`

---

## How to Create a New Release

1. **Update VERSION file** with new version numbers
2. **Create release branch** for the platform:
   ```bash
   git checkout -b release/android/1.1.0
   git checkout -b release/desktop/1.1.0
   git checkout -b release/server/1.1.0
   ```
3. **Push the branch** to trigger the CI/CD pipeline
4. **Update this file** with release notes


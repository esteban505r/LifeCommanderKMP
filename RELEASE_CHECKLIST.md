# Release Checklist

Use this checklist when preparing a new release.

## Pre-Release

- [ ] Update `VERSION` file with new version numbers
- [ ] Update `.github/VERSION_HISTORY.md` with release notes
- [ ] Ensure all tests pass
- [ ] Review and update CHANGELOG (if applicable)
- [ ] Verify build configurations are correct

## Create Release Branches

- [ ] Create `release/android/{version}` branch
- [ ] Create `release/desktop/{version}` branch
- [ ] Create `release/server/{version}` branch

## Push and Verify

- [ ] Push Android release branch → Verify CI builds APK
- [ ] Push Desktop release branch → Verify CI builds all platforms
- [ ] Push Server release branch → Verify CI builds and deploys Docker image

## Post-Release

- [ ] Verify artifacts are uploaded to Google Drive (Android/Desktop)
- [ ] Verify Docker image is pushed to GHCR (Server)
- [ ] Verify deployment is successful (Server)
- [ ] Tag the release in Git (optional but recommended)
- [ ] Update documentation if needed

## Version Bump Example

```bash
# 1. Update VERSION file
# 2. Commit changes
git add VERSION .github/VERSION_HISTORY.md
git commit -m "Bump version to 1.1.0"

# 3. Create and push release branches
git checkout -b release/android/1.1.0
git push origin release/android/1.1.0

git checkout -b release/desktop/1.1.0
git push origin release/desktop/1.1.0

git checkout -b release/server/1.1.0
git push origin release/server/1.1.0

# 4. Return to main
git checkout main
```


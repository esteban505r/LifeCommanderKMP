# FCM Token Retrieval Troubleshooting Guide

## Issue: SERVICE_NOT_AVAILABLE Error

The `SERVICE_NOT_AVAILABLE` error occurs when trying to retrieve FCM tokens. This is a common issue with several possible causes.

## Final Solution Applied

### 1. Created Shared FCM Service
- **Problem**: Library modules (onboarding_presentation) were trying to access Firebase directly
- **Solution**: Created `FcmTokenService` in core-ui module to handle FCM token retrieval
- **Reason**: Library modules shouldn't access Firebase directly; they should use shared services

### 2. Fixed Google Services Plugin Configuration
- **Problem**: Google Services plugin was applied to library modules (onboarding_presentation, core-ui)
- **Fix**: Removed Google Services plugin from library modules, kept only in main app module (composeApp)
- **Reason**: Google Services plugin should only be applied to the main application module, not library modules

### 3. Updated AuthViewModel
- **Problem**: AuthViewModel was trying to use Firebase directly
- **Solution**: Updated AuthViewModel to inject and use FcmTokenService
- **Result**: Clean separation of concerns and proper dependency injection

## Architecture Changes

### Before (Problematic)
```
onboarding_presentation (library module)
├── AuthViewModel (tries to access Firebase directly)
└── build.gradle.kts (has Google Services plugin ❌)

core-ui (library module)
└── build.gradle.kts (has Google Services plugin ❌)

composeApp (main app module)
├── google-services.json
└── build.gradle.kts (has Google Services plugin ✅)
```

### After (Fixed)
```
onboarding_presentation (library module)
├── AuthViewModel (uses FcmTokenService)
└── build.gradle.kts (no Google Services plugin ✅)

core-ui (library module)
├── FcmTokenService (handles Firebase access)
└── build.gradle.kts (no Google Services plugin ✅)

composeApp (main app module)
├── google-services.json
└── build.gradle.kts (has Google Services plugin ✅)
```

## Common Causes of SERVICE_NOT_AVAILABLE

### 1. Emulator Without Google Play Services
- **Symptom**: SERVICE_NOT_AVAILABLE error on emulator
- **Solution**: Test on physical device with Google Play Services
- **Detection**: Code now logs when running on emulator

### 2. Missing Google Play Services
- **Symptom**: SERVICE_NOT_AVAILABLE on physical device
- **Solution**: Install/update Google Play Services
- **Check**: Verify Google Play Services is available and up to date

### 3. Firebase Configuration Issues
- **Symptom**: SERVICE_NOT_AVAILABLE despite having Google Play Services
- **Check**: Verify google-services.json is in composeApp module
- **Check**: Verify Firebase project is properly configured

### 4. Network Issues
- **Symptom**: SERVICE_NOT_AVAILABLE with network errors
- **Solution**: Check internet connectivity
- **Solution**: Check firewall/proxy settings

## Testing Recommendations

### 1. Physical Device Testing
```bash
# Test on a physical device with Google Play Services
# This is the most reliable way to test FCM functionality
```

### 2. Emulator Testing
```bash
# If testing on emulator, expect SERVICE_NOT_AVAILABLE
# The app will gracefully handle this and continue without FCM
# Check logs for emulator detection messages
```

### 3. Debug Logs
```bash
# Look for these log messages:
# - "Device appears to be an emulator"
# - "SERVICE_NOT_AVAILABLE detected - this is usually not recoverable"
# - "FCM token retrieval failed after X attempts"
```

## Expected Behavior

### On Physical Device with Google Play Services
- FCM token retrieval should succeed
- Login request includes FCM token
- Push notifications work

### On Emulator or Device Without Google Play Services
- FCM token retrieval fails with SERVICE_NOT_AVAILABLE
- Login continues without FCM token
- App shows appropriate warning messages
- In-app notifications still work

## Verification Steps

1. **Check Build Configuration**
   ```bash
   # Verify only composeApp has Google Services plugin
   grep -r "googleServices" */build.gradle.kts
   ```

2. **Check Firebase Configuration**
   ```bash
   # Verify google-services.json exists in composeApp
   ls composeApp/google-services.json
   ```

3. **Check Logs**
   ```bash
   # Look for FCM-related log messages during login
   adb logcat | grep -i "fcm\|firebase\|authviewmodel\|fcmtokenservice"
   ```

## Alternative Notification Strategies

When FCM is not available, the app falls back to:
- In-app notifications (when app is open)
- Local notifications (scheduled reminders)
- Email notifications (if configured)
- SMS notifications (if configured)
- WebSocket real-time updates (when app is connected)

## Key Files Modified

1. **core-ui/src/main/java/com/esteban/ruano/core_ui/services/FcmTokenService.kt** (NEW)
   - Shared service for FCM token retrieval
   - Handles Firebase initialization checks
   - Provides detailed error logging

2. **onboarding/onboarding_presentation/src/main/java/com/esteban/lopez/onboarding_presentation/auth/viewmodel/AuthViewModel.kt** (MODIFIED)
   - Removed direct Firebase imports
   - Added FcmTokenService injection
   - Simplified FCM token retrieval logic

3. **onboarding/onboarding_presentation/build.gradle.kts** (MODIFIED)
   - Removed Google Services plugin
   - Kept Firebase dependencies for classes only

4. **core-ui/build.gradle.kts** (MODIFIED)
   - Removed Google Services plugin
   - Kept Firebase dependencies for classes only

## Next Steps

If you're still experiencing issues:

1. Test on a physical device with Google Play Services
2. Check the detailed logs for specific error messages
3. Verify Firebase project configuration
4. Consider using alternative notification methods for development/testing 
# How to View Logcat in Android Studio

## Step 1: Connect Physical Device
1. Enable USB Debugging on your device:
   - Settings → Developer Options → USB Debugging (ON)
2. Connect device via USB cable
3. Android Studio should detect it automatically

## Step 2: Open Logcat Window
1. In Android Studio, go to: **View → Tool Windows → Logcat**
   - Or press: **Alt + 6**
2. Logcat panel opens at bottom of screen

## Step 3: Filter for Health Errors
1. In the Logcat search box, type: `FirestoreRepository|HealthQuestionnaire`
2. Or filter by app name: `com.fida.app`

## Step 4: Reproduce the Error
1. On your device, navigate to Health tab
2. Tap "Update Profile"
3. Fill questionnaire and tap "Submit"
4. Watch Logcat for error messages

## Step 5: Share the Error
Look for lines with:
- `E/FirestoreRepository` (errors)
- `D/FirestoreRepository` (debug info)
- `E/HealthQuestionnaire` (errors)

Copy the full error message and share it.

## Example Error Output
```
E/FirestoreRepository: Failed to save health profile: Permission denied
E/FirestoreRepository: Failed to check user document: PERMISSION_DENIED: Missing or insufficient permissions.
```

## Common Errors & Fixes

### "PERMISSION_DENIED"
- Firestore security rules don't allow write access
- Need to update Firebase rules

### "UID is empty"
- User not logged in properly
- Check if uid is being saved to SharedPreferences

### "Network error"
- No internet connection
- Check WiFi/mobile data

### "User document doesn't exist"
- This is expected - app will create it
- If this fails, it's a permissions issue

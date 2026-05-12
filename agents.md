# FIDA App - HANDOFF Notes

**Last Updated:** 2026-05-13 (UI Overlaps Fixed + Summary Aligned)
**Project:** FIDA - Gamified Fitness Android App (Kotlin + Firebase)

---

## Project Overview

- **Type:** Native Android app (NOT React Native)
- **Language:** Kotlin
- **Architecture:** Traditional Multi-Activity + Fragments (NO Hilt, NO ViewModels)
- **UI:** XML ViewBinding + DataBinding (NOT Jetpack Compose)
- **Backend:** Firebase (Auth, Firestore, Realtime Database) via direct SDK calls
- **State:** SharedPreferences (`PreferenceHelper`) + direct Firestore reads/writes
- **Async:** Firebase callbacks (`addOnSuccessListener` / `addOnFailureListener`)
- **DI:** None — singleton objects (`FirestoreRepository`, `GameManager`) + manual instantiation
- **Build:** Gradle with Kotlin DSL
- **compileSdk:** 36 / **minSdk:** 28 / **targetSdk:** 36

**Repo also contains a separate React web app** (`src/`, `dist/`) but it's independent from the Android app.

### Architecture Diagram
```
SplashActivity → LoginActivity → OnboardingActivity
    → ProfileSetup1 → ProfileSetup2 → AvatarCustomization → ProfileSetup3
    → HomeActivity (5-tab BottomNav: Dashboard, Activities, Achievements, Leaderboard, Profile)

MainActivity = legacy drawer-based main screen (still exists in manifest)

Feature Activities (launched from Dashboard/fragments via Intent):
  WaterIntakeActivity, RecordSleepActivity, PreRunActivity,
  GamificationActivity, QuestBoardActivity, BadgeSystemActivity,
  DailyRewardActivity, ShopActivity, StepCounterActivity, SettingsActivity, ...

Singletons:
  FirestoreRepository – CRUD helpers for Firestore
  GameManager – XP, levels, streaks, badge logic
  PreferenceHelper – SharedPreferences wrapper (per-activity instantiation)
```

---

## Current Build Status: SUCCESS ✅

**Last Build:** 2026-05-13 - `./gradlew.bat --no-daemon assembleDebug`
**APK Location:** `app/build/outputs/apk/debug/app-debug.apk`
**Build Time:** ~2m

UI Overlap in Step Counter and alignment in Run Summary fixed. Build verified.

---

## Screen Implementation Status: 40/40 (100%)

| Category | Screens | Status |
|----------|---------|--------|
| Auth Flow | 4 | ✅ COMPLETE |
| Profile Setup | 4 | ✅ COMPLETE |
| Core Navigation | 5 | ✅ COMPLETE |
| Running Flow | 6 | ✅ COMPLETE |
| Water Intake | 3 | ✅ COMPLETE |
| Sleep Recording | 3 | ✅ COMPLETE |
| Step Tracking | 1 | ✅ COMPLETE (Sensor Auto-Tracking) |
| Gamification | 8 | ✅ COMPLETE |
| Settings & Utility | 5 | ✅ COMPLETE |
| Support & Misc | 3 | ✅ COMPLETE |

---

## UI & Layout Fixes — 2026-05-13

### Summary of Changes

**1. Pre-Run Goal Selection**
- Fixed RadioButton visibility in `activity_pre_run.xml`.
- Changed text color from white to black (`#222222`).
- Selection now clear on white background.

**2. Run Summary Alignment**
- Rewrote `activity_run_summary.xml` for tidy alignment.
- Added `llStatsContainer` (LinearLayout card) to group stats.
- Grouped labels (left) and values (right) in vertical rows.
- Added Pace and Calories display.
- Fixed XML syntax error with escaped quotes in pace text.

**3. Step Counter Overlap**
- Fixed `activity_step_counter.xml` layout.
- Anchored `ScrollView` above `btnBackToMain` to prevent overlap.
- Changed content container to `LinearLayout` for better vertical flow.
- Adjusted FAB height and constraints to prevent screen cutoff.

### Files Modified

| File | Changes |
|------|---------|
| `activity_pre_run.xml` | Fixed RadioButton text colors. |
| `activity_run_summary.xml` | Major layout rewrite; added stats card; fixed XML escaping. |
| `RunSummaryActivity.kt` | Added logic to display pace and calories. |
| `RunTrackingActivity.kt` | Added data passing for calories and uid. |
| `activity_step_counter.xml` | Fixed ScrollView constraints; fixed FAB visibility. |
| `card_background.xml` | Created new drawable for summary card styling. |

---

## Critical Launch Crash Fixes — 2026-05-11

### Overview
Fixed critical crash that prevented app from launching on Android 12+ (API 31+). The crash occurred in `StepTrackingService` due to foreground service restrictions and permission requirements.

### Root Causes

**1. ForegroundServiceStartNotAllowedException**
- `StepTrackingService` was started in `HomeActivity.onCreate()` before the activity was fully foreground
- Android 12+ blocks foreground service starts from background apps

**2. SecurityException (health service type)**
- `foregroundServiceType="health"` requires `ACTIVITY_RECOGNITION` runtime permission
- App was trying to start service before permission was granted

**3. Facebook LoginButton Inflation Crash**
- `activity_login.xml` contained `com.facebook.login.widget.LoginButton`
- Facebook SDK metadata was commented out in AndroidManifest (no app ID configured)
- View inflation crashed before `onCreate()` even ran

### Files Modified

| File | Changes |
|------|---------|
| `AndroidManifest.xml` | Changed `foregroundServiceType` from `"health"` to `"specialUse"`; added `FOREGROUND_SERVICE_SPECIAL_USE` permission; added `ACCESS_COARSE_LOCATION` permission (lint fix) |
| `HomeActivity.kt` | Moved `StepTrackingService.start()` from `onCreate()` to `onResume()`; added `ACTIVITY_RECOGNITION` runtime permission check; added permission request launcher; added imports for `Manifest`, `PackageManager`, `Build`, `ActivityResultContracts`, `ContextCompat` |
| `activity_login.xml` | Replaced `com.facebook.login.widget.LoginButton` with regular `Button` (hidden by default since Facebook not configured) |
| `LoginActivity.kt` | Commented out Facebook SDK initialization code (`callbackManager`, `isFacebookEnabled`, `setupFacebookLogin()`, `firebaseAuthWithFacebook()`) |
| `activity_run_summary.xml` | Fixed `Button` → `com.google.android.material.button.MaterialButton` (WrongViewCast lint error) |

### Permission Changes

| Permission | Type | Purpose |
|------------|------|---------|
| `FOREGROUND_SERVICE_SPECIAL_USE` | Manifest | Required for `specialUse` foreground service type (Android 14+) |
| `ACCESS_COARSE_LOCATION` | Manifest | Required alongside `ACCESS_FINE_LOCATION` (lint fix) |
| `ACTIVITY_RECOGNITION` | Runtime | Requested at runtime in `HomeActivity.onResume()` before starting service |

### Foreground Service Type Change

Changed from `health` to `specialUse`:
```xml
<!-- Before -->
<service android:name=".services.StepTrackingService"
    android:foregroundServiceType="health" />

<!-- After -->
<service android:name=".services.StepTrackingService"
    android:foregroundServiceType="specialUse" />
```

**Why:** The `health` service type requires Health Connect permissions (`READ_HEART_RATE`, etc.) which this app doesn't use. `specialUse` is more appropriate for a step counter that doesn't integrate with Health Connect.

### HomeActivity Service Start Pattern

**Before (crashed):**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    StepTrackingService.start(this)  // ❌ Called too early
}
```

**After (works):**
```kotlin
override fun onResume() {
    super.onResume()
    startStepTrackingServiceIfNeeded()
}

private fun startStepTrackingServiceIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val permission = Manifest.permission.ACTIVITY_RECOGNITION
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            StepTrackingService.start(this)
        } else {
            activityRecognitionPermissionLauncher.launch(permission)
        }
    } else {
        StepTrackingService.start(this)
    }
}
```

---

## Running Flow Fixes — 2026-05-11 (Session 2)

### Overview
Fixed issues in the running activities where users couldn't select goal types (RadioGroup not working) and "API Error" was occurring due to Google Play Services/Location API issues.

### Root Causes

**1. RadioGroup Layout Constraints Issue**
- `activity_pre_run.xml` had incorrect horizontal chain constraints
- RadioGroup was squeezed between other elements, making selection impossible
- ConstraintLayout horizontal chain prevented proper rendering

**2. Google Play Services API Error**
- `RunTrackingActivity` didn't check if Google Play Services was available
- No error handling for `SecurityException` when accessing location
- Permission request callback not implemented properly
- No fallback when location permission denied

**3. Countdown Activity Robustness**
- `RunCountdownActivity` had no null checks for intent extras
- No validation for uid or goalValue before starting tracking

### Files Modified

| File | Changes |
|------|---------|
| `activity_pre_run.xml` | **Rewrote layout** — Fixed constraint issues; changed to proper vertical layout with full-width RadioGroup; added proper padding, spacing, buttonTint; added LinearLayout for goal value + unit; used MaterialButton |
| `PreRunActivity.kt` | **Rewrote** — Added dynamic unit label update (km/min/kcal); added dynamic hint based on goal type; improved validation; added RadioGroup listener |
| `RunTrackingActivity.kt` | **Major rewrite** — Added Google Play Services availability check; added proper error handling (SecurityException, general exceptions); implemented permission request callback; added location filtering for GPS jumps (>100m/s); added cleanup in onDestroy(); timer works even without location permission |
| `RunCountdownActivity.kt` | **Rewrote** — Added null checks for intent extras; added validation for uid/goalValue; added error handling when starting tracking; added countdown timer cleanup |

### PreRunActivity Improvements

**Dynamic Unit Label:**
```kotlin
private fun updateUnitLabel(checkedId: Int) {
    val unit = when (checkedId) {
        R.id.rbDistance -> "km"
        R.id.rbTime -> "min"
        R.id.rbCalories -> "kcal"
        else -> "km"
    }
    tvGoalUnit.text = unit
    
    // Dynamic hint: 5km, 30min, 300kcal
    etGoalValue.hint = when (checkedId) {
        R.id.rbDistance -> "5"
        R.id.rbTime -> "30"
        R.id.rbCalories -> "300"
        else -> "5"
    }
}
```

### RunTrackingActivity Error Handling

**Google Play Services Check:**
```kotlin
private fun checkGooglePlayServicesAndStartTracking() {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
    
    if (resultCode != ConnectionResult.SUCCESS) {
        if (googleApiAvailability.isUserResolvableError(resultCode)) {
            googleApiAvailability.getErrorDialog(this, resultCode, 1000)?.show()
        } else {
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_LONG).show()
        }
        // Start timer anyway even without location
        startTimerOnly()
        return
    }
    startTracking()
}
```

**Location Filtering (removes GPS glitches):**
```kotlin
// Filter out unrealistic jumps (>100m in 1 second)
if (dist < 100) {
    distanceMeters += dist
}
```

### Run Flow Activities

| Activity | Purpose | Fixed Issues |
|----------|---------|--------------|
| `PreRunActivity` | Set goal type (Distance/Time/Calories) and goal value | RadioGroup selection, unit label updates |
| `RunCountdownActivity` | 5-second countdown before run starts | Intent validation, error handling |
| `RunTrackingActivity` | GPS tracking, timer, distance calculation | Google Play Services check, permission handling, location filtering |
| `RunSummaryActivity` | Display run results after completion | Already working (WrongViewCast fixed earlier) |

---

## Step Tracker Implementation — 2026-05-06

### Overview
Added a new **Step Tracker** feature with automatic step counting using Android's hardware step counter sensor (TYPE_STEP_COUNTER). The feature includes:

1. **Dashboard Integration** - New Steps card showing today's steps, distance (km), and progress toward daily target
2. **Foreground Service** - `StepTrackingService` runs continuously to track steps even when app is closed
3. **Boot Receiver** - `BootReceiver` restarts tracking service after device reboot
4. **Real-time Updates** - StepCounterActivity refreshes every 5 seconds while open

### Sensor Implementation Details

**How Step Counter Works:**
- Android's `TYPE_STEP_COUNTER` sensor reports **total steps since device boot**
- We track a "baseline" value at the start of each day
- Daily steps = current sensor value - baseline value
- Baseline resets automatically at midnight (detected via date check)

**Data Storage:**
- Baseline steps stored in SharedPreferences: `baseline_steps`
- Baseline date stored in SharedPreferences: `baseline_date`
- Daily step count saved to Firestore: `users/{uid}/allDays/{date}.stepCounter`
- Step target synced to both SharedPreferences (`max_steps`) and Firestore (`maxStep`)

### Files Created

| File | Purpose |
|------|---------|
| `services/StepTrackingService.kt` | Foreground service with SensorEventListener for step counting |
| `services/BootReceiver.kt` | BroadcastReceiver to restart service on device boot |

### Files Modified

| File | Changes |
|------|---------|
| `AndroidManifest.xml` | Added permissions (FOREGROUND_SERVICE, FOREGROUND_SERVICE_HEALTH, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK), registered service and boot receiver |
| `fragment_dashboard.xml` | Added Steps CardView with icon, summary, distance, progress bar, and "View Details" button |
| `DashboardFragment.kt` | Added step data loading, distance calculation, progress bar updates, navigation to StepCounterActivity |
| `StepCounterActivity.kt` | Added service start call, periodic refresh handler (5s), maxSteps save to SharedPreferences |
| `HomeActivity.kt` | Added StepTrackingService.start() call to begin tracking on app launch |
| `PreferenceHelper.kt` | Added methods: `saveBaselineSteps()`, `getBaselineSteps()`, `saveBaselineDate()`, `getBaselineDate()`, `saveMaxSteps()`, `getMaxSteps()` |

### Permissions Required

| Permission | Purpose |
|------------|---------|
| `ACTIVITY_RECOGNITION` | Required for step counter sensor access (runtime permission) |
| `FOREGROUND_SERVICE` | Run service in foreground |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Special use foreground service (Android 14+) — changed from `health` |
| `POST_NOTIFICATIONS` | Show step tracking notification |
| `RECEIVE_BOOT_COMPLETED` | Restart service after device reboot |
| `WAKE_LOCK` | Keep device awake for tracking |
| `ACCESS_FINE_LOCATION` | Location access (for future features) |
| `ACCESS_COARSE_LOCATION` | Coarse location (required alongside FINE on Android 12+) |

### Testing on Xiaomi Device

**Steps to test:**
1. Install APK on Xiaomi phone
2. Grant **Activity Recognition** permission when prompted (critical for step sensor)
3. Grant **Notifications** permission
4. Walk around - steps should be tracked automatically
5. Check Dashboard - Steps card should update with current count and distance
6. Open StepCounterActivity - should show real-time updates every 5 seconds
7. Set step target - notification will show progress
8. Reboot device - service should restart automatically

**Note:** Some Xiaomi devices have aggressive battery optimization that may kill foreground services. Users may need to:
- Disable battery optimization for FIDA app
- Allow app to run in background (MIUI settings)

---

## Critical Runtime Fixes — 2026-05-03 (Session 2)

### Root Cause: Firestore Document ID Mismatch

**The Problem:** Most of the app uses **Firebase UID** as the Firestore document ID (`users/{uid}`). But `MainActivity`, `ShopActivity`, and `StepCounterActivity` were using **`username`** (a display string) as the document ID. When users logged in via email/password or Google, their Firestore document is keyed by UID, not username — so these 3 screens would **never find user data**.

### Files Fixed

| File | Changes |
|------|---------|
| `MainActivity.kt` | Replaced `safeUsername` (display name) with `safeUid` (Firebase UID) for all Firestore queries. Kept `safeUsername` for UI display only. Added `PreferenceHelper` import. Added `uid` intent extras for card navigation. All `loadData()`, `updateToDate()`, `updateWaterCounter()` now use `uid`. |
| `ShopActivity.kt` | Replaced `safeUsername` with `safeUid` for Firestore operations. Fixed null-safe parsing for `health`/`coin` fields (`(doc.get("coin") as? Long)?.toInt() ?: 0`). Removed unused `purchase()` health leak. Added guard checks for empty `uid`. |
| `StepCounterActivity.kt` | Replaced `safeUsername` with `safeUid` for Firestore document ID. Added `PreferenceHelper` import. All `loadData()`, `sendDataToScreen()`, `updateStepTarget()` now use `uid`. |
| `WaterIntakeActivity.kt` | Added `uid` from `PreferenceHelper` for Firestore. `addWater()` now persists to Firestore via `FirestoreRepository.logDailyActivity()`. `awardGoalXP()` now saves XP to Firestore (not just SharedPreferences). Added missing `android.view.View` import. Fixed `progress` type (Int → Float). |
| `RecordSleepActivity.kt` | Added `uid` from `PreferenceHelper` for Firestore. `logSleep()` now saves `sleepHours` to Firestore via `FirestoreRepository.logDailyActivity()`. `awardSleepXP()` now saves to Firestore. |
| `ProfileFragment.kt` | Fixed `profilePicUrl` → `profilePicture` (matching User model field). Added `GameManager` import. `loadBadgesFromData()` now maps badge IDs to human-readable titles using `GameManager.allBadges` definitions instead of showing raw IDs like "run_5". |
| `AccountSettingsFragment.kt` | **Fixed profile photo save flow.** `updateProfilePhoto()` now saves photo URL to Firestore `profilePicture` field (not just Firebase Auth). Initial photo load also reads from Firestore with Auth fallback. |

### Upload Feature Fix — Detail

**Root Cause:** Profile photo had a broken save-and-display flow. When a user uploaded a photo:
1. ✅ Photo uploaded to Firebase Storage
2. ✅ Photo URL saved to **Firebase Auth** (`user.updateProfile().setPhotoUri()`)
3. ❌ Photo URL was **never saved to Firestore**
4. ❌ `ProfileFragment` reads photo from **Firestore** field `profilePicture` → always saw placeholder

**Fix applied to `AccountSettingsFragment.kt`:**
- `updateProfilePhoto()` now writes the download URL to Firestore `profilePicture` field after uploading
- `setupViews()` now loads the initial photo from Firestore first, with Auth photoUrl as fallback
- Both `AccountSettingsFragment` and `ProfileFragment` now read from the same Firestore field (`profilePicture`)

---

## Authentication System (Updated 2026-05-03)

### Login Flow
1. User enters **username** and password
2. App queries Firestore to find user by username
3. Gets user's **email** from Firestore document
4. Uses Firebase Auth `signInWithEmailAndPassword(email, password)` to authenticate
5. On success, saves UID to SharedPreferences and navigates to HomeActivity

### Signup Flow
1. User enters email, password, confirm password
2. Password validated for strength (8+ chars, uppercase, lowercase, number, special char)
3. Firebase Auth `createUserWithEmailAndPassword()` creates account
4. Firestore document created with default user data
5. User redirected to ProfileSetup1Activity

### Forgot Password Flow
1. User taps "Forgot Password?" on login screen
2. Enters email address in dialog
3. Firebase Auth sends password reset email
4. User clicks link in email and sets new password
5. User can login with new password via Firebase Auth

### Google Sign-In Flow
1. User taps Google Sign-In button
2. Google Sign-In dialog appears
3. User selects Google account
4. Firebase Auth authenticates with Google credential
5. If new user, Firestore document created automatically
6. User redirected to ProfileSetup1Activity (new) or HomeActivity (existing)

### Strong Password Requirements
- Minimum 8 characters
- At least 1 uppercase letter (A-Z)
- At least 1 lowercase letter (a-z)
- At least 1 number (0-9)
- At least 1 special character (!@#$%^&*)

---

## Remaining Tasks

### Priority 1: Testing & Deployment
| Task | Status |
|------|--------|
| Test on device | ⚠️ Needs verification |
| Facebook Sign-In setup | ❌ Not started (App ID needed) |
| Google Sign-In SHA-1 verification | ⚠️ Needs verification |

### Priority 2: Optional Enhancements
| Task | Status |
|------|--------|
| Migrate GoogleSignIn to Credential Manager | Optional |
| Add Lottie animations (level up, badge unlock) | Optional |
| Architecture migration (MVVM + ViewModel + Hilt) | Optional (large refactor) |
| RunTrackingActivity timer cleanup | Optional |

---

## Critical Notes for Windows Development

### JVM Crash Prevention (CRITICAL)
**ALWAYS use `--no-daemon` flag** when building on Windows:
```bash
./gradlew.bat --no-daemon assembleDebug
```

### File Access Rules for Claude Code on Windows
1. **Use Glob tool** for file discovery (NOT `find` or `ls`)
2. **Use Grep tool** for code search (NOT `grep` or `rg`)
3. **Use Read tool** to view files (NOT `cat`, `head`, `tail`)
4. **Use Edit tool** for changes (NOT `sed`, `awk`)
5. **Use Bash tool** only for Gradle builds and git operations

### Dialog Button Text Color Fix Pattern
```kotlin
alertDialog.setOnShowListener {
    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.blue, theme))
    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.grey_500, theme))
}
```

---

## Firestore Document ID Convention

**CRITICAL RULE:** All Firestore document operations must use **Firebase UID** (`prefs.getUid()`) as the document ID — NOT the display username.

```
✅ Correct:
  db.collection("users").document(uid)   // uid from PreferenceHelper

❌ Wrong (will fail to find user data):
  db.collection("users").document(username)  // username is display name, not doc ID
```

---

## Firebase Configuration

- **Required:** `app/google-services.json` must exist
- **Services:** Auth, Firestore, Realtime Database, Analytics
- **Document ID:** Always use Firebase UID, not username

### Key Firestore Collections
- `users/{uid}` - User profiles, stats, badges
- `users/{uid}/runs` - Run history
- `users/{uid}/allDays` - Daily activity data
- `users/{uid}/quests` - User quests
- `users/{uid}/challenges` - User challenges
- `achievements` - Global achievements list
- `rewards` - Shop rewards

---

## Social Login Status

### Google Sign-In
- ✅ Client ID configured in strings.xml
- ⚠️ SHA-1 certificate: `B3:C8:E7:4B:29:D9:47:80:BC:D0:8E:56:98:34:B2:21:29:05:01:B0`
- ⚠️ Deprecation warnings - consider Credential Manager migration

### Facebook Sign-In
- ❌ **DISABLED** — Facebook SDK not configured
- LoginButton replaced with regular Button
- To re-enable: Add App ID to strings.xml, uncomment AndroidManifest metadata, restore LoginActivity code

---

## UI Styling Reference

### Button Styles (themes.xml)
```xml
<style name="Widget.fida.Button" parent="Widget.Material3.Button">
    <item name="android:textColor">@color/white</item>
    <item name="backgroundTint">@color/blue</item>
</style>

<style name="Widget.fida.Button.Outlined" parent="Widget.Material3.Button.OutlinedButton">
    <item name="android:textColor">@color/blue</item>
</style>

<style name="Widget.fida.Button.Text" parent="Widget.Material3.Button.TextButton">
    <item name="android:textColor">@color/blue</item>
</style>
```

---

## Known Issues & Gotchas

1. **JVM Crashes on Windows:** ALWAYS use `--no-daemon` flag
2. **Firestore Doc ID:** ALWAYS use `uid` from `PreferenceHelper`, never `username`
3. **Button text visibility:** Use `Widget.fida.Button` styles or explicit `textColor`
4. **Dialog button colors:** Override with `setOnShowListener`
5. **Facebook SDK:** DISABLED — LoginButton replaced with regular Button
6. **Step sensor:** TYPE_STEP_COUNTER may not exist on all devices
7. **Step baseline reset:** Steps reset to 0 at midnight automatically
8. **MIUI battery:** Xiaomi devices may kill foreground services
9. **Foreground Service Type:** Use `specialUse` (not `health`)
10. **Service start timing:** Use `onResume()` not `onCreate()` for foreground services
11. **Permission before service:** Check `ACTIVITY_RECOGNITION` before service start
12. **Google Play Services:** Always check availability before location APIs
13. **GPS glitches:** Filter jumps >100m/second
14. **RadioGroup constraints:** Use full width (`0dp`), avoid horizontal chains

---

## Quick Command Reference

```bash
# Build (Windows - ALWAYS use --no-daemon)
./gradlew.bat --no-daemon assembleDebug

# Clean build
./gradlew.bat --no-daemon clean assembleDebug

# ADB (Windows path)
"C:/Users/FATHI/AppData/Local/Android/Sdk/platform-tools/adb.exe"

# Grant permissions
adb shell pm grant com.fida.app android.permission.ACTIVITY_RECOGNITION
adb shell pm grant com.fida.app android.permission.POST_NOTIFICATIONS
```

---

## Files Created (2026-05-06)

| File | Purpose |
|------|---------|
| `services/StepTrackingService.kt` | Foreground service with step counter sensor |
| `services/BootReceiver.kt` | Restart service on device boot |
| `layout_custom_water_dialog.xml` | Custom water amount dialog |
| `drawable/ic_back_arrow.xml` | Back navigation icon |

---

## Tips for Future Claude Sessions

1. **Read HANDOFF.md first** — Current state, build status, recent changes
2. **Use `--no-daemon`** for ALL Gradle builds on Windows
3. **Firestore Doc ID** — Always use `uid` from `PreferenceHelper`, NOT `username`
4. **Button visibility** — Use `Widget.fida.Button` styles or explicit `textColor="@color/white"`
5. **Dialog button colors** — Override with `setOnShowListener` pattern
6. **Build verification** — Run `./gradlew.bat --no-daemon assembleDebug` after changes
7. **Badge titles** — Use `GameManager.allBadges` for human-readable names
8. **Step tracking** — TYPE_STEP_COUNTER sensor; use baseline tracking for daily count
9. **Foreground service** — Use `specialUse` type; permission check before start
10. **Xiaomi devices** — May need to disable battery optimization for step tracking
11. **Google Play Services check** — Always verify availability before location APIs
12. **RadioGroup in ConstraintLayout** — Use full width, avoid horizontal chains
13. **Intent validation** — Always validate intent extras (null checks, defaults)
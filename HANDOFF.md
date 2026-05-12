# FIDA App - HANDOFF Notes

**Last Updated:** 2026-05-13 (AI Health Assistant with Gemini Integration Complete)
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
**Build Time:** ~75s

All 40 screens implemented + Step Tracker + Running flow + AI Health Assistant with Gemini API. Build verified. App launches successfully on Android 12+ (API 31+).

---

## Screen Implementation Status: 40/40 (100%)

| Category | Planned | Done | Status |
|----------|---------|------|--------|
| Auth Flow | 4 | 4 | ✅ COMPLETE |
| Profile Setup | 4 | 4 | ✅ COMPLETE |
| Core Navigation | 5 | 5 | ✅ COMPLETE |
| Running Flow | 6 | 6 | ✅ COMPLETE |
| Water Intake | 3 | 3 | ✅ COMPLETE |
| Sleep Recording | 3 | 3 | ✅ COMPLETE |
| Step Tracking | 1 | 1 | ✅ COMPLETE (Sensor Auto-Tracking) |
| Gamification | 8 | 8 | ✅ COMPLETE |
| Settings & Utility | 5 | 5 | ✅ COMPLETE |
| Support & Misc | 3 | 3 | ✅ COMPLETE |
| **AI Health Assistant** | **1** | **1** | **✅ COMPLETE (NEW)** |

---

## AI Health Assistant Implementation — 2026-05-13

### Overview
Added AI-powered health profiling and personalized wellness suggestions using Google Generative AI (Gemini API).

### Features Implemented

**1. Health Questionnaire (4-Screen Form)**
- Screen 1: Basic metrics (weight, height, age, gender)
- Screen 2: Health history (checkup date, conditions, medications, allergies)
- Screen 3: Lifestyle (sleep, stress, smoking, alcohol, shortness of breath)
- Screen 4: Fitness (activity level, injuries, goals)
- Multi-step validation and progress indicator
- Saves to Firestore and SharedPreferences
- BMI calculation

**2. AI Suggestion Engine**
- REST API integration with Google Generative AI (Gemini)
- Dynamic prompt building based on health profile
- 7-day caching to reduce API calls
- Error handling with fallback messages
- Async/coroutine support

**3. Health Tips Fragment**
- New "Health" tab in bottom navigation (replaces Leaderboard)
- Displays AI-generated wellness suggestions
- Medical disclaimer prominently displayed
- Refresh button to regenerate suggestions
- Update Profile button to edit health data
- Loading state with progress bar

**4. Data Storage**
- Firestore: Health profile stored in `users/{uid}/healthProfile/current`
- SharedPreferences: Cached suggestions and profile data
- 7-day cache expiration

### Files Created

| File | Purpose |
|------|---------|
| `HealthProfile.kt` | Data model for health information |
| `HealthQuestionnaireActivity.kt` | 4-screen questionnaire form |
| `HealthTipsFragment.kt` | Displays AI suggestions |
| `HealthSuggestionService.kt` | Handles Gemini API calls via REST |
| `activity_health_questionnaire.xml` | Questionnaire UI |
| `fragment_health_tips.xml` | Health tips display UI |
| `AI_HEALTH_ASSISTANT_SUMMARY.md` | Complete documentation |

### Files Modified

| File | Changes |
|------|---------|
| `build.gradle.kts` | Added OkHttp3 dependency for REST API |
| `AndroidManifest.xml` | Added Google Generative AI API key metadata; registered HealthQuestionnaireActivity |
| `HomeActivity.kt` | Integrated HealthTipsFragment into bottom navigation |
| `FirestoreRepository.kt` | Added `saveHealthProfile()` and `getHealthProfile()` methods |
| `PreferenceHelper.kt` | Added health profile storage methods |
| `bottom_nav_menu.xml` | Added Health tab to navigation |
| `arrays.xml` | Added gender and activity level spinners |

### API Integration

**Google Generative AI (Gemini):**
- Endpoint: `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent`
- Method: REST API via OkHttp3
- Authentication: API key in AndroidManifest metadata
- Caching: 7-day cache to minimize API calls
- Model: `gemini-pro`

### Git Information

**Branch:** `feature/ai-health-assistant`
**Commit:** `acd7b4b`
**Remote:** https://github.com/applefarmm/fida-to-tranferv2/tree/feature/ai-health-assistant

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

### Lint Fixes

| Error | File | Fix |
|-------|------|-----|
| `WrongViewCast` | `RunSummaryActivity.kt:46-47` | Layout had `Button`, code cast to `MaterialButton` — changed layout to use `MaterialButton` |
| `CoarseFineLocation` | `AndroidManifest.xml` | Added `ACCESS_COARSE_LOCATION` alongside `ACCESS_FINE_LOCATION` |

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

**Permission Handling:**
```kotlin
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Location permission denied. Distance tracking disabled.", Toast.LENGTH_LONG).show()
        }
    }
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

### Runtime Issue Analysis (Not Fixed — Documented)

| Issue | File | Impact | Status |
|-------|------|--------|--------|
| `#3 Intent Parcelable` | `RunHistoryActivity.kt` | **NOT A CRASH** — `Run` model already has `@Parcelize` and `kotlin-parcelize` plugin is in build.gradle.kts. Intent passing works correctly. | ✅ Already fine |
| `prefs.getInt()` returning null | `WaterIntakeActivity.kt` | Returns null if key missing (not 0). `?: 0` fallback works correctly. | ✅ Already fine |
| Timer not cancelled on pause | `RunTrackingActivity.kt` | Timer keeps running when paused — minor perf issue. | ⚠️ Known issue |
| No `onDestroy` cleanup | `RunTrackingActivity.kt` | Timer leak if destroyed without calling `endRun()`. | ⚠️ Known issue |
| Gender only male/female | `ProfileSetup1Activity.kt` | Other gender options not supported. | ⚠️ Known issue |

---

## Previous Session Work (2026-05-03 — Session 1)

### 1. Build Error Fix
| Issue | File | Fix |
|-------|------|-----|
| Unresolved reference `username` | `StepCounterActivity.kt:82` | Changed to `safeUsername` |

### 2. Button Text Visibility Fix (Light Theme)
Fixed white text on white buttons issue across multiple screens:

| File | Changes |
|------|---------|
| `values/themes.xml` | Added `Widget.fida.Button`, `Widget.fida.Button.Outlined`, `Widget.fida.Button.Text` styles with proper colors; set as default `materialButtonStyle` |
| `values-night/themes.xml` | Added matching button style defaults for dark theme |
| `activity_water_intake.xml` | Applied `Widget.fida.Button` style to `+250ml`, `+500ml` buttons; `Widget.fida.Button.Outlined` to `Log Custom Amount` |
| `activity_record_sleep.xml` | Applied `Widget.fida.Button` style to `Log Sleep` button |
| `fragment_dashboard.xml` | Added `textColor="@color/white"` to `+1 Glass`, `+1 Hour` quick buttons |
| `activity_avatar_customization.xml` | Added `textColor="@color/white"` to `Continue` button |
| `activity_main.xml` | Added `textColor="@color/white"` to `+`/`-` water buttons |

**Solution:** Filled buttons now have blue background (`#414BB2`) + white text; Outlined buttons have blue text.

### 3. Custom Water Log Dialog Implementation
| File | Changes |
|------|---------|
| `WaterIntakeActivity.kt` | Added `showCustomAmountDialog()` method with MaterialAlertDialog; validation for positive numbers; confirmation toast |
| `layout_custom_water_dialog.xml` | Created new dialog layout with TextInputEditText for ml amount input |
| `WaterIntakeActivity.kt` | Added `setOnShowListener` to fix dialog button text colors (blue for Add, grey for Cancel) |

### 4. Back Navigation Buttons Added
Added back/home navigation to screens that lacked it:

| File | Changes |
|------|---------|
| `drawable/ic_back_arrow.xml` | Created Material Design back arrow icon (blue color) |
| `activity_step_counter.xml` | Added `ivBackStep` ImageView back button |
| `activity_shop.xml` | Added `ivBackShop` ImageView back button |
| `activity_gamification.xml` | Added `ivBackGamification` ImageView back button |
| `StepCounterActivity.kt` | Added back button click handler (`finish()`), added `Intent` import |
| `ShopActivity.kt` | Added back button click handler (`finish()`), added `ImageView` import |
| `GamificationActivity.kt` | Added back button click handler (`finish()`), added `ImageView` import |

---

## Remaining Work

### Priority 1: Testing & Deployment
| Task | Details |
|------|---------|
| Test on device | Install APK and verify all screens work correctly |
| Social login setup | Replace `YOUR_FACEBOOK_APP_ID` in strings.xml with real App ID |
| Google Sign-In | Verify SHA-1 certificate in Firebase Console |

### Priority 2: Optional Enhancements
| Task | Details | Status |
|------|---------|--------|
| Migrate GoogleSignIn to Credential Manager | Deprecation warnings in LoginActivity | Optional |
| Add Lottie animations | Level up, badge unlock, streak freeze | Optional |
| Architecture migration | MVVM + ViewModel + StateFlow + Hilt DI | Optional (significant refactor — 33 Activities + 23 Fragments) |
| RunTrackingActivity timer cleanup | Cancel TimerTask on pause/destroy | Optional |

---

## Critical Notes for Windows Development

### JVM Crash Prevention (CRITICAL)
**ALWAYS use `--no-daemon` flag** when building on Windows:
```bash
./gradlew.bat --no-daemon assembleDebug
```

**Why:** Windows has JVM memory issues with Gradle daemon background processes. Without `--no-daemon`, the daemon consumes memory and eventually crashes.

### File Access Rules for Claude Code on Windows
1. **Use Glob tool** for file discovery (NOT `find` or `ls`)
2. **Use Grep tool** for code search (NOT `grep` or `rg`)
3. **Use Read tool** to view files (NOT `cat`, `head`, `tail`)
4. **Use Edit tool** for changes (NOT `sed`, `awk`)
5. **Use Bash tool** only for Gradle builds and git operations

### Dialog Button Text Color Fix Pattern
When using MaterialAlertDialogBuilder, dialog buttons inherit theme styles which may cause visibility issues. Fix with:
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

When navigating between screens, pass `uid` via Intent:
```kotlin
intent.putExtra("uid", safeUid)   // ✅ correct
intent.putExtra("username", safeUsername) // ❌ wrong for Firestore queries
```

---

## Important File Locations

| Type | Path |
|------|------|
| Activities | `app/src/main/java/com/fida/app/*.kt` |
| Fragments | `app/src/main/java/com/fida/app/fragments/*.kt` |
| Models | `app/src/main/java/com/fida/app/models/*.kt` |
| Utils | `app/src/main/java/com/fida/app/utils/*.kt` |
| Adapters | `app/src/main/java/com/fida/app/adapters/*.kt` |
| Layouts | `app/src/main/res/layout/*.xml` |
| Drawables | `app/src/main/res/drawable/*.xml` |
| Themes | `app/src/main/res/values/themes.xml`, `values-night/themes.xml` |
| Colors | `app/src/main/res/values/colors.xml`, `values-night/colors.xml` |

---

## Firebase Configuration

- **Required:** `app/google-services.json` must exist
- **Services:** Auth, Firestore, Realtime Database, Analytics
- **Document ID:** Always use Firebase UID, not username

### Key Firestore Collections
- `users/{uid}` - User profiles, stats, badges (keyed by Firebase UID)
- `users/{uid}/runs` - Run history
- `users/{uid}/quests` - User quests
- `users/{uid}/challenges` - User challenges
- `achievements` - Global achievements list
- `rewards` - Shop rewards (auto-seeded via `FirestoreRepository.checkAndSeedRewards()`)

---

## Social Login Setup Checklist

### Google Sign-In
- ✅ Client ID configured in strings.xml
- ⚠️ Verify SHA-1 certificate in Firebase Console
- ⚠️ Deprecation warnings - consider Credential Manager migration

### Facebook Sign-In
- ❌ **DISABLED** — Facebook SDK not configured, LoginButton replaced with regular Button
- To re-enable: Replace `YOUR_FACEBOOK_APP_ID` in strings.xml, uncomment Facebook metadata in AndroidManifest.xml, restore Facebook SDK code in LoginActivity.kt

---

## UI Styling Reference

### Button Styles (defined in themes.xml)
```xml
<!-- Filled button: blue background, white text -->
<style name="Widget.fida.Button" parent="Widget.Material3.Button">
    <item name="android:textColor">@color/white</item>
    <item name="backgroundTint">@color/blue</item>
</style>

<!-- Outlined button: blue text, transparent background -->
<style name="Widget.fida.Button.Outlined" parent="Widget.Material3.Button.OutlinedButton">
    <item name="android:textColor">@color/blue</item>
</style>

<!-- Text button: blue text -->
<style name="Widget.fida.Button.Text" parent="Widget.Material3.Button.TextButton">
    <item name="android:textColor">@color/blue</item>
</style>
```

### Color Reference
| Color | Light Theme | Dark Theme |
|-------|-------------|------------|
| `primaryTextColor` | `#FF000000` (black) | `#FFFFFFFF` (white) |
| `blue` | `#414BB2` | `#414BB2` |
| `grey_500` | `#9E9E9E` | `#9E9E9E` |

---

## Known Issues & Gotchas

1. **JVM Crashes on Windows:** ALWAYS use `--no-daemon` flag
2. **Firestore Doc ID:** ALWAYS use `uid` from `PreferenceHelper`, never `username`
3. **Button text visibility:** Use `Widget.fida.Button` styles or explicit `textColor`
4. **Dialog button colors:** Override with `setOnShowListener`
5. **Facebook SDK:** DISABLED — LoginButton replaced with regular Button (no valid App ID configured)
6. **Google Client ID:** Must end with `.apps.googleusercontent.com`
7. **ViewBinding NOT Compose:** Uses traditional Views, not Jetpack Compose
8. **35+ Activities:** Not single-activity architecture - don't convert
9. **Run model is Parcelable:** `kotlin-parcelize` plugin is applied — safe to pass via Intent
10. **Badge data:** Stored as `Map<String, Boolean>` in Firestore; use `GameManager.allBadges` for human-readable titles
11. **Step sensor availability:** TYPE_STEP_COUNTER may not exist on all devices; StepTrackingService logs warning if unavailable
12. **Step baseline reset:** Steps reset to 0 at midnight automatically via date check in service
13. **MIUI battery optimization:** Xiaomi devices may kill foreground services; users need to whitelist FIDA
14. **Foreground Service Type:** Changed to `specialUse` (not `health`) — requires `FOREGROUND_SERVICE_SPECIAL_USE` permission
15. **Activity Recognition Permission:** Must be granted BEFORE `StepTrackingService` starts — handled in `HomeActivity.onResume()`
16. **Service Start Timing:** Foreground services can only start when app is foreground — use `onResume()` not `onCreate()`
17. **Google Play Services:** RunTrackingActivity checks GPS availability; shows error dialog if not available; timer works without location
18. **Location permission:** Must request ACCESS_FINE_LOCATION before starting location updates; handled with onRequestPermissionsResult callback
19. **GPS glitches:** Location updates filtered to remove jumps >100m/second (unrealistic movements)
20. **RadioGroup in ConstraintLayout:** Must use proper constraints (vertical, full width) — horizontal chains can make selection impossible

---

## Quick Command Reference

```bash
# Build (Windows - ALWAYS use --no-daemon)
./gradlew.bat --no-daemon assembleDebug

# Clean build
./gradlew.bat --no-daemon clean assembleDebug

# Git status
git status

# ADB (Windows path)
"C:/Users/FATHI/AppData/Local/Android/Sdk/platform-tools/adb.exe" logcat -d

# Install and launch
./gradlew.bat installDebug
adb shell am start -n com.fida.app/.SplashActivity

# Check crash logs
adb logcat -d | grep -E "FATAL|AndroidRuntime|Exception"

# Grant Activity Recognition permission
adb shell pm grant com.fida.app android.permission.ACTIVITY_RECOGNITION
```

---

## Files Modified This Session (2026-05-06)

| File | Change |
|------|--------|
| `services/StepTrackingService.kt` | **Created** — Foreground service with SensorEventListener for TYPE_STEP_COUNTER, baseline tracking, Firestore persistence |
| `services/BootReceiver.kt` | **Created** — BroadcastReceiver to restart StepTrackingService on BOOT_COMPLETED and MY_PACKAGE_REPLACED |
| `AndroidManifest.xml` | Added 5 new permissions, registered StepTrackingService (foregroundServiceType="health"), registered BootReceiver |
| `fragment_dashboard.xml` | Added Steps CardView after Sleep card with icon (👟), summary text, distance text, progress bar (green), View Details button |
| `DashboardFragment.kt` | Added StepCounterActivity import, step variables, load step data from Firestore, distance calculation (steps/1312.335958), progress bar update, navigation handlers for card and button |
| `StepCounterActivity.kt` | Added StepTrackingService import, Handler/Looper imports, refreshHandler for 5s periodic updates, service start call in onCreate, maxSteps save to SharedPreferences on target set |
| `HomeActivity.kt` | Added StepTrackingService import and start call in onCreate |
| `PreferenceHelper.kt` | Added 6 new methods for step baseline tracking: saveBaselineSteps, getBaselineSteps, saveBaselineDate, getBaselineDate, saveMaxSteps, getMaxSteps |
| `HANDOFF.md` | Added Step Tracker Implementation section, updated screen count, documented sensor implementation details |

---

## Files Modified This Session (2026-05-11)

| File | Change |
|------|--------|
| `AndroidManifest.xml` | Changed `foregroundServiceType` from `"health"` to `"specialUse"`; added `FOREGROUND_SERVICE_SPECIAL_USE` permission; added `ACCESS_COARSE_LOCATION` permission |
| `HomeActivity.kt` | **Major rewrite** — moved service start to `onResume()`; added `ACTIVITY_RECOGNITION` runtime permission check with launcher; added imports for permission handling |
| `activity_login.xml` | Replaced `com.facebook.login.widget.LoginButton` with regular `Button` (visibility=gone) |
| `LoginActivity.kt` | Commented out Facebook SDK initialization code (not configured) |
| `activity_run_summary.xml` | Changed `Button` elements to `com.google.android.material.button.MaterialButton` (fixed WrongViewCast) |
| `activity_pre_run.xml` | **Rewrote layout** — fixed RadioGroup constraint issues; proper vertical layout; full-width goal selection; dynamic unit display; MaterialButton |
| `PreRunActivity.kt` | **Rewrote** — added dynamic unit label (km/min/kcal); added RadioGroup change listener; improved validation; dynamic hints |
| `RunTrackingActivity.kt` | **Major rewrite** — added Google Play Services check; error handling for SecurityException; permission request callback; location filtering for GPS glitches; cleanup in onDestroy() |
| `RunCountdownActivity.kt` | **Rewrote** — added null checks for intent extras; validation for uid/goalValue; error handling; countdown timer cleanup |
| `HANDOFF.md` | Added Critical Launch Crash Fixes section; added Running Flow Fixes section; updated build status; documented all changes |

---

## Files Modified This Session (2026-05-03)

### Session 2 (Runtime Fixes)
| File | Change |
|------|--------|
| `MainActivity.kt` | **Rewrote** — replaced all `username` Firestore queries with `uid`. Added `safeUid` (Firebase UID) and `safeUsername` (display name). All card navigations now pass `uid` via Intent. Added `PreferenceHelper` import. |
| `ShopActivity.kt` | **Rewrote** — replaced `safeUsername` with `safeUid`. Fixed null-safe parsing for `coin`/`health`. Removed unused `ContentValues` import. Added uid guard checks. |
| `StepCounterActivity.kt` | **Rewrote** — replaced `safeUsername` with `safeUid`. Added `PreferenceHelper` import. All Firestore queries now use `uid`. |
| `WaterIntakeActivity.kt` | Added `uid` from `PreferenceHelper`. `addWater()` now saves to Firestore. `awardGoalXP()` saves XP to Firestore. Added missing `View` import. Fixed progress bar type. |
| `RecordSleepActivity.kt` | Added `uid` from `PreferenceHelper`. `logSleep()` now saves `sleepHours` to Firestore. `awardSleepXP()` saves XP to Firestore. |
| `ProfileFragment.kt` | Fixed `profilePicUrl` → `profilePicture`. Added `GameManager` import. Badge titles now use readable names from `GameManager.allBadges`. |
| `AccountSettingsFragment.kt` | **Fixed photo save flow.** `updateProfilePhoto()` now writes to Firestore `profilePicture` field. Initial photo loads from Firestore (with Auth fallback). |
| `HANDOFF.md` | Added critical runtime fixes section, Firestore Doc ID convention, upload fix documentation |

### Session 1 (UI Polish)
| File | Change |
|------|--------|
| `StepCounterActivity.kt` | Fixed `username` → `safeUsername`; added back button handler |
| `activity_step_counter.xml` | Added back button `ivBackStep` |
| `ShopActivity.kt` | Added back button handler |
| `activity_shop.xml` | Added back button `ivBackShop` |
| `GamificationActivity.kt` | Added back button handler |
| `activity_gamification.xml` | Added back button `ivBackGamification` |
| `WaterIntakeActivity.kt` | Implemented custom water log dialog; fixed dialog button colors |
| `layout_custom_water_dialog.xml` | Created dialog layout for custom amount input |
| `activity_water_intake.xml` | Applied proper button styles |
| `activity_record_sleep.xml` | Applied proper button styles |
| `fragment_dashboard.xml` | Added textColor to quick buttons |
| `activity_avatar_customization.xml` | Added textColor to Continue button |
| `activity_main.xml` | Added textColor to water +/- buttons |
| `values/themes.xml` | Added Widget.fida.Button styles; set as defaults |
| `values-night/themes.xml` | Added button style defaults |
| `drawable/ic_back_arrow.xml` | Created back arrow icon |

---

## Tips for Future Claude Sessions

1. **Read HANDOFF.md first** — Current state, build status, recent changes
2. **Use `--no-daemon`** for ALL Gradle builds on Windows
3. **Use Glob/Grep/Read/Edit tools** — NOT bash file commands
4. **Firestore Doc ID** — Always use `uid` from `PreferenceHelper`, NOT `username`
5. **Button visibility in light theme** — Use `Widget.fida.Button` styles or explicit `textColor="@color/white"`
6. **Dialog button colors** — Override with `setOnShowListener` pattern
7. **Build verification** — Run assembleDebug after any code changes
8. **Session recovery** — Read HANDOFF.md + followup.md + git status
9. **Badge titles** — Use `GameManager.allBadges` for human-readable names, not raw IDs
10. **Step tracking** — TYPE_STEP_COUNTER sensor gives total steps since boot; use baseline tracking for daily count
11. **Step service** — StepTrackingService uses `specialUse` foreground service type; requires permission check before start
12. **Xiaomi devices** — May need to disable battery optimization for FIDA to keep step tracking running
13. **Foreground service on Android 12+** — Can only start when app is foreground; use `onResume()` not `onCreate()`
14. **Permission before service** — Check `ACTIVITY_RECOGNITION` permission before calling `StepTrackingService.start()`
15. **Facebook login disabled** — Facebook SDK not configured; LoginButton replaced with regular Button
16. **adb path on Windows** — `C:/Users/FATHI/AppData/Local/Android/Sdk/platform-tools/adb.exe`
17. **Google Play Services check** — Always check `GoogleApiAvailability.isGooglePlayServicesAvailable()` before using location APIs
18. **Location permission callback** — Implement `onRequestPermissionsResult()` for location updates
19. **RadioGroup constraints** — Use full width (`0dp` with match_constraints), avoid horizontal chains in ConstraintLayout
20. **Intent validation** — Always validate intent extras (null checks, default values) before passing between activities
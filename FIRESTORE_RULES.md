# Firestore Security Rules for FIDA App

## Current Issue
Health profile save is failing with "Error saving profile to database" - likely due to Firestore security rules not allowing write access.

## Solution: Update Firestore Rules in Firebase Console

### Steps to Update Rules

1. Go to Firebase Console: https://console.firebase.google.com/
2. Select your FIDA project
3. Go to **Firestore Database** → **Rules** tab
4. Replace existing rules with the rules below
5. Click **Publish**

---

## Recommended Firestore Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Allow authenticated users to read/write their own user document
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
      
      // Allow authenticated users to read/write their own subcollections
      match /{document=**} {
        allow read, write: if request.auth.uid == uid;
      }
    }
    
    // Public collections (read-only for all authenticated users)
    match /achievements/{document=**} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    
    match /rewards/{document=**} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

---

## What These Rules Allow

✅ **Authenticated users can:**
- Read/write their own user document: `users/{uid}`
- Read/write all subcollections under their user: `users/{uid}/healthProfile/current`
- Read/write: `users/{uid}/runs`, `users/{uid}/allDays`, `users/{uid}/quests`, etc.
- Read public collections: `achievements`, `rewards`

❌ **Not allowed:**
- Access other users' data
- Write to public collections (achievements, rewards)
- Anonymous access

---

## Testing After Rule Update

1. Wait 1-2 minutes for rules to propagate
2. Reinstall APK: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. Test health questionnaire submission again
4. Should now save successfully

---

## If Still Getting Error

Check logcat for specific error:
- **"PERMISSION_DENIED"** → Rules still need adjustment
- **"UID is empty"** → User not logged in
- **"Network error"** → No internet connection

Share the exact error message from logcat and I'll help debug further.

---

## Alternative: Test Rules (Development Only)

If you want to test without authentication (NOT for production):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

⚠️ **WARNING:** This allows anyone to read/write all data. Only use for testing!

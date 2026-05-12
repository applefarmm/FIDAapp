# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a hybrid repository with two independent projects:
- **Android app** (`app/`) — Kotlin, Firebase backend, traditional Views with ViewBinding (NOT Jetpack Compose)
- **React web app** (`src/`, `dist/`) — React 18, Vite, JavaScript/JSX

Both projects share the repo but have separate build systems and dependencies.

## Build Commands

### Android (Gradle)
```bash
gradlew.bat assembleDebug          # Build debug APK
gradlew.bat clean                  # Clean build
gradlew.bat --no-daemon assembleDebug  # Use if JVM crashes (Windows memory issues)
```

Note: This project has experienced JVM crashes on Windows. Use `--no-daemon` flag if you encounter memory errors.

### React Web App
```bash
npm run dev      # Start dev server on port 3000
npm run build    # Production build
npm run preview  # Preview production build
```

## Code Style

- **Kotlin**: Use official Kotlin code style (`kotlin.code.style=official`)
- Lint/format commands:
  ```bash
  gradlew.bat ktlintCheck        # Check Kotlin style
  gradlew.bat ktlintFormat       # Auto-fix Kotlin style
  gradlew.bat detekt             # Run Kotlin static analysis
  npm run lint                   # Check React/JS style
  npm run lint:fix               # Auto-fix React/JS style
  npm run format                 # Format React files with Prettier
  ```

## Firebase Setup

- The Android app requires `app/google-services.json` for Firebase
- Uses Firebase Auth, Firestore, Realtime Database, and Analytics
- Google Sign-In is integrated

## Architecture Notes

- Android uses ViewBinding/DataBinding, NOT Jetpack Compose
- Fragment-based navigation with bottom nav
- Key packages: `fragments/`, `models/`, `utils/`
- `GameManager` and `FirestoreRepository` handle data layer
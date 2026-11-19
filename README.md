# DragonDojo

An offline-first Android MVP for managing a martial arts dojo. The app demonstrates a
single-activity Jetpack Compose UI, Material 3 navigation, English/German resources,
role-aware overviews, class booking and attendance, technique search, belt progress,
and persistent demo preferences.

## Features

- Select Student, Parent, Instructor, or Admin roles
- Dashboard, Training, Library, and Profile screens
- Book/cancel representative classes and mark attendance
- Persist role, theme, bookings, attendance, and progress locally
- Search techniques by name, Japanese term, category, description, or belt
- English and German strings (selected by the Android device locale)
- Material 3 dark mode and responsive list-based layouts
- No login, network, payment, Firebase, or AI credential is required

## Architecture

The project follows a small MVVM/Clean-inspired boundary:

- `domain`: data models and pure business use cases
- `data`: offline repository, seed data, and Android SharedPreferences persistence
- `ui`: Compose screens and navigation
- `MainActivity`: the only Activity and composition root

All displayed data is representative local data. “Mark attendance” is intentionally
idempotent and affects progress once per booked session.

## Build and test

Requirements: JDK 17 and Android SDK Platform 35.

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Open the project in Android Studio and run the `app` configuration, or install the
debug APK on an emulator/device.
# SimpleNote for Android

An Android note-taking application built with Kotlin and Jetpack Compose. The app combines account management, a local Room database, and background synchronization with a remote API.

[Watch the demo](https://drive.google.com/file/d/1eMgJEzhg01bzbi0oLFhNNzaZoALGe_vA/view?usp=drive_link)

## Features

- Register, sign in, and manage account settings.
- Create and edit notes through a Compose interface.
- Store notes locally with Room.
- Queue changes in an outbox and synchronize with WorkManager.
- Store authentication and preferences with DataStore.

## Stack

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room, WorkManager, Retrofit, OkHttp, and DataStore.

## Getting started

1. Open the repository in Android Studio.
2. Use JDK 17 and install Android SDK 35. The minimum supported Android version is API 24.
3. Sync the Gradle project and run the `app` configuration on an emulator or device.

To build a debug APK with the included wrapper:

```sh
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`. The APK is generated in `app/build/outputs/apk/debug/`.

The API base URL is configured in `app/build.gradle.kts`. Account and synchronization features require the configured backend to be available.

## Project layout

| Path | Purpose |
| --- | --- |
| `app/src/main/java/com/example/simplenote/data/` | Authentication, storage, networking, and synchronization |
| `app/src/main/java/com/example/simplenote/ui/` | Screens, view models, and theme |
| `app/src/main/java/com/example/simplenote/navigation/` | Navigation graph |
| `app/src/main/res/` | Android resources |
| `docs/snapshots/` | Original reference resource archive |

## Checks

```sh
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

The second command requires an emulator or connected Android device.

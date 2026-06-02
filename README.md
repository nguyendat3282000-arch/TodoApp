# TodoApp

A modern, robust Android Todo application built with Kotlin and the latest Android development practices. This app demonstrates an offline-first architecture, background synchronization, and a seamless user experience.

## Features

- **Authentication**: Secure Google Sign-In using Android Credential Manager and Firebase Authentication.
- **Task Management**: Create, read, update, and delete tasks.
- **Offline-First**: Tasks are stored locally using Room Database, allowing full usage without an internet connection.
- **Cloud Sync**: Background synchronization with Firebase Firestore using WorkManager to keep data backed up and synced across devices.
- **Reminders**: Set specific alarms and notifications for your tasks using AlarmManager.
- **App Widget**: A convenient home screen widget built with Jetpack Glance to quickly view your tasks.
- **Modern UI**: Fully built with Jetpack Compose following Material 3 design guidelines.
- **Type-Safe Navigation**: Utilizing Navigation 3 with Kotlinx Serialization for robust routing.

## Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**: MVVM (Model-View-ViewModel) pattern using Coroutines and StateFlow.
- **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room)
- **Cloud Database**: [Firebase Firestore](https://firebase.google.com/docs/firestore)
- **Authentication**: [Firebase Auth](https://firebase.google.com/docs/auth) with Credential Manager (Google Sign-In)
- **Background Processing**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Alarms & Scheduling**: `AlarmManager`
- **Widgets**: [Glance](https://developer.android.com/jetpack/compose/glance)
- **Navigation**: [Navigation 3](https://developer.android.com/guide/navigation) (Type-Safe)

## Prerequisites

To build and run this project, you will need:
- Android Studio Ladybug or later.
- JDK 17.
- Minimum SDK: API 26 (Android 8.0).
- Target SDK: API 36.

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   ```
2. **Open in Android Studio:** Open the `TodoApp` directory in Android Studio.
3. **Firebase Setup:**
   - The project uses Firebase. Ensure you have a valid `google-services.json` file in the `app/` directory (if not already provided).
   - In your Firebase Console, ensure Google Sign-In is enabled in the Authentication providers.
   - The `GOOGLE_WEB_CLIENT_ID` in `build.gradle.kts` must match your Firebase project's Web Client ID.
4. **Build and Run:** Sync the project with Gradle files and run the app on an emulator or physical device.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

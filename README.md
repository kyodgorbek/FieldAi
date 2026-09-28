# FieldAI

FieldAI is a production-grade, offline-first, AI-powered mobile assistant designed for professionals in the field. Built using Kotlin Multiplatform (KMP), it natively supports Android (Compose) and iOS (SwiftUI) while sharing core business logic, domain models, local databases, and real-time networking capabilities.

## Features

## What We Have Done

- **Project Setup & Architecture:** Migrated to KMP with shared domain models and implemented Clean Architecture + MVVM guidelines.
- **Offline-First Capabilities:** Configured SQLDelight schemas across both platforms and implemented a robust `SyncEngine` with WorkManager fallback.
- **Native AI Integrations:** Wrote the `GroqAiProvider` fetching streaming tokens from Llama 3 via SSE (Server-Sent Events).
- **Native Hardware Features:** Built shared wrappers for iOS/Android native APIs (Camera, Location, Contacts, Calendar, Devices, Voice Recognition).
- **Premium UI Screens:** Built complete, premium Jetpack Compose/SwiftUI UI logic containing: Splash, Login, Home, AI Assistant, Documents, Contacts, Calendar, Map, and Device Management views.
- **Quality & Security:** Cleaned out all `TODO` placeholders. Implemented build validations, comprehensive `Turbine` test cases, CI/CD pipelines via GitHub actions, and secure token injection.

## What Is Left (Future Roadmap)

- **Push to Production:** Distribute via TestFlight and Google Play Beta.
- **Advanced 3D Mapping:** Add advanced OpenGL/Metal overlay layers to the map view.
- **Multi-user Syncing:** Scale the WebSockets layer to support live multi-user collaborative editing in the field.

## Getting Started

### Prerequisites

1. **Android Studio Koala** (or IntelliJ IDEA)
2. **Xcode** (for iOS builds on macOS)
3. **Groq API Key**: You need an active Groq API Key.

### Setup

1. Create a `local.properties` file in the root directory (if it doesn't exist).
2. Add your Groq API Key:
   ```properties
   GROQ_API_KEY=your_key_here
   ```
3. Sync the Gradle project.

### Running the App

**Android:**
Use the run widget in Android Studio to launch the `androidApp` module, or run:
```bash
./gradlew :androidApp:assembleDebug
```

**iOS:**
Open the `iosApp/iosApp.xcworkspace` in Xcode, or run from terminal:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### Running Tests

Unit tests and standard evaluations are automated and executed on JVM/Android test runners via Turbine and Coroutine test dispatchers.
```bash
./gradlew test
```

## Documentation

For deep dives into the implementation:
- [ARCHITECTURE.md](ARCHITECTURE.md) - Describes Clean Architecture, the Sync Engine, and data-flow diagrams.
- [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) - Detailed step-by-step roadmap tracing all project requirements.
- [API.md](API.md) - Outlines API endpoints and the real-time AI protocol.
- [SECURITY.md](SECURITY.md) - Details key management and offline data protection.

## License
MIT License
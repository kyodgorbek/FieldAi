# FieldAI Security & Privacy

This document outlines the security posture, privacy mechanisms, and best practices implemented in the FieldAI application.

## 1. API Key Security

FieldAI interacts with 3rd-party services (e.g., Groq for AI).
- **No Hardcoded Keys:** API keys are never checked into version control. 
- **local.properties:** Keys are placed in `local.properties` (which is excluded from Git via `.gitignore`).
- **BuildConfig Injection:** Keys are dynamically injected into the Kotlin Multiplatform shared module using a Gradle task (e.g., BuildKonfig or expect/actual `BuildConfiguration`).

## 2. Data Encryption at Rest

FieldAI operates with sensitive engineering and project data.
- **Android:** Uses EncryptedSharedPreferences (via Multiplatform Settings) for tokens. The SQLDelight database can be configured to use SQLCipher for 256-bit AES encryption of all local data.
- **iOS:** Uses the iOS Keychain for secure token and key storage.

## 3. Data in Transit

All API communication, including AI streaming and synchronization, enforces TLS 1.2+ via Ktor's `CIO` or platform engines. WebSockets use secure `wss://` protocols.

## 4. Privacy & Permissions

FieldAI requires access to device hardware but respects user privacy through Just-In-Time (JIT) permissions.
- **Microphone:** Requested only when the user explicitly triggers voice input via `VoiceManager`.
- **Location:** Requested only when location-aware context is needed (e.g., `LocationManager` for field mapping).
- **Camera/Bluetooth:** Dynamically requested when scanning documents or IoT devices.

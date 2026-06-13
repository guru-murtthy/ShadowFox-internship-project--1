# Shadowfox Android Developer Internship - Part 1: Beginner Level (Foundations)

This workspace contains the source code, tests, and configuration for the two beginner-level Android applications requested by the Shadowfox Android Developer Internship. Both applications are written in Kotlin using Android Studio best practices, implementing modular and clean architecture.

## Projects Overview

| Project | Description | Key Tech Stack |
| :--- | :--- | :--- |
| **[SimpleLoginApp](./SimpleLoginApp)** | A secure, lifecycle-aware login application. | Kotlin, MVVM (ViewModel), ConstraintLayout, Firebase Authentication, Biometric Prompt API, JUnit |
| **[CalculatorApp](./CalculatorApp)** | A premium scientific calculator featuring modern UI scaling. | Kotlin, ConstraintLayout guidelines/chains, SharedPreferences (Dark Theme), Android Speech API, JUnit |

---

## 🛠️ Project Details & Requirements Met

### 1. Simple Login Page (`SimpleLoginApp`)
- **Activity Lifecycle (State Preservation)**: Uses `LoginViewModel` to store the loading state, transient input errors, and login attempt counters, ensuring no data loss when the screen rotates (survives configuration changes).
- **Explicit Intents**: Secure screen navigation from `LoginActivity` to `WelcomeActivity` passing user session data.
- **Input Sanitization**: Client-side validation for emails (strict Regex pattern check) and password length (minimum 6 characters) with visual UI validation states (`setError` on inputs).
- **Security Audit**: Strictly logs no password or sensitive information to Logcat.
- **Engineering Evolution (Firebase Auth)**: Connects to Firebase Authentication for user registration and login.
- **Fallback Capability**: If `google-services.json` is not provided (or Firebase Auth is not initialized), the app falls back to a clean mock authorization engine with user feedback to allow testing out-of-the-box.
- **Engineering Evolution (Biometric Security)**: Integrates `BiometricPrompt` to allow secure fingerprint/face unlock on compatible devices.

### 2. Basic Calculator App (`CalculatorApp`)
- **Responsive Layout Manager**: Built entirely using `ConstraintLayout` with guidelines, weights, and chains, ensuring perfect button sizing on all mobile devices and tablets without overlapping.
- **Input State Machine**: Prevents illegal formatting on-the-fly (e.g., typing multiple decimal points `12..5` or multiple operators `++5`).
- **Mathematical Error Handling**: Safeguarded calculations using try-catch blocks and explicit division-by-zero checks (displays a user-friendly "Cannot divide by zero" message).
- **Scientific Operations**: Extends basic math to include Scientific modes (Sine `sin`, Cosine `cos`, Logarithm `log`, Square Root `sqrt`).
- **Themes & Preferences**: Implements a "Dark Mode" toggle that dynamically swaps the application theme. User preferences are saved using `SharedPreferences` to ensure they persist across app restarts.
- **Voice Commands**: Incorporates the Android Speech API (`RecognizerIntent`). Click the microphone button and say calculations like "5 times 10" or "sine of 90" to compute the output.

---

## 🧪 Running Automated Unit Tests

Both apps are equipped with comprehensive JUnit tests verifying input validations, error limits, calculations, and backspace state machine transitions.

To run the unit tests, execute the following commands in your terminal:

```bash
# Test SimpleLoginApp
cd SimpleLoginApp
./gradlew testDebugUnitTest

# Test CalculatorApp
cd ../CalculatorApp
./gradlew testDebugUnitTest
```

---

## 🔗 Continuous Integration (GitHub Actions)

A GitHub Actions CI workflow is configured under `.github/workflows/android.yml`. On every push to `main` or pull request, the runner:
1. Sets up JDK 17
2. Caches Gradle dependencies
3. Builds the apps (`assembleDebug`)
4. Runs unit tests to ensure that everything compiles and passes tests.

# CLAUDE.md

## Project Instructions
Please refer to [AGENTS.md](./AGENTS.md) for comprehensive project setup, build commands, code style guidelines, and development instructions.

## Additional Claude-Specific Notes
- Always run tests before making significant changes: `./gradlew test`
- Use the build commands specified in AGENTS.md for validation
- Follow the project structure and coding conventions outlined in AGENTS.md
- When making changes, ensure compatibility across both Android and iOS platforms

## Build Verification Rule
**IMPORTANT**: After implementing each feature, always run both Android and iOS builds to verify there are no compilation errors:

### Android Build:
```bash
./gradlew :composeApp:assembleDebug
```

### iOS Build (MANDATORY for commonMain changes):
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

If any build errors are found:
1. Fix all compilation errors immediately
2. Re-run both builds to ensure they pass
3. Only then proceed to the next feature or task

This ensures the codebase remains in a working state throughout development.

## Kotlin Multiplatform Compatibility
**CRITICAL**: When implementing functionality in `composeApp/src/commonMain/kotlin`:

- **DO NOT** use JVM/Android-only APIs (System.*, android.*, java.*)
- **DO NOT** use platform-specific string formatting or file operations
- **ALWAYS** test iOS compatibility with `./gradlew :composeApp:compileKotlinIosSimulatorArm64`
- **REFER** to AGENTS.md for detailed KMP development rules and common fixes

Any code that fails iOS compilation is NOT acceptable and must be fixed immediately.
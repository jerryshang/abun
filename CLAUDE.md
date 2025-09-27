# CLAUDE.md

## Project Instructions
Please refer to [AGENTS.md](./AGENTS.md) for comprehensive project setup, build commands, code style guidelines, and development instructions.

## Additional Claude-Specific Notes
- Always run tests before making significant changes: `./gradlew test`
- Use the build commands specified in AGENTS.md for validation
- Follow the project structure and coding conventions outlined in AGENTS.md
- When making changes, ensure compatibility across both Android and iOS platforms

## Build Verification Rule
**IMPORTANT**: After implementing each feature, always run the Android build to verify there are no compilation errors:

```bash
./gradlew :composeApp:assembleDebug
```

If any build errors are found:
1. Fix all compilation errors immediately
2. Re-run the build to ensure it passes
3. Only then proceed to the next feature or task

This ensures the codebase remains in a working state throughout development.
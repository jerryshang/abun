# AGENTS.md

## Project Overview
This is a Kotlin Multiplatform project targeting Android and iOS platforms using Compose Multiplatform. The project uses Gradle as the build system.

## Setup Commands
- Build Android debug: `./gradlew :composeApp:assembleDebug`
- Build Android release: `./gradlew :composeApp:assembleRelease`
- Run tests: `./gradlew test`
- Clean build: `./gradlew clean`
- Check code style: `./gradlew ktlintCheck`
- Format code: `./gradlew ktlintFormat`

## Project Structure
- `/composeApp/src/commonMain/kotlin` - Shared code for all platforms
- `/composeApp/src/androidMain/kotlin` - Android-specific code
- `/composeApp/src/iosMain/kotlin` - iOS-specific code
- `/composeApp/src/commonTest/kotlin` - Shared tests
- `/iosApp` - iOS application entry point

## Code Style Guidelines
- Follow Kotlin coding conventions
- Use Compose Multiplatform patterns for UI
- Keep platform-specific code in respective folders
- Prefer composition over inheritance
- Use meaningful variable and function names
- Add KDoc comments for public APIs

## Testing Instructions
- Unit tests are located in `commonTest` for shared logic
- Platform-specific tests should be in respective test folders
- Run `./gradlew test` to execute all tests
- Ensure tests pass on both Android and iOS platforms

## Platform Considerations
### Android
- Minimum SDK level and target SDK are defined in build configuration
- Use Android-specific APIs only in `androidMain` folder

### iOS
- iOS-specific code goes in `iosMain` folder
- For iOS app testing, open `/iosApp` in Xcode
- Ensure compatibility with required iOS versions

## Security Considerations
- Never commit sensitive information like API keys
- Use build configuration or environment variables for secrets
- Validate all user inputs
- Follow platform security best practices for both Android and iOS
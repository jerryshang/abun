# AGENTS.md

## Project Overview
This is a Kotlin Multiplatform project targeting Android and iOS platforms using Compose Multiplatform. The project uses Gradle as the build system and Koin for dependency injection.

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
- `/composeApp/src/commonMain/kotlin/dev/tireless/abun/di/` - Koin dependency injection modules

## Dependency Injection with Koin
This project uses Koin for dependency injection:

### Module Structure
- `AppModule.kt` - Contains common app-level dependencies
- `AndroidModule.kt` - Android-specific dependencies
- `IosModule.kt` - iOS-specific dependencies

### Adding Dependencies
1. Define your dependency in the appropriate module:
   ```kotlin
   val appModule = module {
       single<YourInterface> { YourImplementation() }
       factory { AnotherClass() }
   }
   ```

2. Inject dependencies in Composables:
   ```kotlin
   @Composable
   fun YourComposable() {
       val dependency: YourInterface = koinInject()
       // Use dependency
   }
   ```

3. Inject ViewModels:
   ```kotlin
   val viewModelModule = module {
       viewModel { YourViewModel(get()) }
   }

   @Composable
   fun YourScreen() {
       val viewModel: YourViewModel = koinViewModel()
   }
   ```

### Platform-Specific Dependencies
- Add Android-specific dependencies to `AndroidModule.kt`
- Add iOS-specific dependencies to `IosModule.kt`
- Include platform modules in the main application initialization if needed

## Code Style Guidelines
- Follow Kotlin coding conventions
- Use Koin annotations and DSL properly
- Keep dependency definitions in appropriate modules (common, Android, iOS)
- Use `single` for singletons, `factory` for new instances
- Use `viewModel` for ViewModels in Compose
- Prefer constructor injection over field injection
- Use Compose Multiplatform patterns for UI
- Keep platform-specific code in respective folders
- Prefer composition over inheritance
- Use meaningful variable and function names
- Add KDoc comments for public APIs

## Kotlin Multiplatform Development Rules
**CRITICAL**: For any functionality implemented in `commonMain` sourceSet of composeApp module:

### ❌ DO NOT USE:
- **JVM-only classes**: `System.currentTimeMillis()`, `java.util.*`, `java.time.*`
- **Android-only APIs**: `android.graphics.Color`, `Context`, `Log.d()`, etc.
- **Platform-specific string formatting**: `String.format()` with locale-specific parameters
- **Platform-specific file operations**: `File()`, `FileInputStream`, etc.

### ✅ DO USE:
- **Kotlin stdlib functions**: Basic collections, string operations, math functions
- **Compose Multiplatform APIs**: All `@Composable` functions, Material3 components
- **KMP-compatible libraries**: SQLDelight, Koin, kotlinx.coroutines
- **Custom utility functions**: Create your own cross-platform implementations

### 🧪 MANDATORY TESTING:
After implementing any functionality in `commonMain`, **ALWAYS** run:
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```
Or for device builds:
```bash
./gradlew :composeApp:compileKotlinIosArm64
```

**If this task fails, the code is NOT multiplatform compatible and must be fixed before proceeding.**

### 🔧 Common Fixes:
- Replace `System.currentTimeMillis()` with simplified timestamp strings
- Replace `android.graphics.Color.parseColor()` with custom hex parsing
- Replace `String.format()` with manual string building or custom formatters
- Move platform-specific code to `androidMain`/`iosMain` folders

## Testing Instructions
- Unit tests are located in `commonTest` for shared logic
- Platform-specific tests should be in respective test folders
- Run `./gradlew test` to execute all tests
- Ensure tests pass on both Android and iOS platforms
- Test dependency injection by mocking dependencies in test modules

## Platform Considerations
### Android
- Minimum SDK level and target SDK are defined in build configuration
- Use Android-specific APIs only in `androidMain` folder
- Android-specific Koin modules should be in `androidMain/kotlin/.../di/`

### iOS
- iOS-specific code goes in `iosMain` folder
- For iOS app testing, open `/iosApp` in Xcode
- Ensure compatibility with required iOS versions
- iOS-specific Koin modules should be in `iosMain/kotlin/.../di/`

## Security Considerations
- Never commit sensitive information like API keys
- Use build configuration or environment variables for secrets
- Validate all user inputs
- Follow platform security best practices for both Android and iOS
- Be careful with dependency injection of sensitive data
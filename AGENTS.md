# Repository Guidelines

## Project Structure & Module Organization
- `composeApp/src/commonMain/kotlin` holds shared Compose UI and domain code.
- `composeApp/src/androidMain/kotlin` and `composeApp/src/iosMain/kotlin` contain platform extensions; keep platform APIs here.
- `composeApp/src/commonTest/kotlin` stores multiplatform unit tests, with platform-specific test roots mirroring their source sets.
- `iosApp` packages the Xcode project for the iOS wrapper.
- Root Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`, `gradle/`) configure multiplatform targets and dependencies.

## Build, Test, and Development Commands
- `./gradlew :composeApp:assembleDebug` builds the Android debug APK.
- `./gradlew :composeApp:assembleRelease` produces the signed release bundle.
- `./gradlew test` runs the multiplatform unit test suite.
- `./gradlew :composeApp:compileKotlinIosSimulatorArm64` verifies iOS simulator compatibility for shared code.
- `./gradlew ktlintCheck` / `ktlintFormat` validate and auto-format Kotlin style.
- `./gradlew clean` resets build artifacts before reproducing issues.

## Coding Style & Naming Conventions
- Follow Kotlin style with 4-space indentation and trailing commas where idiomatic.
- Name packages under `dev.tireless.abun`, keeping feature modules in dedicated subpackages.
- Compose screens and components use PascalCase (e.g., `AccountHierarchySelector`); view models end with `ViewModel`.
- Prefer constructor injection via Koin modules; register shared bindings in `AppModule.kt` and platform-specific bindings in their respective modules.
- Document public APIs with concise KDoc and keep files focused on a single feature.

## Testing Guidelines
- Add new coverage in `commonTest` when changing shared logic; mirror platform-specific behaviors in `androidTest` or `iosTest` when needed.
- Name tests using `Function_underTest_expectedResult` for readability.
- Run `./gradlew test` and the iOS compile check before requesting review; tests must pass without flaky behavior.
- Include fixtures or fake Koin modules when isolating dependencies.

## Commit & Pull Request Guidelines
- Use conventional prefixes observed in history (`feat:`, `fix:`, `refactor:`, `chore:`) followed by a clear imperative summary.
- Group coherent changes per commit; avoid bundling unrelated refactors.
- Pull requests should outline intent, list major changes, mention impacted modules, and link issues or tasks.
- Attach screenshots or screen recordings for UI updates and describe validation steps (commands run, devices tested).
- Confirm no secrets are committed; rely on `local.properties` and environment variables for sensitive values.

## Security & Configuration Tips
- Store secrets outside the repo; prefer Gradle properties or CI secrets.
- Keep dependencies updated via Gradle version catalogs and review license compatibility.
- Validate user-provided data on both platforms and avoid logging sensitive information.

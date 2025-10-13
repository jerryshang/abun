# Repository Guidelines

## Project Structure & Module Organization
- `composeApp/` hosts Kotlin Multiplatform sources. Write shared code in `src/commonMain/kotlin/dev/tireless/abun`, and keep platform API bridges in the matching `androidMain` and `iosMain` source sets.
- `composeApp/src/commonMain/sqldelight` stores SQLDelight schemas by domain; colocate migrations with their feature folder (`time/`, `mental/`, `material/`).
- `iosApp/` wraps the shared module in Xcode; Gradle scripts at the root (`build.gradle.kts`, `settings.gradle.kts`, `gradle/`) configure targets, plugins, and version catalogs.
- Keep reference material in `docs/` and contributor policies in the repository root for easy discovery.

## Build, Test, and Development Commands
- `./gradlew :composeApp:assembleDebug` builds the Android debug APK for manual validation.
- `./gradlew :composeApp:compileKotlinIosSimulatorArm64` ensures the shared module compiles against the iOS simulator toolchain.
- `./gradlew test` executes all unit tests across common and platform source sets.
- `./gradlew ktlintCheck` / `./gradlew ktlintFormat` enforce Kotlin style; run before committing.
- `./gradlew clean` purges build outputs when troubleshooting.

## Coding Style & Naming Conventions
- Kotlin files use 4-space indentation, trailing commas in multiline collections, and package roots under `dev.tireless.abun`.
- Compose UI, feature screens, and view models use PascalCase (`TaskScheduleScreen`, `TaskScheduleViewModel`); data sources end with `Repository`.
- SQLDelight table names use snake_case and remain singular (for example, `note`, `account`, `transaction_group_member`).
- Keep feature folders flat—place models, repositories, screens, and view models together within each domain directory.
- Register dependency bindings through Koin modules (`core/AppModule.kt`); avoid global singletons outside DI wiring.

## Testing Guidelines
- Write shared tests in `composeApp/src/commonTest/kotlin`; mirror platform-only behavior in `androidTest` or `iosTest` as appropriate.
- Name test classes after the subject and functions using `functionUnderTest_expectedOutcome`.
- Run `./gradlew test` and the iOS compile task before review requests; ensure tests pass without flakes.
- Prefer fake Koin modules or in-memory drivers for isolation; do not rely on network access during unit tests.

## Commit & Pull Request Guidelines
- Use Conventional Commits (`feat:`, `fix:`, `refactor:`, `chore:`) with imperative summaries under 72 characters.
- Group cohesive changes per commit; rebase or squash cosmetic fixes before opening a pull request.
- Detail PR intent, impacted packages, and validation commands; link issues or specs when available.
- Include screenshots or screen recordings for UI adjustments and note tested devices or simulators.

## Environment & Security
- Store secrets in `local.properties` or CI-managed variables—never commit credentials or signing keys.
- Track dependency updates via the Gradle version catalog; run `./gradlew build` after upgrades to surface regressions.
- Review new dependencies for license compatibility and limit logging of user data.

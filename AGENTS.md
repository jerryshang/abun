# Repository Guidelines

## Project Structure & Module Organization
- Keep shared Kotlin in `composeApp/src/commonMain/kotlin/dev/tireless/abun`; mirror platform bridges in `androidMain` and `iosMain`.
- Place SQLDelight schemas and migrations by domain under `composeApp/src/commonMain/sqldelight/{time|mental|material}`.
- Use `iosApp/` for the Xcode wrapper and root Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`, `gradle/`) for target configuration.
- Reserve `docs/` for reference material and store contributor policies at the repository root for quick discovery.
- Use SQLite (via SQLDelight) to persist all data; keep in-memory state limited to transient UI concerns.

## Build, Test, and Development Commands
- `./gradlew :composeApp:assembleDebug` builds the Android debug APK for device validation.
- `./gradlew :composeApp:compileKotlinIosSimulatorArm64` checks the shared module against the iOS simulator toolchain.
- `./gradlew test` runs all unit tests across common and platform source sets.
- `./gradlew ktlintCheck` (or `ktlintFormat`) enforces Kotlin style before commits.
- `./gradlew clean` clears build outputs to unblock flaky builds.

## Coding Style & Naming Conventions
- Use four-space indentation, trailing commas in multiline lists, and the `dev.tireless.abun` package root.
- Compose screens, view models, and data repositories follow PascalCase (`TaskScheduleViewModel`, `TaskScheduleScreen`, `ScheduleRepository`).
- SQLDelight tables remain singular snake_case (e.g., `transaction_group_member`); keep feature folders flat with models, repositories, and UI side by side.
- Register dependencies via Koin modules under `core/AppModule.kt`; avoid ad-hoc singletons.

## Testing Guidelines
- Write shared tests in `composeApp/src/commonTest/kotlin`; target platform-only behavior in `androidTest` or `iosTest`.
- Name tests with the `functionUnderTest_expectedOutcome` pattern and prefer fakes or in-memory drivers for isolation.
- Always run `./gradlew test` plus the iOS compile task before review to catch cross-platform regressions.

## Commit & Pull Request Guidelines
- Follow Conventional Commits (`feat:`, `fix:`, `chore:`) with imperative summaries under 72 characters.
- Group cohesive changes per commit; rebase or squash cosmetic fixes before opening a PR.
- Document PR intent, affected packages, validation commands, and attach screenshots or recordings for UI updates; link issues or specs when relevant.

## Environment & Security
- Store secrets in `local.properties` or CI-managed variables; never commit credentials or signing keys.
- Track dependency updates through the Gradle version catalog and run `./gradlew build` after upgrades to surface regressions.
- Keep user data out of logs and review new dependencies for license compatibility before merging.

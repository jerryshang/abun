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

## Finance Module - Double-Entry Bookkeeping Rules
**CRITICAL**: The finance module implements proper double-entry bookkeeping. These accounting principles MUST be maintained:

### Core Principles:
1. **Every transaction must balance**: Total debits must equal total credits
2. **Transfers require TWO transaction records**: One debit from source account, one credit to destination account
3. **Never modify double-entry structure**: Do not suggest "simplifying" to single transactions for transfers

### Database Schema Constraints:
- **`transfer_group_id`** (UUID string): Links the two sides of a transfer transaction (debit + credit pair). REQUIRED for transfers.
- **`group_id`** (Integer FK): Links multiple related transactions over time (loans, installments, split bills). Different purpose from `transfer_group_id`.
- These two fields serve different purposes and both are necessary - NOT duplicates.

### Transfer Transaction Rules:
```kotlin
// CORRECT: Two transactions linked by transfer_group_id
Transfer $100 from Cash to Bank:
  Transaction 1: account_id=Cash, amount=100, to_account_id=Bank, transfer_group_id=UUID
  Transaction 2: account_id=Bank, amount=100, to_account_id=Cash, transfer_group_id=UUID

// WRONG: Single transaction (violates double-entry)
Transfer $100 from Cash to Bank:
  Transaction: account_id=Cash, to_account_id=Bank, amount=100  ❌
```

### Update/Delete Rules:
- **Editing a transfer**: Must update BOTH transactions in the pair
- **Deleting a transfer**: Must delete BOTH transactions in the pair
- **Balance adjustments**: Must adjust BOTH account balances (source -amount, destination +amount)
- See `TransactionRepository.kt` updateTransaction() and deleteTransaction() for reference implementation

### Why This Matters:
- Maintains accounting integrity and audit trail
- Ensures account balances are always correct and verifiable
- Prevents orphaned transactions and balance discrepancies
- Standard practice in all professional accounting software

**Never suggest removing or simplifying the double-entry structure, even if it seems complex. The complexity is necessary and correct.**
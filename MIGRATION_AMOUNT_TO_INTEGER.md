# Finance Module Migration Guide: REAL to INTEGER Amount Storage

## Overview
This migration changes all monetary amounts from `REAL` (Double) to `INTEGER` (Long) storage format to avoid floating-point precision loss. All amounts are stored as `actual_value * 10000` to support up to 4 decimal places (.4f precision).

## Database Changes

### Schema Changes
1. `Account.credit_limit`: REAL → INTEGER (default -1 for non-liability)
2. `Transaction.amount`: REAL → INTEGER
3. `TransactionGroup.total_amount`: REAL → INTEGER

### Migration SQL
```sql
-- Drop existing tables and recreate with new schema
-- WARNING: This will delete all existing data!
DROP TABLE IF EXISTS TransactionTag;
DROP TABLE IF EXISTS TransactionGroupMember;
DROP TABLE IF EXISTS TransactionAttachment;
DROP TABLE IF EXISTS TransactionLinkedItem;
DROP TABLE IF EXISTS `Transaction`;
DROP TABLE IF EXISTS TransactionGroup;
DROP TABLE IF EXISTS Account;
DROP TABLE IF EXISTS FinanceCategory;
DROP TABLE IF EXISTS FinanceTag;

-- Recreate tables with INTEGER amounts (see Finance.sq)
```

## Code Changes Completed

### ✅ AmountUtils.kt
- Created conversion functions: `toStorageAmount()`, `toDisplayAmount()`
- Input validation: `isValidAmountInput()`, `parseAmountInput()`
- Formatting: `formatAmount()`, `formatStorageAmount()`

### ✅ Finance.sq
- Updated schema to use INTEGER for all amounts
- Updated `calculateAccountBalance` to return INTEGER
- Set `credit_limit` default to -1

### ✅ FinanceModels.kt
- Updated `Account.creditLimitStorage: Long` with computed `creditLimit: Double?`
- Updated `Transaction.amountStorage: Long` with computed `amount: Double`
- Updated `TransactionGroup.totalAmountStorage: Long?` with computed `totalAmount: Double?`
- Added `getCreditLimitStorage()` helper to input models

### ✅ AccountRepository.kt
- Updated `toDomain()` mapper to use `creditLimitStorage`
- Updated `createAccount()` to use `getCreditLimitStorage()`
- Updated `updateAccount()` to use `getCreditLimitStorage()`
- Updated `calculateAccountBalance()` to convert storage to display amount

## Code Changes Remaining

### ⏳ TransactionRepository.kt
Update all transaction creation/update methods:

```kotlin
// In createTransaction()
queries.insertTransaction(
  amount = input.amount.toStorageAmount(), // Convert to storage
  // ... rest
)

// In createLoan()
queries.insertTransaction(
  amount = input.amount.toStorageAmount(), // Convert to storage
  // ... rest
)

// Mapper function
private fun DbTransaction.toDomain() = Transaction(
  id = id,
  amountStorage = amount, // Already in storage format from DB
  debitAccountId = debit_account_id,
  creditAccountId = credit_account_id,
  // ... rest
)

// Summary functions need conversion
suspend fun getExpenseSummary(...): Map<Long?, Double> {
  queries.getExpenseSumByCategory(...).executeAsList()
    .associate { it.category_id to (it.total?.toDisplayAmount() ?: 0.0) }
}
```

### ⏳ TransactionGroupRepository.kt
Update group creation:

```kotlin
queries.insertTransactionGroup(
  name = name,
  group_type = groupType.name.lowercase(),
  description = description,
  total_amount = totalAmount?.toStorageAmount(), // Convert
  // ... rest
)

// Mapper
private fun DbTransactionGroup.toDomain() = TransactionGroup(
  id = id,
  name = name,
  groupType = TransactionGroupType.fromString(group_type),
  description = description,
  totalAmountStorage = total_amount, // Already in storage format
  // ... rest
)
```

### ⏳ All Finance Dialogs - Input Validation
Update ALL dialogs to:

1. **Validate .4f input**:
```kotlin
OutlinedTextField(
  value = amount,
  onValueChange = {
    if (isValidAmountInput(it)) {
      amount = it
    }
  },
  isError = amount.isNotEmpty() && !isValidAmountInput(amount),
  supportingText = {
    if (amount.isNotEmpty() && !isValidAmountInput(amount)) {
      Text("Please enter valid amount (up to 4 decimal places)")
    }
  }
)
```

2. **Enable button based on category/account selection**:
```kotlin
// For AddExpenseDialog/AddIncomeDialog
Button(
  enabled = parseAmountInput(amount) != null &&
    parseAmountInput(amount)!! > 0 &&
    selectedCategoryId != null && // IMPORTANT: Category must be selected
    selectedAccountId != null, // IMPORTANT: Account must be selected
  onClick = { /* ... */ }
)

// For AddTransferDialog
Button(
  enabled = parseAmountInput(amount) != null &&
    parseAmountInput(amount)!! > 0 &&
    selectedAccountId != null &&
    toAccountId != null &&
    selectedAccountId != toAccountId,
  onClick = { /* ... */ }
)

// For AddLoanDialog
Button(
  enabled = parseAmountInput(amount) != null &&
    parseAmountInput(amount)!! > 0 &&
    payee.isNotBlank() &&
    parseAmountInput(interestRate) != null &&
    loanMonths.toIntOrNull() != null &&
    loanMonths.toIntOrNull()!! > 0 &&
    assetAccounts.isNotEmpty(),
  onClick = { /* ... */ }
)
```

### ⏳ FinanceScreen.kt
Replace `formatAmount()` calls with the one from AmountUtils:
```kotlin
import dev.tireless.abun.finance.formatAmount

// Usage remains the same
Text("¥${formatAmount(account.currentBalance)}")
```

### ⏳ AccountManagementScreen.kt
Update credit limit input handling:
```kotlin
OutlinedTextField(
  value = creditLimit,
  onValueChange = {
    if (isValidAmountInput(it)) {
      creditLimit = it
    }
  },
  label = { Text("Credit Limit") },
  isError = creditLimit.isNotEmpty() && !isValidAmountInput(creditLimit)
)
```

## Testing Checklist

After completing all changes:

- [ ] Android build compiles
- [ ] iOS build compiles
- [ ] Database initializes correctly
- [ ] Can create accounts with credit limit
- [ ] Credit limit displays correctly for liability accounts
- [ ] Can create expense transactions
- [ ] Can create income transactions
- [ ] Can create transfer transactions
- [ ] Can create loans
- [ ] Transaction amounts display correctly
- [ ] Account balances calculate correctly
- [ ] Input validation prevents invalid amounts
- [ ] Submit buttons disabled without required fields
- [ ] All amounts round correctly to 2 decimal places in display
- [ ] No precision loss in calculations

## Breaking Changes

**⚠️ WARNING: This is a BREAKING database migration!**

All existing data will be lost. If preserving data is important:
1. Export all existing transactions to JSON
2. Apply schema changes
3. Re-import transactions with amount conversion

## Rollback Plan

If issues arise, revert by:
1. `git checkout` previous commit
2. Delete app data to remove new schema
3. Reinstall with old schema

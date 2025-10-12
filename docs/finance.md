# Finance Module - System Design

## Overview

The Finance module implements a complete **double-entry bookkeeping** system for personal finance management. It uses a pure hierarchy-based chart of accounts with 5 fundamental root accounts, following standard accounting principles.

## Core Principles

### 1. Double-Entry Bookkeeping

Every financial transaction affects **two accounts**: one debit and one credit. The total debits must always equal total credits, maintaining the accounting equation:

```
Assets = Liabilities + Equity
Assets + Expenses = Liabilities + Equity + Revenue
```

### 2. Chart of Accounts Hierarchy

All accounts are organized in a tree structure with **5 fixed root accounts**:

| Root Account | ID | Type | Description |
|--------------|-----|------|-------------|
| Asset | 1 | Resources owned | Cash, bank accounts, investments |
| Liability | 2 | Debts owed | Loans, credit cards |
| Equity | 3 | Owner's stake | Capital, retained earnings |
| Revenue | 4 | Income streams | Salary, sales, interest income |
| Expense | 5 | Costs incurred | Food, rent, utilities, transport |

**Key Design Decisions:**
- Root account IDs are **fixed** (1-5) and initialized by SQLDelight on database creation
- Root accounts have `parent_id = NULL`
- All user accounts are children of these 5 roots
- Account type is determined by traversing `parent_id` to the root
- Root accounts are **hidden from UI** (`is_visible_in_ui = 0`)
- Root accounts **cannot be deleted** (enforced by SQL: `DELETE WHERE id > 5`)

## Database Schema

### Account Table

```sql
CREATE TABLE Account (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    parent_id INTEGER,  -- NULL for root accounts (1-5)
    initial_balance REAL NOT NULL DEFAULT 0.0,
    current_balance REAL NOT NULL DEFAULT 0.0,
    currency TEXT NOT NULL DEFAULT 'CNY',
    is_active INTEGER NOT NULL DEFAULT 1,
    is_visible_in_ui INTEGER NOT NULL DEFAULT 1,
    icon_name TEXT,
    color_hex TEXT,
    bill_date INTEGER,      -- For credit cards
    payment_date INTEGER,   -- For credit cards/loans
    credit_limit REAL,      -- For credit cards
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (parent_id) REFERENCES Account(id) ON DELETE SET NULL
);
```

**Root Account Initialization (auto-executed by SQLDelight):**
```sql
INSERT INTO Account (id, name, parent_id, ...) VALUES
  (1, 'Asset', NULL, ...),      -- RootAccountIds.ASSET
  (2, 'Liability', NULL, ...),  -- RootAccountIds.LIABILITY
  (3, 'Equity', NULL, ...),     -- RootAccountIds.EQUITY
  (4, 'Revenue', NULL, ...),    -- RootAccountIds.REVENUE
  (5, 'Expense', NULL, ...);    -- RootAccountIds.EXPENSE
```

### Transaction Table

```sql
CREATE TABLE Transaction (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    amount INTEGER NOT NULL,  -- Always positive, stored as actual * 10000
    debit_account_id INTEGER NOT NULL,   -- Account being debited
    credit_account_id INTEGER NOT NULL,  -- Account being credited
    transaction_date INTEGER NOT NULL,
    transfer_group_id TEXT,  -- UUID linking transfer pairs
    payee TEXT,
    member TEXT,
    notes TEXT,
    state TEXT NOT NULL DEFAULT 'confirmed',  -- 'planned', 'estimated', 'confirmed'
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (debit_account_id) REFERENCES Account(id),
    FOREIGN KEY (credit_account_id) REFERENCES Account(id)
);
```

**Key Changes:**
- ✅ **Removed `category_id`**: Categories are now represented by the account hierarchy itself
- ✅ **Amount stored as INTEGER**: `amount * 10000` for precision (supports up to 4 decimal places)
- Transaction type (expense/income) is **inferred from account types**, not stored explicitly

### Transaction Groups (Many-to-Many)

```sql
CREATE TABLE TransactionGroup (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    group_type TEXT NOT NULL,  -- 'loan', 'installment', 'split', 'custom'
    description TEXT,
    total_amount REAL,
    status TEXT NOT NULL DEFAULT 'active',
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE TransactionGroupMember (
    transaction_id INTEGER NOT NULL,
    group_id INTEGER NOT NULL,
    PRIMARY KEY (transaction_id, group_id),
    FOREIGN KEY (transaction_id) REFERENCES Transaction(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES TransactionGroup(id) ON DELETE CASCADE
);
```

**Purpose of Transaction Groups:**
- Link related transactions over time (e.g., loan principal + multiple payment installments)
- Track installment plans
- Group split bills
- Different from `transfer_group_id` (which links the two sides of a single transfer)

### Supporting Tables

```sql
-- Tags (many-to-many with transactions)
CREATE TABLE FinanceTag (...);
CREATE TABLE TransactionTag (...);

-- Attachments
CREATE TABLE TransactionAttachment (...);

-- Cross-module linking
CREATE TABLE TransactionLinkedItem (...);
```

**Note**: The chart of accounts hierarchy (children of root accounts) provides categorization. There is no separate FinanceCategory table - expense and revenue accounts serve as categories naturally.

## Transaction Types & Translation Layer

The system provides a **translation layer** that converts user-friendly transaction types into proper double-entry records.

### 1. Expense Transaction

**User Input:**
```kotlin
CreateTransactionInput(
  type = TransactionType.EXPENSE,
  amount = 100.0,
  accountId = 15,      // "Food" expense account (child of Expense root, id=5)
  toAccountId = 6      // "现金" (Cash account - payment source)
)
```

**Internal Translation:**
- User selects **expense account** (what money is spent on - Food, Transport, etc.)
- User selects **payment source** (where money comes from - Cash, Bank, Credit Card)
- System creates single transaction with:
  - **Debit**: Expense account (accountId)
  - **Credit**: Payment source account (toAccountId)

**Accounting Entry:**
```
Debit:  Food Expense    ¥100
Credit: Cash            ¥100
```

**Effect:** Increases expense, decreases cash asset

### 2. Income Transaction

**User Input:**
```kotlin
CreateTransactionInput(
  type = TransactionType.INCOME,
  amount = 5000.0,
  accountId = 20,     // "Salary" revenue account (child of Revenue root, id=4)
  toAccountId = 7     // "Bank Account" (where money is deposited)
)
```

**Internal Translation:**
- User selects **revenue account** (type of income - Salary, Investment, etc.)
- User selects **receiving account** (where money is deposited - Bank, Cash, etc.)
- System creates single transaction with:
  - **Debit**: Receiving account (toAccountId)
  - **Credit**: Revenue account (accountId)

**Accounting Entry:**
```
Debit:  Bank Account       ¥5000
Credit: Salary Income      ¥5000
```

**Effect:** Increases bank asset, increases revenue

### 3. Transfer Transaction

**User Input:**
```kotlin
CreateTransactionInput(
  type = TransactionType.TRANSFER,
  amount = 200.0,
  accountId = 6,      // From: "现金"
  toAccountId = 7     // To: "Bank Account"
)
```

**Internal Translation:**
1. Generate UUID for `transfer_group_id`
2. Create **TWO** transactions (both with same `transfer_group_id`):

**Transaction 1:**
```
Debit:  Bank Account    ¥200
Credit: Cash            ¥200
```

**Transaction 2 (mirror):**
```
Debit:  Cash            ¥200
Credit: Bank Account    ¥200
```

**Effect:**
- Decreases cash by 200 (net: credit 200 - debit 200)
- Increases bank by 200 (net: debit 200 - credit 200)
- Maintains accounting balance

**Critical Rules:**
- **MUST create TWO transaction records** linked by `transfer_group_id`
- **Update**: Must update BOTH transactions
- **Delete**: Must delete BOTH transactions
- See `TransactionRepository.updateTransaction()` and `deleteTransaction()` for reference implementation

### 4. Loan Transaction

**User Input:**
```kotlin
CreateLoanInput(
  amount = 10000.0,
  accountId = 7,          // Borrower account (where money goes)
  lenderAccountId = 15,   // Lender account
  loanType = LoanType.EQUAL_INSTALLMENT,
  interestRate = 0.05,    // 5% annual
  loanMonths = 12,
  paymentDay = 15,
  startDate = ...
)
```

**Internal Translation:**
1. Create/get liability account: `parent_id = 2` (Liability root)
2. Create initial loan transaction:
   - **Debit**: Borrower's asset account
   - **Credit**: Liability account
3. Create transaction group for tracking payments
4. Generate payment schedule based on `loanType`
5. Create future payment transactions (state = 'planned')

**Initial Entry:**
```
Debit:  Bank Account    ¥10000
Credit: Loan Liability  ¥10000
```

**Monthly Payment Entry (example for equal installment):**
```
Debit:  Loan Liability  ¥800  (principal)
Debit:  Interest Expense ¥42  (interest)
Credit: Bank Account     ¥842 (total payment)
```

## Account Type Derivation

Since `account_type` is not stored in the database, it's derived by traversing the hierarchy:

```kotlin
// In AccountRepository
private fun getAccountTypeFromHierarchy(account: Account, allAccounts: List<Account>): AccountType {
  if (account.isRootAccount()) {
    return account.getRootAccountType() ?: AccountType.ASSET
  }

  // Traverse up to find root
  var current = account
  while (current.parentId != null) {
    val parent = allAccounts.find { it.id == current.parentId } ?: break
    current = parent
  }

  return current.getRootAccountType() ?: AccountType.ASSET
}
```

**Performance Optimization:**
- Account types are cached in `accountTypeCache: Map<Long, AccountType>`
- Cache is refreshed when accounts are created/updated
- `getAccountType(accountId)` provides O(1) lookup

## Key Queries

### Get Categories by Type

```sql
-- Expense categories (children of Expense root, id=5)
SELECT * FROM Account WHERE parent_id = 5 AND is_active = 1 ORDER BY name;

-- Revenue categories (children of Revenue root, id=4)
SELECT * FROM Account WHERE parent_id = 4 AND is_active = 1 ORDER BY name;

-- Asset accounts (children of Asset root, id=1)
SELECT * FROM Account WHERE parent_id = 1 AND is_active = 1 ORDER BY name;

-- Liability accounts (children of Liability root, id=2)
SELECT * FROM Account WHERE parent_id = 2 AND is_active = 1 ORDER BY name;
```

### Transfer Pair Lookup

```sql
-- Get both sides of a transfer
SELECT * FROM Transaction WHERE transfer_group_id = ? ORDER BY created_at;
```

### Transaction Groups

```sql
-- Get all transactions in a group (e.g., loan payments)
SELECT t.* FROM Transaction t
INNER JOIN TransactionGroupMember m ON t.id = m.transaction_id
WHERE m.group_id = ?
ORDER BY t.transaction_date;
```

## Repository Layer

### AccountRepository

**Key Methods:**
- `getExpenseCategories()` - Returns sub-accounts of Expense (id=5)
- `getRevenueCategories()` - Returns sub-accounts of Revenue (id=4)
- `getAssetAccounts()` - Returns sub-accounts of Asset (id=1)
- `getLiabilityAccounts()` - Returns sub-accounts of Liability (id=2)
- `getAccountType(accountId)` - O(1) lookup using cache
- `getOrCreateLiabilityAccount(loanName)` - Auto-creates liability accounts

**Note**: Accounts are created and managed directly by users through the Account Management UI. The chart of accounts hierarchy (children of root accounts) provides natural categorization without needing auto-creation logic.

**Cache Management:**
```kotlin
private var accountCache: Map<Long, Account> = emptyMap()
private var accountTypeCache: Map<Long, AccountType> = emptyMap()

private suspend fun refreshCache() {
  val accounts = queries.getAllAccounts().executeAsList().map { it.toDomain() }
  accountCache = accounts.associateBy { it.id }
  accountTypeCache = accounts.associate { account ->
    account.id to getAccountTypeFromHierarchy(account, accounts)
  }
}
```

### TransactionRepository

**Key Methods:**
- `createTransaction(input)` - Translates user input to double-entry
- `updateTransaction(input)` - Updates transaction, handles transfer pairs
- `deleteTransaction(id)` - Deletes transaction, handles transfer pairs
- `getTransactionWithDetails(id)` - Returns enriched transaction with account types
- `createLoan(input)` - Creates loan with payment schedule

**Transfer Handling:**
```kotlin
suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
  val transaction = getTransactionById(id) ?: return@withContext

  // If part of a transfer, delete both sides
  transaction.transferGroupId?.let { groupId ->
    queries.getTransferPair(groupId).executeAsList().forEach { tx ->
      queries.deleteTransaction(tx.id)
      // Update account balances...
    }
  } ?: run {
    queries.deleteTransaction(id)
    // Update account balances...
  }
}
```

### TransactionGroupRepository

**Key Methods:**
- `createGroup(input)` - Creates transaction group
- `addTransactionToGroup(transactionId, groupId)` - Links transaction to group
- `removeTransactionFromGroup(transactionId, groupId)` - Unlinks transaction
- `getGroupsByTransaction(transactionId)` - Gets all groups a transaction belongs to
- `getTransactionsByGroup(groupId)` - Gets all transactions in a group

## Domain Models

### Core Models

```kotlin
// Fixed root account IDs
object RootAccountIds {
  const val ASSET = 1L
  const val LIABILITY = 2L
  const val EQUITY = 3L
  const val REVENUE = 4L
  const val EXPENSE = 5L
}

// Account type enum
enum class AccountType {
  ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
}

// Account model (no accountType field - derived from hierarchy)
data class Account(
  val id: Long,
  val name: String,
  val parentId: Long? = null,  // NULL = root account
  val initialBalance: Double,
  val currentBalance: Double,
  val currency: String = "CNY",
  val isActive: Boolean = true,
  val isVisibleInUi: Boolean = true,
  val iconName: String? = null,
  val colorHex: String? = null,
  val billDate: Int? = null,
  val paymentDate: Int? = null,
  val creditLimit: Double? = null,
  val createdAt: Long,
  val updatedAt: Long
)

// Pure double-entry transaction
data class Transaction(
  val id: Long,
  val amountStorage: Long,  // Stored as actual * 10000 for precision
  val debitAccountId: Long,
  val creditAccountId: Long,
  val transactionDate: Long,
  val transferGroupId: String? = null,  // UUID for transfer pairs
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val state: TransactionState = TransactionState.CONFIRMED,
  val createdAt: Long,
  val updatedAt: Long
) {
  val amount: Double
    get() = amountStorage.toDisplayAmount()  // Convert back to Double for display
}

// Enriched transaction with account types (for UI)
data class TransactionWithDetails(
  val transaction: Transaction,
  val debitAccount: Account,
  val creditAccount: Account,
  val tags: List<FinanceTag> = emptyList(),
  val debitAccountType: AccountType,   // Cached for performance
  val creditAccountType: AccountType   // Cached for performance
)
```

### Input Models (User-Facing)

```kotlin
data class CreateTransactionInput(
  val amount: Double,
  val type: TransactionType,  // EXPENSE, INCOME, TRANSFER, LOAN
  val transactionDate: Long,
  val accountId: Long,  // For EXPENSE: expense account, INCOME: revenue account, TRANSFER: source
  val toAccountId: Long? = null,  // For EXPENSE: payment source, INCOME: receiving account, TRANSFER: destination
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val tagIds: List<Long> = emptyList()
)
```

**Semantic Meaning of `accountId` and `toAccountId` by Transaction Type:**

| Type | `accountId` | `toAccountId` |
|------|-------------|---------------|
| EXPENSE | Expense account (Food, Transport) | Payment source (Cash, Bank, Credit Card) |
| INCOME | Revenue account (Salary, Investment) | Receiving account (Bank, Cash) |
| TRANSFER | Source account (Cash) | Destination account (Bank) |

**Examples:**
```kotlin
// Expense: $100 on Food, paid with Cash
CreateTransactionInput(
  amount = 100.0,
  type = TransactionType.EXPENSE,
  accountId = 15,  // Food expense account
  toAccountId = 6  // Cash account
)

// Income: $5000 Salary deposited to Bank
CreateTransactionInput(
  amount = 5000.0,
  type = TransactionType.INCOME,
  accountId = 20,  // Salary revenue account
  toAccountId = 7  // Bank account
)

// Transfer: $200 from Cash to Bank
CreateTransactionInput(
  amount = 200.0,
  type = TransactionType.TRANSFER,
  accountId = 6,   // Cash (source)
  toAccountId = 7  // Bank (destination)
)
```

## Loan Management

### Loan Types

```kotlin
enum class LoanType {
  INTEREST_FIRST,     // 先息后本: Interest first, principal last
  EQUAL_PRINCIPAL,    // 等额本金: Equal principal payments
  EQUAL_INSTALLMENT,  // 等额本息: Equal total payments (principal + interest)
  INTEREST_ONLY       // 只还利息: Interest only, no principal repayment
}
```

### Loan Calculation Examples

**Equal Installment (等额本息):**
```
Principal: ¥10,000
Rate: 5% annual (0.4167% monthly)
Term: 12 months

Monthly Payment = P * [r(1+r)^n] / [(1+r)^n - 1]
                = 10,000 * [0.004167(1.004167)^12] / [(1.004167)^12 - 1]
                = ¥856.07

Month 1: Principal ¥814.40 + Interest ¥41.67 = ¥856.07
Month 2: Principal ¥817.80 + Interest ¥38.27 = ¥856.07
...
Month 12: Principal ¥852.51 + Interest ¥3.56 = ¥856.07
```

**Equal Principal (等额本金):**
```
Principal: ¥10,000
Rate: 5% annual
Term: 12 months

Monthly Principal = 10,000 / 12 = ¥833.33
Month 1 Interest = 10,000 * 0.004167 = ¥41.67
Month 1 Total = ¥833.33 + ¥41.67 = ¥875.00

Month 2 Interest = 9,166.67 * 0.004167 = ¥38.19
Month 2 Total = ¥833.33 + ¥38.19 = ¥871.52
...
```

## Data Integrity Rules

### Account Balance Updates

When a transaction is created, updated, or deleted:

1. **For Expense/Income:**
   - Update the asset account balance only
   - Expense/Revenue accounts are tracking accounts (balance doesn't matter for user)

2. **For Transfers:**
   - Update BOTH asset account balances
   - Debit account: balance increases
   - Credit account: balance decreases

3. **For Loans:**
   - Initial loan: Increase borrower asset, increase liability
   - Payment: Decrease liability, decrease expense account, decrease asset

### Transfer Integrity

```kotlin
// CORRECT: Always update both sides
suspend fun updateTransferTransaction(input: UpdateTransactionInput) {
  val transferGroupId = input.transferGroupId!!
  val pair = getTransferPair(transferGroupId)

  // Update both transactions
  pair.forEach { tx ->
    updateTransaction(...)
    updateAccountBalance(...)
  }
}

// WRONG: Never update just one side
suspend fun updateTransferTransaction(input: UpdateTransactionInput) {
  updateTransaction(input.id, ...)  // ❌ Breaks double-entry
}
```

## Constants & Configuration

```kotlin
// Root account IDs (must match Finance.sq initialization)
object RootAccountIds {
  const val ASSET = 1L
  const val LIABILITY = 2L
  const val EQUITY = 3L
  const val REVENUE = 4L
  const val EXPENSE = 5L
}

// Default currency
const val DEFAULT_CURRENCY = "CNY"

// Transaction states
enum class TransactionState {
  PLANNED,     // Fixed recurring payments (rent, loan)
  ESTIMATED,   // Variable recurring payments (utilities)
  CONFIRMED    // Manually logged transactions
}
```

## Migration & Initialization

### Database Initialization

The 5 root accounts are **automatically created** by SQLDelight when the database is first created:

```sql
-- In Finance.sq (executed once on database creation)
INSERT INTO Account (id, name, parent_id, ...) VALUES
  (1, 'Asset', NULL, ...),
  (2, 'Liability', NULL, ...),
  (3, 'Equity', NULL, ...),
  (4, 'Revenue', NULL, ...),
  (5, 'Expense', NULL, ...);
```

No Kotlin initialization code is needed. The `AppModule.kt` explicitly does **NOT** call any account initialization:

```kotlin
// AppModule.kt
single {
  val database = AppDatabase(get())
  // Note: The 5 root accounts (Asset, Liability, Equity, Revenue, Expense)
  // are automatically initialized by SQLDelight on database creation
  database
}
```

## Best Practices

### 1. Always Use Double-Entry

✅ **Correct:**
```kotlin
// Transfer ¥100 from Cash to Bank
Transaction(
  amount = 100.0,
  debitAccountId = bankId,      // Receiving account
  creditAccountId = cashId,     // Sending account
  transferGroupId = uuid
)
```

❌ **Wrong:**
```kotlin
// Single transaction for transfer
Transaction(
  amount = -100.0,  // ❌ Amount should always be positive
  accountId = cashId,
  toAccountId = bankId
)
```

### 2. Use Fixed Root IDs

✅ **Correct:**
```kotlin
val expenseCategories = accountRepository.getExpenseCategories()  // parent_id = 5
val assetAccounts = queries.getAssetAccounts().executeAsList()   // parent_id = 1
```

❌ **Wrong:**
```kotlin
val expenseRoot = accounts.find { it.name == "Expense" }  // ❌ Fragile
val expenseCategories = accounts.filter { it.parentId == expenseRoot.id }
```

### 3. Maintain Transfer Pair Integrity

✅ **Correct:**
```kotlin
suspend fun deleteTransaction(id: Long) {
  val tx = getTransactionById(id)
  tx.transferGroupId?.let { groupId ->
    // Delete BOTH sides of transfer
    getTransferPair(groupId).forEach { delete(it.id) }
  } ?: delete(id)
}
```

❌ **Wrong:**
```kotlin
suspend fun deleteTransaction(id: Long) {
  delete(id)  // ❌ Orphans the other side of transfer
}
```

### 4. Cache Account Types

✅ **Correct:**
```kotlin
val accountType = accountRepository.getAccountType(accountId)  // O(1) cached lookup
```

❌ **Wrong:**
```kotlin
val account = accountRepository.getAccountById(accountId)
var current = account
while (current.parentId != null) {
  current = accountRepository.getAccountById(current.parentId)  // ❌ Multiple DB queries
}
```

### 5. Balance Calculations

- `calculateAccountBalance(accountId, asOfDate)` sums debits minus credits for entries dated on or before the target day and only includes transactions with `state = 'confirmed'`.
- Dashboard totals show “how much money I have today” by adding balances from active, countable asset and liability accounts only; other account types are excluded from the headline figure.

## Future Enhancements

### Phase 1 (Current)
- ✅ Double-entry bookkeeping
- ✅ Hierarchy-based chart of accounts
- ✅ Fixed root account IDs
- ✅ Transfer transaction pairs
- ✅ Loan management with payment schedules
- ✅ Transaction groups (many-to-many)

### Phase 2 (Planned)
- [ ] Transaction templates for recurring expenses
- [ ] Scheduled transactions (auto-execute on date)
- [ ] Budget tracking (by expense category)
- [ ] Multi-currency support with exchange rates
- [ ] Financial reports (income statement, balance sheet)
- [ ] Account reconciliation

### Phase 3 (Future)
- [ ] Investment tracking (stocks, crypto)
- [ ] Tax category mapping
- [ ] Import from bank statements (CSV, OFX)
- [ ] Export to accounting software
- [ ] Shared accounts (multi-user)

## References

- **Double-Entry Bookkeeping**: [Wikipedia](https://en.wikipedia.org/wiki/Double-entry_bookkeeping)
- **Chart of Accounts**: [Accounting Tools](https://www.accountingtools.com/articles/what-is-a-chart-of-accounts.html)
- **Accounting Equation**: `Assets = Liabilities + Equity`
- **SQLDelight**: [Documentation](https://cashapp.github.io/sqldelight/)

---

**Last Updated:** 2025-01-02
**Version:** 1.0
**Module:** Finance (Personal Finance Management)

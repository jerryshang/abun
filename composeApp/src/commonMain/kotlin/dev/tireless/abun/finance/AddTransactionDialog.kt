package dev.tireless.abun.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * Dialog for adding or editing a transaction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
  transactionWithDetails: TransactionWithDetails? = null,
  accounts: List<AccountWithBalance>,
  onDismiss: () -> Unit,
  onConfirm: (CreateTransactionInput) -> Unit,
  accountRepository: AccountRepository = koinInject()
) {
  val transaction = transactionWithDetails?.transaction
  val inferredType = transactionWithDetails?.inferType()
  val primaryAccount = transactionWithDetails?.getPrimaryAccount()
  val secondaryAccount = transactionWithDetails?.getSecondaryAccount()

  var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
  var selectedType by remember { mutableStateOf(inferredType ?: TransactionType.EXPENSE) }
  var selectedAccountId by remember { mutableStateOf(primaryAccount?.id ?: accounts.firstOrNull()?.id ?: 0L) }
  var selectedToAccountId by remember { mutableStateOf<Long?>(secondaryAccount?.id) }
  var selectedExpenseRevenueAccountId by remember { mutableStateOf<Long?>(null) }
  var payee by remember { mutableStateOf(transaction?.payee ?: "") }
  var member by remember { mutableStateOf(transaction?.member ?: "") }
  var notes by remember { mutableStateOf(transaction?.notes ?: "") }

  // Filter accounts based on transaction type
  val filteredAccounts = remember(selectedType, accounts) {
    when (selectedType) {
      TransactionType.EXPENSE -> {
        // For expenses, show Asset/Liability accounts for payment source
        accounts.filter { account ->
          val parentId = account.parentId
          parentId == RootAccountIds.ASSET || parentId == RootAccountIds.LIABILITY
        }
      }
      TransactionType.INCOME -> {
        // For income, show Asset accounts for receiving money
        accounts.filter { account ->
          account.parentId == RootAccountIds.ASSET
        }
      }
      TransactionType.TRANSFER -> {
        // For transfers, show only Asset accounts
        accounts.filter { account ->
          account.parentId == RootAccountIds.ASSET
        }
      }
      else -> accounts
    }
  }

  // Filter expense/revenue accounts for categorization
  val expenseOrRevenueAccounts = remember(selectedType, accounts) {
    when (selectedType) {
      TransactionType.EXPENSE -> accounts.filter { it.parentId == RootAccountIds.EXPENSE }
      TransactionType.INCOME -> accounts.filter { it.parentId == RootAccountIds.REVENUE }
      else -> emptyList()
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(if (transaction == null) "添加交易" else "编辑交易")
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, "关闭")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Transaction Type Selector
        Text("交易类型", style = MaterialTheme.typography.labelLarge)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TransactionType.values().filter { it != TransactionType.LOAN && it != TransactionType.LOAN_PAYMENT }.forEach { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = { selectedType = type },
              label = {
                Text(
                  when (type) {
                    TransactionType.EXPENSE -> "支出"
                    TransactionType.INCOME -> "收入"
                    TransactionType.TRANSFER -> "转账"
                    else -> type.name
                  }
                )
              }
            )
          }
        }

        // Amount Input
        OutlinedTextField(
          value = amount,
          onValueChange = { amount = it },
          label = { Text("金额") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          prefix = { Text("¥") }
        )

        // Account Selector
        var expandedAccounts by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedAccounts,
          onExpandedChange = { expandedAccounts = it }
        ) {
          OutlinedTextField(
            value = filteredAccounts.find { it.id == selectedAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
              Text(
                when (selectedType) {
                  TransactionType.EXPENSE -> "支出账户"
                  TransactionType.INCOME -> "收入账户"
                  TransactionType.TRANSFER -> "源账户"
                  else -> "账户"
                }
              )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccounts) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor()
          )
          ExposedDropdownMenu(
            expanded = expandedAccounts,
            onDismissRequest = { expandedAccounts = false }
          ) {
            filteredAccounts.forEach { account ->
              DropdownMenuItem(
                text = { Text(account.name) },
                onClick = {
                  selectedAccountId = account.id
                  expandedAccounts = false
                }
              )
            }
          }
        }

        // Transfer To Account (if transfer)
        if (selectedType == TransactionType.TRANSFER) {
          var expandedToAccounts by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = expandedToAccounts,
            onExpandedChange = { expandedToAccounts = it }
          ) {
            OutlinedTextField(
              value = filteredAccounts.find { it.id == selectedToAccountId }?.name ?: "",
              onValueChange = {},
              readOnly = true,
              label = { Text("转入账户") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedToAccounts) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
            )
            ExposedDropdownMenu(
              expanded = expandedToAccounts,
              onDismissRequest = { expandedToAccounts = false }
            ) {
              filteredAccounts.filter { it.id != selectedAccountId }.forEach { account ->
                DropdownMenuItem(
                  text = { Text(account.name) },
                  onClick = {
                    selectedToAccountId = account.id
                    expandedToAccounts = false
                  }
                )
              }
            }
          }
        }

        // Expense/Revenue Account Selector (for categorization - not for transfers)
        if (selectedType != TransactionType.TRANSFER && expenseOrRevenueAccounts.isNotEmpty()) {
          var expandedExpenseRevenue by remember { mutableStateOf(false) }

          ExposedDropdownMenuBox(
            expanded = expandedExpenseRevenue,
            onExpandedChange = { expandedExpenseRevenue = it }
          ) {
            OutlinedTextField(
              value = expenseOrRevenueAccounts.find { it.id == selectedExpenseRevenueAccountId }?.name ?: "",
              onValueChange = {},
              readOnly = true,
              label = { Text(if (selectedType == TransactionType.EXPENSE) "支出类别" else "收入类别") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExpenseRevenue) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
              supportingText = {
                Text(if (selectedType == TransactionType.EXPENSE) "花在什么上面" else "收入来源")
              }
            )
            ExposedDropdownMenu(
              expanded = expandedExpenseRevenue,
              onDismissRequest = { expandedExpenseRevenue = false }
            ) {
              expenseOrRevenueAccounts.forEach { account ->
                DropdownMenuItem(
                  text = { Text(account.name) },
                  onClick = {
                    selectedExpenseRevenueAccountId = account.id
                    expandedExpenseRevenue = false
                  }
                )
              }
            }
          }
        }

        // Payee/Source
        OutlinedTextField(
          value = payee,
          onValueChange = { payee = it },
          label = { Text(if (selectedType == TransactionType.INCOME) "来源" else "商家") },
          modifier = Modifier.fillMaxWidth()
        )

        // Member
        OutlinedTextField(
          value = member,
          onValueChange = { member = it },
          label = { Text("成员") },
          modifier = Modifier.fillMaxWidth()
        )

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("备注") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amountValue = amount.toDoubleOrNull() ?: 0.0
          if (amountValue > 0) {
            // Determine correct accountId and toAccountId based on type
            val (finalAccountId, finalToAccountId) = when (selectedType) {
              TransactionType.EXPENSE -> {
                // For expenses: accountId = expense account, toAccountId = payment source
                Pair(selectedExpenseRevenueAccountId ?: selectedAccountId, selectedAccountId)
              }
              TransactionType.INCOME -> {
                // For income: accountId = revenue account, toAccountId = receiving account
                Pair(selectedExpenseRevenueAccountId ?: selectedAccountId, selectedAccountId)
              }
              TransactionType.TRANSFER -> {
                // For transfers: accountId = source, toAccountId = destination
                Pair(selectedAccountId, selectedToAccountId)
              }
              else -> Pair(selectedAccountId, selectedToAccountId)
            }

            onConfirm(
              CreateTransactionInput(
                amount = amountValue,
                type = selectedType,
                transactionDate = 1704067200000L, // 2024-01-01 00:00:00 UTC - Simplified for KMP
                accountId = finalAccountId,
                toAccountId = finalToAccountId,
                payee = payee.ifBlank { null },
                member = member.ifBlank { null },
                notes = notes.ifBlank { null }
              )
            )
          }
        },
        enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
      ) {
        Text("确认")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("取消")
      }
    }
  )
}

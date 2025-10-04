package dev.tireless.abun.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Receipt

/**
 * Dialog for adding an expense transaction
 *
 * User Flow:
 * 1. Enter amount
 * 2. Select payment account (Asset/Liability - where money comes from)
 * 3. Select expense account (what the money is spent on - Food, Transport, etc.)
 * 4. Optional: payee, member, notes
 *
 * Accounting:
 * - Debit: Expense account (selected by user - Expense account)
 * - Credit: Payment account (selected by user - Asset/Liability)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
  onDismiss: () -> Unit,
  onConfirm: (CreateTransactionInput) -> Unit,
  accounts: List<AccountWithBalance>
) {
  var amount by remember { mutableStateOf("") }
  var selectedPaymentAccountId by remember { mutableStateOf<Long?>(null) }
  var selectedExpenseAccountId by remember { mutableStateOf<Long?>(null) }
  var payee by remember { mutableStateOf("") }
  var member by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  // Filter to show only Asset and Liability accounts (user's real accounts)
  val paymentAccounts = remember(accounts) {
    accounts.filter { account ->
      account.parentId == RootAccountIds.ASSET || account.parentId == RootAccountIds.LIABILITY
    }
  }

  // Filter to show only Expense accounts (what money is spent on)
  val expenseAccounts = remember(accounts) {
    accounts.filter { account ->
      account.parentId == RootAccountIds.EXPENSE
    }
  }

  // Set initial selections
  LaunchedEffect(paymentAccounts, expenseAccounts) {
    if (selectedPaymentAccountId == null && paymentAccounts.isNotEmpty()) {
      selectedPaymentAccountId = paymentAccounts.first().id
    }
    if (selectedExpenseAccountId == null && expenseAccounts.isNotEmpty()) {
      selectedExpenseAccountId = expenseAccounts.first().id
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Lucide.Receipt, "支出", modifier = Modifier.size(24.dp))
          Text("添加支出")
        }
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
        // Amount Input
        OutlinedTextField(
          value = amount,
          onValueChange = { amount = it },
          label = { Text("金额") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          prefix = { Text("¥") },
          supportingText = { Text("支出金额") }
        )

        // Payment Account Selector
        var expandedAccounts by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedAccounts,
          onExpandedChange = { expandedAccounts = it }
        ) {
          OutlinedTextField(
            value = paymentAccounts.find { it.id == selectedPaymentAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("支付账户") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccounts) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("从哪个账户支付") }
          )
          ExposedDropdownMenu(
            expanded = expandedAccounts,
            onDismissRequest = { expandedAccounts = false }
          ) {
            paymentAccounts.forEach { account ->
              DropdownMenuItem(
                text = {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(account.name)
                    Text(
                      "¥${formatAmount(account.currentBalance)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                },
                onClick = {
                  selectedPaymentAccountId = account.id
                  expandedAccounts = false
                }
              )
            }
          }
        }

        // Expense Account Selector (what money is spent on)
        var expandedExpense by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedExpense,
          onExpandedChange = { expandedExpense = it }
        ) {
          OutlinedTextField(
            value = expenseAccounts.find { it.id == selectedExpenseAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("支出类别") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExpense) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("花在什么上面（餐饮、交通等）") }
          )
          ExposedDropdownMenu(
            expanded = expandedExpense,
            onDismissRequest = { expandedExpense = false }
          ) {
            expenseAccounts.forEach { account ->
              DropdownMenuItem(
                text = { Text(account.name) },
                onClick = {
                  selectedExpenseAccountId = account.id
                  expandedExpense = false
                }
              )
            }
          }
        }

        // Payee (Optional)
        OutlinedTextField(
          value = payee,
          onValueChange = { payee = it },
          label = { Text("商家（可选）") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        // Member (Optional)
        OutlinedTextField(
          value = member,
          onValueChange = { member = it },
          label = { Text("成员（可选）") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          supportingText = { Text("这笔支出是为谁") }
        )

        // Notes (Optional)
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("备注（可选）") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          maxLines = 4
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amountValue = amount.toDoubleOrNull()
          if (amountValue != null &&
            amountValue > 0 &&
            selectedExpenseAccountId != null &&
            selectedPaymentAccountId != null
          ) {
            onConfirm(
              CreateTransactionInput(
                amount = amountValue,
                type = TransactionType.EXPENSE,
                transactionDate = getCurrentTimeMillis(),
                accountId = selectedExpenseAccountId!!, // Expense account (what money is spent on)
                toAccountId = selectedPaymentAccountId!!, // Payment source (asset/liability)
                payee = payee.takeIf { it.isNotBlank() },
                member = member.takeIf { it.isNotBlank() },
                notes = notes.takeIf { it.isNotBlank() }
              )
            )
            onDismiss()
          }
        },
        enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
          selectedExpenseAccountId != null &&
          selectedPaymentAccountId != null
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

/**
 * Get current timestamp in milliseconds (KMP-compatible)
 * TODO: Replace with proper platform-specific implementation
 */
private fun getCurrentTimeMillis(): Long {
  return 1704067200000L // 2024-01-01 00:00:00 UTC - Placeholder for KMP
}

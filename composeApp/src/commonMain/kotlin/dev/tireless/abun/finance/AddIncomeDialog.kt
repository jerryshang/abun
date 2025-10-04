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
import com.composables.icons.lucide.PiggyBank

/**
 * Dialog for adding an income transaction
 *
 * User Flow:
 * 1. Enter amount
 * 2. Select destination account (Asset - where money goes)
 * 3. Select revenue account (type of income - Salary, Investment, etc.)
 * 4. Optional: payee, member, notes
 *
 * Accounting:
 * - Debit: Destination account (selected by user - Asset account)
 * - Credit: Revenue account (selected by user - Revenue account)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeDialog(
  onDismiss: () -> Unit,
  onConfirm: (CreateTransactionInput) -> Unit,
  accounts: List<AccountWithBalance>
) {
  var amount by remember { mutableStateOf("") }
  var selectedDestinationAccountId by remember { mutableStateOf<Long?>(null) }
  var selectedRevenueAccountId by remember { mutableStateOf<Long?>(null) }
  var payee by remember { mutableStateOf("") }
  var member by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  // Filter to show only Asset accounts (where income is deposited)
  val destinationAccounts = remember(accounts) {
    accounts.filter { account ->
      account.parentId == RootAccountIds.ASSET
    }
  }

  // Filter to show only Revenue accounts (type of income)
  val revenueAccounts = remember(accounts) {
    accounts.filter { account ->
      account.parentId == RootAccountIds.REVENUE
    }
  }

  // Set initial selections
  LaunchedEffect(destinationAccounts, revenueAccounts) {
    if (selectedDestinationAccountId == null && destinationAccounts.isNotEmpty()) {
      selectedDestinationAccountId = destinationAccounts.first().id
    }
    if (selectedRevenueAccountId == null && revenueAccounts.isNotEmpty()) {
      selectedRevenueAccountId = revenueAccounts.first().id
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
          Icon(Lucide.PiggyBank, "收入", modifier = Modifier.size(24.dp))
          Text("添加收入")
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
          supportingText = { Text("收入金额") }
        )

        // Destination Account Selector
        var expandedAccounts by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedAccounts,
          onExpandedChange = { expandedAccounts = it }
        ) {
          OutlinedTextField(
            value = destinationAccounts.find { it.id == selectedDestinationAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("收入账户") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccounts) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("钱存入哪个账户") }
          )
          ExposedDropdownMenu(
            expanded = expandedAccounts,
            onDismissRequest = { expandedAccounts = false }
          ) {
            destinationAccounts.forEach { account ->
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
                  selectedDestinationAccountId = account.id
                  expandedAccounts = false
                }
              )
            }
          }
        }

        // Revenue Account Selector (type of income)
        var expandedRevenue by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedRevenue,
          onExpandedChange = { expandedRevenue = it }
        ) {
          OutlinedTextField(
            value = revenueAccounts.find { it.id == selectedRevenueAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("收入类别") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRevenue) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("收入来源（工资、投资等）") }
          )
          ExposedDropdownMenu(
            expanded = expandedRevenue,
            onDismissRequest = { expandedRevenue = false }
          ) {
            revenueAccounts.forEach { account ->
              DropdownMenuItem(
                text = { Text(account.name) },
                onClick = {
                  selectedRevenueAccountId = account.id
                  expandedRevenue = false
                }
              )
            }
          }
        }

        // Payee (Optional)
        OutlinedTextField(
          value = payee,
          onValueChange = { payee = it },
          label = { Text("来源（可选）") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          supportingText = { Text("例如：公司名称、客户名") }
        )

        // Member (Optional)
        OutlinedTextField(
          value = member,
          onValueChange = { member = it },
          label = { Text("成员（可选）") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          supportingText = { Text("这笔收入是谁的") }
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
            selectedRevenueAccountId != null &&
            selectedDestinationAccountId != null
          ) {
            onConfirm(
              CreateTransactionInput(
                amount = amountValue,
                type = TransactionType.INCOME,
                transactionDate = getCurrentTimeMillis(),
                accountId = selectedRevenueAccountId!!, // Revenue account (what type of income)
                toAccountId = selectedDestinationAccountId!!, // Asset account (where money goes)
                payee = payee.takeIf { it.isNotBlank() },
                member = member.takeIf { it.isNotBlank() },
                notes = notes.takeIf { it.isNotBlank() }
              )
            )
            onDismiss()
          }
        },
        enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
          selectedRevenueAccountId != null &&
          selectedDestinationAccountId != null
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

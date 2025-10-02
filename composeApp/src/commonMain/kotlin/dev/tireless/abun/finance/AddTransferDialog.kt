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

/**
 * Dialog for adding a transfer transaction
 *
 * User Flow:
 * 1. Enter amount
 * 2. Select source account (Asset - where money comes from)
 * 3. Select destination account (Asset - where money goes to)
 * 4. Optional: payee, notes
 *
 * Accounting:
 * - Debit: Destination account (money in)
 * - Credit: Source account (money out)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransferDialog(
  onDismiss: () -> Unit,
  onConfirm: (CreateTransactionInput) -> Unit,
  accounts: List<AccountWithBalance>
) {
  var amount by remember { mutableStateOf("") }
  var selectedSourceAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
  var selectedDestinationAccountId by remember { mutableStateOf<Long?>(null) }
  var payee by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  // Filter to show only Asset accounts (can only transfer between assets)
  val assetAccounts = remember(accounts) {
    accounts.filter { account ->
      account.parentId == RootAccountIds.ASSET
    }
  }

  // Available destination accounts (exclude the selected source)
  val destinationAccounts = remember(assetAccounts, selectedSourceAccountId) {
    assetAccounts.filter { it.id != selectedSourceAccountId }
  }

  // Update selected accounts if the current ones are not in filtered list
  LaunchedEffect(assetAccounts) {
    if (assetAccounts.isNotEmpty() && assetAccounts.none { it.id == selectedSourceAccountId }) {
      selectedSourceAccountId = assetAccounts.first().id
    }
  }

  // Reset destination if it matches source
  LaunchedEffect(selectedSourceAccountId) {
    if (selectedDestinationAccountId == selectedSourceAccountId) {
      selectedDestinationAccountId = destinationAccounts.firstOrNull()?.id
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("添加转账")
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
          supportingText = { Text("转账金额") }
        )

        // Source Account Selector
        var expandedSourceAccounts by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedSourceAccounts,
          onExpandedChange = { expandedSourceAccounts = it }
        ) {
          OutlinedTextField(
            value = assetAccounts.find { it.id == selectedSourceAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("转出账户") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSourceAccounts) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("从哪个账户转出") }
          )
          ExposedDropdownMenu(
            expanded = expandedSourceAccounts,
            onDismissRequest = { expandedSourceAccounts = false }
          ) {
            assetAccounts.forEach { account ->
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
                  selectedSourceAccountId = account.id
                  expandedSourceAccounts = false
                }
              )
            }
          }
        }

        // Destination Account Selector
        var expandedDestinationAccounts by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedDestinationAccounts,
          onExpandedChange = { expandedDestinationAccounts = it }
        ) {
          OutlinedTextField(
            value = destinationAccounts.find { it.id == selectedDestinationAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("转入账户") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDestinationAccounts) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("转入哪个账户") }
          )
          ExposedDropdownMenu(
            expanded = expandedDestinationAccounts,
            onDismissRequest = { expandedDestinationAccounts = false }
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
                  expandedDestinationAccounts = false
                }
              )
            }
          }
        }

        // Payee (Optional)
        OutlinedTextField(
          value = payee,
          onValueChange = { payee = it },
          label = { Text("用途（可选）") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          supportingText = { Text("例如：还款、备用金") }
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
            selectedDestinationAccountId != null &&
            selectedDestinationAccountId != selectedSourceAccountId
          ) {
            onConfirm(
              CreateTransactionInput(
                amount = amountValue,
                type = TransactionType.TRANSFER,
                transactionDate = getCurrentTimeMillis(),
                accountId = selectedSourceAccountId, // Source account (will be credited)
                toAccountId = selectedDestinationAccountId, // Destination account (will be debited)
                payee = payee.takeIf { it.isNotBlank() },
                notes = notes.takeIf { it.isNotBlank() }
              )
            )
            onDismiss()
          }
        },
        enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
          selectedDestinationAccountId != null &&
          selectedDestinationAccountId != selectedSourceAccountId
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

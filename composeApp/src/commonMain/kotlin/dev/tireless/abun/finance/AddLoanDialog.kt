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
 * Dialog for creating a new loan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanDialog(
  accounts: List<Account>,
  onDismiss: () -> Unit,
  onConfirm: (CreateLoanInput) -> Unit,
) {
  var amount by remember { mutableStateOf("") }
  var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
  var selectedLenderAccountId by remember { mutableStateOf<Long?>(null) }
  var selectedLoanType by remember { mutableStateOf(LoanType.EQUAL_INSTALLMENT) }
  var interestRate by remember { mutableStateOf("8") }
  var loanMonths by remember { mutableStateOf("24") }
  var paymentDay by remember { mutableStateOf("1") }
  var notes by remember { mutableStateOf("") }

  // Auto-fill payment day when lender account is selected
  LaunchedEffect(selectedLenderAccountId) {
    selectedLenderAccountId?.let { lenderId ->
      val lenderAccount = accounts.find { it.id == lenderId }
      lenderAccount?.paymentDate?.let { date ->
        paymentDay = date.toString()
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("创建借贷")
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
          label = { Text("借贷金额") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          prefix = { Text("¥") },
          supportingText = { Text("借入的总金额") }
        )

        // Account Selector
        var expandedAccounts by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedAccounts,
          onExpandedChange = { expandedAccounts = it }
        ) {
          OutlinedTextField(
            value = accounts.find { it.id == selectedAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("借入账户") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccounts) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("借入资金存入的账户") }
          )
          ExposedDropdownMenu(
            expanded = expandedAccounts,
            onDismissRequest = { expandedAccounts = false }
          ) {
            accounts.forEach { account ->
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

        // Payee (Lender)
        var expandedLenderAccounts by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
          expanded = expandedLenderAccounts,
          onExpandedChange = { expandedLenderAccounts = it }
        ) {
          OutlinedTextField(
            value = accounts.find { it.id == selectedLenderAccountId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("出借方账户") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLenderAccounts) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            supportingText = { Text("借钱给你的账户（可以是朋友、银行等）") }
          )
          ExposedDropdownMenu(
            expanded = expandedLenderAccounts,
            onDismissRequest = { expandedLenderAccounts = false }
          ) {
            accounts.filter { it.id != selectedAccountId }.forEach { account ->
              DropdownMenuItem(
                text = { Text(account.name) },
                onClick = {
                  selectedLenderAccountId = account.id
                  expandedLenderAccounts = false
                }
              )
            }
          }
        }

        // Loan Type Selector
        Text("还款方式", style = MaterialTheme.typography.labelLarge)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            LoanType.EQUAL_INSTALLMENT to "等额本息",
            LoanType.EQUAL_PRINCIPAL to "等额本金",
            LoanType.INTEREST_FIRST to "先息后本"
          ).forEach { (type, label) ->
            FilterChip(
              selected = selectedLoanType == type,
              onClick = { selectedLoanType = type },
              label = { Text(label) },
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Interest Rate
        OutlinedTextField(
          value = interestRate,
          onValueChange = { interestRate = it },
          label = { Text("年利率") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          suffix = { Text("%") },
          supportingText = { Text("例如：5 表示 5%") }
        )

        // Loan Months
        OutlinedTextField(
          value = loanMonths,
          onValueChange = { loanMonths = it },
          label = { Text("借贷期限") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          suffix = { Text("月") },
          supportingText = { Text("借贷的总月数") }
        )

        // Payment Day
        OutlinedTextField(
          value = paymentDay,
          onValueChange = {
            val day = it.toIntOrNull()
            if (day != null && day in 1..31) {
              paymentDay = it
            } else if (it.isEmpty()) {
              paymentDay = it
            }
          },
          label = { Text("还款日") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          supportingText = { Text("每月还款的日期 (1-31)") }
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
          val rateValue = (interestRate.toDoubleOrNull() ?: 0.0) / 100.0 // Convert to decimal
          val monthsValue = loanMonths.toIntOrNull() ?: 0
          val paymentDayValue = paymentDay.toIntOrNull() ?: 1

          if (amountValue > 0 && rateValue >= 0 && monthsValue > 0 && selectedLenderAccountId != null) {
            onConfirm(
              CreateLoanInput(
                amount = amountValue,
                accountId = selectedAccountId,
                lenderAccountId = selectedLenderAccountId!!,
                loanType = selectedLoanType,
                interestRate = rateValue,
                loanMonths = monthsValue,
                paymentDay = paymentDayValue,
                startDate = 1704067200000L, // Simplified for KMP
                notes = notes.ifBlank { null }
              )
            )
          }
        },
        enabled = amount.toDoubleOrNull() != null &&
          amount.toDoubleOrNull()!! > 0 &&
          selectedLenderAccountId != null &&
          interestRate.toDoubleOrNull() != null &&
          loanMonths.toIntOrNull() != null &&
          loanMonths.toIntOrNull()!! > 0
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

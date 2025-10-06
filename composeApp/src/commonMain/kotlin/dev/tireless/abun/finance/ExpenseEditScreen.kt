package dev.tireless.abun.finance

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Lucide
import kotlinx.datetime.Clock

/**
 * Full screen for adding an expense transaction
 *
 * User Flow:
 * 1. Enter amount
 * 2. Select payment account (Asset/Liability - where money comes from)
 * 3. Select expense account (what the money is spent on - Food, Transport, etc.)
 * 4. Optional: payee, member, notes, date
 *
 * Accounting:
 * - Debit: Expense account (selected by user - Expense account)
 * - Credit: Payment account (selected by user - Asset/Liability)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
  navController: NavHostController,
  accounts: List<AccountWithBalance>,
  onCreate: (CreateTransactionInput) -> Unit,
  onUpdate: (UpdateTransactionInput) -> Unit,
  existingTransaction: TransactionEditPayload? = null,
) {
  val isEditing = existingTransaction?.type == TransactionType.EXPENSE
  val initialAmountText = existingTransaction?.let { formatAmount(it.amount) } ?: "0.00"
  var amount by remember(existingTransaction) {
    mutableStateOf(
      TextFieldValue(
        text = initialAmountText,
        selection = TextRange(0, initialAmountText.length)
      )
    )
  }
  var selectedPaymentAccountId by remember(existingTransaction) { mutableStateOf(existingTransaction?.toAccountId) }
  var selectedExpenseAccountId by remember(existingTransaction) { mutableStateOf(existingTransaction?.accountId) }
  var payee by remember(existingTransaction) { mutableStateOf(existingTransaction?.payee ?: "") }
  var notes by remember(existingTransaction) { mutableStateOf(existingTransaction?.notes ?: "") }
  var selectedDateMillis by remember(existingTransaction) {
    mutableStateOf(existingTransaction?.transactionDate ?: Clock.System.now().toEpochMilliseconds())
  }
  var showDatePicker by remember { mutableStateOf(false) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Request focus and show keyboard on amount field when screen opens
  LaunchedEffect(isEditing) {
    if (!isEditing) {
      kotlinx.coroutines.delay(100) // Small delay to ensure UI is ready
      focusRequester.requestFocus()
    }
  }

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

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (isEditing) "Edit Expense" else "Add Expense") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        },
        actions = {
          val amountValue = amount.text.toDoubleOrNull()
          val canSave = amountValue?.let { it > 0 } == true &&
            selectedExpenseAccountId != null &&
            selectedPaymentAccountId != null

          TextButton(
            onClick = {
              if (!canSave) return@TextButton

              if (isEditing && existingTransaction != null) {
                onUpdate(
                  UpdateTransactionInput(
                    id = existingTransaction.id,
                    amount = amountValue!!,
                    type = TransactionType.EXPENSE,
                    transactionDate = selectedDateMillis,
                    accountId = selectedExpenseAccountId!!,
                    toAccountId = selectedPaymentAccountId!!,
                    payee = payee.takeIf { it.isNotBlank() },
                    member = existingTransaction.member,
                    notes = notes.takeIf { it.isNotBlank() }
                  )
                )
              } else {
                onCreate(
                  CreateTransactionInput(
                    amount = amountValue!!,
                    type = TransactionType.EXPENSE,
                    transactionDate = selectedDateMillis,
                    accountId = selectedExpenseAccountId!!,
                    toAccountId = selectedPaymentAccountId!!,
                    payee = payee.takeIf { it.isNotBlank() },
                    member = null,
                    notes = notes.takeIf { it.isNotBlank() }
                  )
                )
              }
              navController.navigateUp()
            },
            enabled = canSave
          ) {
            Text(if (isEditing) "Update" else "Save")
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Amount Input
      OutlinedTextField(
        value = amount,
        onValueChange = { amount = it },
        label = { Text("Amount") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester)
          .onFocusChanged { focusState ->
            if (focusState.isFocused) {
              if (amount.selection.length != amount.text.length) {
                amount = amount.copy(selection = TextRange(0, amount.text.length))
              }
              keyboardController?.show()
            }
          },
        prefix = { Text("¥") },
        singleLine = true
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
          label = { Text("Payment Account") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccounts) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor()
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
          label = { Text("Expense Category") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExpense) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor()
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

      // Date Picker
      val dateInteractionSource = remember { MutableInteractionSource() }

      LaunchedEffect(dateInteractionSource) {
        dateInteractionSource.interactions.collect { interaction ->
          if (interaction is PressInteraction.Release) {
            showDatePicker = true
          }
        }
      }

      OutlinedTextField(
        value = formatDate(selectedDateMillis),
        onValueChange = {},
        readOnly = true,
        label = { Text("Date") },
        trailingIcon = { Icon(Lucide.Calendar, "Select date") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        interactionSource = dateInteractionSource
      )

      // Payee (Optional)
      OutlinedTextField(
        value = payee,
        onValueChange = { payee = it },
        label = { Text("Merchant (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      // Notes (Optional)
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notes (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5
      )
    }
  }

  // Date Picker Dialog
  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = selectedDateMillis
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let {
              selectedDateMillis = it
            }
            showDatePicker = false
          }
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel")
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}

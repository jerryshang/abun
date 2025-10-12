package dev.tireless.abun.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlin.math.abs

private data class ExpenseEntryState(
  val transactionId: Long? = null,
  val categoryId: Long? = null,
  val amount: String = "",
  val notes: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
  navController: NavHostController,
  accounts: List<AccountWithBalance>,
  onCreate: (SplitExpenseDraft) -> Unit,
  onUpdate: (SplitExpenseDraft) -> Unit,
  existingDraft: SplitExpenseDraft? = null,
  transactionId: Long? = null,
) {
  val isEditing = transactionId != null
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  var amount by remember {
    mutableStateOf(
      TextFieldValue(
        text = "0.00",
        selection = TextRange(0, 4),
      ),
    )
  }
  var selectedPaymentAccountId by remember { mutableStateOf<Long?>(null) }
  var payee by remember { mutableStateOf("") }
  var selectedDateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
  var showDatePicker by remember { mutableStateOf(false) }
  var expenseEntries by remember { mutableStateOf(listOf(ExpenseEntryState())) }
  var initialized by remember { mutableStateOf(false) }
  var successMessage by remember { mutableStateOf<String?>(null) }

  val paymentAccounts = remember(accounts) { accounts.leafAccountsForTypes(AccountType.ASSET, AccountType.LIABILITY) }
  val expenseAccounts = remember(accounts) { accounts.leafAccountsForTypes(AccountType.EXPENSE) }
  val accountLookup = remember(accounts) { accounts.accountLookup() }

  LaunchedEffect(accounts, existingDraft, isEditing) {
    val canInitialize = accounts.isNotEmpty() && (!isEditing || existingDraft != null)
    if (!initialized && canInitialize) {
      if (existingDraft != null) {
        val formattedTotal = formatAmount(existingDraft.totalAmount)
        amount = TextFieldValue(formattedTotal, TextRange(0, formattedTotal.length))
        selectedPaymentAccountId = existingDraft.paymentAccountId
        payee = existingDraft.payee.orEmpty()
        selectedDateMillis = existingDraft.transactionDate
        expenseEntries =
          existingDraft.entries
            .map { entry ->
              ExpenseEntryState(
                transactionId = entry.transactionId,
                categoryId = entry.categoryId,
                amount = formatAmount(entry.amount),
                notes = entry.notes.orEmpty(),
              )
            }.ifEmpty {
              listOf(ExpenseEntryState(categoryId = expenseAccounts.firstOrNull()?.id))
            }
      } else {
        val initialFormatted = amount.text
        amount = TextFieldValue(initialFormatted, TextRange(0, initialFormatted.length))
        selectedPaymentAccountId = paymentAccounts.firstOrNull()?.id
        expenseEntries =
          listOf(
            ExpenseEntryState(categoryId = expenseAccounts.firstOrNull()?.id),
          )
      }

      if (selectedPaymentAccountId == null && paymentAccounts.isNotEmpty()) {
        selectedPaymentAccountId = paymentAccounts.first().id
      }

      initialized = true
    }
  }

  LaunchedEffect(isEditing) {
    if (!isEditing) {
      kotlinx.coroutines.delay(100)
      focusRequester.requestFocus()
    }
  }

  val parsedEntryAmounts = expenseEntries.map { parseAmountInput(it.amount) }
  val entriesComplete = parsedEntryAmounts.none { it == null }
  val nonNullEntryAmounts = parsedEntryAmounts.filterNotNull()
  val entriesTotal = nonNullEntryAmounts.sum()
  val entriesPositive = nonNullEntryAmounts.all { it > 0 }
  val categoriesSelected = expenseEntries.all { it.categoryId != null }
  val amountValue = parseAmountInput(amount.text)
  val totalsMatch =
    entriesComplete && amountValue != null && abs(entriesTotal - amountValue) < 0.0001
  val canSave =
    amountValue != null &&
      amountValue > 0 &&
      selectedPaymentAccountId != null &&
      categoriesSelected &&
      entriesComplete &&
      entriesPositive &&
      totalsMatch

  val memberSnapshot = existingDraft?.member
  val groupNoteSnapshot = existingDraft?.groupNote

  Scaffold(
    topBar = {
      Column {
        TopAppBar(
          windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
          title = { Text(if (isEditing) "Edit Expense" else "Add Expense") },
          navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
          },
          actions = {
            TextButton(
              enabled = canSave,
              onClick = {
                if (!canSave) return@TextButton

                val entryDrafts =
                  expenseEntries.map { state ->
                    SplitExpenseEntry(
                      transactionId = state.transactionId,
                      categoryId = state.categoryId!!,
                      amount = parseAmountInput(state.amount)!!,
                      notes = state.notes.takeIf { it.isNotBlank() },
                    )
                  }

                val draft =
                  SplitExpenseDraft(
                    groupId = existingDraft?.groupId,
                    transactionDate = selectedDateMillis,
                    totalAmount = amountValue!!,
                    paymentAccountId = selectedPaymentAccountId!!,
                    payee = payee.takeIf { it.isNotBlank() },
                    member = memberSnapshot,
                    entries = entryDrafts,
                    groupNote = groupNoteSnapshot,
                  )

                if (isEditing) {
                  onUpdate(draft)
                } else {
                  onCreate(draft)
                }
                keyboardController?.hide()
                successMessage =
                  if (isEditing) "Expense updated" else "Expense saved"
              },
            ) {
              Text(if (isEditing) "Update" else "Save")
            }
          },
        )

        val splitErrorMessage =
          if (entriesComplete && amountValue != null && !totalsMatch) {
            val formattedEntriesTotal = formatAmount(entriesTotal)
            val formattedAmount = formatAmount(amountValue)
            "Split total ¥$formattedEntriesTotal must equal ¥$formattedAmount"
          } else {
            null
          }

        val bannerMessage = successMessage ?: splitErrorMessage
        val isSuccessBanner = successMessage != null

        if (bannerMessage != null) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .background(
                  if (isSuccessBanner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                ).padding(horizontal = 12.dp, vertical = 6.dp),
          ) {
            Text(
              text = bannerMessage,
              color = if (isSuccessBanner) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
              style = MaterialTheme.typography.bodySmall,
            )
          }
        }
      }
    },
  ) { paddingValues ->
    LaunchedEffect(successMessage) {
      if (successMessage != null) {
        kotlinx.coroutines.delay(800)
        navController.navigateUp()
        successMessage = null
      }
    }

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 16.dp, vertical = 20.dp)
          .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
        trailingIcon = { Icon(Lucide.Calendar, contentDescription = "Select date") },
        interactionSource = dateInteractionSource,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )

      PaymentAccountSelector(
        paymentAccounts = paymentAccounts,
        accountLookup = accountLookup,
        selectedPaymentAccountId = selectedPaymentAccountId,
        onAccountSelected = { selectedPaymentAccountId = it },
        modifier = Modifier.fillMaxWidth(),
      )

      OutlinedTextField(
        value = amount,
        onValueChange = { newValue ->
          if (newValue.text.isEmpty() || isValidAmountInput(newValue.text)) {
            amount = newValue
          }
        },
        label = { Text("Total Amount") },
        prefix = { Text("¥") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier =
          Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
              if (state.isFocused) {
                amount = amount.copy(selection = TextRange(0, amount.text.length))
                keyboardController?.show()
              }
            },
      )

      OutlinedTextField(
        value = payee,
        onValueChange = { payee = it },
        label = { Text("Merchant") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )

      if (expenseEntries.size == 1) {
        val single = expenseEntries.first()
        val desired = if (amount.text.isBlank()) "" else amount.text
        if (single.amount != desired) {
          expenseEntries = listOf(single.copy(amount = desired))
        }
      }

      expenseEntries.forEachIndexed { index, entryState ->
        ExpenseEntryRow(
          index = index,
          entryState = entryState,
          expenseAccounts = expenseAccounts,
          accountLookup = accountLookup,
          onCategorySelected = { categoryId ->
            expenseEntries =
              expenseEntries.toMutableList().also {
                it[index] = it[index].copy(categoryId = categoryId)
              }
          },
          onAmountChanged = { updatedAmount ->
            if (updatedAmount.isEmpty() || isValidAmountInput(updatedAmount)) {
              expenseEntries =
                expenseEntries.toMutableList().also {
                  it[index] = it[index].copy(amount = updatedAmount)
                }
            }
          },
          onNotesChanged = { updatedNotes ->
            expenseEntries =
              expenseEntries.toMutableList().also {
                it[index] = it[index].copy(notes = updatedNotes)
              }
          },
          onRemove =
            if (expenseEntries.size > 1) {
              {
                expenseEntries =
                  expenseEntries.filterIndexed { entryIndex, _ -> entryIndex != index }
              }
            } else {
              null
            },
          onAddNew = {
            expenseEntries =
              expenseEntries + ExpenseEntryState(categoryId = expenseAccounts.firstOrNull()?.id)
          },
          isSingleEntry = expenseEntries.size == 1,
        )

        if (index != expenseEntries.lastIndex) {
          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }
  }

  if (showDatePicker) {
    val datePickerState =
      rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
              selectedDateMillis = millis
            }
            showDatePicker = false
          },
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel")
        }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentAccountSelector(
  paymentAccounts: List<AccountWithBalance>,
  accountLookup: Map<Long, AccountWithBalance>,
  selectedPaymentAccountId: Long?,
  onAccountSelected: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it },
    modifier = modifier,
  ) {
    OutlinedTextField(
      value =
        paymentAccounts.find { it.id == selectedPaymentAccountId }?.hierarchyPath(accountLookup)
          ?: "",
      onValueChange = {},
      readOnly = true,
      label = { Text("Payment Account") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier =
        Modifier
          .fillMaxWidth()
          .menuAnchor(MenuAnchorType.PrimaryEditable, true),
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      paymentAccounts.forEach { account ->
        DropdownMenuItem(
          text = {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(account.hierarchyPath(accountLookup))
              Text(
                text = "¥${formatAmount(account.currentBalance)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          onClick = {
            onAccountSelected(account.id)
            expanded = false
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseEntryRow(
  index: Int,
  entryState: ExpenseEntryState,
  expenseAccounts: List<AccountWithBalance>,
  accountLookup: Map<Long, AccountWithBalance>,
  onCategorySelected: (Long) -> Unit,
  onAmountChanged: (String) -> Unit,
  onNotesChanged: (String) -> Unit,
  onRemove: (() -> Unit)?,
  onAddNew: () -> Unit,
  isSingleEntry: Boolean,
) {
  var expanded by remember(entryState.transactionId, index) { mutableStateOf(false) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      OutlinedTextField(
        value = entryState.amount,
        onValueChange = onAmountChanged,
        label = { Text("Amount") },
        prefix = { Text("¥") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        readOnly = isSingleEntry,
        modifier = Modifier.weight(1f),
      )

      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.weight(1f),
      ) {
        OutlinedTextField(
          value =
            expenseAccounts.find { it.id == entryState.categoryId }?.hierarchyPath(accountLookup)
              ?: "",
          onValueChange = {},
          readOnly = true,
          label = { Text("Category") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier =
            Modifier
              .fillMaxWidth()
              .menuAnchor(MenuAnchorType.PrimaryEditable, true),
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
        ) {
          expenseAccounts.forEach { account ->
            DropdownMenuItem(
              text = { Text(account.hierarchyPath(accountLookup)) },
              onClick = {
                onCategorySelected(account.id)
                expanded = false
              },
            )
          }
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = entryState.notes,
        onValueChange = onNotesChanged,
        label = { Text("Notes") },
        modifier = Modifier.weight(1f),
        singleLine = true,
        maxLines = 1,
      )

      if (onRemove != null) {
        IconButton(onClick = onRemove) {
          Icon(Icons.Default.Delete, contentDescription = "Remove expense ${index + 1}")
        }
      }

      IconButton(onClick = onAddNew) {
        Icon(Icons.Default.Add, contentDescription = "Add expense item")
      }
    }
  }
}

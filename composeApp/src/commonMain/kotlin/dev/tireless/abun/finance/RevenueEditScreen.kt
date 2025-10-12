package dev.tireless.abun.finance

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Lucide
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueEditScreen(
  navController: NavHostController,
  accounts: List<AccountWithBalance>,
  onCreate: (CreateTransactionInput) -> Unit,
  onUpdate: (UpdateTransactionInput) -> Unit,
  existingTransaction: TransactionEditPayload? = null,
) {
  val isEditing = existingTransaction?.type == TransactionType.INCOME
  val initialAmountText = existingTransaction?.let { formatAmount(it.amount) } ?: "0.00"
  var selectedDateMillis by remember(existingTransaction) {
    mutableStateOf(existingTransaction?.transactionDate ?: Clock.System.now().toEpochMilliseconds())
  }
  var showDatePicker by remember { mutableStateOf(false) }
  var amount by remember(existingTransaction) {
    mutableStateOf(
      TextFieldValue(
        text = initialAmountText,
        selection = TextRange(0, initialAmountText.length),
      ),
    )
  }
  var selectedDestinationAccountId by remember(existingTransaction) {
    mutableStateOf(existingTransaction?.toAccountId)
  }
  var selectedRevenueAccountId by remember(existingTransaction) {
    mutableStateOf(existingTransaction?.accountId)
  }
  var payee by remember(existingTransaction) { mutableStateOf(existingTransaction?.payee ?: "") }
  var notes by remember(existingTransaction) { mutableStateOf(existingTransaction?.notes ?: "") }

  var isDestinationMenuExpanded by remember { mutableStateOf(false) }
  var isRevenueMenuExpanded by remember { mutableStateOf(false) }
  var destinationAnchorWidth by remember { mutableStateOf(0) }
  var revenueAnchorWidth by remember { mutableStateOf(0) }
  val scrollState = rememberScrollState()

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(isEditing) {
    if (!isEditing) {
      delay(100)
      focusRequester.requestFocus()
    }
  }

  val destinationAccounts =
    remember(accounts) { accounts.leafAccountsForTypes(AccountType.ASSET, AccountType.LIABILITY) }

  val revenueAccounts = remember(accounts) { accounts.leafAccountsForTypes(AccountType.REVENUE) }
  val accountLookup = remember(accounts) { accounts.accountLookup() }
  val destinationSelectableIds = remember(destinationAccounts) { destinationAccounts.map { it.id }.toSet() }
  val revenueSelectableIds = remember(revenueAccounts) { revenueAccounts.map { it.id }.toSet() }

  LaunchedEffect(destinationAccounts, revenueAccounts) {
    if (selectedDestinationAccountId == null && destinationAccounts.isNotEmpty()) {
      selectedDestinationAccountId =
        destinationAccounts.firstOrNull { it.resolveAccountType(accountLookup) == AccountType.ASSET }?.id
          ?: destinationAccounts.first().id
    }
    if (selectedRevenueAccountId == null && revenueAccounts.isNotEmpty()) {
      selectedRevenueAccountId = revenueAccounts.first().id
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        title = { Text(if (isEditing) "Edit Revenue" else "Add Revenue") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          val amountValue = amount.text.toDoubleOrNull()
          val canSave =
            amountValue?.let { it > 0 } == true &&
              selectedDestinationAccountId != null &&
              selectedRevenueAccountId != null

          TextButton(
            enabled = canSave,
            onClick = {
              if (!canSave) return@TextButton
              if (isEditing) {
                onUpdate(
                  UpdateTransactionInput(
                    id = existingTransaction.id,
                    amount = amountValue!!,
                    type = TransactionType.INCOME,
                    transactionDate = selectedDateMillis,
                    accountId = selectedRevenueAccountId!!,
                    toAccountId = selectedDestinationAccountId!!,
                    payee = payee.takeIf { it.isNotBlank() },
                    notes = notes.takeIf { it.isNotBlank() },
                  ),
                )
              } else {
                onCreate(
                  CreateTransactionInput(
                    amount = amountValue!!,
                    type = TransactionType.INCOME,
                    transactionDate = selectedDateMillis,
                    accountId = selectedRevenueAccountId!!,
                    toAccountId = selectedDestinationAccountId!!,
                    payee = payee.takeIf { it.isNotBlank() },
                    notes = notes.takeIf { it.isNotBlank() },
                  ),
                )
              }
              navController.navigateUp()
            },
          ) {
            Text(if (isEditing) "Update" else "Save")
          }
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
      )
    },
  ) { paddingValues ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues),
    ) {
      Column(
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
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

        ExposedDropdownMenuBox(
          expanded = isDestinationMenuExpanded,
          onExpandedChange = { isDestinationMenuExpanded = it },
          modifier = Modifier.fillMaxWidth(),
        ) {
          OutlinedTextField(
            value =
              accounts.find { it.id == selectedDestinationAccountId }?.hierarchyPath(accountLookup)
                ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Destination Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDestinationMenuExpanded) },
            modifier =
              Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .onGloballyPositioned { destinationAnchorWidth = it.size.width },
          )
          AccountHierarchySelector(
            accounts = accounts,
            filter = AccountFilter.NORMAL_ACCOUNTS,
            selectedAccountId = selectedDestinationAccountId,
            onAccountSelect = { id ->
              if (id != null && id in destinationSelectableIds) {
                selectedDestinationAccountId = id
              }
            },
            expanded = isDestinationMenuExpanded,
            onExpandedChange = { isDestinationMenuExpanded = it },
            showAllOption = false,
            menuWidthPx = destinationAnchorWidth,
            isAccountEnabled = { account, hasChildren -> !hasChildren && account.id in destinationSelectableIds },
          )
        }

        OutlinedTextField(
          value = amount,
          onValueChange = { amount = it },
          label = { Text("Amount") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier =
            Modifier
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
          singleLine = true,
        )

        OutlinedTextField(
          value = payee,
          onValueChange = { payee = it },
          label = { Text("Source (optional)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )

        ExposedDropdownMenuBox(
          expanded = isRevenueMenuExpanded,
          onExpandedChange = { isRevenueMenuExpanded = it },
          modifier = Modifier.fillMaxWidth(),
        ) {
          OutlinedTextField(
            value =
              accounts.find { it.id == selectedRevenueAccountId }?.hierarchyPath(accountLookup)
                ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRevenueMenuExpanded) },
            modifier =
              Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .onGloballyPositioned { revenueAnchorWidth = it.size.width },
          )
          AccountHierarchySelector(
            accounts = accounts,
            filter = AccountFilter.REVENUE,
            selectedAccountId = selectedRevenueAccountId,
            onAccountSelect = { id ->
              if (id != null && id in revenueSelectableIds) {
                selectedRevenueAccountId = id
              }
            },
            expanded = isRevenueMenuExpanded,
            onExpandedChange = { isRevenueMenuExpanded = it },
            showAllOption = false,
            menuWidthPx = revenueAnchorWidth,
            isAccountEnabled = { account, hasChildren -> !hasChildren && account.id in revenueSelectableIds },
            showRootLabels = false,
          )
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (optional)") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          maxLines = 4,
        )
      }
    }
  }

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
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

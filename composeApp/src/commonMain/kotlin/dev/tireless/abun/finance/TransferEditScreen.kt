package dev.tireless.abun.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferEditScreen(
  navController: NavHostController,
  accounts: List<AccountWithBalance>,
  onCreate: (CreateTransactionInput) -> Unit,
  onUpdate: (UpdateTransactionInput) -> Unit,
  existingTransaction: TransactionEditPayload? = null,
) {
  val isEditing = existingTransaction?.type == TransactionType.TRANSFER
  val initialAmountText = existingTransaction?.let { formatAmount(it.amount) } ?: "0.00"
  var amount by remember(existingTransaction) {
    mutableStateOf(
      TextFieldValue(
        text = initialAmountText,
        selection = TextRange(0, initialAmountText.length)
      )
    )
  }
  var selectedSourceAccountId by remember(existingTransaction) {
    mutableStateOf(existingTransaction?.accountId)
  }
  var selectedDestinationAccountId by remember(existingTransaction) {
    mutableStateOf(existingTransaction?.toAccountId)
  }
  var payee by remember(existingTransaction) { mutableStateOf(existingTransaction?.payee ?: "") }
  var notes by remember(existingTransaction) { mutableStateOf(existingTransaction?.notes ?: "") }

  var isSourceMenuExpanded by remember { mutableStateOf(false) }
  var isDestinationMenuExpanded by remember { mutableStateOf(false) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(isEditing) {
    if (!isEditing) {
      delay(100)
      focusRequester.requestFocus()
    }
  }

  val assetAccounts = remember(accounts) {
    accounts.filter { account ->
      account.parentId == RootAccountIds.ASSET
    }
  }

  val destinationAccounts = remember(assetAccounts, selectedSourceAccountId) {
    assetAccounts.filter { account -> account.id != selectedSourceAccountId }
  }

  LaunchedEffect(assetAccounts) {
    if (selectedSourceAccountId == null && assetAccounts.isNotEmpty()) {
      selectedSourceAccountId = assetAccounts.first().id
    }
  }

  LaunchedEffect(destinationAccounts) {
    if (selectedDestinationAccountId == null && destinationAccounts.isNotEmpty()) {
      selectedDestinationAccountId = destinationAccounts.first().id
    } else if (destinationAccounts.none { it.id == selectedDestinationAccountId }) {
      selectedDestinationAccountId = destinationAccounts.firstOrNull()?.id
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (isEditing) "Edit Transfer" else "Transfer Funds") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          val amountValue = amount.text.toDoubleOrNull()
          val canSave = amountValue?.let { it > 0 } == true &&
            selectedSourceAccountId != null &&
            selectedDestinationAccountId != null &&
            selectedDestinationAccountId != selectedSourceAccountId

          TextButton(
            enabled = canSave,
            onClick = {
              if (!canSave) return@TextButton

              if (isEditing && existingTransaction != null) {
                onUpdate(
                  UpdateTransactionInput(
                    id = existingTransaction.id,
                    amount = amountValue!!,
                    type = TransactionType.TRANSFER,
                    transactionDate = existingTransaction.transactionDate,
                    accountId = selectedSourceAccountId!!,
                    toAccountId = selectedDestinationAccountId!!,
                    payee = payee.takeIf { it.isNotBlank() },
                    member = existingTransaction.member,
                    notes = notes.takeIf { it.isNotBlank() }
                  )
                )
              } else {
                onCreate(
                  CreateTransactionInput(
                    amount = amountValue!!,
                    type = TransactionType.TRANSFER,
                    transactionDate = Clock.System.now().toEpochMilliseconds(),
                    accountId = selectedSourceAccountId!!,
                    toAccountId = selectedDestinationAccountId!!,
                    payee = payee.takeIf { it.isNotBlank() },
                    member = null,
                    notes = notes.takeIf { it.isNotBlank() }
                  )
                )
              }
              navController.navigateUp()
            }
          ) {
            Text(if (isEditing) "Update" else "Save")
          }
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
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

      ExposedDropdownMenuBox(
        expanded = isSourceMenuExpanded,
        onExpandedChange = { isSourceMenuExpanded = it }
      ) {
        OutlinedTextField(
          value = assetAccounts.find { it.id == selectedSourceAccountId }?.name ?: "",
          onValueChange = {},
          readOnly = true,
          label = { Text("Source Account") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSourceMenuExpanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor()
        )
        DropdownMenu(
          expanded = isSourceMenuExpanded,
          onDismissRequest = { isSourceMenuExpanded = false }
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
                isSourceMenuExpanded = false
              }
            )
          }
        }
      }

      ExposedDropdownMenuBox(
        expanded = isDestinationMenuExpanded,
        onExpandedChange = { isDestinationMenuExpanded = it }
      ) {
        OutlinedTextField(
          value = destinationAccounts.find { it.id == selectedDestinationAccountId }?.name ?: "",
          onValueChange = {},
          readOnly = true,
          label = { Text("Destination Account") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDestinationMenuExpanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor()
        )
        DropdownMenu(
          expanded = isDestinationMenuExpanded,
          onDismissRequest = { isDestinationMenuExpanded = false }
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
                isDestinationMenuExpanded = false
              }
            )
          }
        }
      }

      OutlinedTextField(
        value = payee,
        onValueChange = { payee = it },
        label = { Text("Purpose (optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notes (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 4
      )
    }
  }
}

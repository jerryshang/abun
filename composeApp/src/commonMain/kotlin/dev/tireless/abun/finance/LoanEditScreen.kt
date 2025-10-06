package dev.tireless.abun.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.FilterChip
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
fun LoanEditScreen(
  navController: NavHostController,
  onConfirm: (CreateLoanInput) -> Unit,
  accounts: List<AccountWithBalance>,
) {
  var amount by remember { mutableStateOf(TextFieldValue("0.00", selection = TextRange(0, 4))) }
  var selectedAccountId by remember { mutableStateOf<Long?>(null) }
  var selectedLoanType by remember { mutableStateOf(LoanType.EQUAL_INSTALLMENT) }
  var interestRate by remember { mutableStateOf("8") }
  var loanMonths by remember { mutableStateOf("24") }
  var paymentDay by remember { mutableStateOf("1") }
  var payee by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  var isAccountMenuExpanded by remember { mutableStateOf(false) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    delay(100)
    focusRequester.requestFocus()
  }

  val assetAccounts =
    remember(accounts) {
      accounts.filter { account ->
        account.parentId == RootAccountIds.ASSET
      }
    }

  LaunchedEffect(assetAccounts) {
    if (selectedAccountId == null && assetAccounts.isNotEmpty()) {
      selectedAccountId = assetAccounts.first().id
    }
  }

  val canSave =
    remember(amount, interestRate, loanMonths, payee, selectedAccountId) {
      val amountValue = amount.text.toDoubleOrNull()
      val rateValue = interestRate.toDoubleOrNull()
      val monthsValue = loanMonths.toIntOrNull()
      amountValue != null &&
        amountValue > 0 &&
        rateValue != null &&
        rateValue >= 0 &&
        monthsValue != null &&
        monthsValue > 0 &&
        payee.isNotBlank() &&
        selectedAccountId != null
    }

  Scaffold(
    topBar = {
      TopAppBar(
        windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        title = { Text("Create Loan") },
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
              val amountValue = amount.text.toDoubleOrNull() ?: return@TextButton
              val rateValue = interestRate.toDoubleOrNull() ?: return@TextButton
              val monthsValue = loanMonths.toIntOrNull() ?: return@TextButton
              val paymentDayValue = paymentDay.toIntOrNull() ?: 1

              onConfirm(
                CreateLoanInput(
                  amount = amountValue,
                  accountId = selectedAccountId!!,
                  lenderAccountId = selectedAccountId!!,
                  loanType = selectedLoanType,
                  interestRate = rateValue,
                  loanMonths = monthsValue,
                  paymentDay = paymentDayValue,
                  startDate = Clock.System.now().toEpochMilliseconds(),
                  payee = payee.ifBlank { null },
                  notes = notes.ifBlank { null },
                ),
              )
              navController.navigateUp()
            },
          ) {
            Text("Save")
          }
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
      )
    },
  ) { paddingValues ->
    Column(
      modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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

      ExposedDropdownMenuBox(
        expanded = isAccountMenuExpanded,
        onExpandedChange = { isAccountMenuExpanded = it },
      ) {
        OutlinedTextField(
          value = assetAccounts.find { it.id == selectedAccountId }?.name ?: "",
          onValueChange = {},
          readOnly = true,
          label = { Text("Deposit Account") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAccountMenuExpanded) },
          modifier =
          Modifier
            .fillMaxWidth()
            .menuAnchor(),
        )
        DropdownMenu(
          expanded = isAccountMenuExpanded,
          onDismissRequest = { isAccountMenuExpanded = false },
        ) {
          assetAccounts.forEach { account ->
            DropdownMenuItem(
              text = { Text(account.name) },
              onClick = {
                selectedAccountId = account.id
                isAccountMenuExpanded = false
              },
            )
          }
        }
      }

      Text("Repayment Type", style = MaterialTheme.typography.labelLarge)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf(
          LoanType.EQUAL_INSTALLMENT to "Equal Principal & Interest",
          LoanType.EQUAL_PRINCIPAL to "Equal Principal",
          LoanType.INTEREST_FIRST to "Interest First",
        ).forEach { (type, label) ->
          FilterChip(
            selected = selectedLoanType == type,
            onClick = { selectedLoanType = type },
            label = { Text(label) },
            modifier = Modifier.weight(1f),
          )
        }
      }

      OutlinedTextField(
        value = interestRate,
        onValueChange = { interestRate = it },
        label = { Text("Annual Interest Rate") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        suffix = { Text("%") },
      )

      OutlinedTextField(
        value = loanMonths,
        onValueChange = { loanMonths = it },
        label = { Text("Term Length") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        suffix = { Text("months") },
      )

      OutlinedTextField(
        value = paymentDay,
        onValueChange = {
          val day = it.toIntOrNull()
          if (day == null || day in 1..31) {
            paymentDay = it
          }
        },
        label = { Text("Payment Day") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
      )

      OutlinedTextField(
        value = payee,
        onValueChange = { payee = it },
        label = { Text("Lender Name *") },
        modifier = Modifier.fillMaxWidth(),
      )

      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notes (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
      )
    }
  }
}

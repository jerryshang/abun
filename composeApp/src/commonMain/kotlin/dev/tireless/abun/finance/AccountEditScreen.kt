package dev.tireless.abun.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditScreen(
  navController: NavHostController,
  accountId: Long?,
  viewModel: AccountViewModel = koinInject(),
) {
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  val existingAccount = accountId?.let { id -> accounts.firstOrNull { it.id == id } }
  val isNewAccount = accountId == null

  LaunchedEffect(accountId, accounts.isEmpty()) {
    if (!isNewAccount && existingAccount == null) {
      viewModel.loadAccounts()
    }
  }

  if (!isNewAccount && existingAccount == null) {
    Scaffold(
      topBar = {
        TopAppBar(
          windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
          title = { Text("编辑账户") },
          navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
          },
        )
      },
    ) { paddingValues ->
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        if (isLoading) {
          CircularProgressIndicator()
        } else {
          Text("未找到账户", style = MaterialTheme.typography.bodyLarge)
        }
      }
    }
    return
  }

  var name by remember(accountId, existingAccount) { mutableStateOf(existingAccount?.name ?: "") }
  var currency by remember(accountId, existingAccount) { mutableStateOf(existingAccount?.currency ?: "CNY") }
  var colorHex by remember(accountId, existingAccount) { mutableStateOf(existingAccount?.colorHex.orEmpty()) }
  var isActive by remember(accountId, existingAccount) { mutableStateOf(existingAccount?.isActive ?: true) }
  var parentId by remember(accountId, existingAccount) { mutableStateOf(existingAccount?.parentId) }
  var isVisible by remember(accountId, existingAccount) { mutableStateOf(existingAccount?.isVisibleInUi ?: true) }
  var isCountable by remember(accountId, existingAccount) { mutableStateOf(existingAccount?.isCountable ?: true) }

  fun handleSave() {
    saveAccount(
      viewModel = viewModel,
      account = existingAccount,
      name = name,
      currency = currency,
      colorHex = colorHex,
      parentId = parentId,
      isActive = isActive,
      isVisible = isVisible,
      isCountable = isCountable,
    )
    navController.navigateUp()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        title = { Text(if (isNewAccount) "创建账户" else "编辑账户") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
          }
        },
        actions = {
          TextButton(
            onClick = { handleSave() },
            enabled = name.isNotBlank(),
          ) {
            Text(if (isNewAccount) "创建" else "保存")
          }
        },
      )
    },
  ) { paddingValues ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 24.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("名称") },
      )

      OutlinedTextField(
        value = currency,
        onValueChange = { currency = it.uppercase() },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("币种") },
        singleLine = true,
      )

      OutlinedTextField(
        value = colorHex,
        onValueChange = { value ->
          colorHex = value.take(7)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("颜色HEX (可选)") },
        placeholder = { Text("#4CAF50") },
        singleLine = true,
      )

      ParentAccountSelector(
        accounts = accounts,
        selectedParentId = parentId,
        currentAccountId = accountId,
        onParentSelected = { parentId = it },
      )

      SwitchRow(
        title = "启用账户",
        checked = isActive,
        onCheckedChange = { isActive = it },
      )

      SwitchRow(
        title = "纳入统计",
        checked = isCountable,
        onCheckedChange = { isCountable = it },
      )

      SwitchRow(
        title = "在界面中显示",
        checked = isVisible,
        onCheckedChange = { isVisible = it },
      )

      Spacer(modifier = Modifier.weight(1f))

      Button(
        onClick = { handleSave() },
        enabled = name.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(if (isNewAccount) "创建账户" else "保存修改")
      }
    }
  }
}

@Composable
private fun SwitchRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(title, style = MaterialTheme.typography.bodyLarge)
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(),
    )
  }
}

private fun saveAccount(
  viewModel: AccountViewModel,
  account: AccountWithBalance?,
  name: String,
  currency: String,
  colorHex: String,
  parentId: Long?,
  isActive: Boolean,
  isVisible: Boolean,
  isCountable: Boolean,
) {
  if (account == null) {
    viewModel.createAccount(
      CreateAccountInput(
        name = name,
        currency = currency.ifBlank { "CNY" },
        parentId = parentId,
        isActive = isActive,
        isCountable = isCountable,
        isVisibleInUi = isVisible,
        colorHex = colorHex.takeIf { it.isNotBlank() },
      )
    )
  } else {
    viewModel.updateAccount(
      UpdateAccountInput(
        id = account.id,
        name = name,
        parentId = parentId,
        currency = currency.ifBlank { account.currency },
        isActive = isActive,
        isCountable = isCountable,
        isVisibleInUi = isVisible,
        iconName = account.iconName,
        colorHex = colorHex.takeIf { it.isNotBlank() },
        billDate = account.billDate,
        paymentDate = account.paymentDate,
        creditLimit = account.creditLimit,
      )
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentAccountSelector(
  accounts: List<AccountWithBalance>,
  selectedParentId: Long?,
  currentAccountId: Long?,
  onParentSelected: (Long?) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val options = remember(accounts, currentAccountId) {
    accounts.filter { it.id != currentAccountId }
  }
  val selectedAccount = options.firstOrNull { it.id == selectedParentId }
  val fieldText = selectedAccount?.name ?: "无"

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it },
  ) {
    OutlinedTextField(
      value = fieldText,
      onValueChange = {},
      readOnly = true,
      modifier =
        Modifier
          .fillMaxWidth()
          .menuAnchor(),
      label = { Text("父账户 (可选)") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      DropdownMenuItem(
        text = { Text("无") },
        onClick = {
          onParentSelected(null)
          expanded = false
        },
      )

      options.forEach { account ->
        DropdownMenuItem(
          text = { Text(account.name) },
          onClick = {
            onParentSelected(account.id)
            expanded = false
          },
        )
      }
    }
  }
}

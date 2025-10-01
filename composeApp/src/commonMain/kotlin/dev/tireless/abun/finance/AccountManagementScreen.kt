package dev.tireless.abun.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * Account Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
  viewModel: AccountViewModel = koinInject(),
  onNavigateBack: () -> Unit
) {
  val accounts by viewModel.accounts.collectAsState()
  val totalBalance by viewModel.totalBalance.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var showAddAccountDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("账户管理") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, "返回")
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddAccountDialog = true }
      ) {
        Icon(Icons.Default.Add, "添加账户")
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center)
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
              )
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Text(
                  text = "总资产",
                  style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "¥${formatAmount(totalBalance)}",
                  style = MaterialTheme.typography.headlineLarge,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          items(accounts) { account ->
            AccountCard(
              account = account,
              onEdit = { /* TODO */ },
              onDelete = { viewModel.deleteAccount(account.id) }
            )
          }
        }
      }
    }
  }

  if (showAddAccountDialog) {
    AddAccountDialog(
      onDismiss = { showAddAccountDialog = false },
      onConfirm = { input ->
        viewModel.createAccount(input)
        showAddAccountDialog = false
      }
    )
  }
}

/**
 * Account Card
 */
@Composable
fun AccountCard(
  account: Account,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = account.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = account.type.name.lowercase().replace('_', ' '),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "¥${formatAmount(account.currentBalance)}",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        if (!account.isActive) {
          Text(
            text = "已停用",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }
    }
  }
}

/**
 * Add Account Dialog
 */
@Composable
fun AddAccountDialog(
  onDismiss: () -> Unit,
  onConfirm: (CreateAccountInput) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(AccountType.CASH) }
  var initialBalance by remember { mutableStateOf("0") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("添加账户") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("账户名称") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("账户类型", style = MaterialTheme.typography.labelLarge)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          AccountType.values().forEach { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = { selectedType = type },
              label = {
                Text(
                  type.name.lowercase().replace('_', ' ')
                )
              }
            )
          }
        }

        OutlinedTextField(
          value = initialBalance,
          onValueChange = { initialBalance = it },
          label = { Text("初始余额") },
          modifier = Modifier.fillMaxWidth(),
          prefix = { Text("¥") }
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            CreateAccountInput(
              name = name,
              type = selectedType,
              initialBalance = initialBalance.toDoubleOrNull() ?: 0.0
            )
          )
        },
        enabled = name.isNotBlank()
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

package dev.tireless.abun.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import dev.tireless.abun.navigation.Route
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject

/**
 * Account Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun AccountManagementScreen(
  navController: NavHostController,
  viewModel: AccountViewModel = koinInject(),
) {
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        title = { Text("账户管理") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.ArrowBack, "返回")
          }
        },
        actions = {
          IconButton(onClick = { navController.navigate(Route.AccountEdit(null)) }) {
            Icon(Lucide.Plus, contentDescription = "添加账户")
          }
        },
      )
    },
  ) { paddingValues ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues),
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center),
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(accounts) { account ->
            AccountCard(
              account = account,
              onClick = { navController.navigate(Route.AccountEdit(account.id)) },
            )
          }
        }
      }
    }
  }

}

/**
 * Account Card
 */
@Composable
fun AccountCard(
  account: AccountWithBalance,
  onClick: () -> Unit,
) {
  val accentColor = hexToColorOrNull(account.colorHex) ?: MaterialTheme.colorScheme.secondary
  val containerColor = accentColor.copy(alpha = 0.08f)

  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = containerColor),
//    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = account.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = account.currency,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "¥${formatAmount(account.currentBalance)}",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = if (account.currentBalance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        if (!account.isActive) {
          Text(
            text = "已停用",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
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
  onConfirm: (CreateAccountInput) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var initialBalance by remember { mutableStateOf("0") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("添加账户") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("账户名称") },
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = initialBalance,
          onValueChange = { initialBalance = it },
          label = { Text("初始余额") },
          modifier = Modifier.fillMaxWidth(),
          prefix = { Text("¥") },
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            CreateAccountInput(
              name = name,
            ),
          )
        },
        enabled = name.isNotBlank(),
      ) {
        Text("确认")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("取消")
      }
    },
  )
}

/**
 * Load Template Dialog
 */
@Composable
fun LoadTemplateDialog(
  onDismiss: () -> Unit,
  onConfirm: (AccountTemplateType) -> Unit,
) {
  var selectedTemplate by remember { mutableStateOf(AccountTemplateType.MINIMAL) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("选择预定义账户模板") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "选择要加载的账户模板。加载模板将清除所有现有交易和账户。",
          style = MaterialTheme.typography.bodyMedium,
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
        ) {
          RadioButton(
            selected = selectedTemplate == AccountTemplateType.MINIMAL,
            onClick = { selectedTemplate = AccountTemplateType.MINIMAL },
          )
          Text(
            text = "最小模板 (5个账户)",
            modifier = Modifier.padding(start = 8.dp),
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
        ) {
          RadioButton(
            selected = selectedTemplate == AccountTemplateType.STANDARD,
            onClick = { selectedTemplate = AccountTemplateType.STANDARD },
          )
          Text(
            text = "标准模板 (40+个账户)",
            modifier = Modifier.padding(start = 8.dp),
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = { onConfirm(selectedTemplate) }) {
        Text("下一步")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("取消")
      }
    },
  )
}

/**
 * Warning Dialog for data clearing
 */
@Composable
fun WarningDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("警告") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "加载预定义账户将会:",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "• 删除所有现有交易",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.error,
        )
        Text(
          text = "• 删除除5个根账户外的所有账户",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.error,
        )
        Text(
          text = "• 此操作不可恢复!",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "确定要继续吗?",
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
          ),
      ) {
        Text("确认")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("取消")
      }
    },
  )
}

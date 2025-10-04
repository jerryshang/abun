package dev.tireless.abun.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
  val totalBalance by viewModel.totalBalance.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var showAddAccountDialog by remember { mutableStateOf(false) }
  var showLoadTemplateDialog by remember { mutableStateOf(false) }
  var showWarningDialog by remember { mutableStateOf(false) }
  var selectedTemplate by remember { mutableStateOf<AccountTemplateType?>(null) }
  var showMenu by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("账户管理") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.ArrowBack, "返回")
          }
        },
        actions = {
          IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, "更多选项")
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("加载预定义账户") },
              onClick = {
                showMenu = false
                showLoadTemplateDialog = true
              }
            )
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

  if (showLoadTemplateDialog) {
    LoadTemplateDialog(
      onDismiss = { showLoadTemplateDialog = false },
      onConfirm = { templateType ->
        selectedTemplate = templateType
        showLoadTemplateDialog = false
        showWarningDialog = true
      }
    )
  }

  if (showWarningDialog) {
    WarningDialog(
      onDismiss = {
        showWarningDialog = false
        selectedTemplate = null
      },
      onConfirm = {
        selectedTemplate?.let { template ->
          // Load the CSV content based on template type
          val csvContent = when (template) {
            AccountTemplateType.MINIMAL -> getMinimalTemplateContent()
            AccountTemplateType.STANDARD -> getStandardTemplateContent()
          }
          viewModel.loadTemplate(csvContent)
        }
        showWarningDialog = false
        selectedTemplate = null
      }
    )
  }
}

/**
 * Get minimal template CSV content
 */
fun getMinimalTemplateContent(): String = """
name,parent_name,currency,is_active,is_countable,is_visible,icon_name,color_hex
Cash,Asset,CNY,true,true,true,cash,#4CAF50
Bank,Asset,CNY,true,true,true,bank,#2196F3
Salary,Revenue,CNY,true,true,true,salary,#FFC107
Food,Expense,CNY,true,true,true,restaurant,#FF5722
Transport,Expense,CNY,true,true,true,directions_car,#9C27B0
""".trimIndent()

/**
 * Get standard template CSV content
 */
fun getStandardTemplateContent(): String = """
name,parent_name,currency,is_active,is_countable,is_visible,icon_name,color_hex
Cash,Asset,CNY,true,true,true,cash,#4CAF50
Bank Card,Asset,CNY,true,true,true,credit_card,#2196F3
Alipay,Asset,CNY,true,true,true,account_balance_wallet,#00A0E9
WeChat Pay,Asset,CNY,true,true,true,account_balance,#09BB07
Investment,Asset,CNY,true,true,true,trending_up,#FF9800
Credit Card,Liability,CNY,true,true,true,credit_card,#F44336
Salary,Revenue,CNY,true,true,true,work,#FFC107
Bonus,Revenue,CNY,true,true,true,card_giftcard,#FFEB3B
Investment Income,Revenue,CNY,true,true,true,attach_money,#4CAF50
Food & Dining,Expense,CNY,true,true,true,restaurant,#FF5722
Breakfast,Food & Dining,CNY,true,true,true,free_breakfast,#FF8A80
Lunch,Food & Dining,CNY,true,true,true,lunch_dining,#FF5252
Dinner,Food & Dining,CNY,true,true,true,dinner_dining,#D32F2F
Snacks & Drinks,Food & Dining,CNY,true,true,true,local_cafe,#FFAB91
Transport,Expense,CNY,true,true,true,directions_car,#9C27B0
Public Transit,Transport,CNY,true,true,true,directions_bus,#CE93D8
Taxi & Ride,Transport,CNY,true,true,true,local_taxi,#BA68C8
Gas,Transport,CNY,true,true,true,local_gas_station,#AB47BC
Shopping,Expense,CNY,true,true,true,shopping_cart,#E91E63
Clothing,Shopping,CNY,true,true,true,checkroom,#F48FB1
Electronics,Shopping,CNY,true,true,true,devices,#EC407A
Daily Necessities,Shopping,CNY,true,true,true,shopping_basket,#C2185B
Entertainment,Expense,CNY,true,true,true,movie,#3F51B5
Movies,Entertainment,CNY,true,true,true,theaters,#9FA8DA
Games,Entertainment,CNY,true,true,true,sports_esports,#7986CB
Books,Entertainment,CNY,true,true,true,menu_book,#5C6BC0
Housing,Expense,CNY,true,true,true,home,#00BCD4
Rent,Housing,CNY,true,true,true,apartment,#80DEEA
Utilities,Housing,CNY,true,true,true,water_drop,#4DD0E1
Internet,Housing,CNY,true,true,true,wifi,#26C6DA
Healthcare,Expense,CNY,true,true,true,medical_services,#009688
Medical,Healthcare,CNY,true,true,true,local_hospital,#80CBC4
Medicine,Healthcare,CNY,true,true,true,medication,#4DB6AC
Fitness,Healthcare,CNY,true,true,true,fitness_center,#26A69A
Education,Expense,CNY,true,true,true,school,#FF9800
Tuition,Education,CNY,true,true,true,account_balance,#FFCC80
Books & Supplies,Education,CNY,true,true,true,auto_stories,#FFB74D
Courses,Education,CNY,true,true,true,class,#FFA726
Others,Expense,CNY,true,true,true,more_horiz,#9E9E9E
Gifts,Others,CNY,true,true,true,card_giftcard,#BDBDBD
Charity,Others,CNY,true,true,true,volunteer_activism,#757575
""".trimIndent()

/**
 * Account Card
 */
@Composable
fun AccountCard(
  account: AccountWithBalance,
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
              name = name
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

/**
 * Load Template Dialog
 */
@Composable
fun LoadTemplateDialog(
  onDismiss: () -> Unit,
  onConfirm: (AccountTemplateType) -> Unit
) {
  var selectedTemplate by remember { mutableStateOf(AccountTemplateType.MINIMAL) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("选择预定义账户模板") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "选择要加载的账户模板。加载模板将清除所有现有交易和账户。",
          style = MaterialTheme.typography.bodyMedium
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          RadioButton(
            selected = selectedTemplate == AccountTemplateType.MINIMAL,
            onClick = { selectedTemplate = AccountTemplateType.MINIMAL }
          )
          Text(
            text = "最小模板 (5个账户)",
            modifier = Modifier.padding(start = 8.dp)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          RadioButton(
            selected = selectedTemplate == AccountTemplateType.STANDARD,
            onClick = { selectedTemplate = AccountTemplateType.STANDARD }
          )
          Text(
            text = "标准模板 (40+个账户)",
            modifier = Modifier.padding(start = 8.dp)
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
    }
  )
}

/**
 * Warning Dialog for data clearing
 */
@Composable
fun WarningDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("警告") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "加载预定义账户将会:",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "• 删除所有现有交易",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.error
        )
        Text(
          text = "• 删除除5个根账户外的所有账户",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.error
        )
        Text(
          text = "• 此操作不可恢复!",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "确定要继续吗?",
          style = MaterialTheme.typography.bodyMedium
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error
        )
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

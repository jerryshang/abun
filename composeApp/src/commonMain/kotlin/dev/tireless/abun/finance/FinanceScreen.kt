package dev.tireless.abun.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * Main Finance Screen with transaction list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
  viewModel: TransactionViewModel = koinInject(),
  onNavigateToAccounts: () -> Unit = {},
  onNavigateToCategories: () -> Unit = {},
  onNavigateToPriceComparison: () -> Unit = {},
) {
  val transactions by viewModel.transactions.collectAsState()
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val error by viewModel.error.collectAsState()

  var showAddTransactionDialog by remember { mutableStateOf(false) }
  var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
  var isFabExpanded by remember { mutableStateOf(false) }
  var showAddLoanDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("财务管理") },
        actions = {
          IconButton(onClick = onNavigateToPriceComparison) {
            Icon(Icons.Default.ShoppingCart, "价格对比")
          }
          IconButton(onClick = onNavigateToAccounts) {
            Icon(Icons.Default.AccountBalanceWallet, "账户管理")
          }
          IconButton(onClick = onNavigateToCategories) {
            Icon(Icons.Default.Category, "分类管理")
          }
        },
      )
    },
    floatingActionButton = {
      FanOutFAB(
        isExpanded = isFabExpanded,
        onExpandChange = { isFabExpanded = it },
        onAddTransaction = {
          selectedTransaction = null
          showAddTransactionDialog = true
          isFabExpanded = false
        },
        onCreateLoan = {
          showAddLoanDialog = true
          isFabExpanded = false
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
        Column(modifier = Modifier.fillMaxSize()) {
          // Summary Card
          AccountsSummaryCard(accounts)

          // Transaction List
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(transactions) { transaction ->
              TransactionCard(
                transaction = transaction,
                accounts = accounts,
                onClick = {
                  selectedTransaction = transaction
                  showAddTransactionDialog = true
                },
                onDelete = {
                  viewModel.deleteTransaction(transaction.id)
                },
              )
            }
          }
        }
      }

      error?.let { errorMessage ->
        Snackbar(
          modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
          action = {
            TextButton(onClick = { viewModel.clearError() }) {
              Text("关闭")
            }
          },
        ) {
          Text(errorMessage)
        }
      }
    }
  }

  if (showAddTransactionDialog) {
    AddTransactionDialog(
      transaction = selectedTransaction,
      accounts = accounts,
      onDismiss = {
        showAddTransactionDialog = false
        selectedTransaction = null
      },
      onConfirm = { input ->
        if (selectedTransaction == null) {
          viewModel.createTransaction(input)
        } else {
          viewModel.updateTransaction(
            UpdateTransactionInput(
              id = selectedTransaction!!.id,
              amount = input.amount,
              type = input.type,
              transactionDate = input.transactionDate,
              categoryId = input.categoryId,
              accountId = input.accountId,
              toAccountId = input.toAccountId,
              payee = input.payee,
              member = input.member,
              notes = input.notes,
              tagIds = input.tagIds,
            ),
          )
        }
        showAddTransactionDialog = false
        selectedTransaction = null
      },
    )
  }

  if (showAddLoanDialog) {
    AddLoanDialog(
      accounts = accounts,
      onDismiss = {
        showAddLoanDialog = false
      },
      onConfirm = { input ->
        viewModel.createLoan(input)
        showAddLoanDialog = false
      },
    )
  }
}

/**
 * Accounts summary card showing total balance
 */
@Composable
fun AccountsSummaryCard(accounts: List<Account>) {
  val totalBalance = accounts.filter { it.isActive }.sumOf { it.currentBalance }

  Card(
    modifier =
    Modifier
      .fillMaxWidth()
      .padding(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier =
      Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      Text(
        text = "总资产",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "¥${formatAmount(totalBalance)}",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        accounts.take(3).forEach { account ->
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = account.name,
              style = MaterialTheme.typography.bodySmall,
            )
            Text(
              text = "¥${formatAmount(account.currentBalance)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
            )
          }
        }
      }
    }
  }
}

/**
 * Transaction card item
 */
@Composable
fun TransactionCard(
  transaction: Transaction,
  accounts: List<Account>,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val account = accounts.find { it.id == transaction.accountId }
  val backgroundColor =
    when (transaction.type) {
      TransactionType.EXPENSE -> Color(0xFFFFF3E0)
      TransactionType.INCOME -> Color(0xFFE8F5E9)
      TransactionType.TRANSFER -> Color(0xFFE3F2FD)
      TransactionType.LOAN -> Color(0xFFFFE0B2)
      TransactionType.LOAN_PAYMENT -> Color(0xFFF3E5F5)
    }

  Card(
    modifier =
    Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Row(
      modifier =
      Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector =
            when (transaction.type) {
              TransactionType.EXPENSE -> Icons.Default.Remove
              TransactionType.INCOME -> Icons.Default.Add
              TransactionType.TRANSFER -> Icons.Default.SwapHoriz
              TransactionType.LOAN -> Icons.Default.Add
              TransactionType.LOAN_PAYMENT -> Icons.Default.Remove
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint =
            when (transaction.type) {
              TransactionType.EXPENSE -> Color(0xFFF57C00)
              TransactionType.INCOME -> Color(0xFF388E3C)
              TransactionType.TRANSFER -> Color(0xFF1976D2)
              TransactionType.LOAN -> Color(0xFFFF9800)
              TransactionType.LOAN_PAYMENT -> Color(0xFF9C27B0)
            },
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = transaction.payee ?: transaction.type.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "${account?.name ?: "未知账户"} • ${formatDate(transaction.transactionDate)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        transaction.notes?.let { notes ->
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text =
          when (transaction.type) {
            TransactionType.EXPENSE -> "-¥${formatAmount(transaction.amount)}"
            TransactionType.INCOME -> "+¥${formatAmount(transaction.amount)}"
            TransactionType.TRANSFER -> "¥${formatAmount(transaction.amount)}"
            TransactionType.LOAN -> "+¥${formatAmount(transaction.amount)}"
            TransactionType.LOAN_PAYMENT -> "-¥${formatAmount(transaction.amount)}"
          },
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color =
          when (transaction.type) {
            TransactionType.EXPENSE -> Color(0xFFF57C00)
            TransactionType.INCOME -> Color(0xFF388E3C)
            TransactionType.TRANSFER -> Color(0xFF1976D2)
            TransactionType.LOAN -> Color(0xFFFF9800)
            TransactionType.LOAN_PAYMENT -> Color(0xFF9C27B0)
          },
        )
      }
    }
  }
}

/**
 * Format amount with 2 decimal places
 */
fun formatAmount(amount: Double): String = "%.2f"
  .replace("%.", amount.toString().substringBefore('.') + ".")
  .let { pattern ->
    val intPart = amount.toLong()
    val decPart = ((amount - intPart) * 100).toLong().toString().padStart(2, '0')
    "$intPart.$decPart"
  }

/**
 * Format timestamp to date string (KMP-compatible)
 * Uses simple calculation to convert milliseconds to date
 */
fun formatDate(timestamp: Long): String {
  // Calculate days since epoch (Jan 1, 1970)
  val days = timestamp / (24 * 60 * 60 * 1000)

  // Simple approximation: calculate year, month, day
  // This is simplified - doesn't account for leap years perfectly
  var remainingDays = days
  var year = 1970

  // Rough year calculation
  while (remainingDays >= 365) {
    val daysInYear = if (isLeapYear(year)) 366 else 365
    if (remainingDays >= daysInYear) {
      remainingDays -= daysInYear
      year++
    } else {
      break
    }
  }

  // Month calculation
  val daysInMonths = if (isLeapYear(year)) {
    listOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
  } else {
    listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
  }

  var month = 1
  for (daysInMonth in daysInMonths) {
    if (remainingDays >= daysInMonth) {
      remainingDays -= daysInMonth
      month++
    } else {
      break
    }
  }

  val day = remainingDays + 1 // +1 because days are 1-indexed

  return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

/**
 * Check if a year is a leap year
 */
private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

/**
 * Fan-out FAB with multiple action buttons
 */
@Composable
fun FanOutFAB(
  isExpanded: Boolean,
  onExpandChange: (Boolean) -> Unit,
  onAddTransaction: () -> Unit,
  onCreateLoan: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rotation by animateFloatAsState(
    targetValue = if (isExpanded) 45f else 0f,
  )

  Box(modifier = modifier) {
    Column(
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Action buttons
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Create Loan button
          FabMenuItem(
            icon = Icons.Default.MoneyOff,
            label = "创建借贷",
            onClick = onCreateLoan,
          )

          // Add Transaction button
          FabMenuItem(
            icon = Icons.Default.AttachMoney,
            label = "添加交易",
            onClick = onAddTransaction,
          )
        }
      }

      // Main FAB
      FloatingActionButton(
        onClick = { onExpandChange(!isExpanded) },
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = if (isExpanded) "关闭" else "添加",
          modifier = Modifier.rotate(rotation),
        )
      }
    }
  }
}

/**
 * Individual FAB menu item
 */
@Composable
fun FabMenuItem(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Label
    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
      Text(
        text = label,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.bodyMedium,
      )
    }

    // Small FAB
    SmallFloatingActionButton(
      onClick = onClick,
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

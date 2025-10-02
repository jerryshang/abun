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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tireless.abun.navigation.Route
import org.koin.compose.koinInject

/**
 * UI state for dialog management
 */
sealed interface DialogState {
  data object None : DialogState
  data object Expense : DialogState
  data object Income : DialogState
  data object Transfer : DialogState
  data object Loan : DialogState
  data class Transaction(val transactionWithDetails: TransactionWithDetails?) : DialogState
}

/**
 * Main Finance Screen with transaction list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
  navController: NavHostController,
  viewModel: TransactionViewModel = koinInject(),
) {
  val transactions by viewModel.transactions.collectAsState()
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val error by viewModel.error.collectAsState()
  val selectedAccountId by viewModel.selectedAccountId.collectAsState()

  var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }
  var isFabExpanded by remember { mutableStateOf(false) }
  var showAccountSelector by remember { mutableStateOf(false) }
  var expandedAssets by remember { mutableStateOf(true) }
  var expandedLiabilities by remember { mutableStateOf(true) }
  var filteredTransactions by remember { mutableStateOf<List<TransactionWithDetails>>(emptyList()) }
  val scope = rememberCoroutineScope()

  // Filter accounts to show only Assets and Liabilities
  val selectableAccounts = accounts.filter { account ->
    account.parentId == RootAccountIds.ASSET || account.parentId == RootAccountIds.LIABILITY
  }

  // Update filtered transactions when selection changes
  LaunchedEffect(selectedAccountId, transactions) {
    filteredTransactions = if (selectedAccountId == null) {
      transactions
    } else {
      transactions.filter {
        it.transaction.debitAccountId == selectedAccountId || it.transaction.creditAccountId == selectedAccountId
      }
    }
  }

  // Get selected account name
  val selectedAccount = selectedAccountId?.let { id ->
    accounts.find { it.id == id }
  }

  // Calculate predicted balance (placeholder logic)
  val totalBalance = accounts.filter { it.isActive }.sumOf { it.currentBalance }
  val predictedBalance = totalBalance + 4500.0 // TODO: Calculate based on planned transactions

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Box {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { showAccountSelector = !showAccountSelector }
            ) {
              IconButton(onClick = {
                if (selectedAccountId != null) {
                  viewModel.setSelectedAccount(null)
                } else {
                  showAccountSelector = !showAccountSelector
                }
              }) {
                Icon(
                  imageVector = if (selectedAccountId != null) Icons.Default.Close else Icons.Default.AttachMoney,
                  contentDescription = if (selectedAccountId != null) "清除筛选" else "选择账户"
                )
              }
              if (selectedAccount != null) {
                Text(
                  text = selectedAccount.name,
                  style = MaterialTheme.typography.titleLarge
                )
              } else {
                Text(
                  text = "全部 (All)",
                  style = MaterialTheme.typography.titleLarge
                )
              }
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "展开账户选择",
                modifier = Modifier.size(24.dp)
              )
            }

            DropdownMenu(
              expanded = showAccountSelector,
              onDismissRequest = { showAccountSelector = false }
            ) {
              // Group accounts by parent (Asset or Liability)
              val assetAccounts = selectableAccounts.filter { it.parentId == RootAccountIds.ASSET }
              val liabilityAccounts = selectableAccounts.filter { it.parentId == RootAccountIds.LIABILITY }

              // "All" option
              DropdownMenuItem(
                text = {
                  Text(
                    "全部 (All)",
                    fontWeight = if (selectedAccountId == null) FontWeight.Bold else FontWeight.Normal
                  )
                },
                onClick = {
                  viewModel.setSelectedAccount(null)
                  showAccountSelector = false
                }
              )

              // Assets section with expandable children
              if (assetAccounts.isNotEmpty()) {
                DropdownMenuItem(
                  text = {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        "资产 (Assets)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                      )
                      Icon(
                        imageVector = if (expandedAssets) Icons.Default.KeyboardArrowDown else Icons.Default.ArrowDropDown,
                        contentDescription = if (expandedAssets) "收起" else "展开",
                        modifier = Modifier
                          .size(20.dp)
                          .rotate(if (expandedAssets) 0f else -90f)
                      )
                    }
                  },
                  onClick = { expandedAssets = !expandedAssets }
                )

                if (expandedAssets) {
                  assetAccounts.forEach { account ->
                    DropdownMenuItem(
                      text = {
                        Text(
                          "  • ${account.name}",
                          fontWeight = if (selectedAccountId == account.id) FontWeight.Bold else FontWeight.Normal
                        )
                      },
                      onClick = {
                        viewModel.setSelectedAccount(account.id)
                        showAccountSelector = false
                      }
                    )
                  }
                }
              }

              // Liabilities section with expandable children
              if (liabilityAccounts.isNotEmpty()) {
                DropdownMenuItem(
                  text = {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        "负债 (Liabilities)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                      )
                      Icon(
                        imageVector = if (expandedLiabilities) Icons.Default.KeyboardArrowDown else Icons.Default.ArrowDropDown,
                        contentDescription = if (expandedLiabilities) "收起" else "展开",
                        modifier = Modifier
                          .size(20.dp)
                          .rotate(if (expandedLiabilities) 0f else -90f)
                      )
                    }
                  },
                  onClick = { expandedLiabilities = !expandedLiabilities }
                )

                if (expandedLiabilities) {
                  liabilityAccounts.forEach { account ->
                    DropdownMenuItem(
                      text = {
                        Text(
                          "  • ${account.name}",
                          fontWeight = if (selectedAccountId == account.id) FontWeight.Bold else FontWeight.Normal
                        )
                      },
                      onClick = {
                        viewModel.setSelectedAccount(account.id)
                        showAccountSelector = false
                      }
                    )
                  }
                }
              }
            }
          }
        },
        actions = {
          IconButton(onClick = { navController.navigate(Route.PriceComparison) }) {
            Icon(Icons.Default.ShoppingCart, "价格对比")
          }
          IconButton(onClick = { navController.navigate(Route.AccountManagement) }) {
            Icon(Icons.Default.AccountBalanceWallet, "账户管理")
          }
        },
      )
    },
    floatingActionButton = {
      FanOutFAB(
        isExpanded = isFabExpanded,
        onExpandChange = { isFabExpanded = it },
        onAddExpense = {
          dialogState = DialogState.Expense
          isFabExpanded = false
        },
        onAddIncome = {
          dialogState = DialogState.Income
          isFabExpanded = false
        },
        onAddTransfer = {
          dialogState = DialogState.Transfer
          isFabExpanded = false
        },
        onAddTransaction = {
          dialogState = DialogState.Transaction(null)
          isFabExpanded = false
        },
        onCreateLoan = {
          dialogState = DialogState.Loan
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
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Show either account summary or total assets summary
          item {
            if (selectedAccount != null) {
              // Account-specific summary
              AccountDetailSummaryCard(
                account = selectedAccount,
                onViewDetails = { navController.navigate(Route.AccountDetails(selectedAccount.id)) }
              )
            } else {
              // Total Assets Summary with Financial Prediction
              AccountsSummaryCard(
                accounts = accounts,
                predictedBalance = predictedBalance,
                daysAhead = 30,
                onViewFuture = { navController.navigate(Route.FutureView) },
              )
            }
          }

          // Recent Transactions Section
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = if (selectedAccount != null) {
                  "${selectedAccount.name} 交易记录"
                } else {
                  "最近交易 (Recent Transactions)"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              TextButton(onClick = { navController.navigate(Route.AccountDetails(selectedAccountId)) }) {
                Text("查看全部 >")
              }
            }
          }

          // Recent Transaction Items (limited to 10)
          items(filteredTransactions.take(10)) { transactionWithDetails ->
            TransactionCard(
              transactionWithDetails = transactionWithDetails,
              onClick = {
                dialogState = DialogState.Transaction(transactionWithDetails)
              },
              onDelete = {
                viewModel.deleteTransaction(transactionWithDetails.transaction.id)
              },
            )
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

  // Dialog Management
  when (val state = dialogState) {
    DialogState.None -> { /* No dialog */ }

    DialogState.Expense -> {
      AddExpenseDialog(
        accounts = accounts,
        onDismiss = { dialogState = DialogState.None },
        onConfirm = { input ->
          viewModel.createTransaction(input)
          dialogState = DialogState.None
        }
      )
    }

    DialogState.Income -> {
      AddIncomeDialog(
        accounts = accounts,
        onDismiss = { dialogState = DialogState.None },
        onConfirm = { input ->
          viewModel.createTransaction(input)
          dialogState = DialogState.None
        }
      )
    }

    DialogState.Transfer -> {
      AddTransferDialog(
        accounts = accounts,
        onDismiss = { dialogState = DialogState.None },
        onConfirm = { input ->
          viewModel.createTransaction(input)
          dialogState = DialogState.None
        }
      )
    }

    DialogState.Loan -> {
      AddLoanDialog(
        accounts = accounts,
        onDismiss = { dialogState = DialogState.None },
        onConfirm = { input ->
          viewModel.createLoan(input)
          dialogState = DialogState.None
        }
      )
    }

    is DialogState.Transaction -> {
      AddTransactionDialog(
        transactionWithDetails = state.transactionWithDetails,
        accounts = accounts,
        onDismiss = { dialogState = DialogState.None },
        onConfirm = { input ->
          if (state.transactionWithDetails == null) {
            viewModel.createTransaction(input)
          } else {
            viewModel.updateTransaction(
              UpdateTransactionInput(
                id = state.transactionWithDetails.transaction.id,
                amount = input.amount,
                type = input.type,
                transactionDate = input.transactionDate,
                accountId = input.accountId,
                toAccountId = input.toAccountId,
                payee = input.payee,
                member = input.member,
                notes = input.notes,
                tagIds = input.tagIds,
              ),
            )
          }
          dialogState = DialogState.None
        },
      )
    }
  }
}

/**
 * Account list section showing all accounts (collapsible)
 */
@Composable
fun AccountListSection(
  accounts: List<AccountWithBalance>,
  onManageAccounts: () -> Unit,
  onAccountClick: (Long) -> Unit,
) {
  var isExpanded by remember { mutableStateOf(false) }
  val rotation by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
  )

  Column(
    modifier =
    Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    // Section header with expand/collapse button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { isExpanded = !isExpanded },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = if (isExpanded) "收起" else "展开",
          modifier = Modifier
            .size(20.dp)
            .rotate(rotation),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "账户列表 (Accounts)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }
      TextButton(onClick = onManageAccounts) {
        Text("管理 >")
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Collapsible account items
    AnimatedVisibility(
      visible = isExpanded,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut(),
    ) {
      Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      ) {
        Column {
          accounts.filter { it.isActive }.forEach { account ->
            Row(
              modifier =
              Modifier
                .fillMaxWidth()
                .clickable { onAccountClick(account.id) }
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = account.name,
                style = MaterialTheme.typography.bodyLarge,
              )
              Text(
                text = "${if (account.currentBalance < 0) "-" else ""}¥${formatAmount(account.currentBalance.coerceAtLeast(0.0))}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color =
                if (account.currentBalance < 0) {
                  Color(0xFFD32F2F)
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Accounts summary card showing total balance with visibility toggle and prediction
 */
@Composable
fun AccountsSummaryCard(
  accounts: List<AccountWithBalance>,
  predictedBalance: Double,
  daysAhead: Int = 30,
  onViewFuture: () -> Unit,
) {
  var isBalanceVisible by remember { mutableStateOf(true) }
  val totalBalance = accounts.filter { it.isActive }.sumOf { it.currentBalance }

  // TODO: Calculate month-over-month change from historical data
  val monthlyChange = 2500.0 // Placeholder
  val isPositiveChange = monthlyChange >= 0

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
        .padding(20.dp),
    ) {
      // Header with toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "总资产 (Total Assets)",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(
          onClick = { isBalanceVisible = !isBalanceVisible },
          modifier = Modifier.size(32.dp),
        ) {
          Text(
            text = if (isBalanceVisible) "👁️" else "👁️‍🗨️",
            style = MaterialTheme.typography.titleMedium,
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Large balance display
      Text(
        text = if (isBalanceVisible) "¥${formatAmount(totalBalance)}" else "¥****.**",
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Month-over-month comparison
      if (isBalanceVisible) {
        Text(
          text = "(较上月 ${if (isPositiveChange) "+" else ""}¥${formatAmount(monthlyChange)})",
          style = MaterialTheme.typography.bodySmall,
          color = if (isPositiveChange) Color(0xFF388E3C) else Color(0xFFD32F2F),
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Divider
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(MaterialTheme.colorScheme.outlineVariant)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Financial Prediction Section
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onViewFuture),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "财务预测",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(4.dp))
          if (isBalanceVisible) {
            Text(
              text = "${daysAhead}日后预计: ¥${formatAmount(predictedBalance)}",
              style = MaterialTheme.typography.bodyMedium,
              color = if (predictedBalance >= totalBalance) {
                Color(0xFF388E3C)
              } else {
                Color(0xFFF57C00)
              }
            )
          } else {
            Text(
              text = "${daysAhead}日后预计: ¥****.**",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "查看未来",
          modifier = Modifier.size(16.dp).rotate(90f),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

/**
 * Transaction card item
 */
@Composable
fun TransactionCard(
  transactionWithDetails: TransactionWithDetails,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val transaction = transactionWithDetails.transaction
  val transactionType = transactionWithDetails.inferType()
  val primaryAccount = transactionWithDetails.getPrimaryAccount()
  val secondaryAccount = transactionWithDetails.getSecondaryAccount()

  val backgroundColor =
    when (transactionType) {
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
      .padding(horizontal = 16.dp)
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
            when (transactionType) {
              TransactionType.EXPENSE -> Icons.Default.Remove
              TransactionType.INCOME -> Icons.Default.Add
              TransactionType.TRANSFER -> Icons.Default.SwapHoriz
              TransactionType.LOAN -> Icons.Default.Add
              TransactionType.LOAN_PAYMENT -> Icons.Default.Remove
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint =
            when (transactionType) {
              TransactionType.EXPENSE -> Color(0xFFF57C00)
              TransactionType.INCOME -> Color(0xFF388E3C)
              TransactionType.TRANSFER -> Color(0xFF1976D2)
              TransactionType.LOAN -> Color(0xFFFF9800)
              TransactionType.LOAN_PAYMENT -> Color(0xFF9C27B0)
            },
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = transaction.payee ?: transactionType.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = buildString {
            append(primaryAccount.name)
            if (secondaryAccount != null) {
              append(" → ${secondaryAccount.name}")
            }
            append(" • ${formatDate(transaction.transactionDate)}")
          },
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
          when (transactionType) {
            TransactionType.EXPENSE -> "-¥${formatAmount(transaction.amount)}"
            TransactionType.INCOME -> "+¥${formatAmount(transaction.amount)}"
            TransactionType.TRANSFER -> "¥${formatAmount(transaction.amount)}"
            TransactionType.LOAN -> "+¥${formatAmount(transaction.amount)}"
            TransactionType.LOAN_PAYMENT -> "-¥${formatAmount(transaction.amount)}"
          },
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color =
          when (transactionType) {
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
 * Account detail summary card showing account-specific information
 * - For debit cards (assets): Shows current balance
 * - For credit cards (liabilities): Shows debt amount and available credit
 */
@Composable
fun AccountDetailSummaryCard(
  account: AccountWithBalance,
  onViewDetails: () -> Unit,
) {
  val isLiability = account.parentId == RootAccountIds.LIABILITY
  val isAsset = account.parentId == RootAccountIds.ASSET

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = account.name,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        IconButton(
          onClick = onViewDetails,
          modifier = Modifier.size(32.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "查看详情",
            modifier = Modifier.size(16.dp).rotate(90f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      when {
        isLiability -> {
          // Credit card or loan: Show debt and available credit
          val debtAmount = kotlin.math.abs(account.currentBalance) // Liability balance is typically negative
          val creditLimit = account.creditLimit ?: 0.0
          val availableCredit = creditLimit - debtAmount

          // Debt amount
          Text(
            text = "欠款金额",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "¥${formatAmount(debtAmount)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = if (debtAmount > 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
          )

          if (creditLimit > 0) {
            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Available credit
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column {
                Text(
                  text = "可用额度",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "¥${formatAmount(availableCredit.coerceAtLeast(0.0))}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = if (availableCredit > 0) Color(0xFF388E3C) else Color(0xFFD32F2F),
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "总额度",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "¥${formatAmount(creditLimit)}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }

            // Credit usage indicator
            Spacer(modifier = Modifier.height(12.dp))
            val usagePercent = if (creditLimit > 0) (debtAmount / creditLimit * 100).toInt() else 0
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              androidx.compose.material3.LinearProgressIndicator(
                progress = { (debtAmount / creditLimit).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.weight(1f).height(8.dp),
                color = when {
                  usagePercent >= 90 -> Color(0xFFD32F2F)
                  usagePercent >= 70 -> Color(0xFFF57C00)
                  else -> Color(0xFF388E3C)
                },
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "$usagePercent%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        isAsset -> {
          // Debit card or asset: Show current balance
          Text(
            text = "当前余额",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "¥${formatAmount(account.currentBalance)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = if (account.currentBalance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F),
          )
        }

        else -> {
          // Other account types: Just show balance
          Text(
            text = "余额",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "¥${formatAmount(account.currentBalance)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
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
  onAddExpense: () -> Unit,
  onAddIncome: () -> Unit,
  onAddTransfer: () -> Unit,
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

          // Add Transfer button
          FabMenuItem(
            icon = Icons.Default.SwapHoriz,
            label = "添加转账",
            onClick = onAddTransfer,
          )

          // Add Income button
          FabMenuItem(
            icon = Icons.Default.Add,
            label = "添加收入",
            onClick = onAddIncome,
          )

          // Add Expense button
          FabMenuItem(
            icon = Icons.Default.Remove,
            label = "添加支出",
            onClick = onAddExpense,
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

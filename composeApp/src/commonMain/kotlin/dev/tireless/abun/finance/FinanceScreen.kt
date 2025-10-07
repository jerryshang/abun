package dev.tireless.abun.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.ArrowRightLeft
import com.composables.icons.lucide.Calculator
import com.composables.icons.lucide.Landmark
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PiggyBank
import com.composables.icons.lucide.Receipt
import com.composables.icons.lucide.WalletCards
import dev.tireless.abun.navigation.Route
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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

  var isFabExpanded by remember { mutableStateOf(false) }
  var showAccountSelector by remember { mutableStateOf(false) }
  var selectorAnchorWidth by remember { mutableStateOf(0) }
  var filteredTransactions by remember { mutableStateOf<List<TransactionWithDetails>>(emptyList()) }

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

  Scaffold(
    topBar = {
      TopAppBar(
        windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          scrolledContainerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        title = {
          val accountLabel = selectedAccount?.name ?: "All accounts"
          val chevronRotation by animateFloatAsState(
            targetValue = if (showAccountSelector) 180f else 0f,
            label = "AccountSelectorChevron",
          )

          Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.CenterStart,
          ) {
            Surface(
              shape = RoundedCornerShape(28.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
              modifier = Modifier.onGloballyPositioned { coords -> selectorAnchorWidth = coords.size.width }
            ) {
              Row(
                modifier =
                Modifier
                  .fillMaxWidth()
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                  ) { showAccountSelector = !showAccountSelector }
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Icon(
                  imageVector = Lucide.Landmark,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = accountLabel,
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = if (showAccountSelector) "Collapse account selection" else "Expand account selection",
                  modifier = Modifier
                    .size(18.dp)
                    .rotate(chevronRotation),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            if (selectedAccountId != null) {
              Surface(
                onClick = { viewModel.setSelectedAccount(null) },
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 12.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear account filter",
                  modifier = Modifier
                    .size(16.dp)
                    .padding(4.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            AccountHierarchySelector(
              accounts = accounts,
              filter = AccountFilter.NORMAL_ACCOUNTS,
              selectedAccountId = selectedAccountId,
              onAccountSelected = { viewModel.setSelectedAccount(it) },
              expanded = showAccountSelector,
              onExpandedChange = { showAccountSelector = it },
              showAllOption = true,
              menuWidthPx = selectorAnchorWidth,
            )
          }
        },
        actions = {
          ToolbarActionButton(
            icon = Lucide.Calculator,
            contentDescription = "Price comparison",
            onClick = { navController.navigate(Route.PriceComparison) },
          )
          Spacer(modifier = Modifier.width(8.dp))
          ToolbarActionButton(
            icon = Lucide.WalletCards,
            contentDescription = "Account management",
            onClick = { navController.navigate(Route.AccountManagement) },
          )
        },
      )
    },
    floatingActionButton = {
      FanOutFAB(
        isExpanded = isFabExpanded,
        onExpandChange = { isFabExpanded = it },
        onAddExpense = {
          navController.navigate(Route.ExpenseEdit())
          isFabExpanded = false
        },
        onAddIncome = {
          navController.navigate(Route.RevenueEdit())
          isFabExpanded = false
        },
        onAddTransfer = {
          navController.navigate(Route.TransferEdit())
          isFabExpanded = false
        },
        onCreateLoan = {
          navController.navigate(Route.LoanEdit)
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
          contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
          verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
          item {
            if (selectedAccount != null) {
              AccountDetailSummaryCard(
                account = selectedAccount,
                onViewDetails = { navController.navigate(Route.AccountDetails(selectedAccount.id)) },
                modifier = Modifier.fillMaxWidth(),
              )
            } else {
              AccountsSummaryCard(
                accounts = accounts,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }

          if (filteredTransactions.isNotEmpty()) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = selectedAccount?.let { "${it.name} history" } ?: "Recent transactions",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                TextButton(
                  onClick = { navController.navigate(Route.AccountDetails(selectedAccountId)) },
                  contentPadding = PaddingValues(horizontal = 0.dp),
                ) {
                  Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelLarge,
                  )
                }
              }
            }

            items(filteredTransactions.take(10)) { transactionWithDetails ->
              TransactionCard(
                transactionWithDetails = transactionWithDetails,
                onClick = {
                  handleTransactionClick(navController, transactionWithDetails)
                },
                onDelete = {
                  viewModel.deleteTransaction(transactionWithDetails.transaction.id)
                },
              )
            }
          } else {
            item {
              EmptyTransactionsState(
                modifier = Modifier.fillMaxWidth(),
                onAddTransaction = {
                  isFabExpanded = true
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
              Text("Close")
            }
          },
        ) {
          Text(errorMessage)
        }
      }
    }
  }
}

private fun handleTransactionClick(
  navController: NavHostController,
  transactionWithDetails: TransactionWithDetails,
) {
  val transactionId = transactionWithDetails.transaction.id

  when (transactionWithDetails.inferType()) {
    TransactionType.EXPENSE -> navController.navigate(Route.ExpenseEdit(transactionId))
    TransactionType.INCOME -> navController.navigate(Route.RevenueEdit(transactionId))
    TransactionType.TRANSFER -> navController.navigate(Route.TransferEdit(transactionId))
    TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> Unit
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
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = Modifier.clickable { isExpanded = !isExpanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          modifier = Modifier.size(32.dp),
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = if (isExpanded) "Collapse" else "Expand",
              modifier = Modifier
                .size(16.dp)
                .rotate(rotation),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Text(
          text = "Accounts",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }
      TextButton(
        onClick = onManageAccounts,
        contentPadding = PaddingValues(horizontal = 0.dp),
      ) {
        Text(
          text = "Manage",
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    AnimatedVisibility(
      visible = isExpanded,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut(),
    ) {
      val activeAccounts = accounts.filter { it.isActive }

      Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
      ) {
        Column {
          activeAccounts.forEachIndexed { index, account ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onAccountClick(account.id) }
                .padding(horizontal = 20.dp, vertical = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = account.name,
                style = MaterialTheme.typography.bodyLarge,
              )
              Text(
                text = "¥${formatAmount(account.currentBalance)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (account.currentBalance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
              )
            }
            if (index < activeAccounts.lastIndex) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
  modifier: Modifier = Modifier,
) {
  val totalBalance = accounts.filter { it.isActive }.sumOf { it.currentBalance }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(28.dp),
    tonalElevation = 1.dp,
    shadowElevation = 0.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Total balance",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "¥${formatAmount(totalBalance)}",
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )
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

  val icon =
    when (transactionType) {
      TransactionType.EXPENSE -> Icons.Default.Remove
      TransactionType.INCOME -> Icons.Default.Add
      TransactionType.TRANSFER -> Lucide.ArrowRightLeft
      TransactionType.LOAN -> Lucide.Landmark
      TransactionType.LOAN_PAYMENT -> Lucide.PiggyBank
    }

  val accountColor = resolveTransactionColor(transactionWithDetails)
  val accentColor = accountColor ?: MaterialTheme.colorScheme.secondary
  val backgroundColor = accentColor.copy(alpha = 0.12f)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(24.dp),
    tonalElevation = 1.dp,
    shadowElevation = 0.dp,
    color = backgroundColor,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(color = accentColor.copy(alpha = 0.18f), shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = accentColor,
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = transaction.payee ?: transactionType.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
          text = buildString {
            append(primaryAccount.name)
            if (secondaryAccount != null) {
            append(" -> ${secondaryAccount.name}")
            }
            append(" - ${formatDate(transaction.transactionDate)}")
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        transaction.notes?.takeIf { it.isNotBlank() }?.let { notes ->
          Text(
            text = notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
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
          fontWeight = FontWeight.SemiBold,
          color = accentColor,
        )

        Surface(
          modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onDelete),
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Delete transaction",
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

private fun resolveTransactionColor(transactionWithDetails: TransactionWithDetails): Color? {
  val transactionType = transactionWithDetails.inferType()
  val primaryAccount = transactionWithDetails.getPrimaryAccount()
  val secondaryAccount = transactionWithDetails.getSecondaryAccount()

  val colorHex = when (transactionType) {
    TransactionType.EXPENSE, TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> transactionWithDetails.debitAccount.colorHex
    TransactionType.INCOME -> transactionWithDetails.creditAccount.colorHex
    TransactionType.TRANSFER -> primaryAccount.colorHex ?: secondaryAccount?.colorHex
  }

  return hexToColorOrNull(colorHex)
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
  modifier: Modifier = Modifier,
) {
  val isLiability = account.parentId == RootAccountIds.LIABILITY
  val isAsset = account.parentId == RootAccountIds.ASSET
  val typeLabel = when (account.parentId) {
    RootAccountIds.ASSET -> "Asset account"
    RootAccountIds.LIABILITY -> "Liability account"
    RootAccountIds.REVENUE -> "Revenue account"
    RootAccountIds.EXPENSE -> "Expense account"
    else -> "Account"
  }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(28.dp),
    tonalElevation = 1.dp,
    shadowElevation = 0.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = account.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = typeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        TextButton(
          onClick = onViewDetails,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Text(
            text = "Details",
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      when {
        isLiability -> {
          val debtAmount = kotlin.math.abs(account.currentBalance)
          val creditLimit = account.creditLimit ?: 0.0
          val availableCredit = creditLimit - debtAmount

          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "Outstanding balance",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "¥${formatAmount(debtAmount)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (debtAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              )
            }

            if (creditLimit > 0) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(
                    text = "Available credit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Text(
                    text = "¥${formatAmount(availableCredit.coerceAtLeast(0.0))}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (availableCredit > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                  )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(
                    text = "Credit limit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Text(
                    text = "¥${formatAmount(creditLimit)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }

              val usage = (debtAmount / creditLimit).coerceIn(0.0, 1.0)
              val usagePercent = (usage * 100).toInt()
              val usageColor = when {
                usagePercent >= 90 -> MaterialTheme.colorScheme.error
                usagePercent >= 70 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.secondary
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                androidx.compose.material3.LinearProgressIndicator(
                  progress = { usage.toFloat() },
                  modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                  color = usageColor,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = "$usagePercent%",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }

        isAsset -> {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Current balance",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "¥${formatAmount(account.currentBalance)}",
              style = MaterialTheme.typography.displaySmall,
              fontWeight = FontWeight.Bold,
              color = if (account.currentBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
          }
        }

        else -> {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Balance",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
}

@Composable
fun ToolbarActionButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.size(40.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(20.dp),
    )
  }
}

@Composable
fun EmptyTransactionsState(
  modifier: Modifier = Modifier,
  onAddTransaction: () -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(28.dp),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 40.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        imageVector = Lucide.Receipt,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "No transactions yet",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "Start by logging an expense or income.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      TextButton(
        onClick = onAddTransaction,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
      ) {
        Text(
          text = "Add transaction",
          style = MaterialTheme.typography.labelLarge,
        )
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
 * Fan-out FAB with arc layout for transaction actions
 */
@Composable
fun FanOutFAB(
  isExpanded: Boolean,
  onExpandChange: (Boolean) -> Unit,
  onAddExpense: () -> Unit,
  onAddIncome: () -> Unit,
  onAddTransfer: () -> Unit,
  onCreateLoan: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rotation by animateFloatAsState(
    targetValue = if (isExpanded) 45f else 0f,
  )

  Box(modifier = modifier) {
    // Arc radius and angle range
    val radius = 200f // Distance from FAB center (increased for better spacing)
    val startAngle = PI * 195 / 180 // Start from 195 degrees
    val endAngle = PI * 75 / 180 // End at 75 degrees
    val sweepAngle = startAngle - endAngle // Total sweep angle

    // Button configurations with Lucide icons
    val buttons = listOf(
      Triple(Lucide.Receipt, "Expense", onAddExpense),
      Triple(Lucide.PiggyBank, "Income", onAddIncome),
      Triple(Lucide.ArrowRightLeft, "Transfer", onAddTransfer),
      Triple(Lucide.Landmark, "Loan", onCreateLoan)
    )

    // Fan-out buttons in arc
    buttons.forEachIndexed { index, (icon, description, onClick) ->
      // Calculate angle for this button (evenly distributed across the arc)
      val angle = startAngle - (sweepAngle * index / (buttons.size - 1))
      val offsetX = (radius * cos(angle)).roundToInt()
      val offsetY = -(radius * sin(angle)).roundToInt() // Negative because Y grows downward

      // Animated scale and alpha
      val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        )
      )
      val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        )
      )

      if (scale > 0.01f) {
        IconButton(
          onClick = onClick,
          modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .size(48.dp)
            .graphicsLayer {
              scaleX = scale
              scaleY = scale
              this.alpha = alpha
            }
            .background(
              color = MaterialTheme.colorScheme.secondaryContainer,
              shape = androidx.compose.foundation.shape.CircleShape
            )
        ) {
          Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp),
          )
        }
      }
    }

    // Main FAB
    FloatingActionButton(
      onClick = { onExpandChange(!isExpanded) },
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = if (isExpanded) "Close" else "Add",
        modifier = Modifier.rotate(rotation),
      )
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

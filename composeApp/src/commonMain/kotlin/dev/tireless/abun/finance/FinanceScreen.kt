package dev.tireless.abun.finance

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
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
import org.koin.compose.viewmodel.koinViewModel
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
  viewModel: TransactionViewModel = koinViewModel(),
) {
  val transactions by viewModel.transactions.collectAsState()
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val error by viewModel.error.collectAsState()
  val selectedAccountId by viewModel.selectedAccountId.collectAsState()
  val accountLookup = remember(accounts) { accounts.accountLookup() }

  var isFabExpanded by remember { mutableStateOf(false) }
  var showAccountSelector by remember { mutableStateOf(false) }
  var selectorAnchorWidth by remember { mutableIntStateOf(0) }
  var filteredTransactions by remember { mutableStateOf<List<TransactionWithDetails>>(emptyList()) }
  var transactionPendingDeletion by remember { mutableStateOf<TransactionWithDetails?>(null) }
  var deletionContext by remember { mutableStateOf<TransactionDeletionContext?>(null) }
  var deleteAllRelated by remember { mutableStateOf(false) }
  var isDeletionContextLoading by remember { mutableStateOf(false) }

  // Update filtered transactions when selection changes
  LaunchedEffect(selectedAccountId, transactions) {
    filteredTransactions =
      if (selectedAccountId == null) {
        transactions
      } else {
        transactions.filter {
          it.transaction.debitAccountId == selectedAccountId || it.transaction.creditAccountId == selectedAccountId
        }
      }
  }

  LaunchedEffect(transactionPendingDeletion?.transaction?.id) {
    val transactionId = transactionPendingDeletion?.transaction?.id
    if (transactionId == null) {
      deletionContext = null
      deleteAllRelated = false
      isDeletionContextLoading = false
    } else {
      isDeletionContextLoading = true
      deleteAllRelated = false
      try {
        deletionContext = viewModel.getTransactionDeletionContext(transactionId)
      } catch (e: Exception) {
        deletionContext = TransactionDeletionContext.Empty
      } finally {
        isDeletionContextLoading = false
      }
    }
  }

  // Get selected account name
  val selectedAccount =
    selectedAccountId?.let { id ->
      accounts.find { it.id == id }
    }

  Scaffold(
    topBar = {
      TopAppBar(
        windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        colors =
          TopAppBarDefaults.topAppBarColors(
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
              modifier =
                Modifier.onGloballyPositioned { coords ->
                  selectorAnchorWidth = coords.size.width
                },
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
                if (selectedAccountId != null) {
                  Surface(
                    onClick = { viewModel.setSelectedAccount(null) },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                  ) {
                    Box(
                      modifier = Modifier.fillMaxSize(),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear account filter",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                }
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = if (showAccountSelector) "Collapse account selection" else "Expand account selection",
                  modifier =
                    Modifier
                      .size(24.dp)
                      .rotate(chevronRotation),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            AccountHierarchySelector(
              accounts = accounts,
              filter = AccountFilter.NORMAL_ACCOUNTS,
              selectedAccountId = selectedAccountId,
              onAccountSelect = { viewModel.setSelectedAccount(it) },
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
            icon = Icons.Filled.List,
            contentDescription = "试算 Calculator",
            onClick = { navController.navigate(Route.TrialCalculator) },
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
                accountType = selectedAccount.resolveAccountType(accountLookup),
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
            items(filteredTransactions.take(10)) { transactionWithDetails ->
              TransactionCard(
                transactionWithDetails = transactionWithDetails,
                onClick = {
                  handleTransactionClick(navController, transactionWithDetails)
                },
                onDelete = {
                  transactionPendingDeletion = transactionWithDetails
                },
                modifier = Modifier.fillMaxWidth(),
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

  transactionPendingDeletion?.let { pending ->
    TransactionDeleteConfirmationDialog(
      transaction = pending,
      deletionContext = deletionContext,
      isLoading = isDeletionContextLoading,
      deleteAllChecked = deleteAllRelated,
      onDeleteAllCheckedChange = { deleteAllRelated = it },
      onDismiss = {
        transactionPendingDeletion = null
        deletionContext = null
        deleteAllRelated = false
        isDeletionContextLoading = false
      },
      onConfirm = {
        val groupIds =
          if (deleteAllRelated) {
            deletionContext?.groups?.map { it.group.id } ?: emptyList()
          } else {
            emptyList()
          }
        viewModel.deleteTransaction(pending.transaction.id, deleteGroupIds = groupIds)
        transactionPendingDeletion = null
        deletionContext = null
        deleteAllRelated = false
        isDeletionContextLoading = false
      },
    )
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

private fun formatGroupTag(groupId: Long): String {
  val idString = groupId.toString()
  return if (idString.length > 3) {
    val datePart = idString.dropLast(3)
    val serialPart = idString.takeLast(3).padStart(3, '0')
    "$datePart-$serialPart"
  } else {
    idString
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
  val totalBalance = accounts.totalCountableBalance()

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

@Composable
fun TransactionDeleteConfirmationDialog(
  transaction: TransactionWithDetails,
  deletionContext: TransactionDeletionContext?,
  isLoading: Boolean,
  deleteAllChecked: Boolean,
  onDeleteAllCheckedChange: (Boolean) -> Unit,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  val scrollState = rememberScrollState()

  fun signedAmount(details: TransactionWithDetails): String =
    when (details.inferType()) {
      TransactionType.EXPENSE -> "-¥${formatAmount(details.transaction.amount)}"
      TransactionType.INCOME -> "+¥${formatAmount(details.transaction.amount)}"
      TransactionType.TRANSFER -> "¥${formatAmount(details.transaction.amount)}"
      TransactionType.LOAN -> "+¥${formatAmount(details.transaction.amount)}"
      TransactionType.LOAN_PAYMENT -> "-¥${formatAmount(details.transaction.amount)}"
    }

  fun labelFor(details: TransactionWithDetails): String {
    val payee = details.transaction.payee
    if (!payee.isNullOrBlank()) return payee
    val raw =
      details
        .inferType()
        .name
        .lowercase()
        .replace('_', ' ')
    return raw.replaceFirstChar { char ->
      if (char.isLowerCase()) char.uppercase() else char.toString()
    }
  }

  val transactionSummaryLabel = labelFor(transaction)
  val transactionDate = formatDate(transaction.transaction.transactionDate)
  val relatedGroups = deletionContext?.groups.orEmpty()
  val hasGroups = relatedGroups.isNotEmpty()
  val additionalTransactions =
    relatedGroups
      .flatMap { it.transactions }
      .filter { it.transaction.id != transaction.transaction.id }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Delete transaction",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Will you delete the transaction?",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
          text = "$transactionSummaryLabel — ${signedAmount(transaction)} on $transactionDate",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isLoading) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
            )
            Text(
              text = "Loading related transactions…",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else if (hasGroups) {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "This transaction is part of the following group${if (relatedGroups.size > 1) "s" else ""}:",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )

            relatedGroups.forEach { groupWithTransactions ->
              Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = groupWithTransactions.group.name,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )

                groupWithTransactions.transactions.forEach { groupedTransaction ->
                  val isTarget = groupedTransaction.transaction.id == transaction.transaction.id
                  Text(
                    text = "• ${labelFor(groupedTransaction)} — ${signedAmount(groupedTransaction)} on ${
                      formatDate(
                        groupedTransaction.transaction.transactionDate,
                      )
                    }${if (isTarget) " (selected)" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                      if (isTarget) {
                        MaterialTheme.colorScheme.onSurface
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      },
                  )
                }
              }
            }

            if (additionalTransactions.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Checkbox(
                  checked = deleteAllChecked,
                  onCheckedChange = onDeleteAllCheckedChange,
                )
                Text(
                  text = "Delete all related transactions",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onConfirm, enabled = !isLoading) {
        Text("Delete")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}

/**
 * Transaction card item
 */
@Composable
fun TransactionCard(
  transactionWithDetails: TransactionWithDetails,
  onClick: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
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

  val accentColor = MaterialTheme.colorScheme.secondary
  val backgroundColor = accentColor.copy(alpha = 0.12f)

  Surface(
    modifier = modifier.clickable(onClick = onClick),
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
        modifier =
          Modifier
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
          text =
            buildString {
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

        if (transactionWithDetails.groups.isNotEmpty()) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            transactionWithDetails.groups.forEach { group ->
              Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
              ) {
                Text(
                  text = formatGroupTag(group.id),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
          }
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
          modifier =
            Modifier
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

/**
 * Account detail summary card showing account-specific information
 * - For debit cards (assets): Shows current balance
 * - For credit cards (liabilities): Shows debt amount and available credit
 */
@Composable
fun AccountDetailSummaryCard(
  account: AccountWithBalance,
  accountType: AccountType?,
  onViewDetails: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val resolvedType = accountType ?: AccountType.ASSET
  val isLiability = resolvedType == AccountType.LIABILITY
  val isAsset = resolvedType == AccountType.ASSET

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
                Column(
                  horizontalAlignment = Alignment.End,
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
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
              val usageColor =
                when {
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
                  modifier =
                    Modifier
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
      modifier =
        Modifier
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
  val daysInMonths =
    if (isLeapYear(year)) {
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
    val buttons =
      listOf(
        Triple(Lucide.Receipt, "Expense", onAddExpense),
        Triple(Lucide.PiggyBank, "Income", onAddIncome),
        Triple(Lucide.ArrowRightLeft, "Transfer", onAddTransfer),
        Triple(Lucide.Landmark, "Loan", onCreateLoan),
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
        animationSpec =
          spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
          ),
      )
      val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec =
          spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
          ),
      )

      if (scale > 0.01f) {
        IconButton(
          onClick = onClick,
          modifier =
            Modifier
              .offset { IntOffset(offsetX, offsetY) }
              .size(48.dp)
              .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
              }.background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = androidx.compose.foundation.shape.CircleShape,
              ),
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
      colors =
        CardDefaults.cardColors(
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

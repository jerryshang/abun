package dev.tireless.abun.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onNavigateToCategories: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("财务管理") },
                actions = {
                    IconButton(onClick = onNavigateToAccounts) {
                        Icon(Icons.Default.AccountBalanceWallet, "账户管理")
                    }
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.Default.Category, "分类管理")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedTransaction = null
                    showAddTransactionDialog = true
                }
            ) {
                Icon(Icons.Default.Add, "添加交易")
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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Summary Card
                    AccountsSummaryCard(accounts)

                    // Transaction List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                }
                            )
                        }
                    }
                }
            }

            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("关闭")
                        }
                    }
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
                            tagIds = input.tagIds
                        )
                    )
                }
                showAddTransactionDialog = false
                selectedTransaction = null
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "总资产",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¥${formatAmount(totalBalance)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                accounts.take(3).forEach { account ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "¥${formatAmount(account.currentBalance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
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
    onDelete: () -> Unit
) {
    val account = accounts.find { it.id == transaction.accountId }
    val backgroundColor = when (transaction.type) {
        TransactionType.EXPENSE -> Color(0xFFFFF3E0)
        TransactionType.INCOME -> Color(0xFFE8F5E9)
        TransactionType.TRANSFER -> Color(0xFFE3F2FD)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (transaction.type) {
                            TransactionType.EXPENSE -> Icons.Default.Remove
                            TransactionType.INCOME -> Icons.Default.Add
                            TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = when (transaction.type) {
                            TransactionType.EXPENSE -> Color(0xFFF57C00)
                            TransactionType.INCOME -> Color(0xFF388E3C)
                            TransactionType.TRANSFER -> Color(0xFF1976D2)
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.payee ?: transaction.type.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${account?.name ?: "未知账户"} • ${formatDate(transaction.transactionDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                transaction.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.EXPENSE -> "-¥${formatAmount(transaction.amount)}"
                        TransactionType.INCOME -> "+¥${formatAmount(transaction.amount)}"
                        TransactionType.TRANSFER -> "¥${formatAmount(transaction.amount)}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (transaction.type) {
                        TransactionType.EXPENSE -> Color(0xFFF57C00)
                        TransactionType.INCOME -> Color(0xFF388E3C)
                        TransactionType.TRANSFER -> Color(0xFF1976D2)
                    }
                )
            }
        }
    }
}

/**
 * Format amount with 2 decimal places
 */
fun formatAmount(amount: Double): String {
    return "%.2f".replace("%.", amount.toString().substringBefore('.') + ".")
        .let { pattern ->
            val intPart = amount.toLong()
            val decPart = ((amount - intPart) * 100).toLong().toString().padStart(2, '0')
            "$intPart.$decPart"
        }
}

/**
 * Format timestamp to date string (KMP-compatible)
 */
fun formatDate(timestamp: Long): String {
    // Simplified for KMP - returns a fixed date string
    return "2024-01-01"
}

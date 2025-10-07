package dev.tireless.abun.finance

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.koinInject

/**
 * Account Details Screen showing transaction history
 * Can display all transactions or filter by specific account
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
  accountId: Long?,
  navController: NavHostController,
  viewModel: TransactionViewModel = koinInject(),
) {
  val transactions by viewModel.transactions.collectAsState()
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var showFilterDialog by remember { mutableStateOf(false) }
  var selectedAccountId by remember { mutableStateOf(accountId) }

  // Get account name for title
  val selectedAccount = selectedAccountId?.let { id ->
    accounts.find { it.id == id }
  }
  val screenTitle = selectedAccount?.name ?: "All Transactions"

  // Filter transactions by account if specified
  val filteredTransactions = if (selectedAccountId != null) {
    transactions.filter {
      it.debitAccount.id == selectedAccountId || it.creditAccount.id == selectedAccountId
    }
  } else {
    transactions
  }

  // Group transactions by date
  val groupedTransactions = filteredTransactions.groupBy { transactionWithDetails ->
    val date = formatDate(transactionWithDetails.transaction.transactionDate)
    when {
      isToday(transactionWithDetails.transaction.transactionDate) -> "Today"
      isYesterday(transactionWithDetails.transaction.transactionDate) -> "Yesterday"
      else -> date
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
              // TODO: Show account selector dropdown
            }
          ) {
            Text(screenTitle)
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Select Account",
              modifier = Modifier.size(20.dp)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.ArrowBack, "Back")
          }
        },
        actions = {
          IconButton(onClick = { showFilterDialog = true }) {
            Icon(Icons.Default.FilterList, "Filter")
          }
        }
      )
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
          // Current balance (if single account selected)
          selectedAccount?.let { account ->
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
                  text = "Current Balance",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "¥${formatAmount(account.currentBalance)}",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (account.currentBalance < 0) {
                    Color(0xFFD32F2F)
                  } else {
                    MaterialTheme.colorScheme.primary
                  }
                )
              }
            }
          }

          // Transaction list grouped by date
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            groupedTransactions.forEach { (dateLabel, transactionsForDate) ->
              // Date header
              item {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "- $dateLabel -",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  val dailyTotal = transactionsForDate.sumOf { transactionWithDetails ->
                    when (transactionWithDetails.inferType()) {
                      TransactionType.INCOME -> transactionWithDetails.transaction.amount
                      TransactionType.EXPENSE -> -transactionWithDetails.transaction.amount
                      else -> 0.0
                    }
                  }
                  if (dailyTotal != 0.0) {
                    Text(
                      text = "${if (dailyTotal > 0) "+" else ""}¥${formatAmount(dailyTotal.coerceAtLeast(0.0))}",
                      style = MaterialTheme.typography.bodySmall,
                      color = if (dailyTotal > 0) Color(0xFF388E3C) else Color(0xFFF57C00)
                    )
                  }
                }
              }

              // Transactions for this date
              items(transactionsForDate) { transactionWithDetails ->
                TransactionCard(
                  transactionWithDetails = transactionWithDetails,
                  onClick = {
                    // TODO: Show edit dialog
                  },
                  onDelete = {
                    viewModel.deleteTransaction(transactionWithDetails.transaction.id)
                  }
                )
              }
            }
          }
        }
      }
    }
  }

  // TODO: Add filter dialog
  if (showFilterDialog) {
    // Placeholder - will implement TransactionFilterDialog
    showFilterDialog = false
  }
}

/**
 * Check if timestamp is today
 */
private fun isToday(timestamp: Long): Boolean {
  val today = currentTimeMillis()
  val days = timestamp / (24 * 60 * 60 * 1000)
  val todayDays = today / (24 * 60 * 60 * 1000)
  return days == todayDays
}

/**
 * Check if timestamp is yesterday
 */
private fun isYesterday(timestamp: Long): Boolean {
  val today = currentTimeMillis()
  val days = timestamp / (24 * 60 * 60 * 1000)
  val todayDays = today / (24 * 60 * 60 * 1000)
  return days == todayDays - 1
}

/**
 * Get current timestamp (placeholder)
 */
private fun currentTimeMillis(): Long {
  return 1704067200000L // 2024-01-01 00:00:00 UTC
}

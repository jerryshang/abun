package dev.tireless.abun.finance

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.koinInject

/**
 * Future View Screen showing balance predictions and upcoming transactions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureViewScreen(
  navController: NavHostController,
  viewModel: TransactionViewModel = koinInject(),
) {
  val transactions by viewModel.transactions.collectAsState()
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var selectedAccountId by remember { mutableStateOf<Long?>(null) }
  var selectedDaysAhead by remember { mutableStateOf(30) }

  // Get planned and estimated transactions
  val upcomingTransactions = transactions.filter {
    it.transaction.state == TransactionState.PLANNED || it.transaction.state == TransactionState.ESTIMATED
  }.sortedBy { it.transaction.transactionDate }

  // Calculate current balance
  val currentBalance = if (selectedAccountId != null) {
    accounts.find { it.id == selectedAccountId }?.currentBalance ?: 0.0
  } else {
    accounts.filter { it.isActive }.sumOf { it.currentBalance }
  }

  // Note: Balance trend generation disabled - needs refactoring for TransactionWithDetails
  val balanceTrendData = emptyList<Pair<String, Double>>()
  /*
  // Generate balance trend data for the next N days
  val balanceTrendData = generateBalanceTrend(
    currentBalance = currentBalance,
    upcomingTransactions = upcomingTransactions,
    daysAhead = selectedDaysAhead,
    accountId = selectedAccountId
  )
   */

  val predictedBalance = currentBalance // Simplified - trend calculation disabled

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Future View") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.ArrowBack, "Back")
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
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Account filter
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable {
                  // TODO: Show account selector
                },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Filter: ${selectedAccountId?.let { id -> accounts.find { it.id == id }?.name } ?: "All Accounts"}",
                style = MaterialTheme.typography.bodyMedium
              )
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Account",
                modifier = Modifier.size(16.dp)
              )
            }
          }

          // Balance Trend Chart
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Text(
                  text = "Asset Trend Chart",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Balance trend chart disabled - needs refactoring for TransactionWithDetails
                /*
                // Simple line chart
                BalanceTrendChart(
                  data = balanceTrendData,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                )
                 */
                Text(
                  text = "Balance trend chart temporarily disabled",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(16.dp)
                )
              }
            }
          }

          // Date selector and predicted balance
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Days Ahead: $selectedDaysAhead",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Projected Balance: ¥${formatAmount(predictedBalance)}",
                      style = MaterialTheme.typography.headlineSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (predictedBalance >= currentBalance) {
                        Color(0xFF388E3C)
                      } else {
                        Color(0xFFF57C00)
                      }
                    )
                  }

                  // Quick date selectors
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    QuickDateButton("7d", selectedDaysAhead == 7) {
                      selectedDaysAhead = 7
                    }
                    QuickDateButton("30d", selectedDaysAhead == 30) {
                      selectedDaysAhead = 30
                    }
                    QuickDateButton("90d", selectedDaysAhead == 90) {
                      selectedDaysAhead = 90
                    }
                  }
                }
              }
            }
          }

          // Section header
          item {
            Text(
              text = "Upcoming Transactions",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 16.dp)
            )
          }

          // Upcoming transactions list
          if (upcomingTransactions.isEmpty()) {
            item {
              Text(
                text = "No scheduled transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
            }
          } else {
            items(upcomingTransactions) { transactionWithDetails ->
              UpcomingTransactionCard(
                transactionWithDetails = transactionWithDetails
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Quick date selector button
 */
@Composable
private fun QuickDateButton(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(60.dp, 32.dp)
      .background(
        color = if (isSelected) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.surfaceVariant
        },
        shape = MaterialTheme.shapes.small
      )
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      }
    )
  }
}

/**
 * Upcoming transaction card
 */
@Composable
private fun UpcomingTransactionCard(
  transactionWithDetails: TransactionWithDetails
) {
  val transaction = transactionWithDetails.transaction
  val transactionType = transactionWithDetails.inferType()
  val primaryAccount = transactionWithDetails.getPrimaryAccount()

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    colors = CardDefaults.cardColors(
      containerColor = when (transaction.state) {
        TransactionState.PLANNED -> Color(0xFFE8F5E9)
        TransactionState.ESTIMATED -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.surface
      }
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .background(
                color = when (transactionType) {
                  TransactionType.INCOME -> Color(0xFF388E3C)
                  TransactionType.EXPENSE, TransactionType.LOAN_PAYMENT -> Color(0xFFF57C00)
                  else -> Color(0xFF1976D2)
                },
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (transactionType) {
                TransactionType.INCOME -> Icons.Default.Add
                TransactionType.EXPENSE, TransactionType.LOAN_PAYMENT -> Icons.Default.Remove
                else -> Icons.Default.Add
              },
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            text = transaction.payee ?: transactionType.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "${formatDate(transaction.transactionDate)} - ${primaryAccount.name}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (transaction.state == TransactionState.ESTIMATED) {
          Text(
            text = "Estimated Amount",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFF57C00)
          )
        }
      }

      Text(
        text = when (transactionType) {
          TransactionType.INCOME -> "+¥${formatAmount(transaction.amount)}"
          TransactionType.EXPENSE, TransactionType.LOAN_PAYMENT -> "-¥${formatAmount(transaction.amount)}"
          else -> "¥${formatAmount(transaction.amount)}"
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = when (transactionType) {
          TransactionType.INCOME -> Color(0xFF388E3C)
          TransactionType.EXPENSE, TransactionType.LOAN_PAYMENT -> Color(0xFFF57C00)
          else -> Color(0xFF1976D2)
        }
      )
    }
  }
}

/**
 * Balance trend data point
 */
data class BalanceDataPoint(
  val day: Int,
  val balance: Double
)

/**
 * Generate balance trend for future predictions
 * TODO: Needs refactoring for TransactionWithDetails model - currently disabled
 */
/*
private fun generateBalanceTrend(
  currentBalance: Double,
  upcomingTransactions: List<Transaction>,
  daysAhead: Int,
  accountId: Long?
): List<BalanceDataPoint> {
  val dataPoints = mutableListOf<BalanceDataPoint>()
  var balance = currentBalance

  dataPoints.add(BalanceDataPoint(0, balance))

  // Filter transactions by account if specified
  val filteredTransactions = upcomingTransactions.filter { transaction ->
    accountId == null || transaction.accountId == accountId || transaction.toAccountId == accountId
  }

  // Simulate daily balance changes
  for (day in 1..daysAhead) {
    // Check if there are any transactions on this day
    // For simplicity, we'll distribute transactions evenly
    val dailyTransactions = filteredTransactions.filter {
      val transactionDay = ((it.transactionDate - 1704067200000L) / (24 * 60 * 60 * 1000)).toInt()
      transactionDay == day
    }

    dailyTransactions.forEach { transaction ->
      when (transaction.type) {
        TransactionType.INCOME -> balance += transaction.amount
        TransactionType.EXPENSE, TransactionType.LOAN_PAYMENT -> balance -= transaction.amount
        else -> {}
      }
    }

    dataPoints.add(BalanceDataPoint(day, balance))
  }

  return dataPoints
}
*/

/**
 * Simple balance trend chart
 */
@Composable
fun BalanceTrendChart(
  data: List<BalanceDataPoint>,
  modifier: Modifier = Modifier
) {
  val primaryColor = MaterialTheme.colorScheme.primary

  Canvas(modifier = modifier) {
    if (data.size < 2) return@Canvas

    val width = size.width
    val height = size.height
    val padding = 40f

    // Find min and max values
    val minBalance = data.minOf { it.balance }
    val maxBalance = data.maxOf { it.balance }
    val balanceRange = maxBalance - minBalance
    val adjustedRange = if (balanceRange < 0.01) 1000.0 else balanceRange * 1.2

    // Draw axes
    drawLine(
      color = Color.Gray,
      start = Offset(padding, height - padding),
      end = Offset(width - padding, height - padding),
      strokeWidth = 2f
    )
    drawLine(
      color = Color.Gray,
      start = Offset(padding, padding),
      end = Offset(padding, height - padding),
      strokeWidth = 2f
    )

    // Draw line
    val path = Path()
    val xStep = (width - 2 * padding) / (data.size - 1)

    data.forEachIndexed { index, point ->
      val x = padding + index * xStep
      val normalizedY = if (adjustedRange > 0) {
        ((point.balance - (minBalance - adjustedRange * 0.1)) / adjustedRange).toFloat()
      } else {
        0.5f
      }
      val y = height - padding - normalizedY * (height - 2 * padding)

      if (index == 0) {
        path.moveTo(x, y)
      } else {
        path.lineTo(x, y)
      }

      // Draw point
      drawCircle(
        color = primaryColor,
        radius = 4f,
        center = Offset(x, y)
      )
    }

    // Draw the path
    drawPath(
      path = path,
      color = primaryColor,
      style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
  }
}

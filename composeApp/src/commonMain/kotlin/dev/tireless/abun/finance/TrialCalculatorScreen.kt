package dev.tireless.abun.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

private data class TrialCalculatorEntry(
  val id: Long,
  val isPositive: Boolean = true,
  val amount: String = "",
  val note: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialCalculatorScreen(
  navController: NavHostController,
) {
  val entries = remember {
    mutableStateListOf(
      TrialCalculatorEntry(id = 0),
      TrialCalculatorEntry(id = 1),
    )
  }
  var nextId by remember { mutableStateOf(2L) }

  val total = entries.fold(0.0) { acc, entry ->
    val raw = entry.amount.toDoubleOrNull() ?: 0.0
    acc + if (entry.isPositive) raw else -raw
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.List,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
              tint = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "Trial Calculator",
              style = MaterialTheme.typography.titleLarge,
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
          }
        },
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(innerPadding)
          .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
          ),
      ) {
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "Net cash flow: ¥${formatAmount(total)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color =
              if (total >= 0) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.error
              },
          )
          Text(
            text = "Use plus/minus to mark income or expense, and add new assumptions anytime.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      LazyColumn(
        modifier =
          Modifier
            .weight(1f)
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        var runningSum = 0.0
        itemsIndexed(
          items = entries,
          key = { _, entry -> entry.id },
        ) { index, entry ->
          val amountValue = entry.amount.toDoubleOrNull() ?: 0.0
          runningSum += if (entry.isPositive) amountValue else -amountValue

          val canMoveUp = index > 0
          val canMoveDown = index < entries.lastIndex

          TrialCalculatorRow(
            modifier = Modifier.fillMaxWidth(),
            entry = entry,
            subtotal = runningSum,
            canDelete = entries.size > 1,
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown,
            onToggleSign = { updated ->
              entries.updateEntry(entry.id) { it.copy(isPositive = updated) }
            },
            onAmountChange = { newAmount ->
              if (newAmount.isEmpty() || isValidAmountInput(newAmount)) {
                entries.updateEntry(entry.id) { it.copy(amount = newAmount) }
              }
            },
            onNoteChange = { note ->
              entries.updateEntry(entry.id) { it.copy(note = note) }
            },
            onAddBelow = {
              val newEntry = TrialCalculatorEntry(id = nextId++)
              val currentIndex = entries.indexOfEntry(entry.id)
              if (currentIndex >= 0) {
                entries.add(currentIndex + 1, newEntry)
              } else {
                entries.add(newEntry)
              }
            },
            onDelete = {
              if (entries.size > 1) {
                entries.removeAll { it.id == entry.id }
              }
            },
            onMoveUp = {
              val currentIndex = entries.indexOfEntry(entry.id)
              if (currentIndex > 0) {
                entries.move(currentIndex, currentIndex - 1)
              }
            },
            onMoveDown = {
              val currentIndex = entries.indexOfEntry(entry.id)
              if (currentIndex >= 0 && currentIndex < entries.lastIndex) {
                entries.move(currentIndex, currentIndex + 1)
              }
            },
          )
        }
      }

      TextButton(
        onClick = {
          val newEntry = TrialCalculatorEntry(id = nextId++)
          entries.add(newEntry)
        },
        modifier = Modifier.align(Alignment.CenterHorizontally),
      ) {
        Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = "Add row",
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text("Add new row")
      }
    }
  }
}

private fun MutableList<TrialCalculatorEntry>.updateEntry(
  id: Long,
  transform: (TrialCalculatorEntry) -> TrialCalculatorEntry,
) {
  val index = indexOfEntry(id)
  if (index >= 0) {
    this[index] = transform(this[index])
  }
}

private fun MutableList<TrialCalculatorEntry>.indexOfEntry(id: Long): Int =
  indexOfFirst { it.id == id }

private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
  if (fromIndex == toIndex || fromIndex !in indices || toIndex !in 0..size) return
  val item = removeAt(fromIndex)
  val target = if (toIndex > fromIndex) toIndex - 1 else toIndex
  add(target.coerceIn(0, size), item)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrialCalculatorRow(
  modifier: Modifier = Modifier,
  entry: TrialCalculatorEntry,
  subtotal: Double,
  canDelete: Boolean,
  canMoveUp: Boolean,
  canMoveDown: Boolean,
  onToggleSign: (Boolean) -> Unit,
  onAmountChange: (String) -> Unit,
  onNoteChange: (String) -> Unit,
  onAddBelow: () -> Unit,
  onDelete: () -> Unit,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
) {
  Card(
    modifier = modifier,
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          shape = CircleShape,
          color =
            if (entry.isPositive) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.errorContainer
            },
          tonalElevation = 0.dp,
          modifier =
            Modifier
              .size(40.dp)
              .background(Color.Transparent),
          onClick = { onToggleSign(!entry.isPositive) },
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = if (entry.isPositive) "+" else "-",
              style = MaterialTheme.typography.titleLarge,
              color =
                if (entry.isPositive) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.error
                },
            )
          }
        }

        OutlinedTextField(
          value = entry.amount,
          onValueChange = onAmountChange,
          modifier = Modifier.weight(1f),
          label = { Text("Amount (¥)") },
          singleLine = true,
          keyboardOptions =
            KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next,
            ),
        )

        IconButton(onClick = onAddBelow) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add row below",
          )
        }

        IconButton(
          onClick = onMoveUp,
          enabled = canMoveUp,
        ) {
          Icon(
            imageVector = Icons.Filled.ArrowUpward,
            contentDescription = "Move row up",
            tint =
              if (canMoveUp) {
                MaterialTheme.colorScheme.onSurfaceVariant
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
              },
          )
        }

        IconButton(
          onClick = onMoveDown,
          enabled = canMoveDown,
        ) {
          Icon(
            imageVector = Icons.Filled.ArrowDownward,
            contentDescription = "Move row down",
            tint =
              if (canMoveDown) {
                MaterialTheme.colorScheme.onSurfaceVariant
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
              },
          )
        }

        IconButton(
          onClick = onDelete,
          enabled = canDelete,
        ) {
          Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete row",
            tint =
              if (canDelete) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.3f,
              ),
          )
        }
      }

      OutlinedTextField(
        value = entry.note,
        onValueChange = onNoteChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Note") },
        singleLine = false,
        minLines = 1,
        maxLines = 3,
        keyboardOptions =
          KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
          ),
      )

      Text(
        text = "Running total: ¥${formatAmount(subtotal)}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End,
      )
    }
  }
}

package dev.tireless.abun.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialCalculatorScreen(
  navController: NavHostController,
  viewModel: TrialCalculatorViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()
  val entries = uiState.entries

  val total = entries.fold(0.0) { acc, entry ->
    val raw = entry.amount.toDoubleOrNull() ?: 0.0
    acc + if (entry.isPositive) raw else -raw
  }

  val canClear =
    remember(entries) {
      entries.size > TRIAL_CALCULATOR_DEFAULT_ENTRY_COUNT ||
        entries.any { entry ->
          entry.amount.isNotBlank() || entry.note.isNotBlank() || !entry.isPositive
        }
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
        actions = {
          TextButton(
            onClick = { viewModel.clearAll() },
            enabled = canClear,
          ) {
            Text("Clear")
          }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(innerPadding)
          .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
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
              .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.Center,
        ) {
          Text(
            text = "¥${formatAmount(total)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color =
              if (total >= 0) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.error
              },
          )
        }
      }

      LazyColumn(
        modifier =
          Modifier
            .weight(1f)
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        var runningSum = 0.0
        itemsIndexed(
          items = entries,
          key = { _, entry -> entry.id },
        ) { _, entry ->
          val amountValue = entry.amount.toDoubleOrNull() ?: 0.0
          runningSum += if (entry.isPositive) amountValue else -amountValue

          TrialCalculatorRow(
            modifier = Modifier.fillMaxWidth(),
            entry = entry,
            subtotal = runningSum,
            canDelete = entries.size > 1,
            onToggleSign = { updated -> viewModel.updateSign(entry.id, updated) },
            onAmountChange = { newAmount -> viewModel.updateAmount(entry.id, newAmount) },
            onNoteChange = { note -> viewModel.updateNote(entry.id, note) },
            onAddBelow = { viewModel.addEntryBelow(entry.id) },
            onDelete = { viewModel.deleteEntry(entry.id) },
            onMoveUp = { viewModel.moveEntryUp(entry.id) },
            onMoveDown = { viewModel.moveEntryDown(entry.id) },
          )
        }
      }

    }
  }
}

@Composable
private fun ActionIcon(
  imageVector: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  enabled: Boolean = true,
  tint: Color? = null,
) {
  val resolvedTint =
    tint ?: if (enabled) {
      MaterialTheme.colorScheme.onSurfaceVariant
    } else {
      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    }

  Icon(
    imageVector = imageVector,
    contentDescription = contentDescription,
    tint = resolvedTint,
    modifier =
      Modifier
        .size(24.dp)
        .clickable(enabled = enabled, onClick = onClick),
  )
}

@Composable
private fun DragHandle(
  enabled: Boolean,
  rowHeightPx: Float,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
) {
  var dragAccum by remember { mutableStateOf(0f) }
  val density = LocalDensity.current

  val dragThreshold =
    remember(rowHeightPx) {
      val minimum =
        with(density) {
          32.dp.toPx()
        }
      max(rowHeightPx * 0.25f, minimum)
    }

  val effectiveHeight = rowHeightPx.coerceAtLeast(1f)

  Icon(
    imageVector = Icons.Filled.DragHandle,
    contentDescription = "Reorder",
    tint = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier =
      Modifier
        .alpha(if (enabled) 1f else 0.3f)
        .size(32.dp)
        .pointerInput(enabled, effectiveHeight) {
          if (!enabled) return@pointerInput
          detectDragGestures(
            onDragStart = {
              dragAccum = 0f
            },
            onDragCancel = {
              dragAccum = 0f
            },
            onDragEnd = {
              dragAccum = 0f
            },
            onDrag = { change, dragAmount ->
              change.consumePositionChange()
              dragAccum += dragAmount.y
              while (dragAccum <= -dragThreshold) {
                onMoveUp()
                dragAccum += effectiveHeight
              }
              while (dragAccum >= dragThreshold) {
                onMoveDown()
                dragAccum -= effectiveHeight
              }
            },
          )
        },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrialCalculatorRow(
  modifier: Modifier = Modifier,
  entry: TrialCalculatorEntry,
  subtotal: Double,
  canDelete: Boolean,
  onToggleSign: (Boolean) -> Unit,
  onAmountChange: (String) -> Unit,
  onNoteChange: (String) -> Unit,
  onAddBelow: () -> Unit,
  onDelete: () -> Unit,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
) {
  var rowHeightPx by remember { mutableStateOf(1f) }

  Card(
    modifier = modifier,
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp)
          .onGloballyPositioned { coordinates ->
            rowHeightPx = coordinates.size.height.coerceAtLeast(1).toFloat()
          },
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        DragHandle(
          enabled = canDelete,
          rowHeightPx = rowHeightPx,
          onMoveUp = onMoveUp,
          onMoveDown = onMoveDown,
        )

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

        ActionIcon(
          imageVector = Icons.Filled.Add,
          contentDescription = "Add row below",
          onClick = onAddBelow,
          tint = MaterialTheme.colorScheme.primary,
        )

        ActionIcon(
          imageVector = Icons.Filled.Delete,
          contentDescription = "Delete row",
          enabled = canDelete,
          onClick = onDelete,
        )
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

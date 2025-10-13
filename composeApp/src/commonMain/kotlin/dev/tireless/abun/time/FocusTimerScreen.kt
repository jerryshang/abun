package dev.tireless.abun.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.CircleStop
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.X

@Composable
fun FocusTimerOverlay(
  state: FocusTimerUiState,
  tasks: List<Task>,
  onSelectTask: (Long?) -> Unit,
  onStart: () -> Unit,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onStop: () -> Unit,
  onDismiss: () -> Unit,
  onConfirmStop: (Task?, String?) -> Unit,
  onDismissStop: () -> Unit,
  onClearError: () -> Unit,
) {
  Dialog(
    onDismissRequest = {
      if (!state.isRunning && !state.isCompletingSession && state.stage == FocusStage.Idle) {
        onDismiss()
      }
    },
  ) {
    Card(
      modifier =
        Modifier
          .shadow(24.dp, MaterialTheme.shapes.extraLarge)
          .clip(MaterialTheme.shapes.extraLarge)
          .background(MaterialTheme.colorScheme.surface),
    ) {
      Column(
        modifier =
          Modifier
            .padding(horizontal = 28.dp, vertical = 24.dp)
            .width(360.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        HeaderSection(
          canDismiss = !state.isRunning && state.stage == FocusStage.Idle && !state.isCompletingSession,
          onDismiss = onDismiss,
        )

        if (state.errorMessage != null) {
          ErrorBanner(message = state.errorMessage, onDismiss = onClearError)
        }

        FocusTaskDropdown(
          tasks = tasks,
          selectedTaskId = state.selectedTaskId,
          enabled = !state.isRunning,
          onSelectTask = onSelectTask,
        )

        TimerDisplay(state = state)

        SessionSummary(state = state)

        Divider()

        ControlButtons(
          state = state,
          onStart = onStart,
          onPause = onPause,
          onResume = onResume,
          onStop = onStop,
        )
      }
    }
  }

  if (state.isStopDialogVisible) {
    FocusStopDialog(
      task = tasks.find { it.id == state.selectedTaskId },
      totalFocusMillis = state.totalFocusMillis,
      onConfirm = onConfirmStop,
      onDismiss = onDismissStop,
      isProcessing = state.isCompletingSession,
    )
  }
}

@Composable
private fun HeaderSection(
  canDismiss: Boolean,
  onDismiss: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column {
      Text(
        text = "Focus timer",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "Stay with one task at a time.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    IconButton(onClick = onDismiss, enabled = canDismiss) {
      Icon(
        imageVector = Lucide.X,
        contentDescription = "Close focus timer",
        tint = if (canDismiss) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun FocusTaskDropdown(
  tasks: List<Task>,
  selectedTaskId: Long?,
  enabled: Boolean,
  onSelectTask: (Long?) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedTask = tasks.find { it.id == selectedTaskId }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = "Task",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    FilledTonalButton(
      onClick = { if (enabled) expanded = true },
      enabled = enabled,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = selectedTask?.name ?: "No task selected",
            style = MaterialTheme.typography.bodyLarge,
          )
          selectedTask?.categoryName?.let { category ->
            Text(
              text = category,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Icon(
          imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown,
          contentDescription = null,
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      DropdownItem(
        title = "No task",
        subtitle = "Log focus without a task",
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = {
          onSelectTask(null)
          expanded = false
        },
      )
      tasks.forEach { task ->
        DropdownItem(
          title = task.name,
          subtitle = task.categoryName,
          color = parseCategoryColor(task.categoryColor) ?: MaterialTheme.colorScheme.primary,
          onClick = {
            onSelectTask(task.id)
            expanded = false
          },
        )
      }
    }
  }
}

@Composable
private fun TimerDisplay(state: FocusTimerUiState) {
  val progress =
    if (state.phaseDurationMillis == 0L) {
      0f
    } else {
      1f - (state.remainingMillis.toFloat() / state.phaseDurationMillis.toFloat())
    }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = when (state.stage) {
        FocusStage.Idle -> "Ready"
        FocusStage.Focus -> "Focus"
        FocusStage.ShortBreak -> "Short break"
        FocusStage.LongBreak -> "Long break"
      },
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary,
    )

    Box(contentAlignment = Alignment.Center) {
      CircularProgressIndicator(
        progress = progress.coerceIn(0f, 1f),
        modifier = Modifier.size(200.dp),
        strokeWidth = 10.dp,
      )
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = formatMillis(state.remainingMillis),
          style = MaterialTheme.typography.displaySmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = if (state.stage == FocusStage.Focus) "Stay on task" else "Recover and reset",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun SessionSummary(state: FocusTimerUiState) {
  Card(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "Focus completed",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = state.completedFocusSessions.toString(),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "Focus time",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = formatMillis(state.totalFocusMillis),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun ControlButtons(
  state: FocusTimerUiState,
  onStart: () -> Unit,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onStop: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    when {
      state.stage == FocusStage.Idle -> {
        Button(
          onClick = onStart,
          modifier = Modifier.weight(1f),
          enabled = !state.isCompletingSession,
        ) {
          Icon(Lucide.Timer, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Start")
        }
      }
      state.isRunning -> {
        FilledTonalButton(
          onClick = onPause,
          modifier = Modifier.weight(1f),
          enabled = !state.isCompletingSession,
        ) {
          Icon(Lucide.Pause, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Pause")
        }

        Button(
          onClick = onStop,
          modifier = Modifier.weight(1f),
          enabled = !state.isCompletingSession,
        ) {
          Icon(Lucide.CircleStop, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Stop")
        }
      }
      else -> {
        Button(
          onClick = onResume,
          modifier = Modifier.weight(1f),
          enabled = !state.isCompletingSession,
        ) {
          Icon(Lucide.Play, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Resume")
        }

        FilledTonalButton(
          onClick = onStop,
          modifier = Modifier.weight(1f),
          enabled = !state.isCompletingSession,
        ) {
          Icon(Lucide.CircleStop, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Stop")
        }
      }
    }
  }
}

@Composable
private fun FocusStopDialog(
  task: Task?,
  totalFocusMillis: Long,
  onConfirm: (Task?, String?) -> Unit,
  onDismiss: () -> Unit,
  isProcessing: Boolean,
) {
  val strategies = listOf("plan", "todo", "do", "check")
  var selectedStrategy by remember(task) { mutableStateOf(task?.strategy ?: strategies.first()) }

  AlertDialog(
    onDismissRequest = { if (!isProcessing) onDismiss() },
    confirmButton = {
      Button(
        onClick = { onConfirm(task, selectedStrategy.takeIf { task != null }) },
        enabled = !isProcessing,
      ) {
        Text(if (isProcessing) "Saving…" else "Stop timer")
      }
    },
    dismissButton = {
      TextButton(onClick = { if (!isProcessing) onDismiss() }, enabled = !isProcessing) {
        Text("Cancel")
      }
    },
    title = { Text("Stop focus session") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          text = "You've focused for ${formatMillis(totalFocusMillis)} in this session.",
          style = MaterialTheme.typography.bodyMedium,
        )

        if (task == null) {
          Text(
            text = "No task was selected. We'll end the timer without logging a timeblock.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Update ${task.name} status",
              style = MaterialTheme.typography.labelLarge,
            )

            StrategyPicker(
              strategies = strategies,
              selected = selectedStrategy,
              onSelect = { selectedStrategy = it },
            )
          }
        }
      }
    },
  )
}

@Composable
private fun StrategyPicker(
  strategies: List<String>,
  selected: String,
  onSelect: (String) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    strategies.forEach { strategy ->
      val isSelected = strategy == selected
      FilledTonalButton(
        onClick = { onSelect(strategy) },
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor =
            if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(strategy.uppercase())
          if (isSelected) {
            Text(
              text = "Selected",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ErrorBanner(
  message: String,
  onDismiss: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.errorContainer,
      contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
      )
      TextButton(onClick = onDismiss) {
        Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
      }
    }
  }
}

@Composable
private fun DropdownItem(
  title: String,
  subtitle: String?,
  color: Color,
  onClick: () -> Unit,
) {
  DropdownMenuItem(
    text = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(18.dp)
              .background(color = color, shape = MaterialTheme.shapes.small),
        )
        Column {
          Text(title, style = MaterialTheme.typography.bodyMedium)
          subtitle?.let {
            Text(
              text = it,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    onClick = onClick,
  )
}

private fun formatMillis(millis: Long): String {
  val totalSeconds = millis / 1000
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  val minuteText = minutes.toString().padStart(2, '0')
  val secondText = seconds.toString().padStart(2, '0')
  return "$minuteText:$secondText"
}

private fun parseCategoryColor(hex: String?): Color? {
  if (hex.isNullOrBlank()) return null
  return try {
    val sanitized = hex.removePrefix("#")
    val colorValue = sanitized.toLong(16)
    val argb =
      when (sanitized.length) {
        6 -> 0xFF000000L or colorValue
        8 -> colorValue
        else -> return null
      }
    Color(argb)
  } catch (_: Exception) {
    null
  }
}

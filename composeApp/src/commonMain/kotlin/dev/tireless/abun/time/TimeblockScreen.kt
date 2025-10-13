package dev.tireless.abun.time

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.tireless.abun.tasks.TaskDashboardScreen
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import org.koin.compose.viewmodel.koinViewModel
import dev.tireless.abun.core.time.currentEpochMillis
import kotlin.time.ExperimentalTime
import dev.tireless.abun.navigation.Route
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Timer

data class TimeSlot(
  val hour: Int,
  val minute: Int,
  val displayTime: String,
  val timeblock: TimeblockDisplayData? = null
)

data class TimeblockDisplayData(
  val id: Long,
  val taskName: String,
  val taskStrategy: String,
  val parentTaskName: String?,
  val isFuture: Boolean
)

enum class ZoomLevel {
  FULL_DAY,
  FOUR_HOURS
}

private enum class TimeWorkspaceTab(val label: String) {
  Schedule("Schedule"),
  Tasks("Tasks"),
  Review("Review")
}

@Composable
fun TimeWorkspaceScreen(navController: NavHostController) {
  val timeblockViewModel: TimeblockViewModel = koinViewModel()
  val focusTimerViewModel: FocusTimerViewModel = koinViewModel()
  var currentTab by rememberSaveable { mutableStateOf(TimeWorkspaceTab.Schedule) }
  var selectedDate by rememberSaveable { mutableStateOf("2024-01-01") }
  var showFocusTimer by remember { mutableStateOf(false) }

  val timeblocks by timeblockViewModel.timeblocks.collectAsState()
  val tasks by timeblockViewModel.tasks.collectAsState()
  val focusState by focusTimerViewModel.uiState.collectAsState()

  LaunchedEffect(selectedDate) {
    timeblockViewModel.loadTimeblocks(selectedDate)
  }

  Scaffold(
    modifier =
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
    topBar = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
      ) {
        Text(
          text = "Time",
          style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Start with your schedule, then focus on tasks.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        TabRow(selectedTabIndex = currentTab.ordinal) {
          TimeWorkspaceTab.values().forEach { tab ->
            Tab(
              selected = tab == currentTab,
              onClick = { currentTab = tab },
              text = { Text(tab.label) },
            )
          }
        }
      }
    },
    floatingActionButton = {
      FocusActionButton(onStartFocus = { showFocusTimer = true })
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
    ) {
      when (currentTab) {
        TimeWorkspaceTab.Schedule ->
          TimeblockPlanner(
            modifier = Modifier.fillMaxSize(),
            selectedDate = selectedDate,
            timeblocks = timeblocks,
            viewModel = timeblockViewModel,
            onManageTasks = { navController.navigate(Route.TimeTaskManagement) },
          )
        TimeWorkspaceTab.Tasks ->
          TaskDashboardScreen(
            navController = null,
            modifier = Modifier.fillMaxSize(),
            embedded = true,
          )
        TimeWorkspaceTab.Review ->
          TimeReviewPanel(
            modifier = Modifier.fillMaxSize(),
            selectedDate = selectedDate,
            timeblocks = timeblocks,
          )
      }
    }
  }

  val shouldShowTimer =
    showFocusTimer || focusState.stage != FocusStage.Idle || focusState.isStopDialogVisible

  if (shouldShowTimer) {
    FocusTimerOverlay(
      state = focusState,
      tasks = tasks,
      onSelectTask = focusTimerViewModel::selectTask,
      onStart = focusTimerViewModel::startTimer,
      onPause = focusTimerViewModel::pauseTimer,
      onResume = focusTimerViewModel::resumeTimer,
      onStop = focusTimerViewModel::showStopDialog,
      onDismiss = { showFocusTimer = false },
      onConfirmStop = { task, strategy -> focusTimerViewModel.confirmStop(strategy, task) },
      onDismissStop = focusTimerViewModel::dismissStopDialog,
      onClearError = focusTimerViewModel::clearError,
    )
  }
}

@Composable
private fun TimeblockPlanner(
  modifier: Modifier = Modifier,
  selectedDate: String,
  timeblocks: List<Timeblock>,
  viewModel: TimeblockViewModel,
  onManageTasks: () -> Unit,
) {
  var zoomLevel by remember { mutableStateOf(ZoomLevel.FULL_DAY) }
  var showCreateDialog by remember { mutableStateOf(false) }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
  ) {
    Text(
      text = "Timeblock Planning",
      style = MaterialTheme.typography.titleLarge,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = selectedDate,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(20.dp))

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (zoomLevel == ZoomLevel.FULL_DAY) {
          FilledTonalButton(onClick = { zoomLevel = ZoomLevel.FULL_DAY }) {
            Icon(Lucide.ListChecks, contentDescription = "Full day view")
          }
        } else {
          OutlinedButton(onClick = { zoomLevel = ZoomLevel.FULL_DAY }) {
            Icon(Lucide.ListChecks, contentDescription = "Full day view")
          }
        }

        if (zoomLevel == ZoomLevel.FOUR_HOURS) {
          FilledTonalButton(onClick = { zoomLevel = ZoomLevel.FOUR_HOURS }) {
            Icon(Lucide.Clock, contentDescription = "Four hour view")
          }
        } else {
          OutlinedButton(onClick = { zoomLevel = ZoomLevel.FOUR_HOURS }) {
            Icon(Lucide.Clock, contentDescription = "Four hour view")
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      OutlinedButton(onClick = { /* TODO: Show date picker */ }) {
        Icon(Lucide.Calendar, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(selectedDate)
      }

      Button(onClick = { showCreateDialog = true }) {
        Icon(Lucide.Plus, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Create")
      }

      OutlinedButton(onClick = onManageTasks) {
        Text("Task Hierarchy")
      }
    }

    TimeSlotsList(
      selectedDate = selectedDate,
      zoomLevel = zoomLevel,
      timeblocks = timeblocks,
      modifier = Modifier.weight(1f),
    )
  }

  if (showCreateDialog) {
    CreateTimeblockDialog(
      viewModel = viewModel,
      onDismiss = { showCreateDialog = false },
      selectedDate = selectedDate,
    )
  }
}

@Composable
private fun FocusActionButton(onStartFocus: () -> Unit) {
  ExtendedFloatingActionButton(
    onClick = onStartFocus,
    icon = { Icon(Lucide.Timer, contentDescription = "Start focus session") },
    text = { Text("Focus") },
  )
}

@Composable
private fun TimeReviewPanel(
  modifier: Modifier = Modifier,
  selectedDate: String,
  timeblocks: List<Timeblock>,
) {
  val totalMinutes = remember(timeblocks) { timeblocks.sumOf { it.durationMinutes() } }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Daily review", style = MaterialTheme.typography.titleLarge)
        Text(
          text = selectedDate,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    item {
      ReviewSummaryCard(totalMinutes = totalMinutes, blockCount = timeblocks.size)
    }

    if (timeblocks.isEmpty()) {
      item {
        Text(
          text = "No timeblocks scheduled yet. Capture tasks, then block time to see your day come together.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      item {
        Text("Recent timeblocks", style = MaterialTheme.typography.titleMedium)
      }
      items(timeblocks, key = { it.id }) { block ->
        ReviewTimeblockCard(timeblock = block)
      }
    }
  }
}

@Composable
private fun ReviewSummaryCard(
  totalMinutes: Long,
  blockCount: Int,
) {
  Card {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = "Scheduled time",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = formatDuration(totalMinutes),
        style = MaterialTheme.typography.headlineSmall,
      )
      Text(
        text = "$blockCount timeblocks planned",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun ReviewTimeblockCard(timeblock: Timeblock) {
  Card {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = timeblock.taskName ?: "Unassigned task",
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text =
          buildString {
            append(formatTimeRange(timeblock.startTime, timeblock.endTime))
            timeblock.taskStrategy?.let {
              append(" • ")
              append(it.uppercase())
            }
          },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      timeblock.taskParentName?.let { parentName ->
        Text(
          text = "Parent: $parentName",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@OptIn(ExperimentalTime::class)
private fun Timeblock.durationMinutes(): Long {
  return runCatching {
    val start =
      LocalDateTime
        .parse(startTime)
        .toInstant(TimeZone.currentSystemDefault())
    val end =
      LocalDateTime
        .parse(endTime)
        .toInstant(TimeZone.currentSystemDefault())
    (end - start).inWholeMinutes
  }.getOrDefault(0L)
}

private fun formatDuration(totalMinutes: Long): String {
  if (totalMinutes <= 0) return "0m"
  val hours = totalMinutes / 60
  val minutes = totalMinutes % 60
  return when {
    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
    hours > 0 -> "${hours}h"
    else -> "${minutes}m"
  }
}

private fun formatTimeRange(
  startIso: String,
  endIso: String,
): String {
  return runCatching {
    val start = LocalDateTime.parse(startIso).time
    val end = LocalDateTime.parse(endIso).time
    "${start.toDisplayString()} - ${end.toDisplayString()}"
  }.getOrDefault("—")
}

private fun LocalTime.toDisplayString(): String {
  val hourText = hour.toString().padStart(2, '0')
  val minuteText = minute.toString().padStart(2, '0')
  return "$hourText:$minuteText"
}

@Composable
private fun TimeSlotsList(
  selectedDate: String,
  zoomLevel: ZoomLevel,
  timeblocks: List<Timeblock>,
  modifier: Modifier = Modifier
) {
  val timeSlots = remember(zoomLevel, timeblocks) {
    generateTimeSlots(zoomLevel, timeblocks, selectedDate)
  }

  LazyColumn(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    items(timeSlots) { timeSlot ->
      TimeSlotRow(
        timeSlot = timeSlot,
        zoomLevel = zoomLevel
      )
    }
  }
}

@Composable
private fun TimeSlotRow(
  timeSlot: TimeSlot,
  zoomLevel: ZoomLevel
) {
  // Calculate height based on zoom level
  // 4 hours mode: 1/8 of container height (8 slots * 2 = 16 half-hour slots, so larger height)
  // Full day mode: smaller height to fit all 48 half-hour slots
  val rowHeight = when (zoomLevel) {
    ZoomLevel.FOUR_HOURS -> 80.dp // 1/8 of container height for better visibility
    ZoomLevel.FULL_DAY -> 32.dp // Smaller height to fit more slots
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(rowHeight)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Time display
      Text(
        text = timeSlot.displayTime,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(80.dp),
        textAlign = TextAlign.Start
      )

      // Timeblock content
      val contentHeight = when (zoomLevel) {
        ZoomLevel.FOUR_HOURS -> 60.dp
        ZoomLevel.FULL_DAY -> 20.dp
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .height(contentHeight)
          .background(
            color = timeSlot.timeblock?.let {
              getStrategyColor(it.taskStrategy).copy(alpha = if (it.isFuture) 0.5f else 1.0f)
            } ?: Color.Transparent,
            shape = MaterialTheme.shapes.small
          )
          .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        timeSlot.timeblock?.let { timeblock ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Strategy indicator
            Box(
              modifier = Modifier
                .background(
                  color = getStrategyColor(timeblock.taskStrategy),
                  shape = MaterialTheme.shapes.extraSmall
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
              Text(
                text = timeblock.taskStrategy.uppercase().take(4),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = if (zoomLevel == ZoomLevel.FOUR_HOURS) 10.sp else 8.sp
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Task name
            Text(
              text = timeblock.taskName,
              style = if (zoomLevel == ZoomLevel.FOUR_HOURS) {
                MaterialTheme.typography.bodyMedium
              } else {
                MaterialTheme.typography.bodySmall
              },
              color = Color.White,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )

            timeblock.parentTaskName?.let { parentName ->
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = parentName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

private fun generateTimeSlots(
  zoomLevel: ZoomLevel,
  timeblocks: List<Timeblock>,
  selectedDate: String
): List<TimeSlot> {
  val slots = mutableListOf<TimeSlot>()

  val (startHour, endHour) = when (zoomLevel) {
    ZoomLevel.FULL_DAY -> 0 to 24
    ZoomLevel.FOUR_HOURS -> 9 to 13 // 9 AM to 1 PM as example
  }

  for (hour in startHour until endHour) {
    for (minute in listOf(0, 30)) {
      val displayTime = formatTime(hour, minute)
      val currentTime = "${selectedDate}T$displayTime:00"

      // Find timeblock for this slot
      val timeblock = timeblocks.find { tb ->
        tb.startTime <= currentTime && currentTime < tb.endTime
      }

      val timeblockData = timeblock?.let {
        TimeblockDisplayData(
          id = it.id,
          taskName = it.taskName ?: "Unknown Task",
          taskStrategy = it.taskStrategy ?: "plan",
          parentTaskName = it.taskParentName,
          isFuture = isTimeInFuture(currentTime)
        )
      }

      slots.add(
        TimeSlot(
          hour = hour,
          minute = minute,
          displayTime = displayTime,
          timeblock = timeblockData
        )
      )
    }
  }

  return slots
}

@OptIn(ExperimentalTime::class)
private fun isTimeInFuture(timeString: String): Boolean {
  return try {
    val targetMillis =
      LocalDateTime
        .parse(timeString)
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
    val currentMillis = currentEpochMillis()
    targetMillis > currentMillis
  } catch (_: IllegalArgumentException) {
    false
  }
}

private fun formatTime(hour: Int, minute: Int): String {
  val hourStr = if (hour < 10) "0$hour" else hour.toString()
  val minuteStr = if (minute < 10) "0$minute" else minute.toString()
  return "$hourStr:$minuteStr"
}

private fun getStrategyColor(strategy: String): Color = when (strategy.lowercase()) {
  "plan" -> Color(0xFF2196F3) // Blue
  "todo" -> Color(0xFFFF9800) // Orange
  "do" -> Color(0xFF4CAF50) // Green
  "check" -> Color(0xFF9C27B0) // Purple
  else -> Color(0xFF607D8B) // Blue Grey
}

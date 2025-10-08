package dev.tireless.abun.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import kotlinx.datetime.toInstant
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import dev.tireless.abun.navigation.Route

data class TimeSlot(
  val hour: Int,
  val minute: Int,
  val displayTime: String,
  val timeblock: TimeblockDisplayData? = null
)

data class TimeblockDisplayData(
  val id: Long,
  val taskName: String,
  val categoryColor: String,
  val taskStrategy: String,
  val isFuture: Boolean
)

enum class ZoomLevel {
  FULL_DAY,
  FOUR_HOURS
}

private enum class TimeWorkspacePage {
  Timeblocks,
  Tasks
}

@Composable
fun TimeWorkspaceScreen(navController: NavHostController) {
  var currentPage by rememberSaveable { mutableStateOf(TimeWorkspacePage.Timeblocks) }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (currentPage == TimeWorkspacePage.Timeblocks) {
          FilledTonalButton(
            onClick = { currentPage = TimeWorkspacePage.Timeblocks },
            modifier = Modifier.weight(1f),
          ) {
            Text("Timeblocks")
          }
          OutlinedButton(
            onClick = { currentPage = TimeWorkspacePage.Tasks },
            modifier = Modifier.weight(1f),
          ) {
            Text("Tasks")
          }
        } else {
          OutlinedButton(
            onClick = { currentPage = TimeWorkspacePage.Timeblocks },
            modifier = Modifier.weight(1f),
          ) {
            Text("Timeblocks")
          }
          FilledTonalButton(
            onClick = { currentPage = TimeWorkspacePage.Tasks },
            modifier = Modifier.weight(1f),
          ) {
            Text("Tasks")
          }
        }

        if (currentPage == TimeWorkspacePage.Timeblocks) {
          OutlinedButton(onClick = { navController.navigate(Route.TimeCategoryManagement) }) {
            Text("Categories")
          }
        }
      }
    }

    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .weight(1f),
    ) {
      when (currentPage) {
        TimeWorkspacePage.Timeblocks ->
          TimeblockPlanner(
            modifier = Modifier.fillMaxSize(),
          )
        TimeWorkspacePage.Tasks ->
          TaskDashboardScreen(
            navController = null,
            modifier = Modifier.fillMaxSize(),
            embedded = true,
          )
      }
    }
  }
}

@Composable
private fun TimeblockPlanner(modifier: Modifier = Modifier) {
  val viewModel: TimeblockViewModel = koinViewModel()
  val timeblocks by viewModel.timeblocks.collectAsState()

  var selectedDate by remember { mutableStateOf("2024-01-01") }
  var zoomLevel by remember { mutableStateOf(ZoomLevel.FULL_DAY) }
  var showCreateDialog by remember { mutableStateOf(false) }

  LaunchedEffect(selectedDate) {
    viewModel.loadTimeblocks(selectedDate)
  }

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
            Icon(Icons.Default.ViewDay, contentDescription = "Full day view")
          }
        } else {
          OutlinedButton(onClick = { zoomLevel = ZoomLevel.FULL_DAY }) {
            Icon(Icons.Default.ViewDay, contentDescription = "Full day view")
          }
        }

        if (zoomLevel == ZoomLevel.FOUR_HOURS) {
          FilledTonalButton(onClick = { zoomLevel = ZoomLevel.FOUR_HOURS }) {
            Icon(Icons.Default.ViewModule, contentDescription = "Four hour view")
          }
        } else {
          OutlinedButton(onClick = { zoomLevel = ZoomLevel.FOUR_HOURS }) {
            Icon(Icons.Default.ViewModule, contentDescription = "Four hour view")
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      OutlinedButton(onClick = { /* TODO: Show date picker */ }) {
        Icon(Icons.Default.DateRange, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(selectedDate)
      }

      Button(onClick = { showCreateDialog = true }) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Create")
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
              try {
                val colorLong = parseHexColor(it.categoryColor)
                Color(colorLong).copy(alpha = if (it.isFuture) 0.5f else 1.0f)
              } catch (e: Exception) {
                Color.Gray.copy(alpha = if (it.isFuture) 0.5f else 1.0f)
              }
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
          categoryColor = it.categoryColor ?: "#808080",
          taskStrategy = it.taskStrategy ?: "plan",
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
    val currentMillis = Clock.System.now().toEpochMilliseconds()
    targetMillis > currentMillis
  } catch (_: IllegalArgumentException) {
    false
  }
}

private fun parseHexColor(hexColor: String): Long {
  val cleanHex = hexColor.removePrefix("#")
  return when (cleanHex.length) {
    6 -> (0xFF000000 or cleanHex.toLong(16))
    8 -> cleanHex.toLong(16)
    else -> 0xFF808080 // Default gray
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

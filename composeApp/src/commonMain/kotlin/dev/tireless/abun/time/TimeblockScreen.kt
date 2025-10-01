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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tireless.abun.time.Timeblock
import dev.tireless.abun.time.TimeblockViewModel
import org.koin.compose.viewmodel.koinViewModel

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
    FULL_DAY, FOUR_HOURS
}

@Composable
fun TimeblockScreen() {
    val viewModel: TimeblockViewModel = koinViewModel()
    val timeblocks by viewModel.timeblocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedDate by remember { mutableStateOf("2024-01-01") }
    var zoomLevel by remember { mutableStateOf(ZoomLevel.FULL_DAY) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Load timeblocks when date changes
    LaunchedEffect(selectedDate) {
        viewModel.loadTimeblocks(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Timeblock Planning",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Function bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zoom switcher - Icon only buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Full Day button
                if (zoomLevel == ZoomLevel.FULL_DAY) {
                    FilledTonalButton(
                        onClick = { zoomLevel = ZoomLevel.FULL_DAY }
                    ) {
                        Icon(
                            Icons.Default.ViewDay,
                            contentDescription = "Full Day View"
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { zoomLevel = ZoomLevel.FULL_DAY }
                    ) {
                        Icon(
                            Icons.Default.ViewDay,
                            contentDescription = "Full Day View"
                        )
                    }
                }

                // 4 Hours button
                if (zoomLevel == ZoomLevel.FOUR_HOURS) {
                    FilledTonalButton(
                        onClick = { zoomLevel = ZoomLevel.FOUR_HOURS }
                    ) {
                        Icon(
                            Icons.Default.ViewModule,
                            contentDescription = "4 Hours View"
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { zoomLevel = ZoomLevel.FOUR_HOURS }
                    ) {
                        Icon(
                            Icons.Default.ViewModule,
                            contentDescription = "4 Hours View"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Date picker
            OutlinedButton(
                onClick = { /* TODO: Show date picker */ }
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(selectedDate)
            }

            // Create timeblock
            Button(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create")
            }
        }

        // Time slots list
        TimeSlotsList(
            selectedDate = selectedDate,
            zoomLevel = zoomLevel,
            timeblocks = timeblocks,
            modifier = Modifier.weight(1f)
        )
    }

    // Create timeblock dialog
    if (showCreateDialog) {
        CreateTimeblockDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
            selectedDate = selectedDate
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
        ZoomLevel.FULL_DAY -> 32.dp   // Smaller height to fit more slots
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
                            style = if (zoomLevel == ZoomLevel.FOUR_HOURS)
                                MaterialTheme.typography.bodyMedium
                            else
                                MaterialTheme.typography.bodySmall,
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
            val currentTime = "${selectedDate}T${displayTime}:00"

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

private fun isTimeInFuture(timeString: String): Boolean {
    // Simplified future check - in real app, compare with current time
    return timeString > "2024-01-01T12:00:00"
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

private fun getStrategyColor(strategy: String): Color {
    return when (strategy.lowercase()) {
        "plan" -> Color(0xFF2196F3) // Blue
        "todo" -> Color(0xFFFF9800) // Orange
        "do" -> Color(0xFF4CAF50) // Green
        "check" -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF607D8B) // Blue Grey
    }
}
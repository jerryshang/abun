package dev.tireless.abun.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tireless.abun.tags.Tag
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskDashboardScreen(
  navController: NavHostController? = null,
  modifier: Modifier = Modifier,
  embedded: Boolean = false,
  onClose: (() -> Unit)? = null,
  viewModel: TaskBoardViewModel = koinViewModel(),
) {
  val selectedDate by viewModel.selectedDate.collectAsState()
  val todayNodes by viewModel.todayTasks.collectAsState()
  val upcomingNodes by viewModel.upcomingTasks.collectAsState()
  val allTasks by viewModel.allTasks.collectAsState()
  val tags by viewModel.tags.collectAsState()

  var currentTab by rememberSaveable { mutableStateOf(TaskPage.Today) }
  var showEditor by remember { mutableStateOf(false) }
  var editingTaskId by remember { mutableStateOf<Long?>(null) }
  var parentForNew by remember { mutableStateOf<Long?>(null) }
  var showStateDialog by remember { mutableStateOf(false) }
  var pendingAction by remember { mutableStateOf<TaskActionRequest?>(null) }

  Surface(modifier = modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize()) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
      ) {
      if (embedded) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Tasks", style = MaterialTheme.typography.titleLarge)
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { viewModel.selectDate(selectedDate.minus(1, DateTimeUnit.DAY)) }) {
              Icon(Lucide.ChevronLeft, contentDescription = "Previous day")
            }
            IconButton(onClick = { viewModel.selectDate(selectedDate.plus(1, DateTimeUnit.DAY)) }) {
              Icon(Lucide.ChevronRight, contentDescription = "Next day")
            }
          }
        }
      } else {
        CenterAlignedTopAppBar(
          title = { Text("Tasks") },
          navigationIcon = {
            when {
              navController != null ->
                IconButton(onClick = { navController.navigateUp() }) {
                  Icon(Lucide.Calendar, contentDescription = "Back to schedule")
                }
              onClose != null ->
                IconButton(onClick = onClose) {
                  Icon(Lucide.Calendar, contentDescription = "Close tasks")
                }
            }
          },
          actions = {
            IconButton(onClick = { viewModel.selectDate(selectedDate.minus(1, DateTimeUnit.DAY)) }) {
              Icon(Lucide.ChevronLeft, contentDescription = "Previous day")
            }
            IconButton(onClick = { viewModel.selectDate(selectedDate.plus(1, DateTimeUnit.DAY)) }) {
              Icon(Lucide.ChevronRight, contentDescription = "Next day")
            }
          },
        )
      }

      Text(
        text = formatDate(selectedDate),
        style = MaterialTheme.typography.titleLarge,
        modifier =
          Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp),
      )

      Row(
        modifier =
          Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AssistChip(
          onClick = { currentTab = TaskPage.Today },
          label = { Text("Today") },
          leadingIcon = { Icon(Lucide.ListChecks, contentDescription = null) },
          colors = AssistChipDefaults.assistChipColors(containerColor = if (currentTab == TaskPage.Today) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
        )
        AssistChip(
          onClick = { currentTab = TaskPage.Upcoming },
          label = { Text("Upcoming") },
          leadingIcon = { Icon(Lucide.Calendar, contentDescription = null) },
          colors = AssistChipDefaults.assistChipColors(containerColor = if (currentTab == TaskPage.Upcoming) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
        )
      }

        val list = if (currentTab == TaskPage.Today) todayNodes else upcomingNodes
        TaskHierarchyList(
          nodes = list,
          tagLookup = tags.associateBy { it.id },
          onEdit = { taskId ->
            editingTaskId = taskId
            parentForNew = null
            showEditor = true
          },
          onAddChild = { parentId ->
            editingTaskId = null
            parentForNew = parentId
            showEditor = true
          },
          onStateChange = { request ->
            pendingAction = request
            showStateDialog = true
          },
          onDelete = { viewModel.deleteTask(it) },
        )
      }

      FloatingActionButton(
        onClick = {
          editingTaskId = null
          parentForNew = null
          showEditor = true
        },
        modifier =
          Modifier
            .align(Alignment.BottomEnd)
            .padding(24.dp),
      ) {
        Icon(Lucide.Plus, contentDescription = "Add task")
      }
    }

    if (showEditor) {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ModalBottomSheet(
        onDismissRequest = {
          showEditor = false
          editingTaskId = null
          parentForNew = null
        },
        sheetState = sheetState,
      ) {
        val existingTask = editingTaskId?.let { id -> allTasks.find { it.id == id } }
        TaskEditorSheet(
          allTasks = allTasks,
          tags = tags,
          existing = existingTask,
          parentPrefill = parentForNew,
          onDismiss = {
            showEditor = false
            editingTaskId = null
            parentForNew = null
          },
          onSubmit = { draft ->
            val adjustedDraft = if (parentForNew != null) draft.copy(parentId = parentForNew) else draft
            viewModel.createTask(adjustedDraft)
            showEditor = false
            editingTaskId = null
            parentForNew = null
          },
          onUpdate = { update ->
            viewModel.updateTask(update)
            showEditor = false
            editingTaskId = null
            parentForNew = null
          },
          defaultDate = selectedDate,
          defaultTime = viewModel.defaultStartSlot(),
        )
      }
    }

    if (showStateDialog) {
      val action = pendingAction
      if (action != null) {
        TaskStateDialog(
          task = action.task,
          targetState = action.targetState,
          onDismiss = {
            showStateDialog = false
            pendingAction = null
          },
          onConfirm = { input ->
            viewModel.changeTaskState(action.task.id, action.targetState, input)
            showStateDialog = false
            pendingAction = null
          },
        )
      } else {
        showStateDialog = false
      }
    }
  }
}

private enum class TaskPage { Today, Upcoming }

private data class TaskActionRequest(
  val task: Task,
  val targetState: TaskState,
)

@Composable
private fun TaskHierarchyList(
  nodes: List<TaskNode>,
  tagLookup: Map<Long, Tag>,
  onEdit: (Long) -> Unit,
  onAddChild: (Long) -> Unit,
  onStateChange: (TaskActionRequest) -> Unit,
  onDelete: (Long) -> Unit,
) {
  val flattened = remember(nodes) { nodes.flattenHierarchy() }
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(flattened) { node ->
      TaskNodeCard(
        node = node,
        tagLookup = tagLookup,
        onEdit = onEdit,
        onAddChild = onAddChild,
        onStateChange = onStateChange,
        onDelete = onDelete,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskNodeCard(
  node: TaskNode,
  tagLookup: Map<Long, Tag>,
  onEdit: (Long) -> Unit,
  onAddChild: (Long) -> Unit,
  onStateChange: (TaskActionRequest) -> Unit,
  onDelete: (Long) -> Unit,
) {
  val task = node.task
  var menuExpanded by remember { mutableStateOf(false) }

  OutlinedCard(
    modifier =
      Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .padding(start = (node.depth * 12).dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(Modifier.weight(1f)) {
          Text(task.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
          Text(buildTaskSubtitle(task), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TaskStateChip(task.state)
        IconButton(onClick = { menuExpanded = true }) {
        Icon(Icons.Outlined.MoreVert, contentDescription = "Task actions")
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
          DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Lucide.Pencil, contentDescription = null) }, onClick = {
            menuExpanded = false
            onEdit(task.id)
          })
          DropdownMenuItem(text = { Text("Add subtask") }, leadingIcon = { Icon(Lucide.Plus, contentDescription = null) }, onClick = {
            menuExpanded = false
            onAddChild(task.id)
          })
          DropdownMenuItem(text = { Text("Mark in progress") }, leadingIcon = { Icon(Lucide.Play, contentDescription = null) }, onClick = {
            menuExpanded = false
            onStateChange(TaskActionRequest(task, TaskState.InProgress))
          })
          DropdownMenuItem(text = { Text("Mark done") }, leadingIcon = { Icon(Lucide.Check, contentDescription = null) }, onClick = {
            menuExpanded = false
            onStateChange(TaskActionRequest(task, TaskState.Done))
          })
          DropdownMenuItem(text = { Text("Cancel") }, leadingIcon = { Icon(Lucide.X, contentDescription = null) }, onClick = {
            menuExpanded = false
            onStateChange(TaskActionRequest(task, TaskState.Cancelled))
          })
          DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { Icon(Lucide.Trash2, contentDescription = null) }, onClick = {
            menuExpanded = false
            onDelete(task.id)
          })
        }
      }

      if (task.tagIds.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          task.tagIds.forEach { tagId ->
            val tag = tagLookup[tagId] ?: return@forEach
            AssistChip(onClick = {}, label = { Text(tag.name) }, leadingIcon = {
              Box(
                modifier =
                  Modifier
                    .width(12.dp)
                    .height(12.dp)
                    .background(colorFromHex(tag.colorHex), shape = MaterialTheme.shapes.small),
              )
            })
          }
        }
      }

    }
  }
}

@Composable
private fun TaskStateChip(state: TaskState) {
  val color = when (state) {
    TaskState.Backlog -> MaterialTheme.colorScheme.surfaceVariant
    TaskState.Ready -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    TaskState.InProgress -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
    TaskState.Paused -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
    TaskState.Blocked -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
    TaskState.Done -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    TaskState.Cancelled -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
  }
  Box(
    modifier =
      Modifier
        .padding(start = 12.dp)
        .background(color, shape = MaterialTheme.shapes.small)
        .padding(horizontal = 8.dp, vertical = 4.dp),
  ) {
    Text(state.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
  }
}

@Composable
private fun TaskStateDialog(
  task: Task,
  targetState: TaskState,
  onDismiss: () -> Unit,
  onConfirm: (TaskLogInput) -> Unit,
) {
  val defaultStartTime = task.plannedStart
  val defaultEndTime = addMinutes(defaultStartTime, task.estimateMinutes)
  val defaultStart = LocalDateTime(task.plannedDate, defaultStartTime)
  val defaultEnd = LocalDateTime(task.plannedDate, defaultEndTime)
  var startText by remember { mutableStateOf(formatTime(defaultStartTime)) }
  var endText by remember { mutableStateOf(formatTime(defaultEndTime)) }
  var minutesText by remember { mutableStateOf(task.estimateMinutes.toString()) }
  var noteText by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Log ${targetState.name.lowercase()} state") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Provide timing for \"${task.title}\"")
        OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Start (HH:mm)") }, leadingIcon = { Icon(Lucide.Clock, contentDescription = null) })
        OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("End (HH:mm)") }, leadingIcon = { Icon(Lucide.Clock, contentDescription = null) })
        OutlinedTextField(value = minutesText, onValueChange = { minutesText = it.filter { c -> c.isDigit() } }, label = { Text("Actual minutes") }, leadingIcon = { Icon(Lucide.Timer, contentDescription = null) })
        OutlinedTextField(value = noteText, onValueChange = { noteText = it }, label = { Text("Notes (optional)") })
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val startTime = parseTime(startText) ?: defaultStartTime
          val endTime = parseTime(endText) ?: defaultEndTime
          val start = LocalDateTime(task.plannedDate, startTime)
          val end = LocalDateTime(task.plannedDate, endTime)
          val minutes = minutesText.toIntOrNull() ?: task.estimateMinutes
          onConfirm(
            TaskLogInput(
              startedAt = start,
              endedAt = end,
              actualMinutes = minutes,
              note = noteText.takeIf { it.isNotBlank() },
            ),
          )
        },
      ) {
        Text("Log")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskEditorSheet(
  allTasks: List<Task>,
  tags: List<Tag>,
  existing: Task?,
  parentPrefill: Long?,
  onDismiss: () -> Unit,
  onSubmit: (TaskDraft) -> Unit,
  onUpdate: (TaskUpdate) -> Unit,
  defaultDate: LocalDate,
  defaultTime: LocalTime,
) {
  var title by rememberSaveable { mutableStateOf(existing?.title ?: "") }
  var description by rememberSaveable { mutableStateOf(existing?.description ?: "") }
  var estimate by rememberSaveable { mutableStateOf((existing?.estimateMinutes ?: 45).toString()) }
  var plannedDate by remember { mutableStateOf(existing?.plannedDate ?: defaultDate) }
  var plannedTime by remember { mutableStateOf(existing?.plannedStart ?: defaultTime) }
  var notBefore by remember { mutableStateOf(existing?.notBefore) }
  var parentId by remember { mutableStateOf(existing?.parentId ?: parentPrefill) }
  var selectedTags by remember { mutableStateOf(existing?.tagIds ?: emptySet()) }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(if (existing == null) "New task" else "Edit task", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
    OutlinedTextField(
      value = estimate,
      onValueChange = { text -> estimate = text.filter { it.isDigit() } },
      label = { Text("Estimate (minutes)") },
      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Column(Modifier.weight(1f)) {
        Text("Plan date", style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { plannedDate = plannedDate.plus(1, DateTimeUnit.DAY) }) {
          Icon(Lucide.Calendar, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text(formatDate(plannedDate))
        }
      }
      Column(Modifier.weight(1f)) {
        Text("Start time", style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { plannedTime = nextSlot(plannedTime) }) {
          Icon(Lucide.Clock, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text(formatTime(plannedTime))
        }
      }
    }

    OutlinedButton(onClick = {
      notBefore = if (notBefore == null) plannedDate else null
    }) {
      Text(notBefore?.let { "Not before ${formatDate(it)}" } ?: "No constraint")
    }

    val parentOptions = allTasks.filter { existing == null || it.id != existing.id }
    if (parentOptions.isNotEmpty()) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Parent", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          AssistChip(onClick = { parentId = null }, label = { Text("None") })
          parentOptions.forEach { option ->
            AssistChip(onClick = { parentId = option.id }, label = { Text(option.title) })
          }
        }
      }
    }

    if (tags.isNotEmpty()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tags", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          tags.forEach { tag ->
            val selected = selectedTags.contains(tag.id)
            AssistChip(
              onClick = {
                selectedTags = selectedTags.toMutableSet().also { set ->
                  if (!set.add(tag.id)) set.remove(tag.id)
                }
              },
              label = { Text(tag.name) },
              leadingIcon = {
                Box(
                  modifier =
                    Modifier
                      .width(12.dp)
                      .height(12.dp)
                      .background(colorFromHex(tag.colorHex), shape = MaterialTheme.shapes.small),
                )
              },
            )
          }
        }
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(modifier = Modifier.weight(1f), onClick = onDismiss) {
        Text("Cancel")
      }
      val estimateMinutes = estimate.toIntOrNull() ?: 0
      Button(modifier = Modifier.weight(1f), onClick = {
        if (existing == null) {
          onSubmit(
            TaskDraft(
              title = title,
              description = description,
              estimateMinutes = estimateMinutes,
              plannedDate = plannedDate,
              plannedStart = plannedTime,
              notBefore = notBefore,
              parentId = parentId,
              tagIds = selectedTags,
            ),
          )
        } else {
          onUpdate(
            TaskUpdate(
              id = existing.id,
              title = title,
              description = description,
              estimateMinutes = estimateMinutes,
              plannedDate = plannedDate,
              plannedStart = plannedTime,
              notBefore = notBefore,
              parentId = parentId,
              tagIds = selectedTags,
              state = existing.state,
            ),
          )
        }
      }, enabled = title.isNotBlank() && estimateMinutes > 0) {
        Text(if (existing == null) "Create" else "Update")
      }
    }
  }
}

private fun buildTaskSubtitle(task: Task): String {
  val parts = mutableListOf<String>()
  parts += "${formatTime(task.plannedStart)} • ${task.estimateMinutes}m"
  task.notBefore?.let { parts += "Earliest ${formatDate(it)}" }
  task.actualMinutes?.let { parts += "Actual ${it}m" }
  return parts.joinToString(" • ")
}

private fun formatDate(date: LocalDate): String = "${date.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${date.dayOfMonth}"

private fun formatTime(time: LocalTime): String =
  "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

private fun parseTime(value: String): LocalTime? {
  val parts = value.split(":")
  if (parts.size != 2) return null
  val hour = parts[0].toIntOrNull() ?: return null
  val minute = parts[1].toIntOrNull() ?: return null
  if (hour !in 0..23 || minute !in 0..59) return null
  return LocalTime(hour = hour, minute = minute)
}

private fun addMinutes(time: LocalTime, minutes: Int): LocalTime {
  val total = (time.hour * 60 + time.minute + minutes) % (24 * 60)
  val normalized = if (total < 0) total + 24 * 60 else total
  val hour = (normalized / 60) % 24
  val minute = normalized % 60
  return LocalTime(hour = hour, minute = minute)
}

private fun nextSlot(current: LocalTime): LocalTime {
  val minute = ((current.minute / 15) + 1) * 15
  return if (minute >= 60) {
    LocalTime(hour = (current.hour + 1) % 24, minute = 0)
  } else {
    LocalTime(hour = current.hour, minute = minute)
  }
}

private fun colorFromHex(hex: String): Color {
  val cleaned = hex.removePrefix("#")
  val parsed = cleaned.toLongOrNull(16) ?: return Color(0xFF888888)
  val argb = when (cleaned.length) {
    6 -> 0xFF000000L or parsed
    8 -> parsed
    else -> 0xFF888888
  }
  return Color(argb.toInt())
}

private fun List<TaskNode>.flattenHierarchy(): List<TaskNode> {
  val result = mutableListOf<TaskNode>()
  fun traverse(node: TaskNode) {
    result += node
    node.children.forEach(::traverse)
  }
  forEach(::traverse)
  return result
}

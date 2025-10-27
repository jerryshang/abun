package dev.tireless.abun.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import dev.tireless.abun.core.time.currentInstant
import dev.tireless.abun.tags.Tag
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
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
  val inboxNodes by viewModel.inboxTasks.collectAsState()
  val todayNodes by viewModel.todayTasks.collectAsState()
  val futureNodes by viewModel.futureTasks.collectAsState()
  val archivedNodes by viewModel.archivedTasks.collectAsState()
  val allTasks by viewModel.allTasks.collectAsState()
  val tags by viewModel.tags.collectAsState()

  var currentTab by rememberSaveable { mutableStateOf(TaskCategory.Today) }
  var showEditor by remember { mutableStateOf(false) }
  var editingTaskId by remember { mutableStateOf<Long?>(null) }
  var parentForNew by remember { mutableStateOf<Long?>(null) }
  var showStateDialog by remember { mutableStateOf(false) }
  var pendingAction by remember { mutableStateOf<TaskActionRequest?>(null) }

  val todayDate =
    currentInstant()
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date
  LaunchedEffect(todayDate) {
    viewModel.selectDate(todayDate)
  }

  Surface(modifier = modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize()) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
      ) {
        if (embedded) {
          TaskCategoryHeaderRow(
            selected = currentTab,
            onSelect = { currentTab = it },
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
          )
        } else {
          CenterAlignedTopAppBar(
            title = {
              TaskCategoryHeaderRow(
                selected = currentTab,
                onSelect = { currentTab = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge,
              )
            },
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
          )
        }

        val list =
          when (currentTab) {
            TaskCategory.Inbox -> inboxNodes
            TaskCategory.Today -> todayNodes
            TaskCategory.Future -> futureNodes
            TaskCategory.Archived -> archivedNodes
          }
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
        TaskEditSheet(
          allTasks = allTasks,
          existing = existingTask,
          parentPrefill = parentForNew,
          onDismiss = {
            showEditor = false
            editingTaskId = null
            parentForNew = null
          },
          onSubmit = { draft ->
            val adjustedDraft =
              if (parentForNew != null) draft.copy(parentId = parentForNew) else draft
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

private enum class TaskCategory(
  val label: String,
  val icon: ImageVector,
) {
  Inbox("Inbox", Icons.Outlined.Inbox),
  Today("Today", Icons.Outlined.Today),
  Future("Future", Icons.Outlined.Schedule),
  Archived("Archived", Icons.Outlined.Archive),
}

private data class TaskActionRequest(
  val task: Task,
  val targetState: TaskState,
)

@Composable
private fun TaskCategoryHeaderRow(
  selected: TaskCategory,
  onSelect: (TaskCategory) -> Unit,
  modifier: Modifier = Modifier,
  textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = selected.label,
      style = textStyle,
      modifier = Modifier.weight(1f),
    )
    TaskCategoryIconRow(
      selected = selected,
      onSelect = onSelect,
    )
  }
}

@Composable
private fun TaskCategoryIconRow(
  selected: TaskCategory,
  onSelect: (TaskCategory) -> Unit,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    TaskCategory.entries.forEach { category ->
      val isSelected = category == selected
      Surface(
        shape = MaterialTheme.shapes.small,
        color =
          if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
          } else {
            Color.Transparent
          },
      ) {
        IconButton(onClick = { onSelect(category) }) {
          Icon(
            imageVector = category.icon,
            contentDescription = category.label,
            tint =
              if (isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
          )
        }
      }
    }
  }
}

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
          Text(
            task.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            buildTaskSubtitle(task),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        TaskStateChip(task.state)
        IconButton(onClick = { menuExpanded = true }) {
          Icon(Icons.Outlined.MoreVert, contentDescription = "Task actions")
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
          DropdownMenuItem(
            text = { Text("Edit") },
            leadingIcon = { Icon(Lucide.Pencil, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onEdit(task.id)
            },
          )
          DropdownMenuItem(
            text = { Text("Add subtask") },
            leadingIcon = { Icon(Lucide.Plus, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onAddChild(task.id)
            },
          )
          DropdownMenuItem(
            text = { Text("Mark in progress") },
            leadingIcon = { Icon(Lucide.Play, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onStateChange(TaskActionRequest(task, TaskState.InProgress))
            },
          )
          DropdownMenuItem(
            text = { Text("Mark done") },
            leadingIcon = { Icon(Lucide.Check, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onStateChange(TaskActionRequest(task, TaskState.Done))
            },
          )
          DropdownMenuItem(
            text = { Text("Cancel") },
            leadingIcon = { Icon(Lucide.X, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onStateChange(TaskActionRequest(task, TaskState.Cancelled))
            },
          )
          DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = { Icon(Lucide.Trash2, contentDescription = null) },
            onClick = {
              menuExpanded = false
              onDelete(task.id)
            },
          )
        }
      }

      if (task.tagIds.isNotEmpty()) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
  val color =
    when (state) {
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
    Text(
      state.name.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun TaskStateDialog(
  task: Task,
  targetState: TaskState,
  onDismiss: () -> Unit,
  onConfirm: (TaskLogInput) -> Unit,
) {
  val now = currentInstant().toLocalDateTime(TimeZone.currentSystemDefault())
  val defaultDate = task.plannedDate ?: now.date
  val defaultStartTime = task.plannedStart ?: now.time.run { LocalTime(hour = hour, minute = (minute / 15) * 15) }
  val defaultEndTime = addMinutes(defaultStartTime, task.estimateMinutes)
  val defaultStart = LocalDateTime(defaultDate, defaultStartTime)
  val defaultEnd = LocalDateTime(defaultDate, defaultEndTime)
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
        OutlinedTextField(
          value = startText,
          onValueChange = { startText = it },
          label = { Text("Start (HH:mm)") },
          leadingIcon = { Icon(Lucide.Clock, contentDescription = null) },
        )
        OutlinedTextField(
          value = endText,
          onValueChange = { endText = it },
          label = { Text("End (HH:mm)") },
          leadingIcon = { Icon(Lucide.Clock, contentDescription = null) },
        )
        OutlinedTextField(
          value = minutesText,
          onValueChange = { minutesText = it.filter { c -> c.isDigit() } },
          label = { Text("Actual minutes") },
          leadingIcon = { Icon(Lucide.Timer, contentDescription = null) },
        )
        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it },
          label = { Text("Notes (optional)") },
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val startTime = parseTime(startText) ?: defaultStartTime
          val endTime = parseTime(endText) ?: defaultEndTime
          val start = LocalDateTime(defaultDate, startTime)
          val end = LocalDateTime(defaultDate, endTime)
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
private fun TaskEditSheet(
  allTasks: List<Task>,
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
  val initialMinutes = existing?.estimateMinutes ?: 45
  val initialUnit = EstimateUnit.fromMinutes(initialMinutes)
  val preservedTags = existing?.tagIds ?: emptySet()
  var estimateUnit by rememberSaveable { mutableStateOf(initialUnit) }
  var durationValue by rememberSaveable {
    mutableStateOf(initialUnit.amountFor(initialMinutes)?.toString() ?: initialMinutes.toString())
  }
  var plannedDate by remember { mutableStateOf(existing?.plannedDate) }
  var plannedTime by remember { mutableStateOf(existing?.plannedStart) }
  var constraint by remember { mutableStateOf(existing?.constraint ?: TaskConstraint.Exactly) }
  var parentMenuExpanded by remember { mutableStateOf(false) }
  var constraintMenuExpanded by remember { mutableStateOf(false) }
  var parentId by remember { mutableStateOf(existing?.parentId ?: parentPrefill) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()
  val saveEnabled = title.isNotBlank()
  val topLevelParents =
    remember(allTasks, existing?.id) {
      allTasks
        .filter { task ->
          val notSelf = existing?.id?.let { task.id != it } ?: true
          notSelf && task.parentId == null
        }.sortedBy { it.title.lowercase() }
    }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .imePadding()
        .verticalScroll(scrollState)
        .padding(top = 4.dp, bottom = 32.dp),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      IconButton(onClick = onDismiss) {
        Icon(Lucide.X, contentDescription = "Close editor")
      }
      Text(
        text = if (existing == null) "New task" else "Edit task",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.weight(1f),
      )
      TextButton(
        onClick = {
          val minutes = durationValue.toIntOrNull()?.let { estimateUnit.toMinutes(it) } ?: 0
          if (minutes <= 0 || title.isBlank()) {
            return@TextButton
          }
          if (existing == null) {
            onSubmit(
              TaskDraft(
                title = title.trim(),
                description = description.trim().takeIf { it.isNotBlank() },
                estimateMinutes = minutes,
                plannedDate = plannedDate,
                plannedStart = plannedTime,
                constraint = constraint,
                parentId = parentId,
                tagIds = preservedTags,
              ),
            )
          } else {
            onUpdate(
              TaskUpdate(
                id = existing.id,
                title = title.trim(),
                description = description.trim().takeIf { it.isNotBlank() },
                estimateMinutes = minutes,
                plannedDate = plannedDate,
                plannedStart = plannedTime,
                constraint = constraint,
                parentId = parentId,
                tagIds = preservedTags,
                state = existing.state,
              ),
            )
          }
        },
        enabled = saveEnabled,
      ) {
        Text(if (existing == null) "Save" else "Update")
      }
    }

    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      OutlinedTextField(
        value = title,
        onValueChange = { input -> title = input.take(120) },
        label = { Text("Title *") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Description (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = durationValue,
          onValueChange = { text -> durationValue = text.filter { it.isDigit() } },
          modifier = Modifier.weight(1f),
          singleLine = true,
          placeholder = { Text("Amount") },
          keyboardOptions =
            KeyboardOptions(
              imeAction = ImeAction.Next,
            ),
        )
        SegmentedButtonRow(
          options = EstimateUnit.values().toList(),
          selected = estimateUnit,
          onSelected = { unit ->
            if (unit == estimateUnit) return@SegmentedButtonRow
            val currentMinutes = durationValue.toIntOrNull()?.let { estimateUnit.toMinutes(it) }
            estimateUnit = unit
            durationValue =
              currentMinutes?.let { minutes ->
                unit.amountFor(minutes)?.takeIf { it > 0 }?.toString()
              } ?: ""
          },
        )
      }

      Surface(
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        shape = MaterialTheme.shapes.extraSmall,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = formatOptionalDate(plannedDate),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
          )
          Icon(Lucide.Calendar, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Surface(
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true },
        shape = MaterialTheme.shapes.extraSmall,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = formatOptionalTime(plannedTime),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
          )
          Icon(Lucide.Clock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      ExposedDropdownMenuBox(
        expanded = constraintMenuExpanded,
        onExpandedChange = { constraintMenuExpanded = !constraintMenuExpanded },
      ) {
        OutlinedTextField(
          modifier =
            Modifier
              .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth(),
          value = constraintLabel(constraint),
          onValueChange = {},
          readOnly = true,
          label = { Text("Constraint") },
          supportingText = { Text(constraintDescription(constraint)) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = constraintMenuExpanded) },
        )
        ExposedDropdownMenu(
          expanded = constraintMenuExpanded,
          onDismissRequest = { constraintMenuExpanded = false },
        ) {
          TaskConstraint.entries.forEach { option ->
            DropdownMenuItem(
              text = { Text(constraintLabel(option)) },
              onClick = {
                constraint = option
                constraintMenuExpanded = false
              },
            )
          }
        }
      }

      ExposedDropdownMenuBox(
        expanded = parentMenuExpanded,
        onExpandedChange = { parentMenuExpanded = !parentMenuExpanded },
      ) {
        OutlinedTextField(
          modifier =
            Modifier
              .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth(),
          value =
            parentDisplay(
              titleLookup = topLevelParents.associateBy { it.id },
              parentId = parentId,
            ),
          onValueChange = {},
          readOnly = true,
          label = { Text("Select parent (optional)") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentMenuExpanded) },
        )
        ExposedDropdownMenu(
          expanded = parentMenuExpanded,
          onDismissRequest = { parentMenuExpanded = false },
        ) {
          DropdownMenuItem(
            text = { Text("No parent") },
            onClick = {
              parentId = null
              parentMenuExpanded = false
            },
          )
          topLevelParents.forEach { option ->
            DropdownMenuItem(
              text = { Text(option.title) },
              onClick = {
                parentId = option.id
                parentMenuExpanded = false
              },
            )
          }
        }
      }
    }
  }

  if (showDatePicker) {
    val state =
      rememberDatePickerState(
        initialSelectedDateMillis = plannedDate?.toEpochMillis() ?: defaultDate.toEpochMillis(),
      )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            val millis = state.selectedDateMillis
            if (millis != null) {
              plannedDate = millis.toLocalDate()
            }
            showDatePicker = false
          },
        ) { Text("OK") }
      },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TextButton(
            onClick = {
              plannedDate = null
              showDatePicker = false
            },
          ) { Text("Clear") }
          TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
        }
      },
    ) {
      DatePicker(state = state)
    }
  }

  if (showTimePicker) {
    TimePickerDialog(
      initialTime = plannedTime ?: defaultTime,
      onDismiss = { showTimePicker = false },
      onConfirm = { time ->
        plannedTime = time
        showTimePicker = false
      },
      onClear = {
        plannedTime = null
        showTimePicker = false
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedButtonRow(
  options: List<EstimateUnit>,
  selected: EstimateUnit,
  onSelected: (EstimateUnit) -> Unit,
) {
  SingleChoiceSegmentedButtonRow {
    options.forEachIndexed { index, option ->
      SegmentedButton(
        selected = option == selected,
        onClick = { onSelected(option) },
        shape = SegmentedButtonDefaults.itemShape(index, options.size),
        label = { Text(option.label) },
      )
    }
  }
}

private enum class EstimateUnit(
  val label: String,
  val minutesMultiplier: Int,
) {
  MINUTES("Min", 1),
  HOURS("Hr", 60),
  ;

  fun toMinutes(value: Int): Int = value * minutesMultiplier

  fun amountFor(totalMinutes: Int): Int? = if (totalMinutes % minutesMultiplier == 0) totalMinutes / minutesMultiplier else null

  companion object {
    fun fromMinutes(totalMinutes: Int): EstimateUnit =
      if (totalMinutes >= HOURS.minutesMultiplier && totalMinutes % HOURS.minutesMultiplier == 0) HOURS else MINUTES
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
  initialTime: LocalTime,
  onDismiss: () -> Unit,
  onConfirm: (LocalTime) -> Unit,
  onClear: (() -> Unit)? = null,
) {
  val state =
    rememberTimePickerState(
      initialHour = initialTime.hour,
      initialMinute = initialTime.minute,
      is24Hour = true,
    )
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(LocalTime(hour = state.hour, minute = state.minute))
        },
      ) {
        Text("OK")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onClear != null) {
          TextButton(onClick = onClear) { Text("Clear") }
        }
        TextButton(onClick = onDismiss) { Text("Cancel") }
      }
    },
    text = {
      TimePicker(state = state)
    },
  )
}

private fun LocalDate.toEpochMillis(): Long =
  this
    .atStartOfDayIn(TimeZone.currentSystemDefault())
    .toEpochMilliseconds()

private fun Long.toLocalDate(): LocalDate =
  Instant
    .fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date

private fun parentDisplay(
  titleLookup: Map<Long, Task>,
  parentId: Long?,
): String = parentId?.let { id -> titleLookup[id]?.title } ?: "No parent"

private fun buildTaskSubtitle(task: Task): String {
  val parts = mutableListOf<String>()
  task.plannedStart?.let { parts += formatTime(it) }
  parts += "${task.estimateMinutes}m"
  when (task.constraint) {
    TaskConstraint.Exactly -> task.plannedDate?.let { parts += "On ${formatDate(it)}" }
    TaskConstraint.NotBefore -> task.plannedDate?.let { parts += "Earliest ${formatDate(it)}" }
    TaskConstraint.NotAfter -> task.plannedDate?.let { parts += "Due ${formatDate(it)}" }
  }
  task.actualMinutes?.let { parts += "Actual ${it}m" }
  return parts.joinToString(" • ")
}

private fun constraintLabel(constraint: TaskConstraint): String =
  when (constraint) {
    TaskConstraint.Exactly -> "Exactly on date"
    TaskConstraint.NotBefore -> "Not before date"
    TaskConstraint.NotAfter -> "Not after date"
  }

private fun constraintDescription(constraint: TaskConstraint): String =
  when (constraint) {
    TaskConstraint.Exactly -> "Task occurs only on the scheduled day."
    TaskConstraint.NotBefore -> "Task can start on or any time after the scheduled day."
    TaskConstraint.NotAfter -> "Task must finish on or before the scheduled day."
  }

private fun formatDate(date: LocalDate): String = "${date.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${date.dayOfMonth}"

private fun formatOptionalDate(date: LocalDate?): String = date?.let { formatDate(it) } ?: "any date"

private fun formatTime(time: LocalTime): String = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

private fun formatOptionalTime(time: LocalTime?): String = time?.let { formatTime(it) } ?: "any time"

private fun parseTime(value: String): LocalTime? {
  val parts = value.split(":")
  if (parts.size != 2) return null
  val hour = parts[0].toIntOrNull() ?: return null
  val minute = parts[1].toIntOrNull() ?: return null
  if (hour !in 0..23 || minute !in 0..59) return null
  return LocalTime(hour = hour, minute = minute)
}

private fun addMinutes(
  time: LocalTime,
  minutes: Int,
): LocalTime {
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
  val argb =
    when (cleaned.length) {
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

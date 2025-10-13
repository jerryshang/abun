package dev.tireless.abun.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun TaskHierarchyScreen(navController: NavHostController) {
  val taskRepository: TaskRepository = koinInject()
  val tasks by taskRepository.getAllTasks().collectAsState(initial = emptyList())
  val scope = rememberCoroutineScope()

  var showEditor by remember { mutableStateOf(false) }
  var editingTask by remember { mutableStateOf<Task?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val hierarchicalTasks = remember(tasks) { tasksWithDepth(tasks) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          editingTask = null
          showEditor = true
        },
      ) {
        Icon(Lucide.Plus, contentDescription = "Create task")
      }
    },
  ) { paddingValues ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Task Hierarchy",
          style = MaterialTheme.typography.headlineLarge,
        )
        Text(
          text = "Organize tasks and their parent relationships.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      errorMessage?.let { message ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors =
            androidx.compose.material3.CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = message,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { errorMessage = null }) {
              Text("Dismiss")
            }
          }
        }
      }

      if (hierarchicalTasks.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "No tasks yet. Create your first task to get started.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          items(hierarchicalTasks) { item ->
            TaskRow(
              item = item,
              onEdit = {
                editingTask = item.task
                showEditor = true
              },
              onDelete = {
                scope.launch {
                  try {
                    taskRepository.deleteTask(item.task.id)
                  } catch (error: Exception) {
                    errorMessage =
                      "Failed to delete task: ${error.message ?: "unknown error"}"
                  }
                }
              },
            )
          }
        }
      }
    }
  }

  if (showEditor) {
    TaskEditorDialog(
      tasks = tasks,
      editingTask = editingTask,
      onDismiss = {
        showEditor = false
        editingTask = null
      },
      onConfirm = { name, description, parentId, strategy ->
        scope.launch {
          try {
            if (editingTask == null) {
              taskRepository.insertTask(name, description, parentId, strategy)
            } else {
              taskRepository.updateTask(editingTask!!.id, name, description, parentId, strategy)
            }
            showEditor = false
            editingTask = null
          } catch (error: Exception) {
            errorMessage =
              "Failed to save task: ${error.message ?: "unknown error"}"
          }
        }
      },
    )
  }
}

private data class TaskListItem(
  val task: Task,
  val depth: Int,
)

private fun tasksWithDepth(tasks: List<Task>): List<TaskListItem> {
  if (tasks.isEmpty()) return emptyList()

  val childrenByParent = tasks.groupBy { it.parentTaskId }
  val ordered = mutableListOf<TaskListItem>()

  fun traverse(task: Task, depth: Int) {
    ordered += TaskListItem(task, depth)
    val children = childrenByParent[task.id].orEmpty().sortedBy { it.name.lowercase() }
    children.forEach { traverse(it, depth + 1) }
  }

  val roots = childrenByParent[null].orEmpty().sortedBy { it.name.lowercase() }
  roots.forEach { traverse(it, 0) }

  // Include orphaned items whose parent is missing
  val knownIds = ordered.map { it.task.id }.toSet()
  tasks.filterNot { knownIds.contains(it.id) }
    .sortedBy { it.name.lowercase() }
    .forEach { traverse(it, 0) }

  return ordered
}

@Composable
private fun TaskRow(
  item: TaskListItem,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp, horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Spacer(modifier = Modifier.width((item.depth * 16).dp))

      Box(
        modifier =
          Modifier
            .size(20.dp)
            .background(
              color = getStrategyColor(item.task.strategy),
              shape = RoundedCornerShape(4.dp),
            ),
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = item.task.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
        )
        item.task.description?.takeIf { it.isNotBlank() }?.let { description ->
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "Phase: ${item.task.strategy.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          item.task.parentTaskName?.let { parent ->
            Text(
              text = "Parent: $parent",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      IconButton(onClick = onEdit) {
        Icon(Lucide.Pencil, contentDescription = "Edit task")
      }
      IconButton(onClick = onDelete) {
        Icon(Lucide.Trash2, contentDescription = "Delete task")
      }
    }
  }
}

@Composable
private fun TaskEditorDialog(
  tasks: List<Task>,
  editingTask: Task?,
  onDismiss: () -> Unit,
  onConfirm: (String, String?, Long?, String) -> Unit,
) {
  var name by remember { mutableStateOf(editingTask?.name ?: "") }
  var description by remember { mutableStateOf(editingTask?.description ?: "") }
  var selectedStrategy by remember { mutableStateOf(editingTask?.strategy ?: "plan") }
  var parentMenuExpanded by remember { mutableStateOf(false) }

  val strategies = listOf("plan", "todo", "do", "check")

  val childrenByParent = remember(tasks) { tasks.groupBy { it.parentTaskId } }
  val disallowedParentIds = remember(editingTask, childrenByParent) {
    val accumulator = mutableSetOf<Long>()
    editingTask?.let { task ->
      collectDescendants(task.id, childrenByParent, accumulator)
      accumulator += task.id
    }
    accumulator
  }

  val parentCandidates =
    remember(tasks, disallowedParentIds) {
      tasks
        .filterNot { disallowedParentIds.contains(it.id) }
        .sortedBy { it.name.lowercase() }
    }

  var selectedParentId by remember(editingTask) { mutableStateOf(editingTask?.parentTaskId) }
  val selectedParentName = parentCandidates.firstOrNull { it.id == selectedParentId }?.name

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (editingTask == null) "Create Task" else "Edit Task")
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Task name") },
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (optional)") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Parent task",
            style = MaterialTheme.typography.labelLarge,
          )
          OutlinedButton(
            onClick = { parentMenuExpanded = true },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(selectedParentName ?: "No parent")
              Icon(
                imageVector = if (parentMenuExpanded) Lucide.ChevronUp else Lucide.ChevronDown,
                contentDescription = null,
              )
            }
          }
          DropdownMenu(
            expanded = parentMenuExpanded,
            onDismissRequest = { parentMenuExpanded = false },
          ) {
            DropdownMenuItem(
              text = { Text("No parent") },
              onClick = {
                selectedParentId = null
                parentMenuExpanded = false
              },
            )
            parentCandidates.forEach { task ->
              DropdownMenuItem(
                text = { Text(task.name) },
                onClick = {
                  selectedParentId = task.id
                  parentMenuExpanded = false
                },
              )
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Strategy phase",
            style = MaterialTheme.typography.labelLarge,
          )
          StrategySelector(
            strategies = strategies,
            selected = selectedStrategy,
            onSelect = { selectedStrategy = it },
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            name.trim(),
            description.trim().takeIf { it.isNotEmpty() },
            selectedParentId,
            selectedStrategy,
          )
        },
        enabled = name.isNotBlank(),
      ) {
        Text(if (editingTask == null) "Create" else "Save")
      }
    },
    dismissButton = {
      TextButton(onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
private fun StrategySelector(
  strategies: List<String>,
  selected: String,
  onSelect: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  OutlinedButton(
    onClick = { expanded = true },
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier
              .size(16.dp)
              .background(
                color = getStrategyColor(selected),
                shape = RoundedCornerShape(2.dp),
              ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(selected.uppercase())
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
    strategies.forEach { strategy ->
      DropdownMenuItem(
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier
                  .size(16.dp)
                  .background(
                    color = getStrategyColor(strategy),
                    shape = RoundedCornerShape(2.dp),
                  ),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(strategy.uppercase())
          }
        },
        onClick = {
          onSelect(strategy)
          expanded = false
        },
      )
    }
  }
}

private fun collectDescendants(
  taskId: Long,
  childrenByParent: Map<Long?, List<Task>>,
  accumulator: MutableSet<Long>,
) {
  childrenByParent[taskId]
    ?.forEach { child ->
      if (accumulator.add(child.id)) {
        collectDescendants(child.id, childrenByParent, accumulator)
      }
    }
}

private fun getStrategyColor(strategy: String): Color =
  when (strategy.lowercase()) {
    "plan" -> Color(0xFF2196F3)
    "todo" -> Color(0xFFFF9800)
    "do" -> Color(0xFF4CAF50)
    "check" -> Color(0xFF9C27B0)
    else -> Color(0xFF607D8B)
  }

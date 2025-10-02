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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import dev.tireless.abun.time.Category
import dev.tireless.abun.time.CategoryViewModel
import dev.tireless.abun.time.Task
import dev.tireless.abun.time.TaskRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryManagementScreen(
  navController: NavHostController
) {
  val viewModel: CategoryViewModel = koinViewModel()
  val taskRepository: TaskRepository = koinInject()
  val categories by viewModel.categories.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()

  var showCreateDialog by remember { mutableStateOf(false) }
  var editingCategory by remember { mutableStateOf<Category?>(null) }
  var showTaskManagement by remember { mutableStateOf(false) }
  var selectedCategoryForTasks by remember { mutableStateOf<Category?>(null) }

  // Debug output
  println("CategoryManagementScreen - Categories count: ${categories.size}, isLoading: $isLoading, error: $errorMessage")

  if (showTaskManagement && selectedCategoryForTasks != null) {
    TaskManagementScreen(
      category = selectedCategoryForTasks!!,
      taskRepository = taskRepository,
      onClose = {
        showTaskManagement = false
        selectedCategoryForTasks = null
      }
    )
  } else {
    Scaffold(
      floatingActionButton = {
        FloatingActionButton(
          onClick = { showCreateDialog = true }
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Category")
        }
      }
    ) { paddingValues ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(16.dp)
      ) {
        Text(
          text = "Category Management",
          style = MaterialTheme.typography.headlineLarge,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        // Show error message if any
        errorMessage?.let { error ->
          Text(
            text = "Error: $error",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
          )
        }

        if (categories.isEmpty() && !isLoading) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No categories found. Add your first category!",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(categories) { category ->
              CategoryCard(
                category = category,
                onEdit = { editingCategory = category },
                onDelete = { viewModel.deleteCategory(category.id) },
                onManageTasks = {
                  selectedCategoryForTasks = category
                  showTaskManagement = true
                }
              )
            }
          }
        }
      }
    }

    // Create/Edit Category Dialog
    if (showCreateDialog || editingCategory != null) {
      CategoryDialog(
        category = editingCategory,
        onDismiss = {
          showCreateDialog = false
          editingCategory = null
        },
        onSave = { name, color ->
          if (editingCategory != null) {
            viewModel.updateCategory(editingCategory!!.id, name, color)
          } else {
            viewModel.createCategory(name, color)
          }
          showCreateDialog = false
          editingCategory = null
        }
      )
    }
  }
}

@Composable
private fun CategoryCard(
  category: Category,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onManageTasks: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Color indicator
      Box(
        modifier = Modifier
          .size(24.dp)
          .background(
            color = Color(parseHexColor(category.color)),
            shape = CircleShape
          )
      )

      Spacer(modifier = Modifier.width(16.dp))

      // Category name
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = category.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium
        )
      }

      // Manage Tasks Button
      OutlinedButton(
        onClick = onManageTasks,
        modifier = Modifier.padding(end = 8.dp)
      ) {
        Text("Tasks")
      }

      // Action buttons
      IconButton(onClick = onEdit) {
        Icon(Icons.Default.Edit, contentDescription = "Edit")
      }
      IconButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = "Delete")
      }
    }
  }
}

@Composable
private fun CategoryDialog(
  category: Category? = null,
  onDismiss: () -> Unit,
  onSave: (String, String) -> Unit
) {
  var name by remember { mutableStateOf(category?.name ?: "") }
  var selectedColor by remember { mutableStateOf(category?.color ?: "#4CAF50") }

  val predefinedColors = listOf(
    "#4CAF50", // Green
    "#2196F3", // Blue
    "#FF9800", // Orange
    "#F44336", // Red
    "#9C27B0", // Purple
    "#FF5722", // Deep Orange
    "#795548", // Brown
    "#607D8B", // Blue Grey
    "#E91E63", // Pink
    "#00BCD4" // Cyan
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (category != null) "Edit Category" else "Create Category")
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Category Name") },
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          text = "Choose Color:",
          style = MaterialTheme.typography.labelLarge
        )

        // Color selection grid
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          predefinedColors.chunked(5).forEach { rowColors ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowColors.forEach { color ->
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(
                      color = Color(parseHexColor(color)),
                      shape = CircleShape
                    )
                    .let { modifier ->
                      if (selectedColor == color) {
                        modifier.padding(4.dp)
                          .background(
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                          )
                      } else {
                        modifier
                      }
                    }
                    .let { modifier ->
                      modifier.then(
                        Modifier.background(
                          color = Color(parseHexColor(color)),
                          shape = CircleShape
                        )
                      )
                    }
                ) {
                  // Make the entire box clickable
                  Button(
                    onClick = { selectedColor = color },
                    modifier = Modifier.fillMaxSize(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                      containerColor = Color.Transparent
                    )
                  ) { }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(name, selectedColor) },
        enabled = name.isNotBlank()
      ) {
        Text(if (category != null) "Update" else "Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun parseHexColor(hexColor: String): Long {
  val cleanHex = hexColor.removePrefix("#")
  return when (cleanHex.length) {
    6 -> (0xFF000000 or cleanHex.toLong(16))
    8 -> cleanHex.toLong(16)
    else -> 0xFF808080 // Default gray
  }
}

@Composable
private fun TaskManagementScreen(
  category: Category,
  taskRepository: TaskRepository,
  onClose: () -> Unit
) {
  var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
  var isLoading by remember { mutableStateOf(false) }
  var showCreateTaskDialog by remember { mutableStateOf(false) }
  var editingTask by remember { mutableStateOf<Task?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()

  // Load tasks for this category
  androidx.compose.runtime.LaunchedEffect(category.id) {
    taskRepository.getTasksByCategory(category.id).collect { taskList ->
      tasks = taskList
    }
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateTaskDialog = true }
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Task")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onClose) {
          Icon(Icons.Default.ExpandLess, contentDescription = "Back")
        }
        Text(
          text = "${category.name} Tasks",
          style = MaterialTheme.typography.headlineLarge,
          modifier = Modifier.padding(start = 8.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (tasks.isEmpty() && !isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No tasks found. Add your first task!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(tasks) { task ->
            TaskCard(
              task = task,
              onEdit = { editingTask = task },
              onDelete = {
                scope.launch {
                  try {
                    taskRepository.deleteTask(task.id)
                  } catch (e: Exception) {
                    errorMessage = "Failed to delete task: ${e.message}"
                  }
                }
              }
            )
          }
        }
      }
    }
  }

  // Create/Edit Task Dialog
  if (showCreateTaskDialog || editingTask != null) {
    TaskDialog(
      task = editingTask,
      categoryId = category.id,
      onDismiss = {
        showCreateTaskDialog = false
        editingTask = null
      },
      onSave = { name, description, strategy ->
        scope.launch {
          try {
            if (editingTask != null) {
              taskRepository.updateTask(
                editingTask!!.id,
                name,
                description,
                category.id,
                strategy
              )
            } else {
              taskRepository.insertTask(name, description, category.id, strategy)
            }
            showCreateTaskDialog = false
            editingTask = null
          } catch (e: Exception) {
            errorMessage = "Failed to save task: ${e.message}"
          }
        }
      }
    )
  }
}

@Composable
private fun TaskCard(
  task: Task,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Strategy indicator
      Box(
        modifier = Modifier
          .padding(end = 16.dp)
          .background(
            color = getStrategyColor(task.strategy),
            shape = RoundedCornerShape(4.dp)
          )
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = task.strategy.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = Color.White
        )
      }

      // Task info
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = task.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium
        )
        task.description?.let { desc ->
          Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Action buttons
      IconButton(onClick = onEdit) {
        Icon(Icons.Default.Edit, contentDescription = "Edit")
      }
      IconButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = "Delete")
      }
    }
  }
}

@Composable
private fun TaskDialog(
  task: Task? = null,
  categoryId: Long,
  onDismiss: () -> Unit,
  onSave: (String, String?, String) -> Unit
) {
  var name by remember { mutableStateOf(task?.name ?: "") }
  var description by remember { mutableStateOf(task?.description ?: "") }
  var selectedStrategy by remember { mutableStateOf(task?.strategy ?: "plan") }
  var expanded by remember { mutableStateOf(false) }

  val strategies = listOf("plan", "todo", "do", "check")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (task != null) "Edit Task" else "Create Task")
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Task Name") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (Optional)") },
          modifier = Modifier.fillMaxWidth()
        )

        // Strategy Dropdown
        Text(
          text = "Strategy Phase:",
          style = MaterialTheme.typography.labelLarge
        )

        Box {
          OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .background(
                      color = getStrategyColor(selectedStrategy),
                      shape = RoundedCornerShape(2.dp)
                    )
                    .padding(end = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedStrategy.uppercase())
              }
              Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
              )
            }
          }

          DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
          ) {
            strategies.forEach { strategy ->
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(16.dp)
                        .background(
                          color = getStrategyColor(strategy),
                          shape = RoundedCornerShape(2.dp)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strategy.uppercase())
                  }
                },
                onClick = {
                  selectedStrategy = strategy
                  expanded = false
                }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(name, description.takeIf { it.isNotBlank() }, selectedStrategy) },
        enabled = name.isNotBlank()
      ) {
        Text(if (task != null) "Update" else "Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun getStrategyColor(strategy: String): Color = when (strategy.lowercase()) {
  "plan" -> Color(0xFF2196F3) // Blue
  "todo" -> Color(0xFFFF9800) // Orange
  "do" -> Color(0xFF4CAF50) // Green
  "check" -> Color(0xFF9C27B0) // Purple
  else -> Color(0xFF607D8B) // Blue Grey
}

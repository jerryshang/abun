package dev.tireless.abun.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tireless.abun.time.Category
import dev.tireless.abun.time.Task
import dev.tireless.abun.time.TimeblockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTimeblockDialog(
  viewModel: TimeblockViewModel,
  onDismiss: () -> Unit,
  selectedDate: String = "2024-01-01"
) {
  val tasks by viewModel.tasks.collectAsState()
  val categories by viewModel.categories.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var startTime by remember { mutableStateOf("09:00") }
  var endTime by remember { mutableStateOf("10:00") }
  var selectedTask by remember { mutableStateOf<Task?>(null) }
  var showCreateTask by remember { mutableStateOf(false) }

  // Task creation states
  var taskName by remember { mutableStateOf("") }
  var taskDescription by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<Category?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (showCreateTask) "Create New Task" else "Create Timeblock",
        style = MaterialTheme.typography.headlineSmall
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        if (!showCreateTask) {
          // Timeblock creation form
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = startTime,
              onValueChange = { startTime = it },
              label = { Text("Start Time") },
              placeholder = { Text("09:00") },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = endTime,
              onValueChange = { endTime = it },
              label = { Text("End Time") },
              placeholder = { Text("10:00") },
              modifier = Modifier.weight(1f)
            )
          }

          // Task selection
          Text(
            text = "Select Task:",
            style = MaterialTheme.typography.labelLarge
          )

          Card(
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              tasks.forEach { task ->
                OutlinedButton(
                  onClick = { selectedTask = task },
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        text = task.name,
                        style = MaterialTheme.typography.bodyMedium
                      )
                      task.categoryName?.let { categoryName ->
                        Text(
                          text = categoryName,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                    if (selectedTask?.id == task.id) {
                      Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                  }
                }
              }

              // Create new task button
              Button(
                onClick = { showCreateTask = true },
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Task")
              }
            }
          }
        } else {
          // Task creation form
          OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
          )

          // Category selection
          Text("Select Category:")
          Card(
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              categories.forEach { category ->
                OutlinedButton(
                  onClick = { selectedCategory = category },
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(category.name)
                    if (selectedCategory?.id == category.id) {
                      Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (!showCreateTask) {
        Button(
          onClick = {
            selectedTask?.let { task ->
              val startDateTime = "${selectedDate}T$startTime:00"
              val endDateTime = "${selectedDate}T$endTime:00"
              viewModel.createTimeblock(
                startTime = startDateTime,
                endTime = endDateTime,
                taskId = task.id,
                onSuccess = { onDismiss() },
                onError = { /* Handle error */ }
              )
            }
          },
          enabled = selectedTask != null && !isLoading
        ) {
          Text("Create Timeblock")
        }
      } else {
        Button(
          onClick = {
            if (taskName.isNotBlank() && selectedCategory != null) {
              viewModel.createTask(
                name = taskName,
                description = taskDescription.ifBlank { null },
                categoryId = selectedCategory!!.id,
                onSuccess = { taskId ->
                  // Switch back to timeblock creation with the new task selected
                  showCreateTask = false
                  selectedTask = Task(
                    id = taskId,
                    name = taskName,
                    description = taskDescription.ifBlank { null },
                    categoryId = selectedCategory!!.id,
                    strategy = "plan", // Default strategy for newly created tasks
                    createdAt = "",
                    updatedAt = "",
                    categoryName = selectedCategory!!.name,
                    categoryColor = selectedCategory!!.color
                  )
                  // Reset task creation form
                  taskName = ""
                  taskDescription = ""
                  selectedCategory = null
                },
                onError = { /* Handle error */ }
              )
            }
          },
          enabled = taskName.isNotBlank() && selectedCategory != null && !isLoading
        ) {
          Text("Create Task")
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          if (showCreateTask) {
            showCreateTask = false
            // Reset task creation form
            taskName = ""
            taskDescription = ""
            selectedCategory = null
          } else {
            onDismiss()
          }
        }
      ) {
        Text(if (showCreateTask) "Back" else "Cancel")
      }
    }
  )
}

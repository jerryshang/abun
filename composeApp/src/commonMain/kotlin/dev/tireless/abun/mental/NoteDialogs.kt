package dev.tireless.abun.mental

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import dev.tireless.abun.database.Notes
import dev.tireless.abun.mental.Dimensions

@Composable
fun CreateNoteDialog(
  onDismiss: () -> Unit,
  onCreate: (title: String, content: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create New Note") },
    text = {
      Column {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.Words
          )
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Content") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          maxLines = 6,
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Sentences
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onCreate(title.trim(), content.trim())
          }
        },
        enabled = title.isNotBlank()
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun EditNoteDialog(
  note: Notes,
  onDismiss: () -> Unit,
  onSave: (title: String, content: String) -> Unit
) {
  var title by remember { mutableStateOf(note.title) }
  var content by remember { mutableStateOf(note.content) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Note") },
    text = {
      Column {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.Words
          )
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Content") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          maxLines = 6,
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Sentences
          )
        )
      }
    },
    confirmButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
      ) {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              onSave(title.trim(), content.trim())
            }
          },
          enabled = title.isNotBlank()
        ) {
          Text("Save")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

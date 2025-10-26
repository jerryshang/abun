package dev.tireless.abun.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.TextFieldValue
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Bold
import com.composables.icons.lucide.Code
import com.composables.icons.lucide.Heading
import com.composables.icons.lucide.Italic
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Lucide
import dev.tireless.abun.tags.Tag
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesHomeScreen(
  navController: NavHostController,
  viewModel: NotesViewModel = koinViewModel(),
) {
  val summaries by viewModel.summaries.collectAsState()
  val tags by viewModel.tags.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedTags by viewModel.selectedTagIds.collectAsState()
  val editingNote by viewModel.editingNote.collectAsState()

  var showEditor by remember { mutableStateOf(false) }

  Surface(Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize()) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
      ) {
        CenterAlignedTopAppBar(title = { Text("Notes") })

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
          placeholder = { Text("Search notes") },
          leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
          singleLine = true,
        )

        if (tags.isNotEmpty()) {
          FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            AssistChip(
              onClick = { viewModel.clearFilters() },
              label = { Text("All notes") },
              leadingIcon = { Icon(Icons.Outlined.UnfoldMore, contentDescription = null) },
            )
            tags.forEach { tag ->
              val selected = selectedTags.contains(tag.id)
              AssistChip(
                onClick = { viewModel.toggleTagFilter(tag.id) },
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
                colors = AssistChipDefaults.assistChipColors(containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
              )
            }
          }
        }

        NotesList(
          summaries = summaries,
          tagLookup = tags.associateBy { it.id },
          onOpen = { noteId ->
            viewModel.startEditing(noteId)
            showEditor = true
          },
          onTogglePin = { viewModel.togglePin(it) },
        )
      }

      FloatingActionButton(
        onClick = {
          viewModel.startEditing(null)
          showEditor = true
        },
        modifier =
          Modifier
            .align(Alignment.BottomEnd)
            .padding(24.dp),
      ) {
        Icon(Icons.Outlined.Add, contentDescription = "Add note")
      }
    }

    if (showEditor) {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ModalBottomSheet(
        onDismissRequest = {
          showEditor = false
          viewModel.startEditing(null)
        },
        sheetState = sheetState,
      ) {
        NoteEditorSheet(
          tags = tags,
          existing = editingNote,
          onDismiss = {
            showEditor = false
            viewModel.startEditing(null)
          },
          onSave = { draft, existingId ->
            viewModel.saveNote(existingId, draft)
            showEditor = false
          },
          onDelete = { noteId ->
            viewModel.deleteNote(noteId)
            showEditor = false
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesList(
  summaries: List<NoteSummary>,
  tagLookup: Map<Long, Tag>,
  onOpen: (Long) -> Unit,
  onTogglePin: (Long) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(summaries, key = { it.id }) { summary ->
      Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        onClick = { onOpen(summary.id) },
      ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(summary.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Text(buildNoteSubtitle(summary.updatedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onTogglePin(summary.id) }) {
              Icon(Icons.Outlined.PushPin, contentDescription = "Toggle pin", tint = if (summary.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Text(summary.preview, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
          if (summary.tagIds.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              summary.tagIds.forEach { tagId ->
                val tag = tagLookup[tagId] ?: return@forEach
                AssistChip(onClick = {}, label = { Text(tag.name) })
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorSheet(
  tags: List<Tag>,
  existing: Note?,
  onDismiss: () -> Unit,
  onSave: (NoteDraft, Long?) -> Unit,
  onDelete: (Long) -> Unit,
) {
  var title by rememberSaveable { mutableStateOf(existing?.title ?: "") }
  var content by remember { mutableStateOf(TextFieldValue(existing?.content ?: "")) }
  var selectedTags by remember { mutableStateOf(existing?.tagIds ?: emptySet()) }
  var showPreview by remember { mutableStateOf(false) }
  var pinned by remember { mutableStateOf(existing?.pinned ?: false) }
  val scrollState = rememberScrollState()

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .imePadding()
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(if (existing == null) "New note" else "Edit note", style = MaterialTheme.typography.titleLarge)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Pinned")
        Spacer(Modifier.width(8.dp))
        Switch(checked = pinned, onCheckedChange = { pinned = it })
      }
    }

    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())

    MarkdownToolbar(
      onBold = { content = wrapSelection(content, "**") },
      onItalic = { content = wrapSelection(content, "_") },
      onHeading = { content = insertPrefix(content, "## ") },
      onChecklist = { content = insertPrefix(content, "- [ ] ") },
      onCode = { content = wrapSelection(content, "`") },
      showPreview = showPreview,
      onTogglePreview = { showPreview = !showPreview },
    )

    OutlinedTextField(
      value = content,
      onValueChange = { content = it },
      label = { Text("Content") },
      modifier = Modifier.fillMaxWidth().height(220.dp),
      textStyle = MaterialTheme.typography.bodyMedium,
      maxLines = Int.MAX_VALUE,
    )

    if (showPreview) {
      MarkdownPreview(content.text)
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
            )
          }
        }
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
      if (existing != null) {
        TextButton(onClick = { onDelete(existing.id) }) {
          Icon(Icons.Outlined.Delete, contentDescription = null)
          Spacer(Modifier.width(6.dp))
          Text("Delete")
        }
      }
      Button(onClick = {
        onSave(
          NoteDraft(
            title = title,
            content = content.text,
            tagIds = selectedTags,
            pinned = pinned,
          ),
          existing?.id,
        )
      }, modifier = Modifier.weight(1f), enabled = content.text.isNotBlank()) {
        Text("Save")
      }
    }
  }
}

@Composable
private fun MarkdownToolbar(
  onBold: () -> Unit,
  onItalic: () -> Unit,
  onHeading: () -> Unit,
  onChecklist: () -> Unit,
  onCode: () -> Unit,
  showPreview: Boolean,
  onTogglePreview: () -> Unit,
) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    IconButton(onClick = onBold) { Icon(Lucide.Bold, contentDescription = "Bold") }
    IconButton(onClick = onItalic) { Icon(Lucide.Italic, contentDescription = "Italic") }
    IconButton(onClick = onHeading) { Icon(Lucide.Heading, contentDescription = "Heading") }
    IconButton(onClick = onChecklist) { Icon(Lucide.ListChecks, contentDescription = "Checklist") }
    IconButton(onClick = onCode) { Icon(Lucide.Code, contentDescription = "Code") }
    Spacer(Modifier.weight(1f))
    TextButton(onClick = onTogglePreview) {
      Text(if (showPreview) "Hide preview" else "Show preview")
    }
  }
}

@Composable
private fun MarkdownPreview(content: String) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    content.lines().forEach { line ->
      when {
        line.startsWith("### ") -> Text(line.removePrefix("### "), style = MaterialTheme.typography.titleMedium)
        line.startsWith("## ") -> Text(line.removePrefix("## "), style = MaterialTheme.typography.titleLarge)
        line.startsWith("# ") -> Text(line.removePrefix("# "), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        line.startsWith("- [ ] ") -> MarkdownChecklistRow(line.removePrefix("- [ ] "), false)
        line.startsWith("- [x] ") -> MarkdownChecklistRow(line.removePrefix("- [x] "), true)
        line.startsWith("- ") -> MarkdownBullet(line.removePrefix("- "))
        else -> Text(transformInlineMarkdown(line), style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Composable
private fun MarkdownChecklistRow(text: String, checked: Boolean) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier =
        Modifier
          .width(18.dp)
          .height(18.dp)
          .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small),
    )
    Text(text, style = MaterialTheme.typography.bodyMedium)
  }
}

@Composable
private fun MarkdownBullet(text: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Text("•", style = MaterialTheme.typography.bodyMedium)
    Text(text, style = MaterialTheme.typography.bodyMedium)
  }
}

private fun transformInlineMarkdown(line: String): String {
  return line
    .replace("**", "")
    .replace("*", "")
    .replace("`", "")
}

private fun wrapSelection(value: TextFieldValue, wrapper: String): TextFieldValue {
  val start = value.selection.start.coerceAtLeast(0)
  val end = value.selection.end.coerceAtLeast(0)
  if (start == end) {
    val insertion = "$wrapper$wrapper"
    val newText = value.text.replaceRange(start, end, insertion)
    val cursor = start + wrapper.length
    return value.copy(text = newText, selection = TextRange(cursor, cursor))
  }
  val selected = value.text.substring(start, end)
  val wrapped = "$wrapper$selected$wrapper"
  val newText = value.text.replaceRange(start, end, wrapped)
  return value.copy(text = newText, selection = TextRange(start + wrapped.length, start + wrapped.length))
}

private fun insertPrefix(value: TextFieldValue, prefix: String): TextFieldValue {
  val start = value.text.lastIndexOf('\n', value.selection.start - 1).takeIf { it >= 0 }?.plus(1) ?: 0
  val newText = value.text.substring(0, start) + prefix + value.text.substring(start)
  val delta = prefix.length
  return value.copy(text = newText, selection = TextRange(value.selection.start + delta, value.selection.end + delta))
}

private fun buildNoteSubtitle(updatedAt: kotlinx.datetime.Instant): String {
  val local = updatedAt.toLocalDateTime(TimeZone.currentSystemDefault())
  return "Updated ${local.date} ${local.time.hour}:${local.time.minute.toString().padStart(2, '0')}"
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

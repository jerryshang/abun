package dev.tireless.abun.tags

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class TagRepository {
  private val tags = MutableStateFlow(sampleTags())

  private var idCounter: Long = tags.value.maxOfOrNull { it.id }?.plus(1) ?: 1L

  fun observeAll(): StateFlow<List<Tag>> = tags

  fun observeByDomain(domain: TagDomain): Flow<List<Tag>> {
    return tags.map { list -> list.filter { it.appliesTo(domain) } }
  }

  fun upsert(draft: TagDraft): Tag {
    val now = Clock.System.now()
    val newTag =
      Tag(
        id = nextId(),
        name = draft.name.trim(),
        path = draft.path.trim(),
        colorHex = draft.colorHex,
        domains = draft.domains.ifEmpty { setOf(TagDomain.All) },
        description = draft.description?.takeIf { it.isNotBlank() },
        createdAt = now,
        updatedAt = now,
      )
    tags.value = tags.value + newTag
    return newTag
  }

  fun update(update: TagUpdate) {
    val now = Clock.System.now()
    tags.value =
      tags.value.map { existing ->
        if (existing.id == update.id) {
          existing.copy(
            name = update.name.trim(),
            path = update.path.trim(),
            colorHex = update.colorHex,
            domains = update.domains.ifEmpty { setOf(TagDomain.All) },
            description = update.description?.takeIf { it.isNotBlank() },
            updatedAt = now,
          )
        } else {
          existing
        }
      }
  }

  fun delete(id: Long) {
    tags.value = tags.value.filterNot { it.id == id }
  }

  private fun nextId(): Long = idCounter++

  private fun sampleTags(): List<Tag> {
    val now = Clock.System.now()
    return listOf(
      Tag(
        id = 1,
        name = "Deep Work",
        path = "productivity/focus",
        colorHex = "#1E88E5",
        domains = setOf(TagDomain.Tasks, TagDomain.Notes),
        createdAt = now,
        updatedAt = now,
      ),
      Tag(
        id = 2,
        name = "Personal Finance",
        path = "life/finance",
        colorHex = "#43A047",
        domains = setOf(TagDomain.Finance, TagDomain.Tasks, TagDomain.Notes),
        createdAt = now,
        updatedAt = now,
      ),
      Tag(
        id = 3,
        name = "Reading",
        path = "learning/reading",
        colorHex = "#F4511E",
        domains = setOf(TagDomain.Notes, TagDomain.Tasks),
        createdAt = now,
        updatedAt = now,
      ),
    )
  }
}

class TagManagementViewModel(
  private val repository: TagRepository,
) : ViewModel() {
  val tags = repository.observeAll()

  fun createTag(draft: TagDraft) = repository.upsert(draft)

  fun updateTag(update: TagUpdate) = repository.update(update)

  fun deleteTag(id: Long) = repository.delete(id)
}

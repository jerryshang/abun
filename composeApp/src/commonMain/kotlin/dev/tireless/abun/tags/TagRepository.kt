package dev.tireless.abun.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.core.time.currentInstant
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.Tag as DbTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Instant

class TagRepository(
  private val database: AppDatabase,
) {
  private val queries = database.tagsQueries

  private val tagsFlow: Flow<List<Tag>> =
    queries
      .selectAllTags()
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { rows -> rows.map { it.toDomain() } }

  fun observeAll(): Flow<List<Tag>> = tagsFlow

  fun observeByDomain(domain: TagDomain): Flow<List<Tag>> =
    tagsFlow.map { list -> list.filter { it.appliesTo(domain) } }

  fun upsert(draft: TagDraft): Tag =
    database.transactionWithResult {
      val now = currentInstant()
      val normalizedDomains = draft.domains.ifEmpty { setOf(TagDomain.All) }
      queries.insertTag(
        name = draft.name.trim(),
        path = draft.path.trim(),
        color_hex = draft.colorHex,
        domains = normalizedDomains.encode(),
        description = draft.description?.trim()?.takeIf { it.isNotEmpty() },
        created_at = now.toEpochMilliseconds(),
        updated_at = now.toEpochMilliseconds(),
      )
      val id = queries.lastInsertedTagId().executeAsOne()
      queries.selectTagById(id).executeAsOne().toDomain()
    }

  fun update(update: TagUpdate) {
    val now = currentInstant()
    queries.updateTag(
      name = update.name.trim(),
      path = update.path.trim(),
      color_hex = update.colorHex,
      domains = update.domains.ifEmpty { setOf(TagDomain.All) }.encode(),
      description = update.description?.trim()?.takeIf { it.isNotEmpty() },
      updated_at = now.toEpochMilliseconds(),
      id = update.id,
    )
  }

  fun delete(id: Long) {
    queries.deleteTag(id)
  }
}

class TagManagementViewModel(
  private val repository: TagRepository,
) : ViewModel() {
  val tags =
    repository
      .observeAll()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  fun createTag(draft: TagDraft) = repository.upsert(draft)

  fun updateTag(update: TagUpdate) = repository.update(update)

  fun deleteTag(id: Long) = repository.delete(id)
}

private fun DbTag.toDomain(): Tag =
  Tag(
    id = id,
    name = name,
    path = path,
    colorHex = color_hex,
    domains = domains.decodeDomains(),
    description = description,
    createdAt = Instant.fromEpochMilliseconds(created_at),
    updatedAt = Instant.fromEpochMilliseconds(updated_at),
  )

private fun Set<TagDomain>.encode(): String = joinToString(separator = ",") { it.name }

private fun String.decodeDomains(): Set<TagDomain> =
  split(',')
    .mapNotNull { raw -> raw.trim().takeIf { it.isNotEmpty() } }
    .mapNotNull { value -> runCatching { TagDomain.valueOf(value) }.getOrNull() }
    .toSet()
    .ifEmpty { setOf(TagDomain.All) }

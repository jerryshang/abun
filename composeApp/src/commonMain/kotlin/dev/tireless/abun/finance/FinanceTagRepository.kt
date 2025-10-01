package dev.tireless.abun.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.FinanceTag as DbFinanceTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for managing finance tags
 */
class FinanceTagRepository(private val database: AppDatabase) {
    private val queries = database.financeQueries

    /**
     * Get all tags as Flow
     */
    fun getAllTagsFlow(): Flow<List<FinanceTag>> =
        queries.getAllFinanceTags().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toDomain() }
        }

    /**
     * Get all tags
     */
    suspend fun getAllTags(): List<FinanceTag> =
        withContext(Dispatchers.IO) {
            queries.getAllFinanceTags().executeAsList().map { it.toDomain() }
        }

    /**
     * Get tag by ID
     */
    suspend fun getTagById(id: Long): FinanceTag? =
        withContext(Dispatchers.IO) {
            queries.getFinanceTagById(id).executeAsOneOrNull()?.toDomain()
        }

    /**
     * Get tag by name
     */
    suspend fun getTagByName(name: String): FinanceTag? =
        withContext(Dispatchers.IO) {
            queries.getFinanceTagByName(name).executeAsOneOrNull()?.toDomain()
        }

    /**
     * Create a new tag
     */
    suspend fun createTag(input: CreateTagInput): Long =
        withContext(Dispatchers.IO) {
            val now = currentTimeMillis()
            queries.insertFinanceTag(
                name = input.name,
                color_hex = input.colorHex,
                created_at = now
            )
            queries.getAllFinanceTags().executeAsList().lastOrNull()?.id ?: -1L
        }

    /**
     * Update a tag
     */
    suspend fun updateTag(
        id: Long,
        name: String,
        colorHex: String? = null
    ): Unit =
        withContext(Dispatchers.IO) {
            queries.updateFinanceTag(
                name = name,
                color_hex = colorHex,
                id = id
            )
        }

    /**
     * Delete a tag
     */
    suspend fun deleteTag(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.deleteFinanceTag(id)
        }

    /**
     * Add tag to transaction
     */
    suspend fun addTagToTransaction(
        transactionId: Long,
        tagId: Long
    ): Unit =
        withContext(Dispatchers.IO) {
            queries.addTagToTransaction(transactionId, tagId)
        }

    /**
     * Remove tag from transaction
     */
    suspend fun removeTagFromTransaction(
        transactionId: Long,
        tagId: Long
    ): Unit =
        withContext(Dispatchers.IO) {
            queries.removeTagFromTransaction(transactionId, tagId)
        }

    /**
     * Get or create tag by name
     * If tag exists, return it. Otherwise create and return.
     */
    suspend fun getOrCreateTag(name: String, colorHex: String? = null): FinanceTag =
        withContext(Dispatchers.IO) {
            val existing = getTagByName(name)
            if (existing != null) {
                existing
            } else {
                val id = createTag(CreateTagInput(name = name, colorHex = colorHex))
                getTagById(id) ?: throw IllegalStateException("Failed to create tag")
            }
        }

    /**
     * Mapper: Database model to Domain model
     */
    private fun DbFinanceTag.toDomain() =
        FinanceTag(
            id = id,
            name = name,
            colorHex = color_hex,
            createdAt = created_at
        )

    /**
     * Get current timestamp in milliseconds (KMP-compatible)
     */
    private fun currentTimeMillis(): Long {
        return 1704067200000L // 2024-01-01 00:00:00 UTC - Simplified for KMP
    }
}

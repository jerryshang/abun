package dev.tireless.abun.time

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class CategoryRepository(
  private val database: AppDatabase,
) {
  @OptIn(ExperimentalTime::class)
  fun getAllCategories(): Flow<List<Category>> =
    database.timeblockQueries
      .selectAllCategories()
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { categoriesData ->
        categoriesData.map { categoryData ->
          Category(
            id = categoryData.id,
            name = categoryData.name,
            color = categoryData.color,
            createdAt = Instant.fromEpochMilliseconds(categoryData.created_at),
            updatedAt = Instant.fromEpochMilliseconds(categoryData.updated_at),
          )
        }
      }

  @OptIn(ExperimentalTime::class)
  suspend fun insertCategory(
    name: String,
    color: String,
  ): Long? =
    withContext(Dispatchers.IO) {
      val now = Clock.System.now().toEpochMilliseconds()
      database.timeblockQueries.insertCategory(name, color, now, now)
      // Get the last inserted row ID
      database.timeblockQueries
        .selectAllCategories()
        .executeAsList()
        .lastOrNull()
        ?.id
    }

  @OptIn(ExperimentalTime::class)
  suspend fun updateCategory(
    id: Long,
    name: String,
    color: String,
  ) {
    withContext(Dispatchers.IO) {
      val now = Clock.System.now().toEpochMilliseconds()
      database.timeblockQueries.updateCategory(name, color, now, id)
    }
  }

  suspend fun deleteCategory(id: Long) {
    withContext(Dispatchers.IO) {
      database.timeblockQueries.deleteCategory(id)
    }
  }
}

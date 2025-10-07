package dev.tireless.abun.time

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.tireless.abun.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TaskRepository(
  private val database: AppDatabase,
) {
  fun getAllTasks(): Flow<List<Task>> =
    database.timeblockQueries
      .selectAllTasks()
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData ->
        tasksData.map { taskData ->
          Task(
            id = taskData.id,
            name = taskData.name,
            description = taskData.description,
            categoryId = taskData.category_id,
            strategy = taskData.strategy,
            createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
            updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
            categoryName = taskData.category_name,
            categoryColor = taskData.category_color,
          )
        }
      }

  suspend fun getTaskById(id: Long): Task? =
    withContext(Dispatchers.IO) {
      database.timeblockQueries
        .selectTaskById(id)
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .first()
        ?.let { taskData ->
          Task(
            id = taskData.id,
            name = taskData.name,
            description = taskData.description,
            categoryId = taskData.category_id,
            strategy = taskData.strategy,
            createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
            updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
            categoryName = taskData.category_name,
            categoryColor = taskData.category_color,
          )
        }
    }

  fun getTasksByCategory(categoryId: Long): Flow<List<Task>> =
    database.timeblockQueries
      .selectTasksByCategory(categoryId)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData ->
        tasksData.map { taskData ->
          Task(
            id = taskData.id,
            name = taskData.name,
            description = taskData.description,
            categoryId = taskData.category_id,
            strategy = taskData.strategy,
            createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
            updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
            categoryName = taskData.category_name,
            categoryColor = taskData.category_color,
          )
        }
      }

  fun getTasksByStrategy(strategy: String): Flow<List<Task>> =
    database.timeblockQueries
      .selectTasksByStrategy(strategy)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData ->
        tasksData.map { taskData ->
          Task(
            id = taskData.id,
            name = taskData.name,
            description = taskData.description,
            categoryId = taskData.category_id,
            strategy = taskData.strategy,
            createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
            updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
            categoryName = taskData.category_name,
            categoryColor = taskData.category_color,
          )
        }
      }

  suspend fun insertTask(
    name: String,
    description: String?,
    categoryId: Long,
    strategy: String = "plan",
  ): Long? =
    withContext(Dispatchers.IO) {
      val now = Clock.System.now().toEpochMilliseconds()
      database.timeblockQueries.insertTask(name, description, categoryId, strategy, now, now)
      // Get the last inserted row ID
      database.timeblockQueries
        .selectAllTasks()
        .executeAsList()
        .lastOrNull()
        ?.id
    }

  suspend fun updateTask(
    id: Long,
    name: String,
    description: String?,
    categoryId: Long,
    strategy: String,
  ) {
    withContext(Dispatchers.IO) {
      val now = Clock.System.now().toEpochMilliseconds()
      database.timeblockQueries.updateTask(name, description, categoryId, strategy, now, id)
    }
  }

  suspend fun deleteTask(id: Long) {
    withContext(Dispatchers.IO) {
      database.timeblockQueries.deleteTask(id)
    }
  }
}

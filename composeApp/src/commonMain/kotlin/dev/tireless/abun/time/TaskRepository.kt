package dev.tireless.abun.time

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.SelectAllTasks
import dev.tireless.abun.database.SelectTaskById
import dev.tireless.abun.database.SelectTasksByParent
import dev.tireless.abun.database.SelectTasksByStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import dev.tireless.abun.core.time.currentEpochMillis
import kotlinx.datetime.Instant

class TaskRepository(
  private val database: AppDatabase,
) {
  fun getAllTasks(): Flow<List<Task>> =
    database.timeblockQueries
      .selectAllTasks()
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData -> tasksData.map(::taskFromRow) }

  suspend fun getTaskById(id: Long): Task? =
    withContext(Dispatchers.IO) {
      database.timeblockQueries
        .selectTaskById(id)
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .first()
        ?.let(::taskFromRow)
    }

  fun getTasksByParent(parentTaskId: Long?): Flow<List<Task>> =
    database.timeblockQueries
      .selectTasksByParent(parentTaskId)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData -> tasksData.map(::taskFromRow) }

  fun getTasksByStrategy(strategy: String): Flow<List<Task>> =
    database.timeblockQueries
      .selectTasksByStrategy(strategy)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData -> tasksData.map(::taskFromRow) }

  suspend fun insertTask(
    name: String,
    description: String?,
    parentTaskId: Long?,
    strategy: String = "plan",
  ): Long? =
    withContext(Dispatchers.IO) {
      val now = currentEpochMillis()
      database.timeblockQueries.insertTask(name, description, parentTaskId, strategy, now, now)
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
    parentTaskId: Long?,
    strategy: String,
  ) {
    withContext(Dispatchers.IO) {
      val now = currentEpochMillis()
      database.timeblockQueries.updateTask(name, description, parentTaskId, strategy, now, id)
    }
  }

  suspend fun deleteTask(id: Long) {
    withContext(Dispatchers.IO) {
      database.timeblockQueries.deleteTask(id)
    }
  }

  private fun taskFromRow(taskData: SelectAllTasks): Task =
    Task(
      id = taskData.id,
      name = taskData.name,
      description = taskData.description,
      parentTaskId = taskData.parent_task_id,
      strategy = taskData.strategy,
      createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
      updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
      parentTaskName = taskData.parent_name,
      parentTaskStrategy = taskData.parent_strategy,
    )

  private fun taskFromRow(taskData: SelectTaskById): Task =
    Task(
      id = taskData.id,
      name = taskData.name,
      description = taskData.description,
      parentTaskId = taskData.parent_task_id,
      strategy = taskData.strategy,
      createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
      updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
      parentTaskName = taskData.parent_name,
      parentTaskStrategy = taskData.parent_strategy,
    )

  private fun taskFromRow(taskData: SelectTasksByParent): Task =
    Task(
      id = taskData.id,
      name = taskData.name,
      description = taskData.description,
      parentTaskId = taskData.parent_task_id,
      strategy = taskData.strategy,
      createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
      updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
      parentTaskName = taskData.parent_name,
      parentTaskStrategy = taskData.parent_strategy,
    )

  private fun taskFromRow(taskData: SelectTasksByStrategy): Task =
    Task(
      id = taskData.id,
      name = taskData.name,
      description = taskData.description,
      parentTaskId = taskData.parent_task_id,
      strategy = taskData.strategy,
      createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
      updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
      parentTaskName = taskData.parent_name,
      parentTaskStrategy = taskData.parent_strategy,
    )
}

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
    database.timeQueries
      .selectAllTasks()
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData -> tasksData.map(::taskFromRow) }

  suspend fun getTaskById(id: Long): Task? =
    withContext(Dispatchers.IO) {
      database.timeQueries
        .selectTaskById(id)
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .first()
        ?.let(::taskFromRow)
    }

  fun getTasksByParent(parentId: Long?): Flow<List<Task>> =
    database.timeQueries
      .selectTasksByParent(parentId)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData -> tasksData.map(::taskFromRow) }

  fun getTasksByStrategy(strategy: String): Flow<List<Task>> =
    database.timeQueries
      .selectTasksByStrategy(strategy)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { tasksData -> tasksData.map(::taskFromRow) }

  suspend fun insertTask(
    name: String,
    description: String?,
    parentId: Long?,
    strategy: String = "plan",
    constraint: TaskConstraint = TaskConstraint.Exactly,
  ): Long? =
    withContext(Dispatchers.IO) {
      val now = currentEpochMillis()
      database.timeQueries.insertTask(name, description, parentId, strategy, constraint.raw, now, now)
      // Get the last inserted row ID
      database.timeQueries
        .selectAllTasks()
        .executeAsList()
        .lastOrNull()
        ?.id
    }

  suspend fun updateTask(
    id: Long,
    name: String,
    description: String?,
    parentId: Long?,
    strategy: String,
    constraint: TaskConstraint,
  ) {
    withContext(Dispatchers.IO) {
      val now = currentEpochMillis()
      database.timeQueries.updateTask(name, description, parentId, strategy, constraint.raw, now, id)
    }
  }

  suspend fun deleteTask(id: Long) {
    withContext(Dispatchers.IO) {
      database.timeQueries.deleteTask(id)
    }
  }

  private fun taskFromRow(taskData: SelectAllTasks): Task =
    Task(
      id = taskData.id,
      name = taskData.name,
      description = taskData.description,
      parentId = taskData.parent_id,
      strategy = taskData.strategy,
      constraint = TaskConstraint.fromRaw(taskData.constraint_value),
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
      parentId = taskData.parent_id,
      strategy = taskData.strategy,
      constraint = TaskConstraint.fromRaw(taskData.constraint_value),
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
      parentId = taskData.parent_id,
      strategy = taskData.strategy,
      constraint = TaskConstraint.fromRaw(taskData.constraint_value),
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
      parentId = taskData.parent_id,
      strategy = taskData.strategy,
      constraint = TaskConstraint.fromRaw(taskData.constraint_value),
      createdAt = Instant.fromEpochMilliseconds(taskData.created_at),
      updatedAt = Instant.fromEpochMilliseconds(taskData.updated_at),
      parentTaskName = taskData.parent_name,
      parentTaskStrategy = taskData.parent_strategy,
    )
}

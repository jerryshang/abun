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
import dev.tireless.abun.core.time.currentEpochMillis
import kotlinx.datetime.Instant

class TimeblockRepository(
  private val database: AppDatabase,
) {
  fun getTimeblocksByDateRange(
    startDate: String,
    endDate: String,
  ): Flow<List<Timeblock>> =
    database.timeQueries
      .selectTimeblocksByDateRange(startDate, endDate)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { timeblockData ->
        timeblockData.map { data ->
          Timeblock(
            id = data.id,
            startTime = data.start_time,
            endTime = data.end_time,
            taskId = data.task_id,
            createdAt = Instant.fromEpochMilliseconds(data.created_at),
            updatedAt = Instant.fromEpochMilliseconds(data.updated_at),
            taskName = data.task_name,
            taskDescription = data.task_description,
            taskStrategy = data.task_strategy,
            taskParentName = data.parent_task_name,
            taskParentStrategy = data.parent_task_strategy,
          )
        }
      }

  suspend fun getTimeblockById(id: Long): Timeblock? =
    withContext(Dispatchers.IO) {
      database.timeQueries
        .selectTimeblockById(id)
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .first()
        ?.let { data ->
          Timeblock(
            id = data.id,
            startTime = data.start_time,
            endTime = data.end_time,
            taskId = data.task_id,
            createdAt = Instant.fromEpochMilliseconds(data.created_at),
            updatedAt = Instant.fromEpochMilliseconds(data.updated_at),
            taskName = data.task_name,
            taskDescription = data.task_description,
            taskStrategy = data.task_strategy,
            taskParentName = data.parent_task_name,
            taskParentStrategy = data.parent_task_strategy,
          )
        }
    }

  suspend fun insertTimeblock(
    startTime: String,
    endTime: String,
    taskId: Long,
  ): Long? =
    withContext(Dispatchers.IO) {
      val now = currentEpochMillis()
      database.timeQueries.insertTimeblock(startTime, endTime, taskId, now, now)
      // Get the last inserted row ID - this is a simplified approach
      database.timeQueries
        .selectTimeblocksByDateRange(
          startTime.substring(0, 10), // Extract date part
          startTime.substring(0, 10),
        ).executeAsList()
        .lastOrNull()
        ?.id
    }

  suspend fun updateTimeblock(
    id: Long,
    startTime: String,
    endTime: String,
    taskId: Long,
  ) {
    withContext(Dispatchers.IO) {
      val now = currentEpochMillis()
      database.timeQueries.updateTimeblock(startTime, endTime, taskId, now, id)
    }
  }

  suspend fun deleteTimeblock(id: Long) {
    withContext(Dispatchers.IO) {
      database.timeQueries.deleteTimeblock(id)
    }
  }
}

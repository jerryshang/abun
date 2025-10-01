package dev.tireless.abun.time

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.time.Timeblock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TimeblockRepository(
    private val database: AppDatabase
) {
    fun getTimeblocksByDateRange(startDate: String, endDate: String): Flow<List<Timeblock>> =
        database.timeblockQueries
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
                        createdAt = data.created_at,
                        updatedAt = data.updated_at,
                        taskName = data.task_name,
                        taskDescription = data.task_description,
                        taskStrategy = data.task_strategy,
                        categoryName = data.category_name,
                        categoryColor = data.category_color
                    )
                }
            }

    suspend fun getTimeblockById(id: Long): Timeblock? =
        withContext(Dispatchers.IO) {
            database.timeblockQueries
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
                        createdAt = data.created_at,
                        updatedAt = data.updated_at,
                        taskName = data.task_name,
                        taskDescription = data.task_description,
                        taskStrategy = data.task_strategy,
                        categoryName = data.category_name,
                        categoryColor = data.category_color
                    )
                }
        }

    suspend fun insertTimeblock(
        startTime: String,
        endTime: String,
        taskId: Long
    ): Long? {
        return withContext(Dispatchers.IO) {
            val now = getCurrentTimestamp()
            database.timeblockQueries.insertTimeblock(startTime, endTime, taskId, now, now)
            // Get the last inserted row ID - this is a simplified approach
            database.timeblockQueries.selectTimeblocksByDateRange(
                startTime.substring(0, 10), // Extract date part
                startTime.substring(0, 10)
            ).executeAsList().lastOrNull()?.id
        }
    }

    suspend fun updateTimeblock(
        id: Long,
        startTime: String,
        endTime: String,
        taskId: Long
    ) {
        withContext(Dispatchers.IO) {
            val now = getCurrentTimestamp()
            database.timeblockQueries.updateTimeblock(startTime, endTime, taskId, now, id)
        }
    }

    suspend fun deleteTimeblock(id: Long) {
        withContext(Dispatchers.IO) {
            database.timeblockQueries.deleteTimeblock(id)
        }
    }

    private fun getCurrentTimestamp(): String {
        return "2024-01-01T00:00:00" // Simplified for KMP
    }
}
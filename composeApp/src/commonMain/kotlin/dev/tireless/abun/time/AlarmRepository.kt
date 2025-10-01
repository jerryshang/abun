package dev.tireless.abun.time

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.time.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AlarmRepository(
    private val database: AppDatabase
) {
    fun getAlarmsByTimeblock(timeblockId: Long): Flow<List<Alarm>> =
        database.timeblockQueries
            .selectAlarmsByTimeblock(timeblockId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { alarmData ->
                alarmData.map { data ->
                    Alarm(
                        id = data.id,
                        timeblockId = data.timeblock_id,
                        minutesBefore = data.minutes_before,
                        isEnabled = data.is_enabled == 1L,
                        createdAt = data.created_at
                    )
                }
            }

    suspend fun insertAlarm(
        timeblockId: Long,
        minutesBefore: Long,
        isEnabled: Boolean = true
    ): Long? {
        return withContext(Dispatchers.IO) {
            val now = getCurrentTimestamp()
            database.timeblockQueries.insertAlarm(
                timeblockId,
                minutesBefore,
                if (isEnabled) 1L else 0L,
                now
            )
            // Get the last inserted row ID
            database.timeblockQueries.selectAlarmsByTimeblock(timeblockId)
                .executeAsList().lastOrNull()?.id
        }
    }

    suspend fun updateAlarm(id: Long, minutesBefore: Long, isEnabled: Boolean) {
        withContext(Dispatchers.IO) {
            database.timeblockQueries.updateAlarm(
                minutesBefore,
                if (isEnabled) 1L else 0L,
                id
            )
        }
    }

    suspend fun deleteAlarm(id: Long) {
        withContext(Dispatchers.IO) {
            database.timeblockQueries.deleteAlarm(id)
        }
    }

    suspend fun deleteAlarmsByTimeblock(timeblockId: Long) {
        withContext(Dispatchers.IO) {
            database.timeblockQueries.deleteAlarmsByTimeblock(timeblockId)
        }
    }

    private fun getCurrentTimestamp(): String {
        return "2024-01-01T00:00:00" // Simplified for KMP
    }
}
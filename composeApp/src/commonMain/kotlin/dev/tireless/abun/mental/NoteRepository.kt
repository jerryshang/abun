package dev.tireless.abun.mental

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.Notes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class NoteRepository(
    private val database: AppDatabase,
) {
    fun getAllNotes(): Flow<List<Notes>> =
        database.noteQueries
            .selectAllNotes()
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun getNoteById(id: Long): Notes? =
        withContext(Dispatchers.IO) {
            database.noteQueries
                .selectNoteById(id)
                .asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .first()
        }

    suspend fun insertNote(
        title: String,
        content: String,
        createdAt: String,
        updatedAt: String,
    ): Long = withContext(Dispatchers.IO) {
        database.noteQueries.insertNote(title, content, createdAt, updatedAt)
        database.noteQueries.selectAllNotes().executeAsList().lastOrNull()?.id ?: 0L
    }

    suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        updatedAt: String,
    ) {
        withContext(Dispatchers.IO) {
            database.noteQueries.updateNote(title, content, updatedAt, id)
        }
    }

    suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.IO) {
            database.noteQueries.deleteNote(id)
        }
    }

    fun searchNotes(query: String): Flow<List<Notes>> =
        database.noteQueries
            .searchNotes(query, query)
            .asFlow()
            .mapToList(Dispatchers.IO)
}
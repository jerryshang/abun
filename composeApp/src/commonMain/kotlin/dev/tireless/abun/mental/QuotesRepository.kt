package dev.tireless.abun.mental

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.Quote as DbQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class QuotesRepository(
  private val database: AppDatabase,
) {
  suspend fun getRandomQuote(): DbQuote? =
    withContext(Dispatchers.IO) {
      database.quoteQueries
        .selectRandomQuote()
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .first()
    }

  suspend fun insertQuote(
    id: String,
    content: String,
    source: String?,
    createdAt: Long,
    updatedAt: Long,
  ) {
    withContext(Dispatchers.IO) {
      database.quoteQueries.insertQuote(id, content, source, createdAt, updatedAt)
    }
  }

  suspend fun deleteQuote(id: String) {
    withContext(Dispatchers.IO) {
      database.quoteQueries.deleteQuote(id)
    }
  }

  suspend fun updateQuote(
    id: String,
    content: String,
    source: String?,
    updatedAt: Long,
  ) {
    withContext(Dispatchers.IO) {
      database.quoteQueries.updateQuote(content, source, updatedAt, id)
    }
  }
}

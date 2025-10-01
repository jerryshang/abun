package dev.tireless.abun.core

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.tireless.abun.database.AppDatabase

actual class DatabaseDriverFactory(
  private val context: Context,
) {
  actual fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(
      AppDatabase.Schema,
      context,
      "app.db"
    )
  }
}

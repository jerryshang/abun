package dev.tireless.abun.core

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.tireless.abun.database.AppDatabase

actual class DatabaseDriverFactory(
  private val context: Context,
) {
  actual fun createDriver(): SqlDriver =
    AndroidSqliteDriver(
      schema = AppDatabase.Schema,
      context = context,
      name = "app_v2.db",
      callback =
        object : AndroidSqliteDriver.Callback(AppDatabase.Schema) {
          override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            db.execSQL("PRAGMA foreign_keys=ON;")
          }
        },
    )
}

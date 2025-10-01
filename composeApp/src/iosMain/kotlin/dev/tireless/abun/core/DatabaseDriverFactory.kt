package dev.tireless.abun.core

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.tireless.abun.database.AppDatabase

actual class DatabaseDriverFactory {
  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(AppDatabase.Schema, "app.db")
  }
}

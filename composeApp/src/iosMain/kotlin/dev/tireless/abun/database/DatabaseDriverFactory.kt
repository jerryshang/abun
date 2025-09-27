package dev.tireless.abun.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
  actual fun createDriver(): SqlDriver = NativeSqliteDriver(AppDatabase.Schema, "app.db")
}

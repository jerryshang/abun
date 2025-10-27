package dev.tireless.abun.core

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.tireless.abun.database.AppDatabase

actual class DatabaseDriverFactory {
  actual fun createDriver(): SqlDriver {
    val driver = NativeSqliteDriver(AppDatabase.Schema, "app.db")
    driver.execute(null, "PRAGMA foreign_keys=ON", 0)
    return driver
  }
}

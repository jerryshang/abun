package dev.tireless.abun.core

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
  fun createDriver(): SqlDriver
}

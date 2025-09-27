package dev.tireless.abun.di

import dev.tireless.abun.database.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val iosModule =
  module {
    single { DatabaseDriverFactory() }
  }

actual val platformModules: List<Module> = listOf(iosModule)

package dev.tireless.abun.core

import dev.tireless.abun.core.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val iosModule =
  module {
    single { DatabaseDriverFactory() }
  }

actual val platformModules: List<Module> = listOf(iosModule)

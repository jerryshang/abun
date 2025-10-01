package dev.tireless.abun.core

import dev.tireless.abun.core.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val androidModule =
  module {
    single { DatabaseDriverFactory(androidContext()) }
  }

actual val platformModules: List<Module> = listOf(androidModule)

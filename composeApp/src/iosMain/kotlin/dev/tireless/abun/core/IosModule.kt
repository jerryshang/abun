package dev.tireless.abun.core

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

val iosModule =
  module {
    single<Settings> {
      NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults())
    }
    single { DatabaseDriverFactory() }
  }

actual val platformModules: List<Module> = listOf(iosModule)

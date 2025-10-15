package dev.tireless.abun.core

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val androidModule =
  module {
    single<Settings> {
      val prefs =
        androidContext().getSharedPreferences("abun_preferences", Context.MODE_PRIVATE)
      SharedPreferencesSettings(prefs)
    }
    single { DatabaseDriverFactory(androidContext()) }
  }

actual val platformModules: List<Module> = listOf(androidModule)

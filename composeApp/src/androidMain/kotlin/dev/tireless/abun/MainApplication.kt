package dev.tireless.abun

import android.app.Application
import dev.tireless.abun.di.allModules
import dev.tireless.abun.di.platformModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidContext(this@MainApplication)
      modules(allModules + platformModules)
    }
  }
}
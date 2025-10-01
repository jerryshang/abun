package dev.tireless.abun

import androidx.compose.ui.window.ComposeUIViewController
import dev.tireless.abun.core.allModules
import dev.tireless.abun.core.platformModules
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
  // Initialize Koin for iOS
  startKoin {
    modules(allModules + platformModules)
  }

  App()
}

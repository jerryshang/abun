package dev.tireless.abun.core

import org.koin.core.context.startKoin

fun initKoin() {
  startKoin {
    modules(allModules)
  }
}

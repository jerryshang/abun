package dev.tireless.abun.di

import org.koin.core.context.startKoin

fun initKoin() {
  startKoin {
    modules(allModules)
  }
}

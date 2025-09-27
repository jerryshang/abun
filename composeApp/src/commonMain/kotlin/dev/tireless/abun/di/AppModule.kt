package dev.tireless.abun.di

import dev.tireless.abun.Greeting
import org.koin.dsl.module

val appModule =
  module {
    single { Greeting() }
  }

val viewModelModule =
  module {
    // ViewModels will be added here as needed
  }

val allModules =
  listOf(
    appModule,
    viewModelModule,
  )

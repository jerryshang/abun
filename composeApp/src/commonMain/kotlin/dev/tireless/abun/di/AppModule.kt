package dev.tireless.abun.di

import dev.tireless.abun.Greeting
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.DatabaseDriverFactory
import dev.tireless.abun.repository.QuotesRepository
import dev.tireless.abun.viewmodel.QuoteViewModel
import org.koin.dsl.module

val appModule =
  module {
    single { Greeting() }
    single { get<DatabaseDriverFactory>().createDriver() }
    single { AppDatabase(get()) }
    single { QuotesRepository(get()) }
  }

val viewModelModule =
  module {
    factory { QuoteViewModel(get()) }
  }

val allModules =
  listOf(
    appModule,
    viewModelModule,
  )

expect val platformModules: List<org.koin.core.module.Module>

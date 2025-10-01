package dev.tireless.abun.core

import dev.tireless.abun.Greeting
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.time.AlarmRepository
import dev.tireless.abun.time.CategoryRepository
import dev.tireless.abun.mental.QuotesRepository
import dev.tireless.abun.time.TaskRepository
import dev.tireless.abun.time.TimeblockRepository
import dev.tireless.abun.mental.QuoteViewModel
import dev.tireless.abun.time.TimeblockViewModel
import dev.tireless.abun.time.CategoryViewModel
import org.koin.dsl.module

val appModule =
  module {
    single { Greeting() }
    single { get<DatabaseDriverFactory>().createDriver() }
    single {
      val database = AppDatabase(get())
      // Initialize default data when database is created
      val categoryRepository = CategoryRepository(database)
      kotlinx.coroutines.runBlocking {
        try {
          println("Initializing default database data...")
          categoryRepository.initializeDefaultData()
          println("Default data initialization completed")
        } catch (e: Exception) {
          println("Failed to initialize default data: ${e.message}")
          e.printStackTrace()
        }
      }
      database
    }
    single { QuotesRepository(get()) }
    single { CategoryRepository(get()) }
    single { TaskRepository(get()) }
    single { TimeblockRepository(get()) }
    single { AlarmRepository(get()) }
  }

val viewModelModule =
  module {
    factory { QuoteViewModel(get()) }
    factory { TimeblockViewModel(get(), get(), get()) }
    factory { CategoryViewModel(get()) }
  }

val allModules =
  listOf(
    appModule,
    viewModelModule,
  )

expect val platformModules: List<org.koin.core.module.Module>

package dev.tireless.abun.core

import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.finance.AccountRepository
import dev.tireless.abun.finance.AccountViewModel
import dev.tireless.abun.finance.TransactionGroupRepository
import dev.tireless.abun.finance.TransactionRepository
import dev.tireless.abun.finance.TransactionViewModel
import dev.tireless.abun.mental.NoteRepository
import dev.tireless.abun.mental.NoteViewModel
import dev.tireless.abun.mental.QuoteViewModel
import dev.tireless.abun.mental.QuotesRepository
import dev.tireless.abun.notes.RichNoteRepository
import dev.tireless.abun.notes.NotesViewModel
import dev.tireless.abun.tags.TagManagementViewModel
import dev.tireless.abun.tags.TagRepository
import dev.tireless.abun.tasks.TaskBoardViewModel
import dev.tireless.abun.tasks.TaskPlannerRepository
import dev.tireless.abun.time.AlarmRepository
import dev.tireless.abun.time.CategoryRepository
import dev.tireless.abun.time.CategoryViewModel
import dev.tireless.abun.time.FocusTimerViewModel
import dev.tireless.abun.time.TaskRepository
import dev.tireless.abun.time.TimeblockRepository
import dev.tireless.abun.time.TimeblockViewModel
import org.koin.dsl.module

val appModule =
  module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { AppDatabase(get()) }
    single { QuotesRepository(get()) }
    single { NoteRepository(get()) }
    single { CategoryRepository(get()) }
    single { TaskRepository(get()) }
    single { TimeblockRepository(get()) }
    single { AlarmRepository(get()) }
    single { TagRepository() }
    single { TaskPlannerRepository(get()) }
    single { RichNoteRepository(get()) }
    // Finance repositories
    single { AccountRepository(get()) }
    single { TransactionRepository(get(), get()) } // database, accountRepository
    single { TransactionGroupRepository(get(), get()) } // database, transactionRepository
  }

val viewModelModule =
  module {
    factory { QuoteViewModel(get()) }
    factory { NoteViewModel(get()) }
    factory { TimeblockViewModel(get(), get(), get()) }
    factory { CategoryViewModel(get()) }
    factory { FocusTimerViewModel(get(), get()) }
    factory { TagManagementViewModel(get()) }
    factory { TaskBoardViewModel(get()) }
    factory { NotesViewModel(get()) }
    // Finance ViewModels
    factory { TransactionViewModel(get(), get(), get()) }
    factory { AccountViewModel(get()) } // accountRepository, accountLoaderService
  }

val allModules =
  listOf(
    appModule,
    viewModelModule,
  )

expect val platformModules: List<org.koin.core.module.Module>

# Abun - Personal Life Management System

Abun is a comprehensive Kotlin Multiplatform application designed to help you manage all aspects of your personal life across Android and iOS platforms.

## Product Design

Abun is organized into four main pillars, each addressing a different dimension of personal life management:

---

## 🕐 Time

Manage your time effectively with integrated scheduling, task management, routines, and focus tools.

### Features

#### Schedule (Timeblock Logging)
- **Visual time blocking** for planning your day
- **Category-based scheduling** with customizable colors
- **Task-timeblock integration** to connect tasks with scheduled time
- **Alarm system** for timeblock reminders
- View timeblocks by date range with calendar integration

**Implementation Status**: ✅ Implemented
- Files: `time/Timeblock*.kt`, `time/Category*.kt`, `time/Task*.kt`, `time/Alarm*.kt`

#### Tasks
- **Hierarchical task organization** (future: parent-child relationships)
- **Priority management**
- **Strategy-based workflow**: Plan → TODO → Do → Check
- **Category tagging** for better organization
- Integration with timeblock scheduling

**Implementation Status**: 🟡 Partially Implemented
- Files: `time/TaskRepository.kt`, `time/TimeblockModels.kt`
- Missing: Priority system, hierarchy, dedicated task UI

#### Routines
- **Daily/Weekly/Monthly/Year routines** templates
- Automatic task generation from routine templates
- Routine tracking and completion history

**Implementation Status**: ⚪ Placeholder

#### Focus (Pomodoro Timer)
- **Pomodoro technique** timer
- Focus session tracking
- Break reminders
- Statistics on focus time

**Implementation Status**: ⚪ Placeholder

---

## 🏗️ Material

Track and manage your physical resources, finances, and possessions.

### Features

#### Finance (Double-Entry Bookkeeping)
- **Double-entry accounting** system
- Income and expense tracking
- Account balances and transfers
- Financial reports and analytics

**Implementation Status**: ⚪ Placeholder

#### Bills
- **Recurring payment tracking**
- Payment due date reminders
- Bill history and categorization
- Budget vs. actual comparisons

**Implementation Status**: ⚪ Placeholder

#### Inventory
- **Home inventory management**
- Item location tracking
- Expiration date monitoring (food, medicines)
- Purchase history and warranty tracking

**Implementation Status**: ⚪ Placeholder

#### Lists
- **Shopping lists** with quantity tracking
- **Wish lists** for future purchases
- **Price comparison** tool for smart shopping
- List sharing and collaboration (future)

**Implementation Status**: 🟡 Partially Implemented
- Files: `material/PriceScreen.kt`
- Missing: Persistent shopping/wish lists, list management

---

## 🧠 Mental

Capture thoughts, ideas, and track your intellectual and emotional growth.

### Features

#### Notes
- **Quick note-taking** with markdown support
- Full-text search across all notes
- Note organization and tagging
- Rich text formatting

**Implementation Status**: 🟡 Partially Implemented
- Files: `mental/Note*.kt`, `mental/QuickNoteScreen.kt`
- Missing: Markdown rendering, tagging system

#### Journal
- **Date-based journaling** with templates
- Daily/weekly reflection prompts
- Mood tracking
- Journal entry history and search

**Implementation Status**: ⚪ Placeholder
- Can leverage note system as foundation

#### Sparks
- **Quote collection** and random inspiration
- Save inspiring ideas and thoughts
- Tag and categorize sparks
- Daily spark notifications

**Implementation Status**: 🟡 Partially Implemented
- Files: `mental/Quotes*.kt`, `mental/QuoteViewModel.kt`
- Display in HomeScreen (App.kt)
- Missing: Quote management UI, tagging, notifications

#### Media
- **Watchlist** for movies, TV shows, books
- **Reading list** tracking
- **Music playlist** management
- Progress tracking and ratings
- Recommendations and reviews

**Implementation Status**: ⚪ Placeholder

---

## ⚡ Energy

Personal energy and wellness tracking.

### Features

**Implementation Status**: ⚪ Placeholder - Reserved for future wellness features
- Sleep tracking
- Exercise logging
- Habit tracking
- Energy level monitoring
- Health metrics integration

---

## Technical Architecture

### Platform
- **Kotlin Multiplatform** (KMP) for shared business logic
- **Compose Multiplatform** for unified UI across platforms
- **SQLDelight** for type-safe database operations
- **Koin** for dependency injection

### Project Structure
```
composeApp/src/
├── commonMain/kotlin/dev/tireless/abun/
│   ├── core/              # Shared infrastructure
│   │   ├── AppModule.kt           # Koin DI configuration
│   │   ├── KoinApplication.kt     # Koin setup
│   │   └── DatabaseDriverFactory.kt
│   ├── time/              # Time management feature (flat structure)
│   │   ├── TimeblockModels.kt     # Models: Category, Task, Timeblock, Alarm
│   │   ├── *Repository.kt         # Repositories for data access
│   │   ├── *ViewModel.kt          # ViewModels for business logic
│   │   └── *Screen.kt             # UI components
│   ├── mental/            # Mental/knowledge feature (flat structure)
│   │   ├── NoteModels.kt          # Note models
│   │   ├── *Repository.kt         # Note & Quote repositories
│   │   ├── *ViewModel.kt          # ViewModels
│   │   └── *Screen.kt             # UI components
│   ├── material/          # Material/resources feature (flat structure)
│   │   └── PriceScreen.kt
│   └── App.kt             # Main app with navigation
├── commonMain/sqldelight/dev/tireless/abun/database/
│   ├── Note.sq            # Notes schema and queries
│   ├── Quote.sq           # Quotes schema and queries
│   └── Timeblock.sq       # Timeblock, tasks, categories schema
├── androidMain/           # Android-specific code
└── iosMain/               # iOS-specific code
```

**Structure Philosophy**: Features use a **flat structure** - all files (models, repositories, viewmodels, UI) are at the feature root level. This keeps related code together and easy to find, without artificial separation into subdirectories.

### Database Schema

#### Time Domain
- `categories` - Color-coded categories for tasks/timeblocks
- `tasks` - Tasks with strategy (plan/todo/do/check) and category
- `timeblocks` - Time scheduling blocks linked to tasks
- `alarms` - Reminders for timeblocks

#### Mental Domain
- `notes` - Quick notes with title, content, timestamps
- `quotes` - Inspirational quotes with source attribution

### Navigation
Bottom navigation with 4 main sections:
1. **Home** - Dashboard with daily spark/quote
2. **Financial** - Price comparison and future finance features
3. **Timeblock** - Schedule and time management
4. **Settings** - Category management and app settings

---

## Build Instructions

### Android Build
```bash
./gradlew :composeApp:assembleDebug
```

### iOS Build
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

### Run Tests
```bash
./gradlew test
```

---

## Development Guidelines

Refer to [AGENTS.md](./AGENTS.md) for comprehensive development guidelines, including:
- Kotlin Multiplatform compatibility rules
- Code style and conventions
- Build verification procedures
- Platform-specific considerations

Refer to [CLAUDE.md](./CLAUDE.md) for AI assistant specific instructions.

---

## Legend

- ✅ **Implemented** - Feature is fully functional
- 🟡 **Partially Implemented** - Core functionality exists, needs completion
- ⚪ **Placeholder** - Planned feature, not yet started

---

## Contributing

When implementing new features:
1. Create files in the appropriate feature directory (time/, mental/, material/)
2. Use **flat structure** - put all feature files at the feature root
3. Design database schema in appropriate `.sq` file
4. **Always test both Android and iOS builds** before committing

---

## License

[Specify your license here]

---

## Roadmap

### Phase 1 (Current)
- ✅ Timeblock scheduling
- 🟡 Complete notes system with markdown
- 🟡 Finish task management UI

### Phase 2
- Routines system
- Pomodoro focus timer
- Shopping/wish lists

### Phase 3
- Finance tracking
- Bills management
- Journal with templates

### Phase 4
- Media watchlists
- Inventory management
- Energy/wellness tracking

---

*Last Updated: 2025-10-01*

## Application Function Tree

```
App
├── Shell & Navigation
│   ├── App() composable hosts theme, NavHost, bottom tabs, and remembers theme preference
│   └── Route sealed interface defines Home, Material, Mental, Time, Settings, and finance/time detail routes
├── Home
│   └── HomeScreen provides action cards routing into Time, Notes (Mental), and Finance domains
├── Material (finance)
│   ├── FinanceScreen uses TransactionViewModel (TransactionRepository + AccountRepository + TransactionGroupRepository) for the transaction feed, account filtering, deletion flows, and multi-action FAB
│   ├── AccountManagementScreen + AccountEditScreen work through AccountViewModel (AccountRepository) to manage hierarchical accounts
│   ├── ExpenseEditScreen / RevenueEditScreen / TransferEditScreen rely on TransactionViewModel to create or update ledger entries
│   ├── LoanEditScreen schedules loan payments via TransactionViewModel helpers
│   ├── PriceComparator offers local price-per-unit comparison tooling
│   ├── TrialCalculatorScreen connects TrialCalculatorViewModel (TrialCalculatorRepository) for running-sum what-if analysis
│   └── FutureViewScreen visualizes planned/estimated transactions with TransactionViewModel data
├── Mental
│   ├── NotesHomeScreen backed by NotesViewModel (RichNoteRepository + TagRepository) for searching, filtering, pinning, and editing rich notes
│   └── QuickNoteScreen (NoteViewModel) provides lightweight note CRUD outside the main nav flow
├── Time
│   ├── TimeWorkspaceScreen coordinates TimeblockViewModel (TimeblockRepository + TaskRepository) and FocusTimerViewModel (AlarmRepository + TaskRepository)
│   │   ├── Schedule tab renders TimeblockPlanner and CreateTimeblockDialog for daily planning
│   │   ├── Tasks tab embeds TaskDashboardScreen (TaskBoardViewModel → TaskPlannerRepository + TagRepository)
│   │   ├── Review tab shows TimeReviewPanel summaries
│   │   └── FocusTimerOverlay drives focus sessions and task logging
│   ├── TaskHierarchyScreen surfaces full task tree maintenance via TaskRepository
│   └── TaskDashboardScreen route (Route.TaskPlanner) opens the same task board full screen when launched from navigation
├── Settings
│   ├── Theme controls toggle ThemePreference for the app shell
│   └── Tag management dialog stack uses TagManagementViewModel (TagRepository) to create, edit, and delete tagged domains
└── Shared Infrastructure
    ├── AppModule & viewModelModule wire repositories, SQLDelight AppDatabase, and factory view models through Koin
    ├── DatabaseDriverFactory plus expect/actual platform modules provide storage back ends
    └── Shared utilities cover time helpers, finance models, tag helpers, and other domain DTOs
```

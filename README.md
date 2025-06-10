# a.bun.dance

A playful and productive task management application built with Flutter.

## 🚀 Features

- **Task Management**: Create, update, and track your daily tasks
- **Session Tracking**: Monitor your work/study sessions
- **Routines**: Set up and manage daily/weekly routines
- **Future Planning**: Plan ahead with the future planning feature

## 🛠️ Tech Stack

- **Framework**: Flutter (cross-platform)
- **State Management**: Riverpod
- **Local Database**: SQLite (via sqflite and drift)
- **Architecture**: Feature-based architecture
- **Dependency Injection**: Riverpod

## 📱 Screens

- Home Dashboard
- Tasks Management
- Session Tracking
- Routines
- Future Planning

## 🚀 Getting Started

### Prerequisites

- Flutter SDK (latest stable version)
- Dart SDK (version ^3.8.0)
- Android Studio / Xcode (for mobile development)
- VS Code / Android Studio / IntelliJ IDEA (recommended IDEs)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/abun.git
   cd abun
   ```

2. Install dependencies:
   ```bash
   flutter pub get
   ```

3. Run the app:
   ```bash
   flutter run
   ```

## 🏗️ Project Structure

```
lib/
├── features/          # Feature-based modules
│   ├── home/         # Home screen
│   ├── tasks/        # Task management
│   ├── sessions/     # Session tracking
│   ├── routines/     # Routine management
│   └── future/       # Future planning
├── main.dart         # Application entry point
└── routes.dart       # Route configuration
```

## 📚 Dependencies

- `flutter_riverpod`: State management
- `drift`: Type-safe SQLite database
- `sqflite`: SQLite database for Flutter
- `intl`: Internationalization and localization
- `path_provider`: Filesystem path utilities
- `uuid`: UUID generation

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

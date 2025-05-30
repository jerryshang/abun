import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'package:uuid/uuid.dart';

import '../extensions/time_extensions.dart';
import '../models/task_status.dart';

/// The database tables
part 'database.g.dart';
part 'task_dao.dart';
part 'session_dao.dart';
part 'routine_dao.dart';



@DriftDatabase(tables: [Routines, Tasks, Sessions])
class AppDatabase extends _$AppDatabase {
  // Singleton pattern
  static final AppDatabase _instance = AppDatabase._internal();
  static AppDatabase get instance => _instance;

  // Private constructor
  AppDatabase._internal() : super(_openConnection());

  // Constructor for testing with a custom database connection
  AppDatabase.forTesting(DatabaseConnection super.connection);

  // Public constructor for testing
  factory AppDatabase() => _instance;

  @override
  int get schemaVersion => 1; // Reset to 1 for the new schema

  @override
  MigrationStrategy get migration => MigrationStrategy(
    onCreate: (Migrator m) {
      return m.createAll();
    },
  );



  // Task DAO getter
  TaskDao get taskDao => TaskDao(this);

  // Session DAO getter
  SessionDao get sessionDao => SessionDao(this);

  // Routine DAO getter
  RoutineDao get routineDao => RoutineDao(this);
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'abun.db'));
    return NativeDatabase(file);
  });
}

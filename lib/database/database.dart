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

/// Table for recurring task templates
class Routines extends Table {
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();
  TextColumn get title => text()();
  TextColumn get recurrenceRule => text()(); // e.g., 'FREQ=DAILY;INTERVAL=1'
  TextColumn get estimatedDuration => text().nullable()(); // ISO8601 format, e.g., 'PT25M'
  TextColumn get startTime => text().nullable()();
  TextColumn get dueTime => text().nullable()();
  TextColumn get note => text().nullable()();
  TextColumn get createdAt => text().clientDefault(() => DateTime.now().toIso8601String())();
  TextColumn get updatedAt => text().clientDefault(() => DateTime.now().toIso8601String())();

  @override
  Set<Column> get primaryKey => {id};
}

/// Table for task instances
class Tasks extends Table {
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();
  TextColumn get routineId => text().nullable().references(Routines, #id)();
  TextColumn get title => text()();
  TextColumn get status => text().withDefault(const Constant('inbox'))();
  TextColumn get estimatedDuration => text().nullable()(); // ISO8601 format, e.g., 'PT25M'
  TextColumn get startTime => text().nullable()();
  TextColumn get dueTime => text().nullable()();
  TextColumn get note => text().nullable()();
  TextColumn get createdAt => text().clientDefault(() => DateTime.now().toIso8601String())();
  TextColumn get updatedAt => text().clientDefault(() => DateTime.now().toIso8601String())();

  @override
  Set<Column> get primaryKey => {id};
}

/// Table for focus sessions
class Sessions extends Table {
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();
  TextColumn get taskId => text().nullable().references(Tasks, #id)();
  TextColumn get title => text().nullable()();
  TextColumn get startTime => text().nullable()();
  TextColumn get endTime => text().nullable()();
  TextColumn get duration => text()(); // ISO8601 format, e.g., 'PT25M'
  TextColumn get type => text().withDefault(const Constant('free'))();
  TextColumn get mood => text().nullable().withDefault(const Constant('okay'))();
  TextColumn get note => text().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

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

  // CRUD operations for routines
  Future<List<Routine>> getAllRoutines() async {
    final results = await select(routines).get();
    return results.map((row) => Routine(
      id: row.id,
      title: row.title,
      recurrenceRule: row.recurrenceRule,
      estimatedDuration: row.estimatedDuration,
      startTime: row.startTime,
      dueTime: row.dueTime,
      note: row.note,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    )).toList();
  }

  Stream<List<Routine>> watchAllRoutines() {
    return select(routines).watch().map((rows) => rows.map((row) => Routine(
      id: row.id,
      title: row.title,
      recurrenceRule: row.recurrenceRule,
      estimatedDuration: row.estimatedDuration,
      startTime: row.startTime,
      dueTime: row.dueTime,
      note: row.note,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    )).toList());
  }

  Future<Routine?> getRoutineById(String id) async {
    final result = await (select(routines)..where((r) => r.id.equals(id))).getSingleOrNull();
    if (result == null) return null;

    return Routine(
      id: result.id,
      title: result.title,
      recurrenceRule: result.recurrenceRule,
      estimatedDuration: result.estimatedDuration,
      startTime: result.startTime,
      dueTime: result.dueTime,
      note: result.note,
      createdAt: result.createdAt,
      updatedAt: result.updatedAt,
    );
  }

  Future<String> createRoutine(RoutinesCompanion routine) {
    // Generate a UUID if not provided
    final routineWithId = routine.id.present ? routine : routine.copyWith(
      id: Value(const Uuid().v4()),
    );

    return into(routines).insert(routineWithId).then((_) => routineWithId.id.value);
  }

  Future<bool> updateRoutine(Routine routine) {
    return update(routines).replace(RoutinesCompanion(
      id: Value(routine.id),
      title: Value(routine.title),
      recurrenceRule: Value(routine.recurrenceRule),
      estimatedDuration: Value(routine.estimatedDuration),
      startTime: Value(routine.startTime),
      dueTime: Value(routine.dueTime),
      note: Value(routine.note),
      updatedAt: Value(DateTime.now().toIso8601String()),
    ));
  }

  Future<int> deleteRoutine(String id) =>
      (delete(routines)..where((r) => r.id.equals(id))).go();

  // Task DAO getter
  TaskDao get taskDao => TaskDao(this);

  // Session DAO getter
  SessionDao get sessionDao => SessionDao(this);

}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'abun.db'));
    return NativeDatabase(file);
  });
}

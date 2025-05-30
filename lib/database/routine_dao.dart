part of 'database.dart';

/// Table for recurring task templates
class Routines extends Table {
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();

  TextColumn get title => text()();

  TextColumn get recurrenceRule => text()();

  TextColumn get estimatedDuration => text().nullable()();

  TextColumn get startTime => text().nullable()();

  TextColumn get dueTime => text().nullable()();

  TextColumn get note => text().nullable()();

  TextColumn get createdAt => text().clientDefault(() => DateTime.now().toIso8601String())();

  TextColumn get updatedAt => text().clientDefault(() => DateTime.now().toIso8601String())();

  @override
  Set<Column> get primaryKey => {id};
}

extension RoutineRow on Routine {
  Routine toRoutine() => Routine(
    id: id,
    title: title,
    recurrenceRule: recurrenceRule,
    estimatedDuration: estimatedDuration,
    startTime: startTime,
    dueTime: dueTime,
    note: note,
    createdAt: createdAt,
    updatedAt: updatedAt,
  );
}

class RoutineDao {
  final AppDatabase _db;

  RoutineDao(this._db);

  // CRUD operations for routines
  Future<List<Routine>> getAllRoutines() async {
    final results = await _db.select(_db.routines).get();
    return results.map((row) => row.toRoutine()).toList();
  }

  Stream<List<Routine>> watchAllRoutines() {
    return _db.select(_db.routines).watch().map((rows) => rows.map((row) => row.toRoutine()).toList());
  }

  Future<Routine?> getRoutineById(String id) async {
    final result = await (_db.select(_db.routines)..where((r) => r.id.equals(id))).getSingleOrNull();
    if (result == null) return null;

    return result.toRoutine();
  }

  Future<String> createRoutine(RoutinesCompanion routine) {
    // Generate a UUID if not provided
    final routineWithId = routine.id.present ? routine : routine.copyWith(id: Value(const Uuid().v4()));

    return _db.into(_db.routines).insert(routineWithId).then((_) => routineWithId.id.value);
  }

  Future<bool> updateRoutine(Routine routine) {
    return _db
        .update(_db.routines)
        .replace(
          RoutinesCompanion(
            id: Value(routine.id),
            title: Value(routine.title),
            recurrenceRule: Value(routine.recurrenceRule),
            estimatedDuration: Value(routine.estimatedDuration),
            startTime: Value(routine.startTime),
            dueTime: Value(routine.dueTime),
            note: Value(routine.note),
            updatedAt: Value(DateTime.now().toIso8601String()),
          ),
        );
  }

  Future<int> deleteRoutine(String id) => (_db.delete(_db.routines)..where((r) => r.id.equals(id))).go();
}

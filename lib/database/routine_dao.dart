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

  Future<int> deleteRoutine(String id) async {
    // First delete future tasks for this routine
    await (_db.delete(_db.tasks)
      ..where((t) => t.routineId.equals(id) & t.dueTime.isBiggerOrEqualValue(DateTime.now().toIso8601String()))
    ).go();

    // Then delete the routine
    return (_db.delete(_db.routines)..where((r) => r.id.equals(id))).go();
  }

  /// Generates tasks from a routine, ensuring there are always 7 future tasks
  /// Returns the number of tasks created
  Future<int> generateTasksFromRoutine(String routineId) async {
    // Get the routine
    final routine = await getRoutineById(routineId);
    if (routine == null) throw Exception('Routine not found');

    // Get existing future tasks for this routine
    final now = DateTime.now();
    final existingTasks =
        await (_db.select(_db.tasks)
              ..where((t) => t.routineId.equals(routineId) & t.dueTime.isBiggerOrEqualValue(now.toIso8601String()))
              ..orderBy([(t) => OrderingTerm(expression: t.dueTime, mode: OrderingMode.asc)]))
            .get();

    // If we already have 7 or more future tasks, no need to create more
    if (existingTasks.length >= 7) return 0;

    // Calculate how many tasks we need to create
    final tasksToCreate = 7 - existingTasks.length;

    // Get the last due date to start creating new tasks from
    DateTime lastDueDate = existingTasks.isNotEmpty ? DateTime.parse(existingTasks.last.dueTime!) : now;

    // If no existing tasks, use routine's start time if available, otherwise use now
    if (existingTasks.isEmpty && routine.startTime != null) {
      lastDueDate = DateTime.parse(routine.startTime!);
    }

    // Create new tasks
    int createdCount = 0;
    for (int i = 0; i < tasksToCreate; i++) {
      // Create task
      final task = TasksCompanion.insert(
        title: routine.title,
        routineId: drift.Value(routine.id),
        status: drift.Value(TaskStatus.planned.value),
        estimatedDuration: routine.estimatedDuration != null
            ? drift.Value(routine.estimatedDuration!)
            : const drift.Value.absent(),
        startTime: drift.Value(lastDueDate.toIso8601String()),
        dueTime: const drift.Value.absent(),
        note: routine.note != null ? drift.Value(routine.note!) : const drift.Value.absent(),
        createdAt: drift.Value(DateTime.now().toIso8601String()),
        updatedAt: drift.Value(DateTime.now().toIso8601String()),
      );

      await _db.into(_db.tasks).insert(task);
      createdCount++;
      lastDueDate = lastDueDate.add(const Duration(days: 1));
    }

    return createdCount;
  }
}

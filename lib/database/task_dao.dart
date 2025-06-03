part of 'database.dart';

/// Table for task instances
class Tasks extends Table {
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();

  TextColumn get routineId => text().nullable().references(Routines, #id)();

  TextColumn get title => text()();

  TextColumn get status => text().withDefault(const Constant('inbox'))();

  TextColumn get estimatedDuration => text().nullable()();

  TextColumn get startTime => text().nullable()();

  TextColumn get dueTime => text().nullable()();

  TextColumn get note => text().nullable()();

  TextColumn get createdAt => text().clientDefault(() => DateTime.now().toIso8601String())();

  TextColumn get updatedAt => text().clientDefault(() => DateTime.now().toIso8601String())();

  @override
  Set<Column> get primaryKey => {id};
}

/// Extension to convert Tasks table row to Task model
extension TaskRow on Task {
  Task toTask() => Task(
    id: id,
    routineId: routineId,
    title: title,
    status: status,
    estimatedDuration: estimatedDuration,
    startTime: startTime,
    dueTime: dueTime,
    note: note,
    createdAt: createdAt,
    updatedAt: updatedAt,
  );
}

/// Data Access Object for Task-related database operations
class TaskDao {
  final AppDatabase _db;

  TaskDao(this._db);

  Future<List<Task>> getAllTasks() =>
      _db.select(_db.tasks).get().then((rows) => rows.map((row) => row.toTask()).toList());

  Stream<List<Task>> watchAllTasks() {
    return _db.select(_db.tasks).watch().map((rows) => rows.map((row) => row.toTask()).toList());
  }

  Stream<List<Task>> watchTasks({bool showCompleted = false, bool recentFirst = true, bool showRoutineGenerated = false}) {
    final query = _db.select(_db.tasks);

    if (!showCompleted) {
      query.where((t) => t.status.isNotIn([TaskStatus.completed.value, TaskStatus.eliminated.value]));
    }

    if (!showRoutineGenerated) {
      query.where((t) => t.routineId.isNull());
    }

    query.orderBy([(t) => recentFirst ? OrderingTerm.desc(t.updatedAt) : OrderingTerm.asc(t.updatedAt)]);

    return query.watch().map((rows) => rows.map((row) => row.toTask()).toList());
  }

  Future<Task?> getTaskById(String id) async {
    final result = await (_db.select(_db.tasks)..where((t) => t.id.equals(id))).getSingleOrNull();
    return result?.toTask();
  }

  Future<List<Task>> getTasksByRoutine(String routineId) async {
    final results = await (_db.select(_db.tasks)..where((t) => t.routineId.equals(routineId))).get();
    return results.map((row) => row.toTask()).toList();
  }

  Stream<List<Task>> watchTasksByRoutine(String routineId) {
    return (_db.select(
      _db.tasks,
    )..where((t) => t.routineId.equals(routineId))).watch().map((rows) => rows.map((row) => row.toTask()).toList());
  }

  Future<String> createTask(TasksCompanion task) {
    // Generate a UUID if not provided
    final taskWithId = task.id.present ? task : task.copyWith(id: Value(const Uuid().v4()));

    return _db.into(_db.tasks).insert(taskWithId).then((_) => taskWithId.id.value);
  }

  Future<bool> updateTask(Task task) {
    return _db
        .update(_db.tasks)
        .replace(
          TasksCompanion(
            id: Value(task.id),
            routineId: Value(task.routineId),
            title: Value(task.title),
            status: Value(task.status),
            estimatedDuration: Value(task.estimatedDuration),
            startTime: Value(task.startTime),
            dueTime: Value(task.dueTime),
            note: Value(task.note),
            updatedAt: Value(DateTime.now().toIso8601String()),
          ),
        );
  }

  Future<int> deleteTask(String id) => (_db.delete(_db.tasks)..where((t) => t.id.equals(id))).go();

  Future<int> clearAllTasks() => _db.delete(_db.tasks).go();

  /// Gets active tasks that are either in progress or planned with time conditions
  Future<List<Task>> getActiveTasks() async {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);

    return (await _db.select(_db.tasks).get()).where((task) {
      if (task.status == TaskStatus.inProgress.value) {
        return true;
      }
      if (task.status == TaskStatus.inbox.value ||
          task.status == TaskStatus.completed.value ||
          task.status == TaskStatus.eliminated.value) {
        return false;
      }

      // For planned tasks, check time conditions
      if (task.status == TaskStatus.planned.value) {
        // If task has a routine, only check if it's within the forecast window
        if (task.routineId != null) {
          // For routine tasks, only show if start date is today
          if (task.startTime == null) return false;
          final startDate = task.startTime!.toDateTime()!;
          return startDate.year == today.year &&
                 startDate.month == today.month &&
                 startDate.day == today.day;
        }

        // For non-routine tasks, check both start time and forecast window
        return DateTimeUtils.isForecastWindowCoveredByStartTime(
              anchorTime: today,
              forecastDuration: Duration.zero,
              startTime: task.startTime?.toDateTime() ?? DateTimeExtension.farFuture,
            ) ||
            DateTimeUtils.isForecastWindowCovered(
              anchorTime: today,
              forecastDuration: Duration(days: 1),
              estimatedDuration: task.estimatedDuration?.toDuration(),
              dueTime: task.dueTime?.toDateTime(),
            );
      }

      return false;
    }).toList();
  }

  /// Watches active tasks that are either in progress or planned with time conditions
  Stream<List<Task>> watchActiveTasks() {
    return _db.select(_db.tasks).watch().asyncMap((_) => getActiveTasks());
  }

  /// Gets all completed tasks
  Future<List<Task>> getCompletedTasks() async {
    final results = await (_db.select(_db.tasks)..where((t) => t.status.equals(TaskStatus.completed.value))).get();
    return results.map((row) => row.toTask()).toList();
  }

  /// Watches completed tasks
  Stream<List<Task>> watchCompletedTasks() {
    return (_db.select(_db.tasks)..where((t) => t.status.equals(TaskStatus.completed.value))).watch().map(
      (rows) => rows.map((row) => row.toTask()).toList(),
    );
  }

  /// In your session_dao.dart file

  /// Watches completed tasks that have sessions ending today
  Stream<List<Task>> watchCompletedTasksWithTodaysSessions() {
    final today = DateTime.now();
    final startOfDay = DateTime(today.year, today.month, today.day);
    final endOfDay = startOfDay.add(const Duration(days: 1));

    // Use a subquery to find distinct task IDs with sessions ending today
    final sessionSubquery = _db.selectOnly(_db.sessions)
      ..addColumns([_db.sessions.taskId])
      ..where(_db.sessions.endTime.isBetweenValues(startOfDay.toIso8601String(), endOfDay.toIso8601String()));

    // Main query that joins with the subquery
    final query = _db.select(_db.tasks)
      ..where((tbl) => tbl.id.isInQuery(sessionSubquery) & tbl.status.isIn(['completed', 'eliminated']));

    return query.watch();
  }

  /// Gets all tasks for a specific routine, ordered by due date descending
  Future<List<Task>> getTasksByRoutineId(String routineId) async {
    final results = await (_db.select(_db.tasks)
      ..where((t) => t.routineId.equals(routineId))
      ..orderBy([(t) => OrderingTerm.desc(t.dueTime)]))
        .get();
    return results.map((row) => row.toTask()).toList();
  }
}

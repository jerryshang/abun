part of 'database.dart';

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

  Future<List<Task>> getAllTasks() async {
    final results = await _db.select(_db.tasks).get();
    return results.map((row) => row.toTask()).toList();
  }

  Stream<List<Task>> watchAllTasks() {
    return _db.select(_db.tasks).watch().map((rows) => rows.map((row) => row.toTask()).toList());
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

    final allTasks = await getAllTasks();

    return allTasks.where((task) {
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
}

part of 'database.dart';

/// Data Access Object for Task-related database operations
class TaskDao {
  final AppDatabase _db;

  TaskDao(this._db);

  Future<List<Task>> getAllTasks() async {
    final results = await _db.select(_db.tasks).get();
    return results.map((row) => Task(
      id: row.id,
      routineId: row.routineId,
      title: row.title,
      status: row.status,
      estimatedDuration: row.estimatedDuration,
      startTime: row.startTime,
      dueTime: row.dueTime,
      note: row.note,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    )).toList();
  }

  Stream<List<Task>> watchAllTasks() {
    return _db.select(_db.tasks).watch().map((rows) => rows.map((row) => Task(
      id: row.id,
      routineId: row.routineId,
      title: row.title,
      status: row.status,
      estimatedDuration: row.estimatedDuration,
      startTime: row.startTime,
      dueTime: row.dueTime,
      note: row.note,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    )).toList());
  }

  Future<Task?> getTaskById(String id) async {
    final result = await (_db.select(_db.tasks)..where((t) => t.id.equals(id))).getSingleOrNull();
    if (result == null) return null;

    return Task(
      id: result.id,
      routineId: result.routineId,
      title: result.title,
      status: result.status,
      estimatedDuration: result.estimatedDuration,
      startTime: result.startTime,
      dueTime: result.dueTime,
      note: result.note,
      createdAt: result.createdAt,
      updatedAt: result.updatedAt,
    );
  }

  Future<List<Task>> getTasksByRoutine(String routineId) async {
    final results = await (_db.select(_db.tasks)..where((t) => t.routineId.equals(routineId))).get();
    return results.map((row) => Task(
      id: row.id,
      routineId: row.routineId,
      title: row.title,
      status: row.status,
      estimatedDuration: row.estimatedDuration,
      startTime: row.startTime,
      dueTime: row.dueTime,
      note: row.note,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    )).toList();
  }

  Stream<List<Task>> watchTasksByRoutine(String routineId) {
    return (_db.select(_db.tasks)..where((t) => t.routineId.equals(routineId)))
        .watch()
        .map((rows) => rows.map((row) => Task(
              id: row.id,
              routineId: row.routineId,
              title: row.title,
              status: row.status,
              estimatedDuration: row.estimatedDuration,
              startTime: row.startTime,
              dueTime: row.dueTime,
              note: row.note,
              createdAt: row.createdAt,
              updatedAt: row.updatedAt,
            )).toList());
  }

  Future<String> createTask(TasksCompanion task) {
    // Generate a UUID if not provided
    final taskWithId = task.id.present ? task : task.copyWith(
      id: Value(const Uuid().v4()),
    );

    return _db.into(_db.tasks).insert(taskWithId).then((_) => taskWithId.id.value);
  }

  Future<bool> updateTask(Task task) {
    return _db.update(_db.tasks).replace(TasksCompanion(
      id: Value(task.id),
      routineId: Value(task.routineId),
      title: Value(task.title),
      status: Value(task.status),
      estimatedDuration: Value(task.estimatedDuration),
      startTime: Value(task.startTime),
      dueTime: Value(task.dueTime),
      note: Value(task.note),
      updatedAt: Value(DateTime.now().toIso8601String()),
    ));
  }

  Future<int> deleteTask(String id) =>
      (_db.delete(_db.tasks)..where((t) => t.id.equals(id))).go();

  Future<int> clearAllTasks() => _db.delete(_db.tasks).go();

  /// Gets active tasks that are either in progress or planned with time conditions
  Future<List<Task>> getActiveTasks() async {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);

    // Get all tasks first
    final allTasks = await getAllTasks();

    // Filter tasks based on conditions
    return allTasks.where((task) {
      // Include tasks that are in progress
      if (task.status == TaskStatus.inProgress.value) {
        return true;
      }

      // For planned tasks, check time conditions
      if (task.status == TaskStatus.planned.value) {
        // Parse start time if exists
        DateTime? startTime;
        if (task.startTime != null) {
          startTime = DateTime.parse(task.startTime!);
        }

        // Parse due time if exists
        DateTime? dueTime;
        if (task.dueTime != null) {
          dueTime = DateTime.parse(task.dueTime!);
        }

        // Parse estimated duration if exists
        Duration? estimatedDuration;
        if (task.estimatedDuration != null) {
          estimatedDuration = Duration(
            minutes: int.tryParse(task.estimatedDuration!.replaceAll(RegExp(r'[^0-9]'), '')) ?? 0,
          );
        }

        // Check if today is after start_time - estimated_duration
        if (startTime != null && estimatedDuration != null) {
          final adjustedStartTime = startTime.subtract(estimatedDuration);
          if (now.isAfter(adjustedStartTime)) {
            return true;
          }
        }

        // Check if today is after due_time - estimated_duration
        if (dueTime != null && estimatedDuration != null) {
          final adjustedDueTime = dueTime.subtract(estimatedDuration);
          if (now.isAfter(adjustedDueTime)) {
            return true;
          }
        }
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
    final results = await (_db.select(_db.tasks)
      ..where((t) => t.status.equals(TaskStatus.completed.value))).get();
    
    return results.map((row) => Task(
      id: row.id,
      routineId: row.routineId,
      title: row.title,
      status: row.status,
      estimatedDuration: row.estimatedDuration,
      startTime: row.startTime,
      dueTime: row.dueTime,
      note: row.note,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    )).toList();
  }

  /// Watches completed tasks
  Stream<List<Task>> watchCompletedTasks() {
    return (_db.select(_db.tasks)..where((t) => t.status.equals(TaskStatus.completed.value)))
        .watch()
        .map((rows) => rows.map((row) => Task(
              id: row.id,
              routineId: row.routineId,
              title: row.title,
              status: row.status,
              estimatedDuration: row.estimatedDuration,
              startTime: row.startTime,
              dueTime: row.dueTime,
              note: row.note,
              createdAt: row.createdAt,
              updatedAt: row.updatedAt,
            )).toList());
  }
}

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../database/database.dart';

part 'database_provider.g.dart';

/// Provider for the database instance
@Riverpod(keepAlive: true)
AppDatabase database(Ref ref) {
  return AppDatabase.instance;
}

/// Provider for all tasks
@riverpod
Future<List<Task>> allTasks(Ref ref) async {
  final db = ref.watch(databaseProvider);
  return db.taskDao.getAllTasks();
}

/// Provider for a single task by ID
@riverpod
Future<Task?> taskById(Ref ref, String id) async {
  final db = ref.watch(databaseProvider);
  return db.taskDao.getTaskById(id);
}

/// Provider for a stream of all tasks (reactive)
@riverpod
Stream<List<Task>> watchAllTasks(Ref ref) {
  final db = ref.watch(databaseProvider);
  return db.taskDao.watchAllTasks();
}

/// Provider for all routines
@riverpod
Future<List<Routine>> allRoutines(Ref ref) async {
  final db = ref.watch(databaseProvider);
  return db.getAllRoutines();
}

/// Provider for a stream of all routines (reactive)
@riverpod
Stream<List<Routine>> watchAllRoutines(Ref ref) {
  final db = ref.watch(databaseProvider);
  return db.watchAllRoutines();
}

/// Provider for a single routine by ID
@riverpod
Future<Routine?> routineById(Ref ref, String id) async {
  final db = ref.watch(databaseProvider);
  return db.getRoutineById(id);
}

/// Provider for tasks by routine ID
@riverpod
Future<List<Task>> tasksByRoutine(Ref ref, String routineId) async {
  final db = ref.watch(databaseProvider);
  return db.taskDao.getTasksByRoutine(routineId);
}

/// Provider for a stream of tasks by routine ID (reactive)
@riverpod
Stream<List<Task>> watchTasksByRoutine(Ref ref, String routineId) {
  final db = ref.watch(databaseProvider);
  return db.taskDao.watchTasksByRoutine(routineId);
}

/// Provider for all sessions
@riverpod
Future<List<Session>> allSessions(Ref ref) async {
  final db = ref.watch(databaseProvider);
  return db.getAllSessions();
}

/// Provider for a stream of all sessions (reactive)
@riverpod
Stream<List<Session>> watchAllSessions(Ref ref) {
  final db = ref.watch(databaseProvider);
  return db.watchAllSessions();
}

/// Provider for sessions by task ID
@riverpod
Future<List<Session>> sessionsByTask(Ref ref, String taskId) async {
  final db = ref.watch(databaseProvider);
  return db.getSessionsByTask(taskId);
}

/// Provider for a stream of sessions by task ID (reactive)
@riverpod
Stream<List<Session>> watchSessionsByTask(Ref ref, String taskId) {
  final db = ref.watch(databaseProvider);
  return db.sessionDao.watchSessionsByTask(taskId);
}

/// Provider for active tasks (in progress or planned with time conditions)
@riverpod
Future<List<Task>> activeTasks(Ref ref) async {
  final db = ref.watch(databaseProvider);
  return db.taskDao.getActiveTasks();
}

/// Provider for a stream of active tasks (reactive)
@riverpod
Stream<List<Task>> watchActiveTasks(Ref ref) {
  final db = ref.watch(databaseProvider);
  return db.taskDao.watchActiveTasks();
}

/// Provider for completed tasks
@riverpod
Future<List<Task>> completedTasks(Ref ref) async {
  // Depend on database and today's sessions to refresh when either changes
  final db = ref.watch(databaseProvider);
  return db.taskDao.getCompletedTasks();
}

/// Provider for a stream of completed tasks (reactive)
@riverpod
Stream<List<Task>> watchCompletedTasks(Ref ref) {
  final db = ref.watch(databaseProvider);

  // Combine with the task stream
  return db.taskDao.watchCompletedTasks();
}

/// Provider for today's sessions
@riverpod
Future<List<Session>> sessionsByDay(Ref ref, DateTime? date) async {
  final db = ref.watch(databaseProvider);
  final day = date ?? DateTime.now();
  final endOfDay = day.add(const Duration(days: 1));

  final sessions = await db.sessionDao.getSessionsInRange(
    day.toIso8601String(),
    endOfDay.toIso8601String(),
  );

  return sessions;
}

@riverpod
Stream<List<Session>> watchSessionsByDay(Ref ref, DateTime? date)  {
  final db = ref.watch(databaseProvider);
  final now = DateTime.now();
  final today = DateTime(now.year, now.month, now.day);
  final day = date ?? today;
  final endOfDay = day.add(const Duration(days: 1));

  return  db.sessionDao.watchSessionsInRange(
    day.toIso8601String(),
    endOfDay.toIso8601String(),
  );
}

/// Provider for a stream of today's sessions without tasks (reactive)
@riverpod
Stream<List<Session>> watchSessionsWithoutTasks(Ref ref) {
  final db = ref.watch(databaseProvider);
  final now = DateTime.now();
  final startOfDay = DateTime(now.year, now.month, now.day);
  final endOfDay = startOfDay.add(const Duration(days: 1));

  // Watch for changes to all sessions to trigger updates
  ref.watch(watchAllSessionsProvider);

  return db.sessionDao.watchSessionsWithoutTaskInRange(
    startOfDay.toIso8601String(),
    endOfDay.toIso8601String(),
  );
}



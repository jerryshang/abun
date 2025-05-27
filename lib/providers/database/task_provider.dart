import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../database/database.dart';
import '../current_date_provider.dart';
import 'database_provider.dart';

part 'task_provider.g.dart';

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

/// Provider for active tasks (in progress or planned with time conditions)
@riverpod
Future<List<Task>> activeTasks(Ref ref) async {
  final db = ref.watch(databaseProvider);
  return db.taskDao.getActiveTasks();
}

/// Provider for a stream of active tasks (reactive)
@riverpod
Stream<List<Task>> watchActiveTasks(Ref ref) {
  // Add todayKey as a dependency to trigger refresh when date changes
  ref.watch(currentDateProvider);
  final db = ref.watch(databaseProvider);
  return db.taskDao.watchActiveTasks();
}

/// Provider for completed tasks
@riverpod
Future<List<Task>> completedTasks(Ref ref) async {
  final db = ref.watch(databaseProvider);
  return db.taskDao.getCompletedTasks();
}

/// Provider for a stream of completed tasks (reactive)
@riverpod
Stream<List<Task>> watchCompletedTasks(Ref ref) {
  ref.watch(currentDateProvider);
  final db = ref.watch(databaseProvider);
  return db.taskDao.watchCompletedTasks();
}

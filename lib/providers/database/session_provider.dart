import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../database/database.dart';
import 'database_provider.dart';

part 'session_provider.g.dart';

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

/// Provider for sessions by day
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

/// Provider for a stream of sessions by day (reactive)
@riverpod
Stream<List<Session>> watchSessionsByDay(Ref ref, DateTime? date) {
  final db = ref.watch(databaseProvider);
  final now = DateTime.now();
  final today = DateTime(now.year, now.month, now.day);
  final day = date ?? today;
  final endOfDay = day.add(const Duration(days: 1));

  return db.sessionDao.watchSessionsInRange(
    day.toIso8601String(),
    endOfDay.toIso8601String(),
  );
}

/// Provider for a stream of sessions without tasks (reactive)
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

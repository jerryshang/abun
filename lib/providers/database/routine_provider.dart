import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../database/database.dart';
import 'database_provider.dart';

part 'routine_provider.g.dart';

/// Provider for all routines
@riverpod
Future<List<Routine>> allRoutines(Ref ref) async {
  final db = ref.watch(databaseProvider);
  return db.routineDao.getAllRoutines();
}

/// Provider for a stream of all routines (reactive)
@riverpod
Stream<List<Routine>> watchAllRoutines(Ref ref) {
  final db = ref.watch(databaseProvider);
  return db.routineDao.watchAllRoutines();
}

/// Provider for a single routine by ID
@riverpod
Future<Routine?> routineById(Ref ref, String id) async {
  final db = ref.watch(databaseProvider);
  return db.routineDao.getRoutineById(id);
}

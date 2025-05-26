import 'package:drift/drift.dart' as drift;
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../database/database.dart';
import 'database_provider.dart';

part 'task_notifier.g.dart';

/// Task operations state
enum TaskOperationState { initial, loading, success, error }

/// Task operations state class
class TaskOperations {
  final TaskOperationState state;
  final String? errorMessage;

  TaskOperations({this.state = TaskOperationState.initial, this.errorMessage});

  TaskOperations copyWith({TaskOperationState? state, String? errorMessage}) {
    return TaskOperations(
      state: state ?? this.state,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}

/// Task operations notifier
@riverpod
class TaskNotifier extends _$TaskNotifier {
  @override
  TaskOperations build() {
    return TaskOperations();
  }

  /// Create a new task
  Future<void> createTask({
    required String title,
    String? routineId,
    String status = 'inbox',
    String? estimatedDuration,
    DateTime? startTime,
    DateTime? dueTime,
    String? note,
  }) async {
    // Update state to loading
    state = TaskOperations(state: TaskOperationState.loading);

    try {
      final db = ref.read(databaseProvider);
      final now = DateTime.now().toIso8601String();

      await db.taskDao.createTask(
        TasksCompanion.insert(
          title: title,
          routineId: routineId != null
              ? drift.Value(routineId)
              : const drift.Value.absent(),
          status: drift.Value(status),
          estimatedDuration: estimatedDuration != null
              ? drift.Value(estimatedDuration)
              : const drift.Value.absent(),
          startTime: startTime != null
              ? drift.Value(startTime.toIso8601String())
              : const drift.Value.absent(),
          dueTime: dueTime != null
              ? drift.Value(dueTime.toIso8601String())
              : const drift.Value.absent(),
          note: note != null ? drift.Value(note) : const drift.Value.absent(),
          createdAt: drift.Value(now),
          updatedAt: drift.Value(now),
        ),
      );

      // Update state to success
      state = TaskOperations(state: TaskOperationState.success);

      // Invalidate the task providers to refresh the data
      ref.invalidate(allTasksProvider);
    } catch (e) {
      // Update state to error
      state = TaskOperations(
        state: TaskOperationState.error,
        errorMessage: e.toString(),
      );
    }
  }

  /// Update an existing task
  Future<void> updateTask(Task task) async {
    // Update state to loading
    state = TaskOperations(state: TaskOperationState.loading);

    try {
      final db = ref.read(databaseProvider);

      // Create a copy of the task with updated timestamp
      final updatedTask = task.copyWith(
        updatedAt: DateTime.now().toIso8601String(),
      );

      await db.taskDao.updateTask(updatedTask);

      // Update state to success
      state = TaskOperations(state: TaskOperationState.success);

      // Invalidate the task providers to refresh the data
      ref.invalidate(allTasksProvider);
      ref.invalidate(taskByIdProvider(task.id));

      // If the task has a routine, invalidate the tasks by routine provider
      if (task.routineId != null) {
        ref.invalidate(tasksByRoutineProvider(task.routineId!));
      }
    } catch (e) {
      // Update state to error
      state = TaskOperations(
        state: TaskOperationState.error,
        errorMessage: e.toString(),
      );
    }
  }

  /// Delete a task
  Future<void> deleteTask(String id) async {
    // Update state to loading
    state = TaskOperations(state: TaskOperationState.loading);

    try {
      final db = ref.read(databaseProvider);

      // Get the task to check if it has a routine
      final task = await db.taskDao.getTaskById(id);

      await db.taskDao.deleteTask(id);

      // Update state to success
      state = TaskOperations(state: TaskOperationState.success);

      // Invalidate the task providers to refresh the data
      ref.invalidate(allTasksProvider);
      ref.invalidate(taskByIdProvider(id));

      // If the task had a routine, invalidate the tasks by routine provider
      if (task != null && task.routineId != null) {
        ref.invalidate(tasksByRoutineProvider(task.routineId!));
      }
    } catch (e) {
      // Update state to error
      state = TaskOperations(
        state: TaskOperationState.error,
        errorMessage: e.toString(),
      );
    }
  }

  /// Clear all tasks from the database
  Future<void> clearAllTasks() async {
    // Update state to loading
    state = TaskOperations(state: TaskOperationState.loading);

    try {
      final db = ref.read(databaseProvider);
      await db.taskDao.clearAllTasks();
      await db.sessionDao.clearAllSessions();

      // Update state to success
      state = TaskOperations(state: TaskOperationState.success);

      // Invalidate the task providers to refresh the data
      ref.invalidate(allTasksProvider);
    } catch (e) {
      // Update state to error
      state = TaskOperations(
        state: TaskOperationState.error,
        errorMessage: e.toString(),
      );
    }
  }
}

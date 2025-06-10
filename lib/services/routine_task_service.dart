import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:timezone/data/latest.dart' as tz;
import 'package:uuid/uuid.dart';
import 'package:drift/drift.dart' show Value;

import '../database/database.dart';
import '../providers/database/database_provider.dart';
import '../providers/current_date_provider.dart';

class RoutineTaskService {
  final Ref ref;

  RoutineTaskService(this.ref);

  /// Initialize the service and set up hourly checks
  Future<void> initialize() async {
    print('[RoutineTaskService] Initializing...');
    // Initialize timezone database
    tz.initializeTimeZones();

    // Run initial check
    print('[RoutineTaskService] Running initial task check...');
    await _checkAndCreateRoutineTasks();

    // Set up hourly timer
    print('[RoutineTaskService] Setting up hourly check...');
    _setupHourlyCheck();
    print('[RoutineTaskService] Initialization complete');
  }

  /// Set up a timer to check for routine tasks every hour
  void _setupHourlyCheck() {
    // Calculate milliseconds until next hour
    final now = DateTime.now();
    final nextHour = DateTime(now.year, now.month, now.day, now.hour + 1);
    final durationUntilNextHour = nextHour.difference(now);

    // Set timer to trigger at the next hour and then every hour after that
    Future.delayed(durationUntilNextHour, () {
      _checkAndCreateRoutineTasks();
      // Set up the next hourly check
      _setupHourlyCheck();
    });
  }

  /// Check all routines and create tasks if needed
  Future<void> _checkAndCreateRoutineTasks() async {
    try {
      print('[RoutineTaskService] Checking for routine tasks...');
      final db = ref.read(databaseProvider);
      final currentDate = ref.read(currentDateProvider);

      // Get all active routines
      final routines = await db.routineDao.getAllRoutines();
      print(
        '[RoutineTaskService] Found ${routines.length} routines to process',
      );

      for (final routine in routines) {
        print(
          '[RoutineTaskService] Processing routine: ${routine.title} (${routine.id})',
        );
        await _processRoutine(routine, currentDate);
      }
    } catch (e) {
      print('Error checking routine tasks: $e');
    }
  }

  /// Process a routine for a specific date and create a task if needed
  Future<bool> processRoutineForDate(Routine routine, DateTime date) async {
    try {
      print(
        '[RoutineTaskService] Processing routine ${routine.id} for date: $date',
      );
      final db = ref.read(databaseProvider);
      final today = DateTime(date.year, date.month, date.day);
      final existingTasks = await db.taskDao.getTasksByRoutine(routine.id);

      print(
        '[RoutineTaskService] Found ${existingTasks.length} existing tasks for routine ${routine.id}',
      );

      final hasTaskForDate = existingTasks.any((task) {
        final taskDate = DateTime.parse(task.createdAt);
        final isSameDay =
            taskDate.year == today.year &&
            taskDate.month == today.month &&
            taskDate.day == today.day;
        if (isSameDay) {
          print(
            '[RoutineTaskService] Found existing task for date: ${taskDate.toIso8601String()}',
          );
          print('[RoutineTaskService] Existing task: $task');
        }
        return isSameDay;
      });

      if (!hasTaskForDate) {
        print(
          '[RoutineTaskService] No task found for today, creating new task...',
        );
        final now = DateTime.now().toIso8601String();
        final newTask = TasksCompanion(
          id: Value(const Uuid().v4()),
          title: Value(routine.title),
          status: const Value('planned'),
          estimatedDuration: routine.estimatedDuration != null
              ? Value(routine.estimatedDuration!)
              : const Value.absent(),
          startTime: Value(today.toIso8601String()),
          dueTime: routine.dueTime != null
              ? Value(routine.dueTime!)
              : Value(
                  DateTime(
                    today.year,
                    today.month,
                    today.day,
                    23,
                    59,
                  ).toIso8601String(),
                ),
          note: routine.note != null
              ? Value(routine.note!)
              : const Value.absent(),
          routineId: Value(routine.id),
          createdAt: Value(now),
          updatedAt: Value(now),
        );

        await db.taskDao.createTask(newTask);
        print(
          '[RoutineTaskService] Successfully created new task for routine: ${routine.title}',
        );
        return true;
      }
      return false;
    } catch (e) {
      print('Error processing routine ${routine.id} for date $date: $e');
      return false;
    }
  }

  /// Process a single routine and create tasks if needed
  Future<void> _processRoutine(Routine routine, DateTime currentDate) async {
    await processRoutineForDate(routine, currentDate);
  }

  /// Generates tasks from a routine, ensuring there are always 7 future tasks
  /// Returns the number of tasks created
  Future<int> generateTasksFromRoutine(String routineId) async {
    try {
      print('[RoutineTaskService] Generating tasks for routine: $routineId');
      final db = ref.read(databaseProvider);
      final routine = await db.routineDao.getRoutineById(routineId);
      if (routine == null) throw Exception('Routine not found');

      final now = DateTime.now();
      final existingTasks = await db.taskDao.getTasksByRoutine(routineId);

      // Filter future tasks
      final futureTasks =
          existingTasks
              .where(
                (t) => DateTime.parse(t.dueTime ?? t.startTime!).isAfter(now),
              )
              .toList()
            ..sort(
              (a, b) => (a.dueTime ?? a.startTime)!.compareTo(
                b.dueTime ?? b.startTime!,
              ),
            );

      // If we already have 7 or more future tasks, no need to create more
      if (futureTasks.length >= 7) {
        print(
          '[RoutineTaskService] Already have ${futureTasks.length} future tasks, no need to create more',
        );
        return 0;
      }

      // Calculate how many tasks we need to create
      final tasksToCreate = 7 - futureTasks.length;

      // Get the last due date to start creating new tasks from
      DateTime lastDueDate = futureTasks.isNotEmpty
          ? DateTime.parse(
              futureTasks.last.dueTime ?? futureTasks.last.startTime!,
            )
          : routine.startTime != null
          ? DateTime.parse(routine.startTime!)
          : now;

      // Create new tasks
      int createdCount = 0;
      print('[RoutineTaskService] Need to create $tasksToCreate new tasks');
      for (int i = 0; i < tasksToCreate; i++) {
        final nextDate = lastDueDate.add(Duration(days: i + 1));
        print(
          '[RoutineTaskService] Creating task for date: ${nextDate.toIso8601String()}',
        );
        final created = await processRoutineForDate(routine, nextDate);
        if (created) {
          createdCount++;
          print(
            '[RoutineTaskService] Successfully created task ${i + 1}/$tasksToCreate',
          );
        } else {
          print(
            '[RoutineTaskService] Task already exists for date: ${nextDate.toIso8601String()}',
          );
        }
      }

      return createdCount;
    } catch (e) {
      print('Error generating tasks for routine $routineId: $e');
      rethrow;
    }
  }
}

// Provider for the routine task service
final routineTaskServiceProvider = Provider<RoutineTaskService>((ref) {
  return RoutineTaskService(ref);
});

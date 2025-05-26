import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/widgets.dart';
import 'package:abun/database/database.dart';
import 'package:drift/drift.dart' hide isNotNull;
import 'package:drift/native.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:uuid/uuid.dart';

// Create a test database that runs in memory
AppDatabase createTestDatabase() {
  // Initialize sqflite_common_ffi for testing
  sqfliteFfiInit();

  // Create a new instance of AppDatabase with an in-memory database
  return AppDatabase.forTesting(DatabaseConnection(NativeDatabase.memory()));
}

void main() {
  // Initialize Flutter binding
  WidgetsFlutterBinding.ensureInitialized();
  late AppDatabase database;

  setUp(() async {
    // Create a test database before each test
    database = createTestDatabase();

    // Clear all tables
    await database.delete(database.tasks).go();
    await database.delete(database.routines).go();
    await database.delete(database.sessions).go();
  });

  test('Insert and retrieve a task', () async {
    // Create a task
    final taskId = await database.taskDao.createTask(
      TasksCompanion.insert(
        title: 'Test Task',
        status: Value('inbox'),
      ),
    );

    // Verify the task was inserted
    expect(taskId, isNotNull);

    // Retrieve all tasks
    final tasks = await database.taskDao.getAllTasks();

    // Verify we got back the task we inserted
    expect(tasks.length, 1);
    expect(tasks[0].title, 'Test Task');
    expect(tasks[0].status, 'inbox');
    expect(tasks[0].id, taskId);
  });

  test('Update a task', () async {
    // Create a task
    final taskId = await database.taskDao.createTask(
      TasksCompanion.insert(
        title: 'Task to update',
        status: Value('inbox'),
      ),
    );

    // Retrieve the task
    final task = await database.taskDao.getTaskById(taskId);
    expect(task, isNotNull);

    if (task != null) {
      // Update the task
      final updatedTask = task.copyWith(
        title: 'Updated task',
        status: 'completed',
      );

      final success = await database.taskDao.updateTask(updatedTask);
      expect(success, isTrue);

      // Retrieve the updated task
      final retrievedTask = await database.taskDao.getTaskById(taskId);
      expect(retrievedTask?.title, 'Updated task');
      expect(retrievedTask?.status, 'completed');
    }
  });

  test('Delete a task', () async {
    // Create a task
    final taskId = await database.taskDao.createTask(
      TasksCompanion.insert(
        title: 'Task to delete',
        status: Value('inbox'),
      ),
    );

    // Verify the task was inserted
    var tasks = await database.taskDao.getAllTasks();
    expect(tasks.length, 1);

    // Delete the task
    final rowsAffected = await database.taskDao.deleteTask(taskId);
    expect(rowsAffected, 1);

    // Verify the task was deleted
    tasks = await database.taskDao.getAllTasks();
    expect(tasks.length, 0);
  });

  test('Get tasks by routine', () async {
    // Create a routine
    final routineId = await database.createRoutine(
      RoutinesCompanion.insert(
        id: Value(const Uuid().v4()),
        title: 'Daily Routine',
        recurrenceRule: 'FREQ=DAILY;INTERVAL=1',
        createdAt: Value(DateTime.now().toIso8601String()),
        updatedAt: Value(DateTime.now().toIso8601String()),
      ),
    );

    // Create tasks for the routine
    await database.taskDao.createTask(
      TasksCompanion.insert(
        title: 'Task 1 for routine',
        routineId: Value(routineId),
        status: Value('inbox'),
      ),
    );

    await database.taskDao.createTask(
      TasksCompanion.insert(
        title: 'Task 2 for routine',
        routineId: Value(routineId),
        status: Value('completed'),
      ),
    );

    // Create a task not associated with the routine
    await database.taskDao.createTask(
      TasksCompanion.insert(
        title: 'Independent Task',
        status: Value('inbox'),
      ),
    );

    // Get tasks by routine
    final tasksByRoutine = await database.taskDao.getTasksByRoutine(routineId);

    // Verify we got the correct tasks
    expect(tasksByRoutine.length, 2);
    expect(tasksByRoutine.every((task) => task.routineId == routineId), isTrue);

    // Verify all tasks
    final allTasks = await database.taskDao.getAllTasks();
    expect(allTasks.length, 3);
  });

  test('Create and retrieve a session', () async {
    // Create a task first
    final taskId = await database.taskDao.createTask(
      TasksCompanion.insert(
        title: 'Task for session',
        status: Value('in_progress'),
      ),
    );

    // Create a session for the task
    final now = DateTime.now();
    final startTime = now.subtract(const Duration(minutes: 25));
    final endTime = now;

    final sessionId = await database.createSession(
      SessionsCompanion.insert(
        id: Value(const Uuid().v4()),
        taskId: Value(taskId),
        startTime: Value(startTime.toIso8601String()),
        endTime: Value(endTime.toIso8601String()),
        duration: 'PT25M', // 25 minutes in ISO8601 duration format
        type: const Value('pomodoro'),
        mood: const Value('productive'),
      ),
    );

    // Verify the session was created
    expect(sessionId, isNotNull);

    // Get sessions by task
    final sessions = await database.getSessionsByTask(taskId);

    // Verify we got the correct session
    expect(sessions.length, 1);
    expect(sessions[0].taskId, taskId);
    expect(sessions[0].type, 'pomodoro');
    expect(sessions[0].duration, 'PT25M');
    expect(sessions[0].mood, 'productive');
  });

  test('Create and retrieve a session without a task', () async {
    final now = DateTime.now();
    final startTime = now.subtract(const Duration(minutes: 25));
    final endTime = now;

    final sessionId = await database.createSession(
      SessionsCompanion.insert(
        id: Value(const Uuid().v4()),
        title: Value('Session without task'),
        taskId: Value.absent(),
        startTime: Value(startTime.toIso8601String()),
        endTime: Value(endTime.toIso8601String()),
        duration: 'PT25M', // 25 minutes in ISO8601 duration format
        type: const Value('pomodoro'),
        mood: const Value('productive'),
      ),
    );

    // Verify the session was created
    expect(sessionId, isNotNull);

  });
}

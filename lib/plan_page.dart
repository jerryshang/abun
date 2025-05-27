import 'package:abun/widgets/plan_task_card.dart';
import 'package:abun/widgets/task_form.dart';
import 'package:drift/drift.dart' as drift hide Column;
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'database/database.dart';
import 'models/task_status.dart';
import 'providers/database/index.dart';
import 'providers/task_notifier.dart';

class PlanPage extends ConsumerStatefulWidget {
  final String title;

  const PlanPage({super.key, required this.title});

  @override
  ConsumerState<PlanPage> createState() => _PlanPageState();
}

class _PlanPageState extends ConsumerState<PlanPage> {
  // Controllers for the form fields
  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _estimatedDurationController =
      TextEditingController();
  final TextEditingController _noteController = TextEditingController();
  DateTime? _startTime;
  DateTime? _dueTime;
  TaskStatus _selectedStatus = TaskStatus.inbox; // Default status

  // Selected task for editing
  Task? _selectedTask;

  @override
  void dispose() {
    // Clean up controllers when the widget is disposed
    _titleController.dispose();
    _estimatedDurationController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  // Reset form fields
  void _resetForm() {
    setState(() {
      _titleController.clear();
      _estimatedDurationController.clear();
      _noteController.clear();
      _startTime = null;
      _dueTime = null;
      _selectedStatus = TaskStatus.inbox;
      _selectedTask = null;
    });
  }

  // Moved to PlanTaskCard widget

  // Create a new task
  Future<void> _createTask() async {
    if (_titleController.text.isEmpty) {
      // Show error message if title is empty
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Task title cannot be empty'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    // Format estimated duration if provided (convert minutes to ISO8601 duration)
    String? estimatedDuration;
    if (_estimatedDurationController.text.isNotEmpty) {
      try {
        final minutes = int.parse(_estimatedDurationController.text);
        estimatedDuration = 'PT${minutes}M'; // ISO8601 duration format
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Estimated time must be a number in minutes'),
            backgroundColor: Colors.red,
          ),
        );
        return;
      }
    }

    await ref
        .read(taskNotifierProvider.notifier)
        .createTask(
          title: _titleController.text,
          status: _selectedStatus.value,
          estimatedDuration: estimatedDuration,
          startTime: _startTime,
          dueTime: _dueTime,
          note: _noteController.text.isNotEmpty ? _noteController.text : null,
        );

    // Reset form after creating task
    _resetForm();
  }

  // Update an existing task
  Future<void> _updateTask() async {
    if (_selectedTask == null || _titleController.text.isEmpty) {
      return;
    }

    // Format estimated duration if provided (convert minutes to ISO8601 duration)
    String? estimatedDuration;
    if (_estimatedDurationController.text.isNotEmpty) {
      try {
        final minutes = int.parse(_estimatedDurationController.text);
        estimatedDuration = 'PT${minutes}M'; // ISO8601 duration format
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Estimated time must be a number in minutes'),
            backgroundColor: Colors.red,
          ),
        );
        return;
      }
    }

    final updatedTask = _selectedTask!.copyWith(
      title: _titleController.text,
      status: _selectedStatus.value,
      estimatedDuration: estimatedDuration != null
          ? drift.Value(estimatedDuration)
          : const drift.Value.absent(),
      startTime: _startTime != null
          ? drift.Value(_startTime!.toIso8601String())
          : const drift.Value.absent(),
      dueTime: _dueTime != null
          ? drift.Value(_dueTime!.toIso8601String())
          : const drift.Value.absent(),
      note: _noteController.text.isNotEmpty
          ? drift.Value(_noteController.text)
          : const drift.Value.absent(),
      updatedAt: DateTime.now().toIso8601String(),
    );

    await ref.read(taskNotifierProvider.notifier).updateTask(updatedTask);

    // Reset form after updating task
    _resetForm();
  }

  // Delete a task
  Future<void> _deleteTask(String id) async {
    await ref.read(taskNotifierProvider.notifier).deleteTask(id);

    // Reset form if the deleted task was selected
    if (_selectedTask?.id == id) {
      _resetForm();
    }
  }

  // Select a task for editing
  void _selectTask(Task task) {
    setState(() {
      _selectedTask = task;
      _titleController.text = task.title;
      _selectedStatus = TaskStatus.fromString(task.status);

      // Parse ISO8601 duration to minutes for the UI
      if (task.estimatedDuration != null &&
          task.estimatedDuration!.startsWith('PT') &&
          task.estimatedDuration!.endsWith('M')) {
        try {
          final minutes = int.parse(
            task.estimatedDuration!.substring(
              2,
              task.estimatedDuration!.length - 1,
            ),
          );
          _estimatedDurationController.text = minutes.toString();
        } catch (e) {
          _estimatedDurationController.clear();
        }
      } else {
        _estimatedDurationController.clear();
      }

      // Parse ISO8601 strings to DateTime objects
      _startTime = task.startTime != null
          ? DateTime.parse(task.startTime!)
          : null;
      _dueTime = task.dueTime != null ? DateTime.parse(task.dueTime!) : null;
      _noteController.text = task.note ?? '';
    });
  }

  // Show confirmation dialog before deleting a task
  Future<void> _showDeleteConfirmation(BuildContext context, String id) async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext dialogContext) {
        return AlertDialog(
          title: const Text('Delete Task'),
          content: const SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text('Are you sure you want to delete this task?'),
                Text('This action cannot be undone.'),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Cancel'),
              onPressed: () {
                Navigator.of(dialogContext).pop();
              },
            ),
            TextButton(
              child: const Text('Delete', style: TextStyle(color: Colors.red)),
              onPressed: () {
                Navigator.of(dialogContext).pop();
                _deleteTask(id);
              },
            ),
          ],
        );
      },
    );
  }

  // Show bottom sheet for creating/editing tasks
  void _showTaskBottomSheet(BuildContext context) {
    // Reset form if we're creating a new task (not editing)
    if (_selectedTask == null) {
      _resetForm();
    }

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (BuildContext context) {
        return Padding(
          padding: EdgeInsets.only(
            bottom: MediaQuery.of(context).viewInsets.bottom,
            left: 16,
            right: 16,
            top: 16,
          ),
          child: TaskForm(
            task: _selectedTask,
            titleController: _titleController,
            estimatedDurationController: _estimatedDurationController,
            noteController: _noteController,
            initialStartTime: _startTime,
            initialDueTime: _dueTime,
            initialStatus: _selectedStatus,
            onTaskSaved: () {
              if (_selectedTask == null) {
                _createTask();
              } else {
                _updateTask();
              }
              Navigator.pop(context);
            },
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    // Watch the task operation state
    final taskOperations = ref.watch(taskNotifierProvider);

    // Watch the tasks using the provider
    final tasksAsync = ref.watch(watchAllTasksProvider);

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
        // We don't need a custom back button with named routes
        // Flutter will automatically add a back button to the AppBar
        // when there are routes in the navigation stack
      ),
      body: Stack(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Task list
                Expanded(
                  child: tasksAsync.when(
                    data: (tasks) {
                      if (tasks.isEmpty) {
                        return const Center(
                          child: Text(
                            'No tasks available. Create your first task!',
                          ),
                        );
                      }

                      return ListView.builder(
                        itemCount: tasks.length,
                        itemBuilder: (context, index) {
                          final task = tasks[index];
                          return PlanTaskCard(
                            task: task,
                            formatDate: _formatDate,
                            onEditPressed: () {
                              _selectTask(task);
                              _showTaskBottomSheet(context);
                            },
                            onDeletePressed: () => _showDeleteConfirmation(context, task.id),
                            onSchedulePressed: (selectedDate) async {
                              await ref.read(taskNotifierProvider.notifier).updateTask(
                                task.copyWith(
                                  status: TaskStatus.planned.value,
                                  startTime: drift.Value(selectedDate.toIso8601String()),
                                ),
                              );
                            },
                          );
                        },
                      );
                    },
                    loading: () =>
                        const Center(child: CircularProgressIndicator()),
                    error: (error, stackTrace) =>
                        Center(child: Text('Error: $error')),
                  ),
                ),
              ],
            ),
          ),

          // Clear database button at the bottom left
          Positioned(
            left: 16,
            bottom: 16,
            child: FloatingActionButton(
              onPressed: () => _showClearDatabaseConfirmation(context),
              tooltip: 'Clear Database',
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
              // Ensure icon is visible
              heroTag: 'clearDatabase',
              // Unique hero tag
              child: const Icon(Icons.delete_forever),
            ),
          ),

          // Add task button at the bottom right
          Positioned(
            right: 16,
            bottom: 16,
            child: FloatingActionButton(
              onPressed: () {
                _resetForm(); // Reset form before showing bottom sheet
                _showTaskBottomSheet(context);
              },
              tooltip: 'New Task',
              backgroundColor: Theme.of(context).colorScheme.primary,
              foregroundColor: Colors.white,
              // Ensure icon is visible
              heroTag: 'addTask',
              // Unique hero tag
              child: const Icon(Icons.add),
            ),
          ),
        ],
      ),
    );
  }

  // Helper method to format date
  String _formatDate(DateTime date) {
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }

  // Helper method to format date and time
  String _formatDateTime(DateTime dateTime) {
    return '${dateTime.year}-${dateTime.month.toString().padLeft(2, '0')}-${dateTime.day.toString().padLeft(2, '0')} ${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  }

  // Helper method to get icon for task status
  // Moved to PlanTaskCard widget

  // Show confirmation dialog before clearing the database
  Future<void> _showClearDatabaseConfirmation(BuildContext context) async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext dialogContext) {
        return AlertDialog(
          title: const Text('Clear Database'),
          content: const SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text('Are you sure you want to delete all tasks and sessions?'),
                Text('This action cannot be undone.'),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Cancel'),
              onPressed: () {
                Navigator.of(dialogContext).pop();
              },
            ),
            TextButton(
              child: const Text(
                'Clear All',
                style: TextStyle(color: Colors.red),
              ),
              onPressed: () {
                Navigator.of(dialogContext).pop();
                ref.read(taskNotifierProvider.notifier).clearAllTasks();
                _resetForm();
              },
            ),
          ],
        );
      },
    );
  }
}

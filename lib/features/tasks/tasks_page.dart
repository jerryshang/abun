import 'package:abun/widgets/plan_task_card.dart';
import 'package:abun/widgets/task_form.dart';
import 'package:drift/drift.dart' as drift hide Column;
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../database/database.dart';
import '../../models/task_status.dart';
import '../../providers/database/index.dart';
import '../../providers/task_notifier.dart';

class TasksPage extends ConsumerStatefulWidget {
  const TasksPage({super.key});

  @override
  ConsumerState<TasksPage> createState() => _PlanPageState();
}

class _PlanPageState extends ConsumerState<TasksPage> {
  bool _showCompleted = false;
  bool _showRoutineTasks = false;
  bool _recentFirst = true;

  // Form state
  TaskStatus _selectedStatus = TaskStatus.inbox; // Default status
  Task? _selectedTask; // Selected task for editing

  @override
  Widget build(BuildContext context) {
    final tasks = ref.watch(
      watchTasksProvider.call(
        showCompleted: _showCompleted,
        recentFirst: _recentFirst,
        showRoutineGenerated: _showRoutineTasks,
      ),
    );

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text("Tasks"),
        actions: [
          PopupMenuButton<dynamic>(
            icon: const Icon(Icons.more_vert),
            position: PopupMenuPosition.under,
            itemBuilder: (BuildContext context) => <PopupMenuEntry<dynamic>>[
              PopupMenuItem(
                value: 'sort_desc',
                child: Row(
                  children: [
                    Checkbox(
                      value: _recentFirst,
                      onChanged: null, // Disable direct checkbox interaction
                    ),
                    const Text("Sort by Date Desc"),
                  ],
                ),
              ),
              PopupMenuItem(
                value: 'sort_asc',
                child: Row(
                  children: [
                    Checkbox(
                      value: !_recentFirst,
                      onChanged: null, // Disable direct checkbox interaction
                    ),
                    const Text("Sort by Date Asc"),
                  ],
                ),
              ),
              const PopupMenuDivider(),
              PopupMenuItem(
                value: 'toggle_completed',
                child: Row(
                  children: [
                    Checkbox(
                      value: _showCompleted,
                      onChanged: null, // Disable direct checkbox interaction
                    ),
                    const Text("Show Completed Tasks"),
                  ],
                ),
              ),
              PopupMenuItem(
                value: 'toggle_routine',
                child: Row(
                  children: [
                    Checkbox(
                      value: _showRoutineTasks,
                      onChanged: null, // Disable direct checkbox interaction
                    ),
                    const Text("Show Routine Tasks"),
                  ],
                ),
              ),
              const PopupMenuDivider(),
              PopupMenuItem(
                value: 'delete_all',
                child: Row(
                  children: [
                    const Icon(Icons.delete_forever, color: Colors.red, size: 20),
                    const SizedBox(width: 12),
                    Text('Delete All Tasks', style: TextStyle(color: Colors.red)),
                  ],
                ),
              ),
            ],
            onSelected: (value) {
              if (value == 'sort_desc') {
                setState(() => _recentFirst = true);
              } else if (value == 'sort_asc') {
                setState(() => _recentFirst = false);
              } else if (value == 'toggle_completed') {
                setState(() => _showCompleted = !_showCompleted);
              } else if (value == 'toggle_routine') {
                setState(() => _showRoutineTasks = !_showRoutineTasks);
              } else if (value == 'delete_all') {
                _showClearDatabaseConfirmation(context);
              }
            },
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: tasks.when(
          data: (tasks) {
            if (tasks.isEmpty) {
              return const Center(child: Text('No tasks available. Create your first task!'));
            }

            return ListView.builder(
              itemCount: tasks.length + 1,
              itemBuilder: (context, index) {
                if (index == tasks.length) {
                  return const SizedBox(height: 60.0);
                }
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
                    await ref
                        .read(taskNotifierProvider.notifier)
                        .updateTask(
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
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, stackTrace) => Center(child: Text('Error: $error')),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          _resetForm();
          _showTaskBottomSheet(context);
        },
        tooltip: 'New Task',
        backgroundColor: Theme.of(context).colorScheme.primary,
        foregroundColor: Colors.white,
        heroTag: 'addTask',
        child: const Icon(Icons.add),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
  }

  // Reset form state
  void _resetForm() {
    setState(() {
      _selectedTask = null;
      _selectedStatus = TaskStatus.inbox;
    });
  }

  // Moved to PlanTaskCard widget

  // Create a new task
  Future<void> _createTask(TaskFormData formData) async {
    // Format estimated duration if provided (convert minutes to ISO8601 duration)
    String? estimatedDuration;
    if (formData.estimatedDuration != null && formData.estimatedDuration!.isNotEmpty) {
      try {
        final minutes = int.parse(formData.estimatedDuration!);
        estimatedDuration = 'PT${minutes}M'; // ISO8601 duration format
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Estimated time must be a number in minutes'), backgroundColor: Colors.red),
          );
        }
        return;
      }
    }

    await ref
        .read(taskNotifierProvider.notifier)
        .createTask(
          title: formData.title,
          // status: formData.status,
          estimatedDuration: estimatedDuration,
          startTime: formData.startTime,
          dueTime: formData.dueTime,
          note: formData.note,
        );

    // Reset form after creating task
    _resetForm();
  }

  // Update an existing task
  Future<void> _updateTask(TaskFormData formData) async {
    if (_selectedTask == null) return;

    // Format estimated duration if provided (convert minutes to ISO8601 duration)
    String? estimatedDuration;
    if (formData.estimatedDuration != null && formData.estimatedDuration!.isNotEmpty) {
      try {
        final minutes = int.parse(formData.estimatedDuration!);
        estimatedDuration = 'PT${minutes}M'; // ISO8601 duration format
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Estimated time must be a number in minutes'), backgroundColor: Colors.red),
          );
        }
        return;
      }
    }

    final updatedTask = _selectedTask!.copyWith(
      title: formData.title,
      status: formData.status,
      estimatedDuration: estimatedDuration != null ? drift.Value(estimatedDuration) : drift.Value(null),
      startTime: formData.startTime != null ? drift.Value(formData.startTime!.toIso8601String()) : drift.Value(null),
      dueTime: formData.dueTime != null ? drift.Value(formData.dueTime!.toIso8601String()) : drift.Value(null),
      note: formData.note != null ? drift.Value(formData.note!) : drift.Value(null),
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
      _selectedStatus = TaskStatus.fromString(task.status);
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
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (BuildContext context) {
        return Padding(
          padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom, left: 16, right: 16, top: 16),
          child: TaskForm(
            task: _selectedTask,
            initialTitle: _selectedTask?.title ?? '',
            initialEstimatedDuration:
                _selectedTask?.estimatedDuration != null &&
                    _selectedTask!.estimatedDuration!.startsWith('PT') &&
                    _selectedTask!.estimatedDuration!.endsWith('M')
                ? _selectedTask!.estimatedDuration!.substring(2, _selectedTask!.estimatedDuration!.length - 1)
                : null,
            initialNote: _selectedTask?.note,
            initialStartTime: _selectedTask?.startTime != null ? DateTime.parse(_selectedTask!.startTime!) : null,
            initialDueTime: _selectedTask?.dueTime != null ? DateTime.parse(_selectedTask!.dueTime!) : null,
            initialStatus: _selectedStatus,
            onSaved: (formData) {
              if (_selectedTask == null) {
                _createTask(formData);
              } else {
                _updateTask(formData);
              }
              Navigator.pop(context);
            },
          ),
        );
      },
    );
  }

  // Helper method to format date
  String _formatDate(DateTime date) {
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }

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
              child: const Text('Clear All', style: TextStyle(color: Colors.red)),
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

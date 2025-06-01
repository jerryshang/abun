import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../providers/database/task_provider.dart';

class TasksByRoutinePage extends ConsumerWidget {
  final String routineId;
  final String routineTitle;

  const TasksByRoutinePage({
    super.key,
    required this.routineId,
    required this.routineTitle,
  });

  Color _getStatusColor(String status) {
    switch (status) {
      case 'completed':
        return Colors.green;
      case 'in_progress':
        return Colors.blue;
      case 'planned':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tasksAsync = ref.watch(tasksByRoutineProvider(routineId));

    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text('Tasks: $routineTitle'),
      ),
      body: tasksAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(child: Text('Error: $error')),
        data: (tasks) {
          if (tasks.isEmpty) {
            return const Center(child: Text('No tasks found for this routine'));
          }
          return ListView.builder(
            padding: const EdgeInsets.all(8),
            itemCount: tasks.length,
            itemBuilder: (context, index) {
              final task = tasks[index];
              return Card(
                margin: const EdgeInsets.symmetric(vertical: 4),
                child: ListTile(
                  title: Text(
                    DateFormat('yyyy-MM-dd HH:mm').format(
                      DateTime.parse(task.startTime ?? task.dueTime ?? DateTime.now().toIso8601String()),
                    ),
                  ),
                  trailing: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: _getStatusColor(task.status),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      task.status.replaceAll('_', ' ').toUpperCase(),
                      style: const TextStyle(color: Colors.white, fontSize: 12),
                    ),
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}

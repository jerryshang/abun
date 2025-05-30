import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../constants/app_constants.dart';
import '../database/database.dart';
import '../extensions/task_status_extension.dart';
import '../models/task_status.dart';

class PlanTaskCard extends ConsumerWidget {
  final Task task;
  final VoidCallback onEditPressed;
  final VoidCallback onDeletePressed;
  final Future<void> Function(DateTime) onSchedulePressed;
  final String Function(DateTime) formatDate;

  const PlanTaskCard({
    super.key,
    required this.task,
    required this.onEditPressed,
    required this.onDeletePressed,
    required this.onSchedulePressed,
    required this.formatDate,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Text(task.title),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (task.estimatedDuration != null &&
                task.estimatedDuration!.startsWith('PT') &&
                task.estimatedDuration!.endsWith('M'))
              Text('Est. Time: ${task.estimatedDuration!.substring(2, task.estimatedDuration!.length - 1)} min'),
            if (task.startTime != null) Text('Start: ${formatDate(DateTime.parse(task.startTime!))}'),
            if (task.dueTime != null) Text('Due: ${formatDate(DateTime.parse(task.dueTime!))}'),
            if (task.note != null && task.note!.isNotEmpty)
              Text('Note: ${task.note!.length > 30 ? '${task.note!.substring(0, 30)}...' : task.note}'),
          ],
        ),
        // isThreeLine: true,
        leading: _getIconForStatus(TaskStatus.fromString(task.status)),
        trailing: Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            SizedBox(
              width: AppConstants.defaultIconButtonSize,
              child: IconButton(
                icon: const Icon(Icons.schedule, color: Colors.green),
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
                onPressed: () async {
                  final selectedDate = await _showDatePickerDialog(context);
                  if (selectedDate != null) {
                    await onSchedulePressed(selectedDate);
                  }
                },
              ),
            ),
            SizedBox(
              width: AppConstants.defaultIconButtonSize,
              child: IconButton(
                icon: const Icon(Icons.edit, color: Colors.blue),
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
                onPressed: onEditPressed,
              ),
            ),
            SizedBox(
              width: AppConstants.defaultIconButtonSize,
              child: IconButton(
                icon: const Icon(Icons.delete, color: Colors.red),
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
                onPressed: onDeletePressed,
              ),
            ),
          ],
        ),
        onTap: onEditPressed,
      ),
    );
  }

  Widget _getIconForStatus(TaskStatus status) => status.toIcon();

  Future<DateTime?> _showDatePickerDialog(BuildContext context) async {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final tomorrow = today.add(const Duration(days: 1));
    final nextWeek = today.add(const Duration(days: 7));

    return await showDialog<DateTime>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Select Start Date'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(title: const Text('Today'), onTap: () => Navigator.pop(context, today)),
              ListTile(title: const Text('Tomorrow'), onTap: () => Navigator.pop(context, tomorrow)),
              ListTile(title: const Text('Next Week'), onTap: () => Navigator.pop(context, nextWeek)),
              const Divider(),
              ListTile(
                title: const Text('Pick a date...'),
                onTap: () async {
                  final date = await showDatePicker(
                    context: context,
                    initialDate: today,
                    firstDate: today,
                    lastDate: today.add(const Duration(days: 365)),
                  );
                  if (date != null && context.mounted) {
                    Navigator.pop(context, date);
                  }
                },
              ),
            ],
          ),
          actions: [TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancel'))],
        );
      },
    );
  }
}

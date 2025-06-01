import 'package:abun/database/database.dart';
import 'package:abun/extensions/task_status_extension.dart';
import 'package:abun/extensions/time_extensions.dart';
import 'package:abun/models/task_status.dart';
import 'package:abun/providers/database/index.dart';
import 'package:abun/widgets/session_form.dart';
import 'package:drift/drift.dart' hide Column;
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class TaskCard extends ConsumerWidget {
  final Task task;
  final VoidCallback? onStartPressed;
  final VoidCallback? onCompletePressed;

  const TaskCard({super.key, required this.task, this.onStartPressed, this.onCompletePressed});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessionsAsync = ref.watch(watchSessionsByTaskProvider(task.id));

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 2, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header row with title and status icon
                  Row(
                    children: [
                      // Status icon
                      TaskStatus.fromString(task.status).toIcon(size: 28),
                      const SizedBox(width: 8),
                      // Title
                      Expanded(
                        child: Text(
                          task.title,
                          style: Theme.of(context).textTheme.headlineSmall,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),

                  // Note if exists
                  if (task.note != null && task.note!.isNotEmpty) ...{
                    const SizedBox(height: 8),
                    Text(
                      task.note!,
                      style: Theme.of(context).textTheme.bodyMedium,
                      maxLines: 3,
                      overflow: TextOverflow.ellipsis,
                    ),
                  },

                  if (task.dueTime != null) ...{
                    const SizedBox(height: 8),
                    Text(
                      task.dueTime!,
                      style: Theme.of(context).textTheme.bodySmall,
                      maxLines: 1,
                    ),
                  },

                  const SizedBox(height: 8),
                  // Sessions preview
                  Row(
                    children: [
                      Expanded(
                        child: sessionsAsync.when(
                          data: (sessions) {
                            if (sessions.isEmpty) return const SizedBox.shrink();
                            return Wrap(
                              spacing: 4,
                              runSpacing: 4,
                              children: sessions.take(5).map((session) {
                                return Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: Theme.of(context).colorScheme.primaryContainer,
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: Text(
                                    _formatDuration(session.duration),
                                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                      color: Theme.of(context).colorScheme.onPrimaryContainer,
                                    ),
                                  ),
                                );
                              }).toList(),
                            );
                          },
                          loading: () => const SizedBox(
                            height: 24,
                            width: 24,
                            child: Padding(
                              padding: EdgeInsets.all(4.0),
                              child: CircularProgressIndicator(strokeWidth: 2),
                            ),
                          ),
                          error: (_, __) => const SizedBox.shrink(),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            SizedBox(
              height: 40,
              child: VerticalDivider(
                width: 4,
                thickness: 1,
                color: Theme.of(context).dividerColor,
              ),
            ),

            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (onStartPressed != null)
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      IconButton(
                        icon: const Icon(Icons.fact_check, size: 24, color: Colors.green),
                        onPressed: () => _showSessionForm(context),
                        // padding: const EdgeInsets.symmetric(horizontal: 8),
                        constraints: const BoxConstraints(),
                      ),
                      // const Text('Session', style: TextStyle(fontSize: 12, color: Colors.green)),
                    ],
                  ),

                // Column(
                //   mainAxisSize: MainAxisSize.min,
                //   children: [
                //     IconButton(
                //       icon: const Icon(Icons.timer, size: 28, color: Colors.blue),
                //       onPressed: onStartPressed,
                //       padding: const EdgeInsets.symmetric(horizontal: 8),
                //       constraints: const BoxConstraints(),
                //     ),
                //     // const Text('pomo', style: TextStyle(fontSize: 12, color: Colors.blue)),
                //   ],
                // ),
                Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      icon: const Icon(Icons.restore, size: 24, color: Colors.orange),
                      onPressed: () async {
                        _showPostponeDialog(context, ref);
                      },
                      // padding: const EdgeInsets.symmetric(horizontal: 8),
                      constraints: const BoxConstraints(),
                    ),
                    // const Text('postpone', style: TextStyle(fontSize: 12, color: Colors.orange)),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _showPostponeDialog(BuildContext context, WidgetRef ref) async {
    final now = DateTime.now();

    // Calculate new dates based on current task dates
    final currentStartTime = task.startTime?.toDateTime() ?? now;
    final currentDueTime = task.dueTime?.toDateTime() ?? now;

    final result = await showDialog<Duration?>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Postpone Task'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(title: const Text('1 day later'), onTap: () => Navigator.pop(context, const Duration(days: 1))),
            ListTile(title: const Text('3 days later'), onTap: () => Navigator.pop(context, const Duration(days: 3))),
            ListTile(title: const Text('1 week later'), onTap: () => Navigator.pop(context, const Duration(days: 7))),
            const Divider(),
            ListTile(
              title: const Text('Put back to INBOX', style: TextStyle(color: Colors.red)),
              onTap: () => Navigator.pop(context, const Duration(days: 0)),
            ),
          ],
        ),
        actions: [TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancel'))],
      ),
    );

    if (result == null) return;

    if (!context.mounted) return;

    try {
      if (result == Duration(days: 0)) {
        // Handle unscheduled
        await ref
            .read(databaseProvider)
            .taskDao
            .updateTask(task.copyWith(startTime: Value(null), dueTime: Value(null)));
      } else {
        // Handle postpone with duration
        final newStartTime = task.startTime != null ? currentStartTime.add(result) : null;
        final newDueTime = task.dueTime != null ? currentDueTime.add(result) : null;

        await ref
            .read(databaseProvider)
            .taskDao
            .updateTask(
              task.copyWith(
                startTime: Value(newStartTime?.toIso8601String()),
                dueTime: Value(newDueTime?.toIso8601String()),
              ),
            );
      }

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Task postponed')));
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: ${e.toString()}')));
      }
    }
  }

  void _showSessionForm(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (context) => SessionForm(task: task),
    );
  }

  String _formatDuration(String? duration) {
    if (duration == null || duration.isEmpty) return '';

    try {
      // Parse ISO 8601 duration (e.g., PT25M for 25 minutes)
      final regex = RegExp(r'^PT(\d+H)?(\d+M)?(\d+S)?$');
      final match = regex.firstMatch(duration);

      if (match == null) return duration;

      final hours = match.group(1) != null ? '${match.group(1)!.replaceAll('H', '')}h ' : '';
      final minutes = match.group(2) != null ? '${match.group(2)!.replaceAll('M', '')}m ' : '';
      final seconds = match.group(3) != null ? '${match.group(3)!.replaceAll('S', '')}s' : '';

      return '$hours$minutes$seconds'.trim();
    } catch (e) {
      return duration;
    }
  }
}

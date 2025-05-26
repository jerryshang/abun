import 'package:flutter/material.dart';
import 'package:abun/database/database.dart';
import 'package:abun/extensions/task_status_extension.dart';
import 'package:abun/models/task_status.dart';
import 'package:abun/providers/database_provider.dart';
import 'package:abun/widgets/session_form.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class TaskCard extends ConsumerWidget {
  final Task task;
  final VoidCallback? onStartPressed;
  final VoidCallback? onCompletePressed;

  const TaskCard({
    super.key,
    required this.task,
    this.onStartPressed,
    this.onCompletePressed,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessionsAsync = ref.watch(watchSessionsByTaskProvider(task.id));

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
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
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 6,
                                    vertical: 2,
                                  ),
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
            const SizedBox(width: 8),
            if (task.status == 'completed')
              const Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.check_circle, color: Colors.green, size: 18),
                  SizedBox(width: 4),
                  Text('Completed'),
                ],
              )
            else
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (onStartPressed != null)
                    Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.fact_check, size: 28, color: Colors.green),
                          onPressed: () => _showSessionForm(context),
                          padding: const EdgeInsets.symmetric(horizontal: 8),
                          constraints: const BoxConstraints(),
                        ),
                        const Text('Session', style: TextStyle(fontSize: 12, color: Colors.green)),
                      ],
                    ),
                    Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.timer, size: 28, color: Colors.blue),
                          onPressed: onStartPressed,
                          padding: const EdgeInsets.symmetric(horizontal: 8),
                          constraints: const BoxConstraints(),
                        ),
                        const Text('pomo', style: TextStyle(fontSize: 12, color: Colors.blue)),
                      ],
                    ),

                ],
              ),
            ],
          ),
        ),
    );
  }

  // Status icon is now provided by TaskStatusExtension

  void _showSessionForm(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(
          top: Radius.circular(16),
        ),
      ),
      builder: (context) => SessionForm(taskId: task.id),
    );
  }

  String _formatDuration(String? duration) {
    if (duration == null || duration.isEmpty) return '';

    try {
      // Parse ISO 8601 duration (e.g., PT25M for 25 minutes)
      final regex = RegExp(r'^PT(\d+H)?(\d+M)?(\d+S)?$');
      final match = regex.firstMatch(duration);

      if (match == null) return duration;

      final hours = match.group(1) != null
          ? '${match.group(1)!.replaceAll('H', '')}h '
          : '';
      final minutes = match.group(2) != null
          ? '${match.group(2)!.replaceAll('M', '')}m '
          : '';
      final seconds = match.group(3) != null
          ? '${match.group(3)!.replaceAll('S', '')}s'
          : '';

      return '$hours$minutes$seconds'.trim();
    } catch (e) {
      return duration;
    }
  }
}

import 'package:abun/extensions/time_extensions.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../constants/app_constants.dart';
import '../../database/database.dart';
import '../../providers/database/index.dart';

class SessionsPage extends ConsumerStatefulWidget {
  const SessionsPage({super.key});

  @override
  ConsumerState<SessionsPage> createState() => _SessionsPageState();
}

class _SessionsPageState extends ConsumerState<SessionsPage> {
  DateTimeRange _dateRange = DateTimeRange(
    start: DateTime.now().subtract(const Duration(days: 7)),
    end: DateTime.now().add(const Duration(days: 1)),
  );
  bool _groupByTask = false;

  void _toggleGroupByTask() {
    setState(() {
      _groupByTask = !_groupByTask;
    });
  }

  Future<void> _selectDateRange(BuildContext context) async {
    final DateTimeRange? picked = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2020),
      lastDate: DateTime(2100),
      initialDateRange: _dateRange,
    );
    if (picked != null && picked != _dateRange) {
      setState(() {
        _dateRange = picked;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (kDebugMode) {
      print('Building SessionsPage with date range: ${_dateRange.start} to ${_dateRange.end}');
    }

    final sessionsAsync = ref.watch(watchAllSessionsProvider);

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Sessions'),
        actions: [
          IconButton(icon: const Icon(Icons.date_range), onPressed: () => _selectDateRange(context)),
          IconButton(icon: Icon(_groupByTask ? Icons.view_list : Icons.view_agenda), onPressed: _toggleGroupByTask),
        ],
      ),
      body: sessionsAsync.when(
        data: (sessions) {
          return FutureBuilder<List<SessionWithTask>>(
            future: _getSessionsWithTasks(sessions),
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return _buildLoading();
              }

              if (snapshot.hasError) {
                return _buildError(snapshot.error.toString());
              }

              final sessionsWithTasks = snapshot.data ?? [];

              if (sessionsWithTasks.isEmpty) {
                return _buildNoSessions();
              }

              if (kDebugMode) {
                print('Rendering ${sessionsWithTasks.length} sessions');
              }

              return _buildContent(sessionsWithTasks);
            },
          );
        },
        loading: () => _buildLoading(),
        error: (error, _) => _buildError(error.toString()),
      ),
    );
  }

  Future<List<SessionWithTask>> _getSessionsWithTasks(List<Session> sessions) async {
    // Filter sessions by date range
    final filtered = sessions.where((session) {
      if (session.endTime == null) return false;

      try {
        final sessionDate = DateTime.parse(session.endTime!);
        return !sessionDate.isBefore(_dateRange.start) &&
            !sessionDate.isAfter(_dateRange.end.add(const Duration(days: 1)));
      } catch (e) {
        if (kDebugMode) {
          print('Error parsing session date: $e');
        }
        return false;
      }
    }).toList();

    // Sort by start time (newest first)
    filtered.sort((a, b) {
      final aTime = a.endTime ?? '';
      final bTime = b.endTime ?? '';
      return bTime.compareTo(aTime); // Sort descending (newest first)
    });

    // Get tasks for each session
    final sessionsWithTasks = <SessionWithTask>[];
    final db = ref.read(databaseProvider);

    for (final session in filtered) {
      Task? task;
      if (session.taskId != null) {
        try {
          task = await db.taskDao.getTaskById(session.taskId!);
        } catch (e) {
          if (kDebugMode) {
            print('Error loading task for session ${session.id}: $e');
          }
        }
      }
      sessionsWithTasks.add(SessionWithTask(session: session, task: task));
    }

    return sessionsWithTasks;
  }

  Widget _buildContent(List<SessionWithTask> sessions) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Text(
            '${DateFormat('MMM d, yyyy').format(_dateRange.start)} - ${DateFormat('MMM d, yyyy').format(_dateRange.end)}',
            style: Theme.of(context).textTheme.titleMedium,
          ),
        ),
        Expanded(child: _groupByTask ? _buildGroupedSessions(sessions) : _buildSessionsList(sessions)),
      ],
    );
  }

  Widget _buildLoading() {
    return const Center(child: CircularProgressIndicator());
  }

  Widget _buildError(String error) {
    return Center(child: Text('Error: $error'));
  }

  Widget _buildNoSessions() {
    return const Center(child: Text('No sessions found'));
  }

  Widget _buildSessionsList(List<SessionWithTask> sessions) {
    return ListView.builder(
      itemCount: sessions.length,
      itemBuilder: (context, index) {
        final session = sessions[index];
        return _buildSessionTile(session, false);
      },
    );
  }

  Widget _buildGroupedSessions(List<SessionWithTask> sessions) {
    // Group sessions by task
    final Map<String?, List<SessionWithTask>> groupedSessions = {};
    for (var session in sessions) {
      final taskId = session.task?.id ?? AppConstants.nilUuid;
      groupedSessions.putIfAbsent(taskId, () => []).add(session);
    }

    return ListView.builder(
      itemCount: groupedSessions.length,
      itemBuilder: (context, index) {
        final taskId = groupedSessions.keys.elementAt(index);
        final taskSessions = groupedSessions[taskId]!;
        final task = taskSessions.first.task;
        final totalDuration = _calculateTotalDuration(taskSessions);
        final grouped = taskId != AppConstants.nilUuid;

        return Card(
          margin: const EdgeInsets.all(8.0),
          child: ExpansionTile(
            leading: task != null
                ? CircleAvatar(child: Text(task.title[0].toUpperCase()))
                : const Icon(Icons.help_outline),
            title: Text(task?.title ?? 'No Task', style: Theme.of(context).textTheme.titleMedium),
            subtitle: Text('${taskSessions.length} sessions • $totalDuration'),
            children: taskSessions.map((session) => _buildSessionTile(session, grouped)).toList(),
          ),
        );
      },
    );
  }

  Future<void> _deleteSession(Session session) async {
    // Store context in a local variable before any async gap
    final context = this.context;
    final messenger = ScaffoldMessenger.of(context);

    try {
      final database = ref.read(databaseProvider);
      await database.sessionDao.deleteSession(session.id);

      if (!context.mounted) return;

      messenger.showSnackBar(const SnackBar(content: Text('Session deleted')));
    } catch (e) {
      if (!context.mounted) return;

      messenger.showSnackBar(SnackBar(content: Text('Error deleting session: $e')));
    }
  }

  Widget _buildSessionTile(SessionWithTask sessionWithTask, bool grouped) {
    final session = sessionWithTask.session;
    final task = sessionWithTask.task;

    // Handle nullable startTime and endTime
    final endTime = session.endTime != null ? DateTime.tryParse(session.endTime!) : null;
    final duration = _formatDuration(session.duration);

    return Dismissible(
      key: Key('session_${session.id}'),
      direction: DismissDirection.endToStart,
      background: Container(
        color: Colors.red,
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20.0),
        child: const Icon(Icons.delete, color: Colors.white),
      ),
      confirmDismiss: (direction) async {
        // Show a confirmation dialog before deleting
        final confirmed = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Delete Session'),
            content: const Text('Are you sure you want to delete this session?'),
            actions: [
              TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('CANCEL')),
              TextButton(
                onPressed: () => Navigator.of(context).pop(true),
                style: TextButton.styleFrom(foregroundColor: Colors.red),
                child: const Text('DELETE'),
              ),
            ],
          ),
        );
        return confirmed ?? false;
      },
      onDismissed: (direction) {
        _deleteSession(session);
      },
      child: ListTile(
        leading: const Icon(Icons.timer_outlined),
        title: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(duration)]),
        subtitle: grouped ? null : Text(session.title ?? 'No Title', style: Theme.of(context).textTheme.titleSmall),
        trailing: endTime != null
            ? Text(DateFormat('MM-dd HH:mm').format(endTime), style: Theme.of(context).textTheme.bodySmall)
            : null,
      ),
    );
  }

  String _calculateTotalDuration(List<SessionWithTask> sessions) {
    final totalMinutes = sessions.fold<int>(0, (sum, session) {
      final duration = session.session.duration;
      final minutes = duration.toMinutes();
      return sum + minutes;
    });

    if (totalMinutes < 60) {
      return '$totalMinutes min';
    } else {
      final hours = totalMinutes ~/ 60;
      final minutes = totalMinutes % 60;
      return '$hours h ${minutes}m';
    }
  }

  String _formatDuration(String duration) {
    try {
      final minutes = duration.toMinutes();
      if (minutes < 60) {
        return '$minutes min';
      } else {
        final hours = minutes ~/ 60;
        final remainingMinutes = minutes % 60;
        return '$hours h ${remainingMinutes}m';
      }
    } catch (e) {
      return duration;
    }
  }
}

class SessionWithTask {
  final Session session;
  final Task? task;

  SessionWithTask({required this.session, this.task});
}

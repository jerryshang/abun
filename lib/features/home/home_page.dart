import 'package:abun/constants/app_constants.dart';
import 'package:abun/database/database.dart';
import 'package:abun/extensions/time_extensions.dart';
import 'package:abun/providers/current_date_provider.dart';
import 'package:abun/providers/database/index.dart';
import 'package:abun/routes.dart';
import 'package:abun/widgets/session_form.dart';
import 'package:abun/widgets/task_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class HomePage extends ConsumerStatefulWidget {
  const HomePage({super.key});

  @override
  ConsumerState<HomePage> createState() => _HomePageState();
}

class _HomePageState extends ConsumerState<HomePage> {
  bool _showCompleted = true;

  @override
  void initState() {
    super.initState();
  }

  bool _isExpanded = false;
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();

  void _toggleMenu() {
    setState(() {
      _isExpanded = !_isExpanded;
    });
  }

  @override
  Widget build(BuildContext context) {
    print("Hello from subtask");
    // Watch for date changes to trigger refresh
    final todayKey = ref.watch(currentDateProvider);

    final activeTasksStream = ref.watch(watchActiveTasksProvider);
    final completedTasksStream = ref.watch(completedTasksWithTodaysSessionsProvider);
    final completedIndependentSessionsStream = ref.watch(watchSessionsWithoutTasksProvider);
    // session of today, for time blocks
    final sessionsStream = ref.watch(watchSessionsByDayProvider.call(todayKey));

    return Scaffold(
      key: _scaffoldKey,
      drawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            DrawerHeader(
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primary,
              ),
              child: Text(
                'A.Bun.Dance',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  color: Theme.of(context).colorScheme.onPrimary,
                ),
              ),
            ),
            ListTile(
              leading: const Icon(Icons.history),
              title: const Text('Sessions'),
              onTap: () {
                Navigator.pop(context);
                Routes.navigateToSessions(context);
              },
            ),
            ListTile(
              leading: const Icon(Icons.repeat),
              title: const Text('Routines'),
              onTap: () {
                Navigator.pop(context);
                Routes.navigateToRoutines(context);
              },
            ),
            ListTile(
              leading: const Icon(Icons.task),
              title: const Text('Tasks'),
              onTap: () {
                Navigator.pop(context);
                Routes.navigateToTasks(context);
              },
            ),
            ListTile(
              leading: const Icon(Icons.calendar_month),
              title: const Text('Future'),
              onTap: () {
                Navigator.pop(context);
                Routes.navigateToFuture(context);
              },
            ),
          ],
        ),
      ),
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text("Today"),
        leading: IconButton(
          icon: const Icon(Icons.menu),
          onPressed: () => _scaffoldKey.currentState?.openDrawer(),
        ),
      ),
      body: Stack(
        children: [
          // Main content with tasks
          Column(
            spacing: AppConstants.defaultVerticalSpacing,
            children: [
              // Tasks section
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.only(bottom: 180),
                  // Add padding for time blocks at bottom
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Active tasks list
                      SizedBox(height: 8),
                      _buildActiveTasksList(activeTasksStream),
                      // Completed tasks section
                      _buildCompletedTasksSection(completedTasksStream, completedIndependentSessionsStream),
                    ],
                  ),
                ),
              ),
              _buildTimeBlocksSection(sessionsStream),
            ],
          ),
        ],
      ),
      floatingActionButton: Stack(
        children: [
          // Create Session Button (top-left in arc)
          AnimatedPositioned(
            bottom: _isExpanded ? 70 : 0,
            right: 0,
            duration: const Duration(milliseconds: 200),
            child: AnimatedOpacity(
              opacity: _isExpanded ? 1 : 0,
              duration: const Duration(milliseconds: 200),
              child: FloatingActionButton(
                heroTag: 'newSession',
                onPressed: () {
                  setState(() => _isExpanded = false);
                  showModalBottomSheet(
                    context: context,
                    isScrollControlled: true,
                    shape: const RoundedRectangleBorder(
                      borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
                    ),
                    builder: (context) => const SessionForm(),
                  );
                },
                child: const Icon(Icons.event_note),
              ),
            ),
          ),
          // Create Task Button (top in arc)
          // AnimatedPositioned(
          //   bottom: _isExpanded ? 140 : 0,
          //   right: 0,
          //   duration: const Duration(milliseconds: 200),
          //   child: AnimatedOpacity(
          //     opacity: _isExpanded ? 1 : 0,
          //     duration: const Duration(milliseconds: 200),
          //     child: FloatingActionButton(
          //       heroTag: 'newTask',
          //       onPressed: () {
          //         // Handle new task
          //         setState(() => _isExpanded = false);
          //       },
          //       child: const Icon(Icons.add_task),
          //     ),
          //   ),
          // ),
          // Main FAB (stays at the bottom)
          Positioned(
            bottom: 0,
            right: 0,
            child: FloatingActionButton(
              onPressed: _toggleMenu,
              child: AnimatedRotation(
                duration: const Duration(milliseconds: 300),
                turns: _isExpanded ? 0.125 : 0,
                child: const Icon(Icons.add),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActiveTasksList(AsyncValue<List<Task>> tasksAsync) {
    return tasksAsync.when(
      data: (tasks) {
        if (tasks.isEmpty) {
          return Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text('What a day without tasks?', style: Theme.of(context).textTheme.titleMedium),
          );
        }
        return ListView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          itemCount: tasks.length,
          itemBuilder: (context, index) {
            final task = tasks[index];
            return TaskCard(
              key: ValueKey(task.id),
              task: task,
              onStartPressed: task.status != 'completed'
                  ? () async {
                      final updatedTask = task.copyWith(
                        status: 'in_progress',
                        updatedAt: DateTime.now().toIso8601String(),
                      );
                      await ref.read(databaseProvider).taskDao.updateTask(updatedTask);
                    }
                  : null,
              onCompletePressed: task.status != 'completed'
                  ? () async {
                      final updatedTask = task.copyWith(
                        status: 'completed',
                        updatedAt: DateTime.now().toIso8601String(),
                      );
                      await ref.read(databaseProvider).taskDao.updateTask(updatedTask);
                    }
                  : null,
            );
          },
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, stackTrace) => Center(child: Text('Error: $error')),
    );
  }

  Widget _buildCompletedTasksSection(
    AsyncValue<List<Task>> completedTasks,
    AsyncValue<List<Session>> sessionsWithoutTasksStream,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Header with toggle button
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
          child: Row(
            children: [
              Text('Completed', style: Theme.of(context).textTheme.titleLarge),
              const Spacer(),
              IconButton(
                icon: Icon(_showCompleted ? Icons.expand_more : Icons.chevron_left),
                onPressed: () {
                  setState(() {
                    _showCompleted = !_showCompleted;
                  });
                },
              ),
            ],
          ),
        ),
        // Content (conditionally shown)
        if (_showCompleted)
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Completed/Eliminated tasks with today's sessions
              Consumer(
                builder: (context, ref, _) {
                  return completedTasks.when(
                    data: (completedTasks) {
                      if (completedTasks.isEmpty) {
                        return const SizedBox.shrink();
                      }
                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: completedTasks.map(_buildCompletedTaskItem).toList(),
                      );
                    },
                    loading: () => const Center(child: CircularProgressIndicator()),
                    error: (error, stackTrace) => Center(child: Text('Error: $error')),
                  );
                },
              ),

              // Today's sessions without tasks
              Consumer(
                builder: (context, ref, _) => _buildSessionsWithoutTasksList(sessionsWithoutTasksStream),
              ),

              // Show message if no content
              Builder(
                builder: (context) {
                  if (completedTasks.isLoading || sessionsWithoutTasksStream.isLoading) {
                    return const SizedBox.shrink();
                  }
                  if (completedTasks.hasValue &&
                      completedTasks.value!.isEmpty &&
                      sessionsWithoutTasksStream.hasValue &&
                      sessionsWithoutTasksStream.value!.isEmpty) {
                    return const Padding(padding: EdgeInsets.all(16.0), child: Text('No activity yet for today.'));
                  }
                  return const SizedBox.shrink();
                },
              ),
            ],
          ),
      ],
    );
  }

  Widget _buildCompletedTaskItem(Task task) {
    return Consumer(
      builder: (context, ref, _) {
        final sessionsAsync = ref.watch(sessionsByTaskProvider(task.id));

        return sessionsAsync.when(
          data: (sessions) {
            final totalMinutes = sessions.fold<int>(0, (sum, session) => sum + session.duration.toMinutes());

            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.start,
                children: [
                  Icon(Icons.done_all, color: Colors.green, size: 20),
                  SizedBox(width: 8),
                  Text(task.title, style: const TextStyle(fontSize: 20)),
                  Expanded(child: Container()),
                  Text('$totalMinutes min', style: TextStyle(color: Colors.grey[600], fontSize: 16)),
                ],
              ),
            );
          },
          loading: () => const SizedBox(
            height: 24,
            child: Center(child: SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2))),
          ),
          error: (_, __) => const SizedBox.shrink(),
        );
      },
    );
  }

  Widget _buildSessionsWithoutTasksList(AsyncValue<List<Session>> sessionsStream) {
    return sessionsStream.when(
      data: (sessions) {
        if (sessions.isEmpty) {
          return const SizedBox.shrink();
        }
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: sessions.map((session) {
            final duration = session.duration.toString().toMinutes();
            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.start,
                children: [
                  const Icon(Icons.check, color: Colors.green, size: 20),
                  const SizedBox(width: 8),
                  Text(session.title ?? 'Untitled Session', style: const TextStyle(fontSize: 20)),
                  const Expanded(child: SizedBox()),
                  Text('$duration min', style: TextStyle(color: Colors.grey[600], fontSize: 16)),
                ],
              ),
            );
          }).toList(),
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, stackTrace) => Center(child: Text('Error: $error')),
    );
  }

  Widget _buildTimeBlocksSection(AsyncValue<List<Session>> sessions) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);

    // Initialize a map to store minutes per hour (6-23)
    final minutesPerHour = <int, int>{for (var i = 6; i <= 23; i++) i: 0};

    for (final session in sessions.value ?? []) {
      final end = DateTime.parse(session.endTime);
      final start = end.subtract(session.duration.toString().toDuration());

      // Only process sessions that fall within our time range
      if (end.isBefore(DateTime(today.year, today.month, today.day, 6)) ||
          start.isAfter(DateTime(today.year, today.month, today.day, 23, 59))) {
        continue;
      }

      // For each hour from 8 to 23
      for (var hour = 6; hour <= 23; hour++) {
        final hourStart = DateTime(today.year, today.month, today.day, hour);
        final hourEnd = hour == 23
            ? DateTime(today.year, today.month, today.day + 1) // End of day
            : DateTime(today.year, today.month, today.day, hour + 1);

        final overlapMinutes = DateTimeUtils.overlapInMinutes(start, end, hourStart, hourEnd);
        minutesPerHour[hour] = (minutesPerHour[hour] ?? 0) + overlapMinutes;
      }
    }

    return Container(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Time Blocks', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          // First row of time blocks
          Row(
            mainAxisAlignment: MainAxisAlignment.start,
            spacing: 4,
            children: List.generate(6, (index) => _buildTimeBlock(index + 6, minutesPerHour[index + 6] ?? 0)),
          ),
          const SizedBox(height: 4),
          // Second row of time blocks
          Row(
            mainAxisAlignment: MainAxisAlignment.start,
            spacing: 4,
            children: List.generate(6, (index) => _buildTimeBlock(index + 12, minutesPerHour[index + 12] ?? 0)),
          ),
          const SizedBox(height: 4),
          // Second row of time blocks
          Row(
            mainAxisAlignment: MainAxisAlignment.start,
            spacing: 4,
            children: List.generate(6, (index) => _buildTimeBlock(index + 18, minutesPerHour[index + 18] ?? 0)),
          ),
        ],
      ),
    );
  }

  Widget _buildTimeBlock(hour, minutes) {
    final fraction = minutes / 60;
    final color = Color.lerp(
      Theme.of(context).colorScheme.primary.withOpacity(0.2),
      Theme.of(context).colorScheme.primary,
      fraction,
    );

    return Container(
      width: 32,
      height: 24,
      margin: const EdgeInsets.all(0),
      decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(4)),
      child: Center(
        child: Text(
          hour.toString(),
          style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.normal),
        ),
      ),
    );
  }
}

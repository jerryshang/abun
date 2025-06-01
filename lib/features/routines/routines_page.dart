import 'package:abun/constants/app_constants.dart';
import 'package:abun/database/database.dart';
import 'package:abun/features/routines/tasks_by_routine_page.dart';
import 'package:abun/providers/database/index.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';

class RoutinesPage extends ConsumerStatefulWidget {
  const RoutinesPage({super.key});

  @override
  ConsumerState<RoutinesPage> createState() => _RoutinesPageState();
}

class _RoutinesPageState extends ConsumerState<RoutinesPage> {
  bool _showArchived = false;
  bool _recentFirst = true;

  // Controllers for the form fields
  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _estimatedDurationController = TextEditingController();
  final TextEditingController _noteController = TextEditingController();
  final TextEditingController _recurrenceRuleController = TextEditingController();
  DateTime? _startTime;
  DateTime? _dueTime;

  // Selected routine for editing
  Routine? _selectedRoutine;

  @override
  void dispose() {
    _titleController.dispose();
    _estimatedDurationController.dispose();
    _noteController.dispose();
    _recurrenceRuleController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final routinesAsync = ref.watch(watchAllRoutinesProvider);

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text("Routines"),
        actions: [
          SizedBox(
            width: AppConstants.defaultIconButtonSize,
            child: IconButton(
              icon: const Icon(Icons.archive),
              tooltip: 'Show Archived',
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(),
              onPressed: () {
                setState(() {
                  _showArchived = !_showArchived;
                });
              },
            ),
          ),
          SizedBox(
            width: AppConstants.defaultIconButtonSize,
            child: IconButton(
              icon: const Icon(Icons.sort_by_alpha),
              tooltip: 'Sort by Date',
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(),
              onPressed: () {
                setState(() {
                  _recentFirst = !_recentFirst;
                });
              },
            ),
          ),
        ],
      ),
      body: Stack(
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Routine list
                Expanded(
                  child: routinesAsync.when(
                    data: (routines) {
                      if (routines.isEmpty) {
                        return const Center(child: Text('No routines available. Create your first routine!'));
                      }

                      // Filter and sort routines
                      var filteredRoutines = routines.where((r) => _showArchived || true).toList();
                      filteredRoutines.sort((a, b) {
                        final aTime = DateTime.tryParse(a.updatedAt) ?? DateTime(0);
                        final bTime = DateTime.tryParse(b.updatedAt) ?? DateTime(0);
                        return _recentFirst ? bTime.compareTo(aTime) : aTime.compareTo(bTime);
                      });

                      return ListView.builder(
                        itemCount: filteredRoutines.length,
                        itemBuilder: (context, index) {
                          final routine = filteredRoutines[index];
                          return _buildRoutineCard(context, routine);
                        },
                      );
                    },
                    loading: () => const Center(child: CircularProgressIndicator()),
                    error: (error, stack) => Center(child: Text('Error: $error')),
                  ),
                ),
              ],
            ),
          ),
          // Add routine button at the bottom right
          Positioned(
            right: 16,
            bottom: 16,
            child: FloatingActionButton(
              onPressed: () {
                _resetForm();
                _showRoutineBottomSheet(context);
              },
              tooltip: 'New Routine',
              backgroundColor: Theme.of(context).colorScheme.primary,
              foregroundColor: Colors.white,
              heroTag: 'addRoutine',
              child: const Icon(Icons.add),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRoutineCard(BuildContext context, Routine routine) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8.0),
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => TasksByRoutinePage(routineId: routine.id, routineTitle: routine.title),
            ),
          );
        },
        child: ListTile(
          title: Text(routine.title),
          subtitle: Text(
            routine.recurrenceRule.isNotEmpty ? 'Recurrence: ${routine.recurrenceRule}' : 'No recurrence set',
          ),
          trailing: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              IconButton(
                icon: const Icon(Icons.edit, size: 20),
                onPressed: () {
                  _selectRoutine(routine);
                  _showRoutineBottomSheet(context);
                },
              ),
              IconButton(
                icon: const Icon(Icons.delete, size: 20, color: Colors.red),
                onPressed: () => _showDeleteConfirmation(context, routine.id),
              ),
            ],
          ),
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => TasksByRoutinePage(
                  routineId: routine.id,
                  routineTitle: routine.title,
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  void _selectRoutine(Routine routine) {
    setState(() {
      _selectedRoutine = routine;
      _titleController.text = routine.title;
      _recurrenceRuleController.text = routine.recurrenceRule;
      _estimatedDurationController.text = routine.estimatedDuration ?? '';
      _startTime = routine.startTime != null ? DateTime.parse(routine.startTime!) : null;
      _dueTime = routine.dueTime != null ? DateTime.parse(routine.dueTime!) : null;
      _noteController.text = routine.note ?? '';
    });
  }

  Future<void> _showRoutineBottomSheet(BuildContext context) async {
    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (BuildContext context) {
        return SingleChildScrollView(
          padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom, left: 16, right: 16, top: 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                _selectedRoutine == null ? 'New Routine' : 'Edit Routine',
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _titleController,
                decoration: const InputDecoration(labelText: 'Title', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _recurrenceRuleController,
                decoration: const InputDecoration(
                  labelText: 'Recurrence Rule (e.g., FREQ=DAILY;INTERVAL=1)',
                  border: OutlineInputBorder(),
                  helperText: 'Use iCalendar RRULE format',
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _estimatedDurationController,
                decoration: const InputDecoration(
                  labelText: 'Estimated Duration (e.g., PT25M for 25 minutes)',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.text,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _noteController,
                decoration: const InputDecoration(labelText: 'Notes', border: OutlineInputBorder()),
                maxLines: 3,
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: TextButton(
                      onPressed: () async {
                        final date = await showDatePicker(
                          context: context,
                          initialDate: _startTime ?? DateTime.now(),
                          firstDate: DateTime(2000),
                          lastDate: DateTime(2100),
                        );
                        if (date != null) {
                          setState(() {
                            _startTime = date;
                          });
                        }
                      },
                      child: Text(_startTime == null ? 'Set Start Date' : 'Start: ${_formatDate(_startTime!)}'),
                    ),
                  ),
                  Expanded(
                    child: TextButton(
                      onPressed: () async {
                        final date = await showDatePicker(
                          context: context,
                          initialDate: _dueTime ?? DateTime.now(),
                          firstDate: DateTime(2000),
                          lastDate: DateTime(2100),
                        );
                        if (date != null) {
                          setState(() {
                            _dueTime = date;
                          });
                        }
                      },
                      child: Text(_dueTime == null ? 'Set Due Date' : 'Due: ${_formatDate(_dueTime!)}'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: _saveRoutine,
                child: Text(_selectedRoutine == null ? 'Create Routine' : 'Update Routine'),
              ),
              const SizedBox(height: 16),
            ],
          ),
        );
      },
    );
  }

  Future<void> _saveRoutine() async {
    if (_titleController.text.isEmpty) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Title is required')));
      }
      return;
    }

    final routine = Routine(
      id: _selectedRoutine?.id ?? const Uuid().v4(),
      title: _titleController.text,
      recurrenceRule: _recurrenceRuleController.text,
      estimatedDuration: _estimatedDurationController.text.isEmpty ? null : _estimatedDurationController.text,
      startTime: _startTime?.toIso8601String(),
      dueTime: _dueTime?.toIso8601String(),
      note: _noteController.text.isEmpty ? null : _noteController.text,
      createdAt: _selectedRoutine?.createdAt ?? DateTime.now().toIso8601String(),
      updatedAt: DateTime.now().toIso8601String(),
    );

    try {
      final db = ref.read(databaseProvider);
      final isNew = _selectedRoutine == null;

      if (isNew) {
        await db.routineDao.createRoutine(routine.toCompanion(true));
      } else {
        await db.routineDao.updateRoutine(routine);
      }

      // Generate tasks for the routine
      try {
        final count = await db.routineDao.generateTasksFromRoutine(routine.id);

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                isNew ? 'Routine created! Generated $count new tasks.' : 'Routine updated! Generated $count new tasks.',
              ),
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(const SnackBar(content: Text('Routine saved, but there was an error generating tasks.')));
        }
      }

      if (mounted) {
        Navigator.pop(context);
        _resetForm();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error saving routine: $e')));
      }
    }
  }

  void _resetForm() {
    setState(() {
      _selectedRoutine = null;
      _titleController.clear();
      _recurrenceRuleController.clear();
      _estimatedDurationController.clear();
      _noteController.clear();
      _startTime = null;
      _dueTime = null;
    });
  }

  Future<void> _showDeleteConfirmation(BuildContext context, String id) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Routine'),
        content: const Text('Are you sure you want to delete this routine?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Delete', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        final db = ref.read(databaseProvider);
        await db.routineDao.deleteRoutine(id);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Routine deleted')));
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error deleting routine: $e')));
        }
      }
    }
  }

  String _formatDate(DateTime date) {
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }
}

import 'package:drift/drift.dart' hide Column;
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';

import '../constants/app_constants.dart';
import '../database/database.dart';
import '../models/session_mood.dart';
import '../models/session_type.dart';
import '../models/task_status.dart';
import '../providers/database/index.dart';
import 'datetime_helper.dart';

class SessionForm extends ConsumerStatefulWidget {
  final Task? task;

  const SessionForm({super.key, this.task});

  @override
  ConsumerState<SessionForm> createState() => _SessionFormState();
}

class _SessionFormState extends ConsumerState<SessionForm> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _noteController = TextEditingController();
  SessionType _type = SessionType.focus;
  SessionMood _selectedMood = SessionMood.okay;
  bool _completeTask = false;
  final _durationController = TextEditingController(text: '${AppConstants.defaultTimeBlockMinutes}');
  DateTime? _startTime;
  DateTime? _endTime;
  TimeOfDay _selectedTime = TimeOfDay.now();

  @override
  void initState() {
    super.initState();
    final now = DateTime.now();
    _selectedTime = TimeOfDay(hour: now.hour, minute: (now.minute / 5).round() * 5);
    _endTime = DateTime.now();

    // Only set title to empty if taskId is provided
    if (widget.task != null) {
      _titleController.text = '';
      // Use Future.microtask to schedule the async operation after the widget is built
      Future.microtask(() => _loadRoutineData());
    } else {
      // For sessions without a task, set a default type
      _type = SessionType.free;
    }
  }

  Future<void> _loadRoutineData() async {
    if (widget.task == null || widget.task!.routineId == null || !mounted) return;

    try {
      final routine = await ref.read(databaseProvider).routineDao.getRoutineById(widget.task!.routineId!);
      if (!mounted) return;

      print('Routine data loaded: $routine');

      if (routine != null) {
        setState(() {
          _completeTask = true;
          _durationController.text = '${routine.estimatedDuration}';
        });
      }
    } catch (e) {
      debugPrint('Error loading routine data: $e');
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _noteController.dispose();
    _durationController.dispose();
    super.dispose();
  }

  Future<void> _selectDateTime(BuildContext context) async {
    if (!context.mounted) return;

    final time = await DateTimeHelper.showCustomTimePicker(context, initialTime: _selectedTime, title: 'End Time');

    if (time == null) return;

    final now = DateTime.now();
    final dateTime = DateTime(now.year, now.month, now.day, time.hour, time.minute);

    setState(() {
      _selectedTime = time;
      _endTime = dateTime;
    });
  }

  // Time picker functionality has been moved to DateTimeHelper

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom, left: 16, right: 16, top: 16),
      child: Form(
        key: _formKey,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            spacing: 16,
            children: [
              // form title
              Text(
                widget.task != null ? 'New Task Session' : 'New Free Session',
                style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
              // session content. Only show when free session
              if (widget.task == null)
                TextFormField(
                  controller: _titleController,
                  decoration: const InputDecoration(
                    labelText: 'What do you finished?',
                    border: OutlineInputBorder(),
                    hintText: 'Your awesome action...',
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please fill what you did';
                    }
                    return null;
                  },
                ),
              // complete task? Only show when task session
              if (widget.task != null)
                CheckboxListTile(
                  title: const Text('Mark task as complete'),
                  value: _completeTask,
                  onChanged: (value) {
                    setState(() {
                      _completeTask = value ?? false;
                    });
                  },
                  controlAffinity: ListTileControlAffinity.leading,
                  contentPadding: EdgeInsets.zero,
                ),
              // mood
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: SessionMood.values.map((mood) {
                  return GestureDetector(
                    onTap: () {
                      setState(() {
                        _selectedMood = mood;
                      });
                    },
                    child: Container(
                      // padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: _selectedMood == mood
                            ? Theme.of(context).colorScheme.primary.withOpacity(0.1)
                            : Colors.transparent,
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(
                          color: _selectedMood == mood ? Theme.of(context).colorScheme.primary : Colors.grey[300]!,
                          width: _selectedMood == mood ? 1.5 : 1,
                        ),
                      ),
                      child: Text(mood.displayName, style: const TextStyle(fontSize: 24)),
                    ),
                  );
                }).toList(),
              ),
              // time cost
              Row(
                children: [
                  Expanded(
                    child: TextFormField(
                      controller: _durationController,
                      decoration: const InputDecoration(labelText: 'Time cost', border: OutlineInputBorder()),
                      keyboardType: TextInputType.number,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Required';
                        }
                        if (int.tryParse(value) == null) {
                          return 'Enter a valid number';
                        }
                        return null;
                      },
                    ),
                  ),
                ],
              ),

              Row(
                children: [
                  Expanded(
                    child: ListTile(
                      title: Text(
                        '${_selectedTime.hour.toString().padLeft(2, '0')}:${_selectedTime.minute.toString().padLeft(2, '0')}',
                      ),
                      trailing: const Icon(Icons.access_time),
                      onTap: () => _selectDateTime(context),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(4),
                        side: BorderSide(color: Colors.grey[300]!),
                      ),
                    ),
                  ),
                ],
              ),

              // form actions
              Consumer(
                builder: (context, ref, _) {
                  return ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Theme.of(context).primaryColor,
                      foregroundColor: Colors.white,
                    ),
                    onPressed: () async {
                      if (_formKey.currentState!.validate()) {
                        final database = ref.read(databaseProvider);
                        final now = DateTime.now().toIso8601String();

                        // Update task status if needed
                        if (widget.task != null && _completeTask) {
                          try {
                            final task = await database.taskDao.getTaskById(widget.task!.id);
                            if (task != null) {
                              final updatedTask = task.copyWith(status: TaskStatus.completed.value, updatedAt: now);
                              await database.taskDao.updateTask(updatedTask);
                            }
                          } catch (e) {
                            if (context.mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(content: Text('Error updating task status: $e'), backgroundColor: Colors.red),
                              );
                            }
                            return; // Don't proceed with session creation if task update fails
                          }
                        }

                        try {
                          final duration = "PT${_durationController.text}M";

                          final session = SessionsCompanion.insert(
                            id: Value(const Uuid().v4()),
                            taskId: widget.task != null ? Value(widget.task!.id) : const Value.absent(),
                            title: widget.task != null && _titleController.text.isEmpty
                                ? const Value.absent()
                                : Value(_titleController.text),
                            note: _noteController.text.isNotEmpty ? Value(_noteController.text) : const Value.absent(),
                            startTime: _startTime != null ? Value(_startTime!.toIso8601String()) : Value.absent(),
                            endTime: _endTime != null ? Value(_endTime!.toIso8601String()) : Value.absent(),
                            duration: duration,
                            type: Value(_type.value),
                            mood: Value(_selectedMood.value),
                          );

                          if (kDebugMode) {
                            print('Session object created: $session');
                          }

                          final sessionId = await database.sessionDao.createSession(session);
                          if (kDebugMode) {
                            print('Session saved successfully with ID: $sessionId');
                          }
                          if (context.mounted) {
                            ScaffoldMessenger.of(
                              context,
                            ).showSnackBar(const SnackBar(content: Text('Session saved successfully')));
                            Navigator.pop(context, true);
                          }
                        } catch (e) {
                          if (context.mounted) {
                            ScaffoldMessenger.of(
                              context,
                            ).showSnackBar(SnackBar(content: Text('Error saving session: $e')));
                          }
                        }
                      }
                    },
                    child: const Text('Save Session'),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

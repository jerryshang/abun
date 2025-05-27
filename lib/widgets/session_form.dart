import 'package:drift/drift.dart' hide Column;
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:uuid/uuid.dart';

import '../constants/app_constants.dart';
import '../database/database.dart';
import '../models/session_mood.dart';
import '../models/session_type.dart';
import '../models/task_status.dart';
import '../providers/database/index.dart';

class SessionForm extends ConsumerStatefulWidget {
  final String? taskId;

  const SessionForm({super.key, this.taskId});

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

  @override
  void initState() {
    super.initState();
    _endTime = DateTime.now();
    _startTime = _startTime?.subtract(const Duration(minutes: AppConstants.defaultTimeBlockMinutes));

    // Only set title to empty if taskId is provided
    if (widget.taskId != null) {
      _titleController.text = '';
    } else {
      // For sessions without a task, set a default type
      _type = SessionType.free;
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _noteController.dispose();
    _durationController.dispose();
    super.dispose();
  }

  Future<void> _selectDateTime(BuildContext context, bool isStart) async {
    final now = DateTime.now();
    // final date = await showDatePicker(
    //   context: context,
    //   initialDate: isStart ? now : _startTime ?? now,
    //   firstDate: DateTime(now.year - 1),
    //   lastDate: DateTime(now.year + 1),
    // );

    // if (date == null) return;
    if (!context.mounted) return;

    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(isStart ? now : _startTime ?? now),
    );

    if (time == null) return;

    final dateTime = DateTime(now.year, now.month, now.day, time.hour, time.minute);

    setState(() {
      if (isStart) {
        _startTime = dateTime;
      } else {
        _endTime = dateTime;
      }
    });
  }

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
              Text(
                widget.taskId != null ? 'New Task Session' : 'New Free Session',
                style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
              // Only show title field if no task ID is provided
              if (widget.taskId == null)
                TextFormField(
                  controller: _titleController,
                  decoration: const InputDecoration(
                    labelText: 'What do you finished?',
                    border: OutlineInputBorder(),
                    hintText: 'Your awesome action...',
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter a title';
                    }
                    return null;
                  },
                ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
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
                      title: Text(_startTime == null ? 'Start Time' : DateFormat('HH:mm').format(_startTime!)),
                      trailing: const Icon(Icons.access_time),
                      onTap: () => _selectDateTime(context, true),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(4),
                        side: BorderSide(color: Colors.grey[300]!),
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: ListTile(
                      title: Text(_endTime == null ? 'End Time' : DateFormat('HH:mm').format(_endTime!)),
                      trailing: const Icon(Icons.access_time),
                      onTap: () => _selectDateTime(context, false),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(4),
                        side: BorderSide(color: Colors.grey[300]!),
                      ),
                    ),
                  ),
                ],
              ),
              // Show complete task checkbox only when there's a taskId
              if (widget.taskId != null) ...[
                const SizedBox(height: 16),
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
              ],

              TextFormField(
                controller: _noteController,
                decoration: const InputDecoration(
                  labelText: 'Notes (optional)',
                  border: OutlineInputBorder(),
                  hintText: 'Any notes about this session...',
                ),
                maxLines: 2,
              ),
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
                        if (widget.taskId != null && _completeTask) {
                          try {
                            final task = await database.taskDao.getTaskById(widget.taskId!);
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

                        // Validate title for sessions without a task
                        if (widget.taskId == null && _titleController.text.isEmpty) {
                          if (context.mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Please enter a title for the session'),
                                backgroundColor: Colors.orange,
                              ),
                            );
                          }
                          return;
                        }

                        try {
                          final duration = "PT${_durationController.text}M";

                          final session = SessionsCompanion.insert(
                            id: Value(const Uuid().v4()),
                            taskId: widget.taskId != null ? Value(widget.taskId!) : const Value.absent(),
                            title: widget.taskId != null && _titleController.text.isEmpty
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

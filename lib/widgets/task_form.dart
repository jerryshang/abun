import 'package:abun/extensions/time_extensions.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../database/database.dart';
import '../models/task_status.dart';
import '../providers/task_notifier.dart';
import 'datetime_helper.dart';

class TaskFormData {
  final String title;
  final String? estimatedDuration;
  final String? note;
  final DateTime? startTime;
  final DateTime? dueTime;
  final String status;

  const TaskFormData({
    required this.title,
    this.estimatedDuration,
    this.note,
    this.startTime,
    this.dueTime,
    required this.status,
  });
}

class TaskForm extends ConsumerStatefulWidget {
  final Task? task;
  final Function(TaskFormData) onSaved;
  final String? initialTitle;
  final String? initialEstimatedDuration;
  final String? initialNote;
  final DateTime? initialStartTime;
  final DateTime? initialDueTime;
  final TaskStatus initialStatus;

  const TaskForm({
    super.key,
    this.task,
    required this.onSaved,
    this.initialTitle = '',
    this.initialEstimatedDuration,
    this.initialNote,
    this.initialStartTime,
    this.initialDueTime,
    this.initialStatus = TaskStatus.inbox,
  });

  @override
  ConsumerState<TaskForm> createState() => _TaskFormState();
}

class _TaskFormState extends ConsumerState<TaskForm> {
  late final TextEditingController _titleController;
  late final TextEditingController _estimatedDurationController;
  late final TextEditingController _noteController;
  late DateTime? _startTime;
  late DateTime? _dueTime;
  late TaskStatus _status;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.initialTitle);
    _estimatedDurationController = TextEditingController(text: widget.initialEstimatedDuration);
    _noteController = TextEditingController(text: widget.initialNote);
    _startTime = widget.initialStartTime;
    _dueTime = widget.initialDueTime;
    _status = widget.initialStatus;

    // If we have a task, use its values
    if (widget.task != null) {
      _titleController.text = widget.task!.title;
      _status = TaskStatus.fromString(widget.task!.status);
      // Parse estimated duration from ISO8601 format if needed
      if (widget.task!.estimatedDuration != null &&
          widget.task!.estimatedDuration!.startsWith('PT') &&
          widget.task!.estimatedDuration!.endsWith('M')) {
        try {
          _estimatedDurationController.text = widget.task!.estimatedDuration!.substring(
            2,
            widget.task!.estimatedDuration!.length - 1,
          );
        } catch (e) {
          // Ignore parsing errors
        }
      }
      _noteController.text = widget.task!.note ?? '';
      _startTime = widget.task!.startTime != null ? DateTime.parse(widget.task!.startTime!) : null;
      _dueTime = widget.task!.dueTime != null ? DateTime.parse(widget.task!.dueTime!) : null;
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _estimatedDurationController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  void _handleSave() {
    if (_titleController.text.trim().isEmpty) {
      // Show error if title is empty
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Please enter a title')));
      return;
    }

    widget.onSaved(
      TaskFormData(
        title: _titleController.text.trim(),
        estimatedDuration: _estimatedDurationController.text.trim().isNotEmpty
            ? _estimatedDurationController.text.trim()
            : null,
        note: _noteController.text.trim().isNotEmpty ? _noteController.text.trim() : null,
        startTime: _startTime,
        dueTime: _dueTime,
        status: _startTime == null && _dueTime == null ? 'inbox' : _status.value,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final taskOperations = ref.watch(taskNotifierProvider);

    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      spacing: 16,
      children: [
        Text(
          widget.task == null ? 'Create New Task' : 'Edit Task',
          style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          textAlign: TextAlign.center,
        ),
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          spacing: 16,
          children: [
            Expanded(
              flex: 2,
              child: TextField(
                controller: _titleController,
                decoration: const InputDecoration(
                  labelText: 'What to do?',
                  border: OutlineInputBorder(),
                  hintText: 'Your awesome task...',
                ),
                maxLines: 1,
                autofocus: true,
              ),
            ),
          ],
        ),
        Row(
          children: [
            Expanded(
              child: TextFormField(
                controller: _estimatedDurationController,
                decoration: const InputDecoration(
                  labelText: 'Est. Time (min)',
                  border: OutlineInputBorder(),
                  hintText: 'Minutes',
                ),
                keyboardType: TextInputType.number,
              ),
            ),
          ],
        ),
        // due time
        Row(
          children: [
            Expanded(
              child: InkWell(
                onTap: () async {
                  final currentContext = context;
                  if (!currentContext.mounted) return;

                  final pickedDate = await showDatePicker(
                    context: currentContext,
                    initialDate: _dueTime ?? DateTime.now().tomorrow(),
                    firstDate: DateTime.now().today(),
                    lastDate: DateTime(2100),
                  );
                  if (!currentContext.mounted) return;

                  if (pickedDate != null) {
                    if (!currentContext.mounted) return;

                    final newDueTime = DateTime(pickedDate.year, pickedDate.month, pickedDate.day);
                    setState(() {
                      _dueTime = newDueTime;
                    });
                  }
                },
                child: InputDecorator(
                  decoration: InputDecoration(
                    labelText: 'Due date',
                    border: const OutlineInputBorder(),
                    suffixIcon: _dueTime != null
                        ? IconButton(
                            icon: const Icon(Icons.clear, size: 20),
                            onPressed: () {
                              setState(() {
                                _dueTime = null;
                              });
                            },
                            padding: EdgeInsets.zero,
                            constraints: const BoxConstraints(),
                            tooltip: 'Clear due date',
                          )
                        : null,
                  ),
                  child: Text(
                    _dueTime != null
                        ? '${_dueTime!.year}-${_dueTime!.month.toString().padLeft(2, '0')}-${_dueTime!.day.toString().padLeft(2, '0')}'
                        : 'Select due date',
                  ),
                ),
              ),
            ),
          ],
        ),
        Row(
          children: [
            Expanded(
              child: InkWell(
                onTap: () async {
                  final currentContext = context;
                  if (!currentContext.mounted) return;

                  final pickedDate = await showDatePicker(
                    context: currentContext,
                    initialDate: _startTime ?? DateTime.now().today(),
                    firstDate: DateTime.now().today(),
                    lastDate: DateTime(2100),
                  );
                  if (!currentContext.mounted) return;

                  if (pickedDate != null) {
                    final pickedTime = await DateTimeHelper.showCustomTimePicker(
                      currentContext,
                      initialTime: TimeOfDay.fromDateTime(_startTime ?? DateTime.now()),
                      title: 'Select Start Time',
                    );
                    if (!currentContext.mounted) return;

                    if (pickedTime != null) {
                      if (!currentContext.mounted) return;
                      final newStartTime = DateTime(
                        pickedDate.year,
                        pickedDate.month,
                        pickedDate.day,
                        pickedTime.hour,
                        pickedTime.minute,
                      );
                      setState(() {
                        _startTime = newStartTime;
                      });
                    }
                  }
                },
                child: InputDecorator(
                  decoration: InputDecoration(
                    labelText: 'Start Time',
                    border: const OutlineInputBorder(),
                    suffixIcon: _startTime != null
                        ? IconButton(
                            icon: const Icon(Icons.clear, size: 20),
                            onPressed: () {
                              setState(() {
                                _startTime = null;
                              });
                            },
                            padding: EdgeInsets.zero,
                            constraints: const BoxConstraints(),
                            tooltip: 'Clear start time',
                          )
                        : null,
                  ),
                  child: Text(
                    _startTime != null
                        ? '${_startTime!.year}-${_startTime!.month.toString().padLeft(2, '0')}-${_startTime!.day.toString().padLeft(2, '0')} ${_startTime!.hour.toString().padLeft(2, '0')}:${_startTime!.minute.toString().padLeft(2, '0')}'
                        : 'Select start time',
                  ),
                ),
              ),
            ),
          ],
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () {
                  Navigator.pop(context);
                },
                icon: const Icon(Icons.close),
                label: const Text('Cancel'),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: ElevatedButton.icon(
                onPressed: taskOperations.state == TaskOperationState.loading ? null : _handleSave,
                icon: Icon(widget.task == null ? Icons.add : Icons.save),
                label: Text(widget.task == null ? 'Create' : 'Update'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: widget.task == null ? Theme.of(context).colorScheme.primary : Colors.orange,
                  foregroundColor: Colors.white,
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

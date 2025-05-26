import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/task_status.dart';
import '../providers/task_notifier.dart';
import '../database/database.dart';

class TaskForm extends ConsumerStatefulWidget {
  final Task? task;
  final Function() onTaskSaved;
  final TextEditingController titleController;
  final TextEditingController estimatedDurationController;
  final TextEditingController noteController;
  final DateTime? initialStartTime;
  final DateTime? initialDueTime;
  final TaskStatus initialStatus;

  const TaskForm({
    super.key,
    this.task,
    required this.onTaskSaved,
    required this.titleController,
    required this.estimatedDurationController,
    required this.noteController,
    this.initialStartTime,
    this.initialDueTime,
    this.initialStatus = TaskStatus.inbox,
  });

  @override
  ConsumerState<TaskForm> createState() => _TaskFormState();
}

class _TaskFormState extends ConsumerState<TaskForm> {
  late DateTime? _startTime;
  late DateTime? _dueTime;

  @override
  void initState() {
    super.initState();
    _startTime = widget.initialStartTime;
    _dueTime = widget.initialDueTime;
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
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
          textAlign: TextAlign.center,
        ),
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          spacing: 16,
          children: [
            Expanded(
              flex: 2,
              child: TextField(
                controller: widget.titleController,
                decoration: const InputDecoration(
                  labelText: 'What to do?',
                  border: OutlineInputBorder(),
                  hintText: 'Your awesome task...',
                ),
                maxLines: 1,
                autofocus: true,
              ),
            ),
            Expanded(
              child: TextField(
                controller: widget.estimatedDurationController,
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

        Row(
          spacing: 16,
          children: [
            Expanded(
              child: InkWell(
                onTap: () async {
                  final currentContext = context;
                  if (!currentContext.mounted) return;

                  final pickedDate = await showDatePicker(
                    context: currentContext,
                    initialDate: _startTime ?? DateTime.now(),
                    firstDate: DateTime(2000),
                    lastDate: DateTime(2100),
                  );
                  if (!currentContext.mounted) return;

                  if (pickedDate != null) {
                    final pickedTime = await showTimePicker(
                      context: currentContext,
                      initialTime: TimeOfDay.fromDateTime(_startTime ?? DateTime.now()),
                    );
                    if (!currentContext.mounted) return;

                    if (pickedTime != null) {
                      if (!currentContext.mounted) return;
                      setState(() {
                        _startTime = DateTime(
                          pickedDate.year,
                          pickedDate.month,
                          pickedDate.day,
                          pickedTime.hour,
                          pickedTime.minute,
                        );
                      });
                    }
                  }
                },
                child: InputDecorator(
                  decoration: const InputDecoration(
                    labelText: 'Start Time',
                    border: OutlineInputBorder(),
                  ),
                  child: Text(
                    _startTime != null
                        ? '${_startTime!.year}-${_startTime!.month.toString().padLeft(2, '0')}-${_startTime!.day.toString().padLeft(2, '0')} ${_startTime!.hour.toString().padLeft(2, '0')}:${_startTime!.minute.toString().padLeft(2, '0')}'
                        : 'Select start time',
                  ),
                ),
              ),
            ),
            Expanded(
              child: InkWell(
                onTap: () async {
                  final currentContext = context;
                  if (!currentContext.mounted) return;

                  final pickedDate = await showDatePicker(
                    context: currentContext,
                    initialDate: _dueTime ?? DateTime.now(),
                    firstDate: DateTime(2000),
                    lastDate: DateTime(2100),
                  );
                  if (!currentContext.mounted) return;

                  if (pickedDate != null) {
                    final pickedTime = await showTimePicker(
                      context: currentContext,
                      initialTime: TimeOfDay.fromDateTime(_dueTime ?? DateTime.now()),
                    );
                    if (!currentContext.mounted) return;

                    if (pickedTime != null) {
                      if (!currentContext.mounted) return;
                      setState(() {
                        _dueTime = DateTime(
                          pickedDate.year,
                          pickedDate.month,
                          pickedDate.day,
                          pickedTime.hour,
                          pickedTime.minute,
                        );
                      });
                    }
                  }
                },
                child: InputDecorator(
                  decoration: const InputDecoration(
                    labelText: 'Due Time',
                    border: OutlineInputBorder(),
                  ),
                  child: Text(
                    _dueTime != null
                        ? '${_dueTime!.year}-${_dueTime!.month.toString().padLeft(2, '0')}-${_dueTime!.day.toString().padLeft(2, '0')} ${_dueTime!.hour.toString().padLeft(2, '0')}:${_dueTime!.minute.toString().padLeft(2, '0')}'
                        : 'Select due time',
                  ),
                ),
              ),
            ),
          ],
        ),
        TextField(
          controller: widget.noteController,
          decoration: const InputDecoration(
            labelText: 'Notes',
            border: OutlineInputBorder(),
            hintText: 'Enter additional notes here...',
          ),
          maxLines: 2,
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
                onPressed: taskOperations.state == TaskOperationState.loading
                    ? null
                    : () {
                  widget.onTaskSaved();
                },
                icon: Icon(widget.task == null ? Icons.add : Icons.save),
                label: Text(widget.task == null ? 'Create' : 'Update'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: widget.task == null
                      ? Theme.of(context).colorScheme.primary
                      : Colors.orange,
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

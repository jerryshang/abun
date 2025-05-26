import 'package:flutter/material.dart';
import 'package:abun/models/task_status.dart';

extension TaskStatusExtension on TaskStatus {
  static const double defaultIconSize = 28.0;

  IconData get iconData {
    switch (this) {
      case TaskStatus.inbox:
        return Icons.inbox;
      case TaskStatus.planned:
        return Icons.event;
      case TaskStatus.inProgress:
        return Icons.hourglass_top;
      case TaskStatus.completed:
        return Icons.check_circle;
      case TaskStatus.eliminated:
        return Icons.cancel;
    }
  }

  Color get color {
    switch (this) {
      case TaskStatus.inbox:
        return Colors.yellow;
      case TaskStatus.planned:
        return Colors.orange;
      case TaskStatus.inProgress:
        return Colors.blue;
      case TaskStatus.completed:
        return Colors.green;
      case TaskStatus.eliminated:
        return Colors.red;
    }
  }

  Widget toIcon({double? size}) => Icon(
        iconData,
        color: color,
        size: size ?? defaultIconSize,
      );
}

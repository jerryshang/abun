/// Enum representing the possible statuses of a task
enum TaskStatus {
  inbox('inbox'),
  planned('planned'),
  inProgress('in_progress'),
  completed('completed'),
  eliminated('eliminated');

  final String value;
  const TaskStatus(this.value);

  /// Convert a string to a TaskStatus
  static TaskStatus fromString(String value) {
    return TaskStatus.values.firstWhere(
      (status) => status.value == value,
      orElse: () => TaskStatus.inbox,
    );
  }

  /// Get a display name for the status
  String get displayName {
    switch (this) {
      case TaskStatus.inbox:
        return '📥';
      case TaskStatus.planned:
        return '🗓️';
      case TaskStatus.inProgress:
        return '🏃‍♂️';
      case TaskStatus.completed:
        return '✅';
      case TaskStatus.eliminated:
        return '❌';
    }
  }
}

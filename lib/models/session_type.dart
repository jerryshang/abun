/// Enum representing the possible types of a session
enum SessionType {
  focus('focus'),
  free('free');

  final String value;
  const SessionType(this.value);

  /// Convert a string to a SessionType
  static SessionType fromString(String value) {
    return SessionType.values.firstWhere(
      (type) => type.value == value,
      orElse: () => SessionType.focus, // Default to 'focus' if not found
    );
  }

  /// Get a display name for the session type
  String get displayName {
    switch (this) {
      case SessionType.focus:
        return 'Focus';
      case SessionType.free:
        return 'Free';
    }
  }
}

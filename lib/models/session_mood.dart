/// Enum representing the possible mood values for a session
enum SessionMood {
  flowing('flowing'),
  focused('focused'),
  okay('okay'),
  distracted('distracted'),
  unproductive('unproductive');

  final String value;
  const SessionMood(this.value);

  /// Convert a string to a SessionMood
  static SessionMood fromString(String value) {
    return SessionMood.values.firstWhere(
      (mood) => mood.value == value,
      orElse: () => SessionMood.okay, // Default to 'okay' if not found
    );
  }

  /// Get a display name for the mood
  String get displayName {
    switch (this) {
      case SessionMood.flowing:
        return '🔥';
      case SessionMood.focused:
        return '🎯';
      case SessionMood.okay:
        return '🙂';
      case SessionMood.distracted:
        return '😵‍';
      case SessionMood.unproductive:
        return '🐢';
    }
  }
}

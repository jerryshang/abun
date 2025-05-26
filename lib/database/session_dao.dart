part of 'database.dart';

/// Data Access Object for Session-related database operations
class SessionDao {
  final AppDatabase _db;

  SessionDao(this._db);

  /// Creates a new session in the database
  Future<String> createSession(SessionsCompanion session) async {
    try {
      // Ensure we have a valid ID
      final sessionId = session.id.present ? session.id.value : const Uuid().v4();

      // Create a new session with the ID and ensure all required fields are set
      final sessionToInsert = session.copyWith(
        id: Value(sessionId),
        duration: Value(session.duration.value ?? 'PT25M'), // Default duration if not set
        type: Value(session.type.value ?? 'free'), // Default type if not set
      );

      await _db.into(_db.sessions).insert(sessionToInsert);
      return sessionId;
    } catch (e) {
      if (kDebugMode) {
        print('Error creating session: $e');
      }
      rethrow;
    }
  }

  /// Updates an existing session
  Future<bool> updateSession(Session session) {
    return _db.update(_db.sessions).replace(SessionsCompanion(
      id: Value(session.id),
      taskId: Value(session.taskId),
      startTime: Value(session.startTime),
      endTime: Value(session.endTime),
      duration: Value(session.duration),
      type: Value(session.type),
      mood: Value(session.mood),
    ));
  }

  /// Deletes a session by ID
  Future<int> deleteSession(String id) {
    return (_db.delete(_db.sessions)..where((s) => s.id.equals(id))).go();
  }

  /// Helper method to map a database row to a Session
  Session _mapRowToSession(dynamic row) {
    return Session(
      id: row.id,
      taskId: row.taskId,
      title: row.title,
      note: row.note,
      startTime: row.startTime,
      endTime: row.endTime,
      duration: row.duration,
      type: row.type,
      mood: row.mood,
    );
  }

  /// Gets a session by ID
  Future<Session?> getSessionById(String id) async {
    final result = await (_db.select(_db.sessions)..where((s) => s.id.equals(id))).getSingleOrNull();
    if (result == null) return null;
    return _mapRowToSession(result);
  }

  /// Gets all sessions
  Future<List<Session>> getAllSessions() async {
    final results = await _db.select(_db.sessions).get();
    return results.map(_mapRowToSession).toList();
  }

  /// Watches all sessions for real-time updates
  Stream<List<Session>> watchAllSessions() {
    return _db.select(_db.sessions).watch()
        .map((rows) => rows.map(_mapRowToSession).toList());
  }

  /// Gets all sessions for a specific task
  Future<List<Session>> getSessionsByTask(String taskId) async {
    final results = await (_db.select(_db.sessions)..where((s) => s.taskId.equals(taskId))).get();
    return results.map(_mapRowToSession).toList();
  }

  /// Watches sessions for a specific task for real-time updates
  Stream<List<Session>> watchSessionsByTask(String taskId) {
    return (_db.select(_db.sessions)..where((s) => s.taskId.equals(taskId)))
        .watch()
        .map((rows) => rows.map(_mapRowToSession).toList());
  }

  /// Deletes all sessions
  Future<int> clearAllSessions() => _db.delete(_db.sessions).go();

  /// Gets sessions within a specific date range
  Future<List<Session>> getSessionsInRange(String startDate, String endDate) async {
    final results = await (_db.select(_db.sessions)
      ..where((s) => s.endTime.isBiggerOrEqualValue(startDate) &
                     s.endTime.isSmallerOrEqualValue(endDate))).get();

    return results.map(_mapRowToSession).toList();
  }

  /// Watches sessions within a specific date range for real-time updates
  Stream<List<Session>> watchSessionsInRange(String startDate, String endDate) {
    return (_db.select(_db.sessions)
      ..where((s) => s.endTime.isBiggerOrEqualValue(startDate) &
                     s.endTime.isSmallerOrEqualValue(endDate)))
        .watch()
        .map((rows) => rows.map(_mapRowToSession).toList());
  }

  /// Gets sessions without tasks within a specific date range
  Future<List<Session>> getSessionsWithoutTaskInRange(String startDate, String endDate) async {
    final results = await (_db.select(_db.sessions)
      ..where((s) => s.taskId.isNull() &
                     s.endTime.isBiggerOrEqualValue(startDate) &
                     s.endTime.isSmallerOrEqualValue(endDate))).get();

    return results.map(_mapRowToSession).toList();
  }

  /// Watches sessions without tasks within a specific date range for real-time updates
  Stream<List<Session>> watchSessionsWithoutTaskInRange(String startDate, String endDate) {
    return (_db.select(_db.sessions)
      ..where((s) => s.taskId.isNull() &
                     s.endTime.isBiggerOrEqualValue(startDate) &
                     s.endTime.isSmallerOrEqualValue(endDate)))
        .watch()
        .map((rows) => rows.map(_mapRowToSession).toList());
  }
}

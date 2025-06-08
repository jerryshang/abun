import 'package:flutter_riverpod/flutter_riverpod.dart';

final currentDateProvider = StateNotifierProvider<CurrentDateNotifier, DateTime>((ref) {
  return CurrentDateNotifier();
});

class CurrentDateNotifier extends StateNotifier<DateTime> {
  CurrentDateNotifier() : super(_getCurrentDateKey()) {
    // Check for date change periodically (every minute)
    _checkDateChange();
  }

  static DateTime _getCurrentDateKey() {
    final now = DateTime.now();
    return DateTime(now.year, now.month, now.day);
  }

  void _checkDateChange() async {
    while (true) {
      await Future.delayed(const Duration(seconds: 1));
      final currentDate = _getCurrentDateKey();
      if (currentDate != state) {
        state = currentDate;
      }
    }
  }

  // Call this method to force a refresh
  void refresh() {
    state = _getCurrentDateKey();
  }
}

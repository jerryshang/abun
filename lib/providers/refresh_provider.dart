import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

final refreshProvider = StateNotifierProvider<RefreshNotifier, DateTime>((ref) {
  return RefreshNotifier();
});

class RefreshNotifier extends StateNotifier<DateTime> {
  RefreshNotifier() : super(_getCurrentDateKey()) {
    // Check for date change periodically (every minute)
    _checkDateChange();
  }

  static DateTime _getCurrentDateKey() {
    final now = DateTime.now();
    return DateTime(now.year, now.month, now.day);
  }

  void _checkDateChange() async {
    while (true) {
      await Future.delayed(const Duration(minutes: 1));
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

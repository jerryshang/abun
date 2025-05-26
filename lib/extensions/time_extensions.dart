import 'dart:math';

extension Iso8601DurationExtension on String {
  /// Parses an ISO 8601 duration string (e.g., PT25M, PT1H30M) and returns the total minutes
  int toMinutes() {
    try {
      final match = RegExp(r'^PT(\d+H)?(\d+M)?(\d+S)?$').firstMatch(this);
      if (match == null) return 0;

      final hours = match.group(1) != null ? int.parse(match.group(1)!.replaceAll('H', '')) : 0;
      final minutes = match.group(2) != null ? int.parse(match.group(2)!.replaceAll('M', '')) : 0;

      return (hours * 60) + minutes;
    } catch (e) {
      return 0;
    }
  }

  Duration toDuration() {
    try {
      final match = RegExp(r'^PT(\d+H)?(\d+M)?(\d+S)?$').firstMatch(this);
      if (match == null) return Duration.zero;

      final hours = match.group(1) != null ? int.parse(match.group(1)!.replaceAll('H', '')) : 0;
      final minutes = match.group(2) != null ? int.parse(match.group(2)!.replaceAll('M', '')) : 0;

      return Duration(hours: hours, minutes: minutes);
    } catch (e) {
      return Duration.zero;
    }
  }
}

class DateTimeUtils {
  /// Calculates the overlap in minutes of two time ranges
  static int overlapInMinutes(DateTime start1, DateTime end1, DateTime start2, DateTime end2) {
    final latestStart = start1.isAfter(start2) ? start1 : start2;
    final earliestEnd = end1.isBefore(end2) ? end1 : end2;
    final overlap = earliestEnd.isAfter(latestStart)
        ? earliestEnd.difference(latestStart)
        : Duration.zero;
    return overlap.inMinutes;
  }
}

extension Iso8601StringExtension on String {
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

  DateTime? toDateTime() {
    try {
      return DateTime.tryParse(this);
    } catch (_) {
      return null;
    }
  }
}

class DateTimeUtils {
  /// Calculates the overlap in minutes of two time ranges
  static int overlapInMinutes(DateTime start1, DateTime end1, DateTime start2, DateTime end2) {
    final latestStart = start1.isAfter(start2) ? start1 : start2;
    final earliestEnd = end1.isBefore(end2) ? end1 : end2;
    final overlap = earliestEnd.isAfter(latestStart) ? earliestEnd.difference(latestStart) : Duration.zero;
    return overlap.inMinutes;
  }

  static bool visible(DateTime anchor, DateTime? due, Duration? duration, int pre) {
    final dueTime = (due ?? DateTimeExtension.farFuture).date();
    final estimatedDuration = duration ?? Duration.zero;
    final estimatedStartDate = dueTime.subtract(estimatedDuration).date();
    return (anchor == estimatedStartDate || anchor.isAfter(estimatedStartDate));
  }

  static bool isForecastWindowCovered({
    required DateTime anchorTime,
    required Duration forecastDuration,
    Duration? estimatedDuration,
    DateTime? dueTime,
  }) {
    final Duration effectiveDuration = estimatedDuration ?? Duration.zero;
    final DateTime effectiveDueTime = dueTime ?? DateTime(9999, 12, 31);
    final estimatedStartTime = effectiveDueTime.subtract(effectiveDuration);

    return isForecastWindowCoveredByStartTime(
      anchorTime: anchorTime,
      forecastDuration: forecastDuration,
      startTime: estimatedStartTime,
    );
  }

  static bool isForecastWindowCoveredByStartTime({
    required DateTime anchorTime,
    required Duration forecastDuration,
    DateTime? startTime,
  }) {
    final latestAcceptableTime = anchorTime.add(forecastDuration);

    final estimatedStartTime = startTime ?? DateTime(9999, 12, 31);

    return estimatedStartTime.isBefore(latestAcceptableTime) ||
        estimatedStartTime.isAtSameMomentAs(latestAcceptableTime);
  }
}

extension DateTimeExtension on DateTime {
  static DateTime get farFuture => DateTime(9999, 12, 31, 23, 59, 59, 999, 999);

  DateTime date() {
    return DateTime(year, month, day);
  }
}

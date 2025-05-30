import 'package:abun/extensions/time_extensions.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('isForecastWindowCovered', () {
    // Test case 1: Task starts before the forecast window ends
    test('should return true when task starts before forecast window ends', () {
      final anchorTime = DateTime(2023, 6, 1);
      final forecastDuration = const Duration(days: 7);
      final dueTime = DateTime(2023, 6, 10);
      final estimatedDuration = const Duration(days: 2);
      
      final result = DateTimeUtils.isForecastWindowCovered(
        anchorTime: anchorTime,
        forecastDuration: forecastDuration,
        estimatedDuration: estimatedDuration,
        dueTime: dueTime,
      );
      
      expect(result, isTrue);
    });

    // Test case 2: Task starts exactly when forecast window ends
    test('should return true when task starts exactly when forecast window ends', () {
      final anchorTime = DateTime(2023, 6, 1);
      final forecastDuration = const Duration(days: 7);
      final dueTime = DateTime(2023, 6, 8);
      final estimatedDuration = const Duration(days: 1);
      
      final result = DateTimeUtils.isForecastWindowCovered(
        anchorTime: anchorTime,
        forecastDuration: forecastDuration,
        estimatedDuration: estimatedDuration,
        dueTime: dueTime,
      );
      
      expect(result, isTrue);
    });

    // Test case 3: Task starts after forecast window ends
    test('should return false when task starts after forecast window ends', () {
      final anchorTime = DateTime(2023, 6, 1);
      final forecastDuration = const Duration(days: 7);
      final dueTime = DateTime(2023, 6, 15);
      final estimatedDuration = const Duration(days: 2);
      
      final result = DateTimeUtils.isForecastWindowCovered(
        anchorTime: anchorTime,
        forecastDuration: forecastDuration,
        estimatedDuration: estimatedDuration,
        dueTime: dueTime,
      );
      
      expect(result, isFalse);
    });

    // Test case 4: Task with zero duration
    test('should handle zero duration tasks correctly', () {
      final anchorTime = DateTime(2023, 6, 1);
      final forecastDuration = const Duration(days: 7);
      final dueTime = DateTime(2023, 6, 8);
      const estimatedDuration = Duration.zero;
      
      final result = DateTimeUtils.isForecastWindowCovered(
        anchorTime: anchorTime,
        forecastDuration: forecastDuration,
        estimatedDuration: estimatedDuration,
        dueTime: dueTime,
      );
      
      expect(result, isTrue);
    });

    // Test case 5: Task with long duration that ends in forecast window
    test('should return true when task spans into forecast window', () {
      final anchorTime = DateTime(2023, 6, 1);
      final forecastDuration = const Duration(days: 7);
      final dueTime = DateTime(2023, 6, 10);
      final estimatedDuration = const Duration(days: 5);
      
      final result = DateTimeUtils.isForecastWindowCovered(
        anchorTime: anchorTime,
        forecastDuration: forecastDuration,
        estimatedDuration: estimatedDuration,
        dueTime: dueTime,
      );
      
      expect(result, isTrue);
    });

    // Test case 6: Task with due time in past but long duration
    test('should handle tasks with due time in past but long duration', () {
      final anchorTime = DateTime(2023, 6, 1);
      final forecastDuration = const Duration(days: 7);
      final dueTime = DateTime(2023, 5, 30);
      final estimatedDuration = const Duration(days: 10);
      
      final result = DateTimeUtils.isForecastWindowCovered(
        anchorTime: anchorTime,
        forecastDuration: forecastDuration,
        estimatedDuration: estimatedDuration,
        dueTime: dueTime,
      );
      
      expect(result, isTrue);
    });
  });
}

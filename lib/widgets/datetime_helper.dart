import 'package:flutter/material.dart';
import 'package:numberpicker/numberpicker.dart';

/// A collection of helper functions for date and time operations.
class DateTimeHelper {
  /// Displays a custom time picker dialog with hour and minute selection.
  ///
  /// The picker allows selecting hours from 0-23 and minutes in 5-minute increments.
  /// The minutes are automatically rounded to the nearest 5-minute interval.
  ///
  /// [context] - The build context used to show the dialog.
  /// [initialTime] - The initial time to display in the picker.
  /// [title] - The title to display at the top of the picker dialog.
  ///
  /// Returns a [Future] that resolves to the selected [TimeOfDay] when the user
  /// taps "OK", or `null` if the user cancels the dialog.
  static Future<TimeOfDay?> showCustomTimePicker(
    BuildContext context, {
    required TimeOfDay initialTime,
    required String title,
  }) async {
    int originalMinutes = initialTime.minute;
    int nearestMinutes = (originalMinutes / 5).round() * 5;
    TimeOfDay selectedTime = TimeOfDay(hour: initialTime.hour, minute: nearestMinutes);

    return showDialog<TimeOfDay>(
      context: context,
      builder: (BuildContext context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: Text(title),
              content: SizedBox(
                height: 120,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    // Hours picker
                    SizedBox(
                      width: 80,
                      child: NumberPicker(
                        minValue: 0,
                        maxValue: 23,
                        value: selectedTime.hour,
                        onChanged: (value) {
                          setState(() {
                            selectedTime = selectedTime.replacing(hour: value);
                          });
                        },
                        itemHeight: 40,
                        itemWidth: 60,
                        textStyle: Theme.of(context).textTheme.bodyLarge,
                        selectedTextStyle: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          color: Theme.of(context).colorScheme.primary,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    // Minutes picker
                    SizedBox(
                      width: 80,
                      child: NumberPicker(
                        minValue: 0,
                        maxValue: 55,
                        value: selectedTime.minute,
                        step: 5,
                        onChanged: (value) {
                          setState(() {
                            selectedTime = selectedTime.replacing(minute: value);
                          });
                        },
                        itemHeight: 40,
                        itemWidth: 60,
                        textStyle: Theme.of(context).textTheme.bodyLarge,
                        selectedTextStyle: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          color: Theme.of(context).colorScheme.primary,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              actions: <Widget>[
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Cancel'),
                ),
                TextButton(
                  onPressed: () => Navigator.pop(context, selectedTime),
                  child: const Text('OK'),
                ),
              ],
            );
          },
        );
      },
    );
  }
}

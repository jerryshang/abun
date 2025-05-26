// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:abun/main.dart';

void main() {
  testWidgets('Should find a FloatingActionButton at bottom right', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const ProviderScope(child: AbunApp()));

    // Pump a few frames to allow the UI to build
    await tester.pump(const Duration(milliseconds: 100));

    // Find any FloatingActionButton with the add icon
    expect(find.byIcon(Icons.add), findsWidgets);

    // Find all FloatingActionButtons
    final fabFinder = find.byType(FloatingActionButton);
    expect(fabFinder, findsWidgets);

    // Verify at least one of them has the add icon
    bool foundAddButton = false;
    for (int i = 0; i < tester.widgetList(fabFinder).length; i++) {
      final currentFab = fabFinder.at(i);
      if (find.descendant(
        of: currentFab,
        matching: find.byIcon(Icons.add),
      ).evaluate().isNotEmpty) {
        foundAddButton = true;
        break;
      }
    }

    expect(foundAddButton, isTrue, reason: 'Should find a FloatingActionButton with an add icon');
  });
}

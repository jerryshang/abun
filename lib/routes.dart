import 'package:flutter/material.dart';

import 'features/index.dart';

/// Route names used throughout the app
class Routes {
  static const String home = '/';
  static const String tasks = '/tasks';
  static const String sessions = '/sessions';
  static const String routines = '/routines';

  /// Define routes for the application
  static Map<String, WidgetBuilder> getRoutes() {
    return {
      home: (context) => const HomePage(),
      tasks: (context) => const TasksPage(),
      sessions: (context) => const SessionsPage(),
      routines: (context) => const RoutinesPage(),
    };
  }

  /// Navigate to the plan page
  static void navigateToTasks(BuildContext context) {
    Navigator.pushNamed(context, tasks);
  }

  /// Navigate to the home page
  static void navigateToHome(BuildContext context) {
    Navigator.pushNamedAndRemoveUntil(context, home, (route) => false);
  }

  /// Navigate to the sessions page
  static void navigateToSessions(BuildContext context) {
    Navigator.pushNamed(context, sessions);
  }

  /// Navigate to the routines page
  static void navigateToRoutines(BuildContext context) {
    Navigator.pushNamed(context, routines);
  }
}

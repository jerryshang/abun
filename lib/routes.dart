import 'package:flutter/material.dart';
import 'home_page.dart';
import 'plan_page.dart';
import 'sessions_page.dart';

/// Route names used throughout the app
class Routes {
  static const String home = '/';
  static const String plan = '/plan';
  static const String sessions = '/sessions';

  /// Define routes for the application
  static Map<String, WidgetBuilder> getRoutes() {
    return {
      home: (context) => const HomePage(title: 'Today'),
      plan: (context) => const PlanPage(title: 'Plan'),
      sessions: (context) => const SessionsPage(),
    };
  }

  /// Navigate to the plan page
  static void navigateToPlan(BuildContext context) {
    Navigator.pushNamed(context, plan);
  }

  /// Navigate to the home page
  static void navigateToHome(BuildContext context) {
    Navigator.pushNamedAndRemoveUntil(context, home, (route) => false);
  }

  /// Navigate to the sessions page
  static void navigateToSessions(BuildContext context) {
    Navigator.pushNamed(context, sessions);
  }
}

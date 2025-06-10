import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'routes.dart';
import 'services/routine_task_service.dart';

void main() async {
  // Ensure Flutter is initialized
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize the app with ProviderScope
  final container = ProviderContainer();
  
  // Initialize the routine task service
  final routineTaskService = container.read(routineTaskServiceProvider);
  await routineTaskService.initialize();

  // Run the app
  runApp(
    UncontrolledProviderScope(
      container: container,
      child: const AbunApp(),
    ),
  );
}

class AbunApp extends ConsumerWidget {
  const AbunApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp(
      title: 'a.bun.dance',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.green),
      ),
      initialRoute: Routes.home,
      routes: Routes.getRoutes(),
    );
  }
}

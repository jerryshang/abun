import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'routes.dart';

void main() async {
  print("Test");
  // Ensure Flutter is initialized
  WidgetsFlutterBinding.ensureInitialized();

  // Run the app with ProviderScope for Riverpod
  runApp(const ProviderScope(child: AbunApp()));
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

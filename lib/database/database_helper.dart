import 'dart:async';

import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();

  static DatabaseHelper get instance => _instance;

  static Database? _database;

  DatabaseHelper._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    // Get the path to the database file
    String path = join(await getDatabasesPath(), 'abun_database.db');

    // Open/create the database at the given path
    return await openDatabase(path, version: 1, onCreate: _createDb);
  }

  Future<void> _createDb(Database db, int version) async {
    // Create the task table with the specified fields
    await db.execute('''
      CREATE TABLE task(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        parent_id INTEGER,
        content TEXT NOT NULL,
        state TEXT NOT NULL
      )
    ''');
  }

  // Check if the database exists
  Future<bool> databaseExists() async {
    String path = join(await getDatabasesPath(), 'abun_database.db');
    return await databaseFactory.databaseExists(path);
  }
}

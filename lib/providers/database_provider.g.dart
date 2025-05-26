// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'database_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$databaseHash() => r'282ab6de18cf5659cf0ccd20808c37fe9789ee0e';

/// Provider for the database instance
///
/// Copied from [database].
@ProviderFor(database)
final databaseProvider = Provider<AppDatabase>.internal(
  database,
  name: r'databaseProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$databaseHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef DatabaseRef = ProviderRef<AppDatabase>;
String _$allTasksHash() => r'e5b91ac5ac7a8007bbca5f022f10bdaf1185cdb5';

/// Provider for all tasks
///
/// Copied from [allTasks].
@ProviderFor(allTasks)
final allTasksProvider = AutoDisposeFutureProvider<List<Task>>.internal(
  allTasks,
  name: r'allTasksProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$allTasksHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef AllTasksRef = AutoDisposeFutureProviderRef<List<Task>>;
String _$taskByIdHash() => r'c4a6ae21ed4de1acda6c58672a9692a7efdd3914';

/// Copied from Dart SDK
class _SystemHash {
  _SystemHash._();

  static int combine(int hash, int value) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + value);
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x0007ffff & hash) << 10));
    return hash ^ (hash >> 6);
  }

  static int finish(int hash) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x03ffffff & hash) << 3));
    // ignore: parameter_assignments
    hash = hash ^ (hash >> 11);
    return 0x1fffffff & (hash + ((0x00003fff & hash) << 15));
  }
}

/// Provider for a single task by ID
///
/// Copied from [taskById].
@ProviderFor(taskById)
const taskByIdProvider = TaskByIdFamily();

/// Provider for a single task by ID
///
/// Copied from [taskById].
class TaskByIdFamily extends Family<AsyncValue<Task?>> {
  /// Provider for a single task by ID
  ///
  /// Copied from [taskById].
  const TaskByIdFamily();

  /// Provider for a single task by ID
  ///
  /// Copied from [taskById].
  TaskByIdProvider call(String id) {
    return TaskByIdProvider(id);
  }

  @override
  TaskByIdProvider getProviderOverride(covariant TaskByIdProvider provider) {
    return call(provider.id);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'taskByIdProvider';
}

/// Provider for a single task by ID
///
/// Copied from [taskById].
class TaskByIdProvider extends AutoDisposeFutureProvider<Task?> {
  /// Provider for a single task by ID
  ///
  /// Copied from [taskById].
  TaskByIdProvider(String id)
    : this._internal(
        (ref) => taskById(ref as TaskByIdRef, id),
        from: taskByIdProvider,
        name: r'taskByIdProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$taskByIdHash,
        dependencies: TaskByIdFamily._dependencies,
        allTransitiveDependencies: TaskByIdFamily._allTransitiveDependencies,
        id: id,
      );

  TaskByIdProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
  }) : super.internal();

  final String id;

  @override
  Override overrideWith(FutureOr<Task?> Function(TaskByIdRef provider) create) {
    return ProviderOverride(
      origin: this,
      override: TaskByIdProvider._internal(
        (ref) => create(ref as TaskByIdRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<Task?> createElement() {
    return _TaskByIdProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is TaskByIdProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin TaskByIdRef on AutoDisposeFutureProviderRef<Task?> {
  /// The parameter `id` of this provider.
  String get id;
}

class _TaskByIdProviderElement extends AutoDisposeFutureProviderElement<Task?>
    with TaskByIdRef {
  _TaskByIdProviderElement(super.provider);

  @override
  String get id => (origin as TaskByIdProvider).id;
}

String _$watchAllTasksHash() => r'e41dadd3788544486e309f339c9e87200c3b109a';

/// Provider for a stream of all tasks (reactive)
///
/// Copied from [watchAllTasks].
@ProviderFor(watchAllTasks)
final watchAllTasksProvider = AutoDisposeStreamProvider<List<Task>>.internal(
  watchAllTasks,
  name: r'watchAllTasksProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$watchAllTasksHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef WatchAllTasksRef = AutoDisposeStreamProviderRef<List<Task>>;
String _$allRoutinesHash() => r'2d3837b1a2a6c1b066485903f9caad613cff7175';

/// Provider for all routines
///
/// Copied from [allRoutines].
@ProviderFor(allRoutines)
final allRoutinesProvider = AutoDisposeFutureProvider<List<Routine>>.internal(
  allRoutines,
  name: r'allRoutinesProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$allRoutinesHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef AllRoutinesRef = AutoDisposeFutureProviderRef<List<Routine>>;
String _$watchAllRoutinesHash() => r'f8c942127604434288ee4c2c98c5167b0a946d54';

/// Provider for a stream of all routines (reactive)
///
/// Copied from [watchAllRoutines].
@ProviderFor(watchAllRoutines)
final watchAllRoutinesProvider =
    AutoDisposeStreamProvider<List<Routine>>.internal(
      watchAllRoutines,
      name: r'watchAllRoutinesProvider',
      debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
          ? null
          : _$watchAllRoutinesHash,
      dependencies: null,
      allTransitiveDependencies: null,
    );

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef WatchAllRoutinesRef = AutoDisposeStreamProviderRef<List<Routine>>;
String _$routineByIdHash() => r'151cd8dad57cdd75a1316ff089914b0138d5639f';

/// Provider for a single routine by ID
///
/// Copied from [routineById].
@ProviderFor(routineById)
const routineByIdProvider = RoutineByIdFamily();

/// Provider for a single routine by ID
///
/// Copied from [routineById].
class RoutineByIdFamily extends Family<AsyncValue<Routine?>> {
  /// Provider for a single routine by ID
  ///
  /// Copied from [routineById].
  const RoutineByIdFamily();

  /// Provider for a single routine by ID
  ///
  /// Copied from [routineById].
  RoutineByIdProvider call(String id) {
    return RoutineByIdProvider(id);
  }

  @override
  RoutineByIdProvider getProviderOverride(
    covariant RoutineByIdProvider provider,
  ) {
    return call(provider.id);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'routineByIdProvider';
}

/// Provider for a single routine by ID
///
/// Copied from [routineById].
class RoutineByIdProvider extends AutoDisposeFutureProvider<Routine?> {
  /// Provider for a single routine by ID
  ///
  /// Copied from [routineById].
  RoutineByIdProvider(String id)
    : this._internal(
        (ref) => routineById(ref as RoutineByIdRef, id),
        from: routineByIdProvider,
        name: r'routineByIdProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$routineByIdHash,
        dependencies: RoutineByIdFamily._dependencies,
        allTransitiveDependencies: RoutineByIdFamily._allTransitiveDependencies,
        id: id,
      );

  RoutineByIdProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
  }) : super.internal();

  final String id;

  @override
  Override overrideWith(
    FutureOr<Routine?> Function(RoutineByIdRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: RoutineByIdProvider._internal(
        (ref) => create(ref as RoutineByIdRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<Routine?> createElement() {
    return _RoutineByIdProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is RoutineByIdProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin RoutineByIdRef on AutoDisposeFutureProviderRef<Routine?> {
  /// The parameter `id` of this provider.
  String get id;
}

class _RoutineByIdProviderElement
    extends AutoDisposeFutureProviderElement<Routine?>
    with RoutineByIdRef {
  _RoutineByIdProviderElement(super.provider);

  @override
  String get id => (origin as RoutineByIdProvider).id;
}

String _$tasksByRoutineHash() => r'30be6fb4178c03d1957a5378418e51bb5f4d6e96';

/// Provider for tasks by routine ID
///
/// Copied from [tasksByRoutine].
@ProviderFor(tasksByRoutine)
const tasksByRoutineProvider = TasksByRoutineFamily();

/// Provider for tasks by routine ID
///
/// Copied from [tasksByRoutine].
class TasksByRoutineFamily extends Family<AsyncValue<List<Task>>> {
  /// Provider for tasks by routine ID
  ///
  /// Copied from [tasksByRoutine].
  const TasksByRoutineFamily();

  /// Provider for tasks by routine ID
  ///
  /// Copied from [tasksByRoutine].
  TasksByRoutineProvider call(String routineId) {
    return TasksByRoutineProvider(routineId);
  }

  @override
  TasksByRoutineProvider getProviderOverride(
    covariant TasksByRoutineProvider provider,
  ) {
    return call(provider.routineId);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'tasksByRoutineProvider';
}

/// Provider for tasks by routine ID
///
/// Copied from [tasksByRoutine].
class TasksByRoutineProvider extends AutoDisposeFutureProvider<List<Task>> {
  /// Provider for tasks by routine ID
  ///
  /// Copied from [tasksByRoutine].
  TasksByRoutineProvider(String routineId)
    : this._internal(
        (ref) => tasksByRoutine(ref as TasksByRoutineRef, routineId),
        from: tasksByRoutineProvider,
        name: r'tasksByRoutineProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$tasksByRoutineHash,
        dependencies: TasksByRoutineFamily._dependencies,
        allTransitiveDependencies:
            TasksByRoutineFamily._allTransitiveDependencies,
        routineId: routineId,
      );

  TasksByRoutineProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.routineId,
  }) : super.internal();

  final String routineId;

  @override
  Override overrideWith(
    FutureOr<List<Task>> Function(TasksByRoutineRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: TasksByRoutineProvider._internal(
        (ref) => create(ref as TasksByRoutineRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        routineId: routineId,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<Task>> createElement() {
    return _TasksByRoutineProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is TasksByRoutineProvider && other.routineId == routineId;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, routineId.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin TasksByRoutineRef on AutoDisposeFutureProviderRef<List<Task>> {
  /// The parameter `routineId` of this provider.
  String get routineId;
}

class _TasksByRoutineProviderElement
    extends AutoDisposeFutureProviderElement<List<Task>>
    with TasksByRoutineRef {
  _TasksByRoutineProviderElement(super.provider);

  @override
  String get routineId => (origin as TasksByRoutineProvider).routineId;
}

String _$watchTasksByRoutineHash() =>
    r'93b4fd8576afb1321cd59583e752b3b2d538477b';

/// Provider for a stream of tasks by routine ID (reactive)
///
/// Copied from [watchTasksByRoutine].
@ProviderFor(watchTasksByRoutine)
const watchTasksByRoutineProvider = WatchTasksByRoutineFamily();

/// Provider for a stream of tasks by routine ID (reactive)
///
/// Copied from [watchTasksByRoutine].
class WatchTasksByRoutineFamily extends Family<AsyncValue<List<Task>>> {
  /// Provider for a stream of tasks by routine ID (reactive)
  ///
  /// Copied from [watchTasksByRoutine].
  const WatchTasksByRoutineFamily();

  /// Provider for a stream of tasks by routine ID (reactive)
  ///
  /// Copied from [watchTasksByRoutine].
  WatchTasksByRoutineProvider call(String routineId) {
    return WatchTasksByRoutineProvider(routineId);
  }

  @override
  WatchTasksByRoutineProvider getProviderOverride(
    covariant WatchTasksByRoutineProvider provider,
  ) {
    return call(provider.routineId);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'watchTasksByRoutineProvider';
}

/// Provider for a stream of tasks by routine ID (reactive)
///
/// Copied from [watchTasksByRoutine].
class WatchTasksByRoutineProvider
    extends AutoDisposeStreamProvider<List<Task>> {
  /// Provider for a stream of tasks by routine ID (reactive)
  ///
  /// Copied from [watchTasksByRoutine].
  WatchTasksByRoutineProvider(String routineId)
    : this._internal(
        (ref) => watchTasksByRoutine(ref as WatchTasksByRoutineRef, routineId),
        from: watchTasksByRoutineProvider,
        name: r'watchTasksByRoutineProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$watchTasksByRoutineHash,
        dependencies: WatchTasksByRoutineFamily._dependencies,
        allTransitiveDependencies:
            WatchTasksByRoutineFamily._allTransitiveDependencies,
        routineId: routineId,
      );

  WatchTasksByRoutineProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.routineId,
  }) : super.internal();

  final String routineId;

  @override
  Override overrideWith(
    Stream<List<Task>> Function(WatchTasksByRoutineRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: WatchTasksByRoutineProvider._internal(
        (ref) => create(ref as WatchTasksByRoutineRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        routineId: routineId,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<List<Task>> createElement() {
    return _WatchTasksByRoutineProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is WatchTasksByRoutineProvider && other.routineId == routineId;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, routineId.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin WatchTasksByRoutineRef on AutoDisposeStreamProviderRef<List<Task>> {
  /// The parameter `routineId` of this provider.
  String get routineId;
}

class _WatchTasksByRoutineProviderElement
    extends AutoDisposeStreamProviderElement<List<Task>>
    with WatchTasksByRoutineRef {
  _WatchTasksByRoutineProviderElement(super.provider);

  @override
  String get routineId => (origin as WatchTasksByRoutineProvider).routineId;
}

String _$allSessionsHash() => r'a68a7adccf220cd37780d9960062d40abcc90aae';

/// Provider for all sessions
///
/// Copied from [allSessions].
@ProviderFor(allSessions)
final allSessionsProvider = AutoDisposeFutureProvider<List<Session>>.internal(
  allSessions,
  name: r'allSessionsProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$allSessionsHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef AllSessionsRef = AutoDisposeFutureProviderRef<List<Session>>;
String _$watchAllSessionsHash() => r'5fd3d7d5e55d44cfc42cc67fb3c3b5a9984b0b87';

/// Provider for a stream of all sessions (reactive)
///
/// Copied from [watchAllSessions].
@ProviderFor(watchAllSessions)
final watchAllSessionsProvider =
    AutoDisposeStreamProvider<List<Session>>.internal(
      watchAllSessions,
      name: r'watchAllSessionsProvider',
      debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
          ? null
          : _$watchAllSessionsHash,
      dependencies: null,
      allTransitiveDependencies: null,
    );

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef WatchAllSessionsRef = AutoDisposeStreamProviderRef<List<Session>>;
String _$sessionsByTaskHash() => r'eb54c18b47d7af23e7fd6c2b6618010d0a5b6739';

/// Provider for sessions by task ID
///
/// Copied from [sessionsByTask].
@ProviderFor(sessionsByTask)
const sessionsByTaskProvider = SessionsByTaskFamily();

/// Provider for sessions by task ID
///
/// Copied from [sessionsByTask].
class SessionsByTaskFamily extends Family<AsyncValue<List<Session>>> {
  /// Provider for sessions by task ID
  ///
  /// Copied from [sessionsByTask].
  const SessionsByTaskFamily();

  /// Provider for sessions by task ID
  ///
  /// Copied from [sessionsByTask].
  SessionsByTaskProvider call(String taskId) {
    return SessionsByTaskProvider(taskId);
  }

  @override
  SessionsByTaskProvider getProviderOverride(
    covariant SessionsByTaskProvider provider,
  ) {
    return call(provider.taskId);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'sessionsByTaskProvider';
}

/// Provider for sessions by task ID
///
/// Copied from [sessionsByTask].
class SessionsByTaskProvider extends AutoDisposeFutureProvider<List<Session>> {
  /// Provider for sessions by task ID
  ///
  /// Copied from [sessionsByTask].
  SessionsByTaskProvider(String taskId)
    : this._internal(
        (ref) => sessionsByTask(ref as SessionsByTaskRef, taskId),
        from: sessionsByTaskProvider,
        name: r'sessionsByTaskProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$sessionsByTaskHash,
        dependencies: SessionsByTaskFamily._dependencies,
        allTransitiveDependencies:
            SessionsByTaskFamily._allTransitiveDependencies,
        taskId: taskId,
      );

  SessionsByTaskProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.taskId,
  }) : super.internal();

  final String taskId;

  @override
  Override overrideWith(
    FutureOr<List<Session>> Function(SessionsByTaskRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: SessionsByTaskProvider._internal(
        (ref) => create(ref as SessionsByTaskRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        taskId: taskId,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<Session>> createElement() {
    return _SessionsByTaskProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is SessionsByTaskProvider && other.taskId == taskId;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, taskId.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin SessionsByTaskRef on AutoDisposeFutureProviderRef<List<Session>> {
  /// The parameter `taskId` of this provider.
  String get taskId;
}

class _SessionsByTaskProviderElement
    extends AutoDisposeFutureProviderElement<List<Session>>
    with SessionsByTaskRef {
  _SessionsByTaskProviderElement(super.provider);

  @override
  String get taskId => (origin as SessionsByTaskProvider).taskId;
}

String _$watchSessionsByTaskHash() =>
    r'36f816756f5ce15d82d1c5b1ae70d5d075d26e22';

/// Provider for a stream of sessions by task ID (reactive)
///
/// Copied from [watchSessionsByTask].
@ProviderFor(watchSessionsByTask)
const watchSessionsByTaskProvider = WatchSessionsByTaskFamily();

/// Provider for a stream of sessions by task ID (reactive)
///
/// Copied from [watchSessionsByTask].
class WatchSessionsByTaskFamily extends Family<AsyncValue<List<Session>>> {
  /// Provider for a stream of sessions by task ID (reactive)
  ///
  /// Copied from [watchSessionsByTask].
  const WatchSessionsByTaskFamily();

  /// Provider for a stream of sessions by task ID (reactive)
  ///
  /// Copied from [watchSessionsByTask].
  WatchSessionsByTaskProvider call(String taskId) {
    return WatchSessionsByTaskProvider(taskId);
  }

  @override
  WatchSessionsByTaskProvider getProviderOverride(
    covariant WatchSessionsByTaskProvider provider,
  ) {
    return call(provider.taskId);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'watchSessionsByTaskProvider';
}

/// Provider for a stream of sessions by task ID (reactive)
///
/// Copied from [watchSessionsByTask].
class WatchSessionsByTaskProvider
    extends AutoDisposeStreamProvider<List<Session>> {
  /// Provider for a stream of sessions by task ID (reactive)
  ///
  /// Copied from [watchSessionsByTask].
  WatchSessionsByTaskProvider(String taskId)
    : this._internal(
        (ref) => watchSessionsByTask(ref as WatchSessionsByTaskRef, taskId),
        from: watchSessionsByTaskProvider,
        name: r'watchSessionsByTaskProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$watchSessionsByTaskHash,
        dependencies: WatchSessionsByTaskFamily._dependencies,
        allTransitiveDependencies:
            WatchSessionsByTaskFamily._allTransitiveDependencies,
        taskId: taskId,
      );

  WatchSessionsByTaskProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.taskId,
  }) : super.internal();

  final String taskId;

  @override
  Override overrideWith(
    Stream<List<Session>> Function(WatchSessionsByTaskRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: WatchSessionsByTaskProvider._internal(
        (ref) => create(ref as WatchSessionsByTaskRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        taskId: taskId,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<List<Session>> createElement() {
    return _WatchSessionsByTaskProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is WatchSessionsByTaskProvider && other.taskId == taskId;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, taskId.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin WatchSessionsByTaskRef on AutoDisposeStreamProviderRef<List<Session>> {
  /// The parameter `taskId` of this provider.
  String get taskId;
}

class _WatchSessionsByTaskProviderElement
    extends AutoDisposeStreamProviderElement<List<Session>>
    with WatchSessionsByTaskRef {
  _WatchSessionsByTaskProviderElement(super.provider);

  @override
  String get taskId => (origin as WatchSessionsByTaskProvider).taskId;
}

String _$activeTasksHash() => r'bcbcad4a5b91dac506e3d8de698fac0ad3d24a1e';

/// Provider for active tasks (in progress or planned with time conditions)
///
/// Copied from [activeTasks].
@ProviderFor(activeTasks)
final activeTasksProvider = AutoDisposeFutureProvider<List<Task>>.internal(
  activeTasks,
  name: r'activeTasksProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$activeTasksHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef ActiveTasksRef = AutoDisposeFutureProviderRef<List<Task>>;
String _$watchActiveTasksHash() => r'ca8fd49031bf7fc9ad35c912cf496ccb8f5ae9f1';

/// Provider for a stream of active tasks (reactive)
///
/// Copied from [watchActiveTasks].
@ProviderFor(watchActiveTasks)
final watchActiveTasksProvider = AutoDisposeStreamProvider<List<Task>>.internal(
  watchActiveTasks,
  name: r'watchActiveTasksProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$watchActiveTasksHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef WatchActiveTasksRef = AutoDisposeStreamProviderRef<List<Task>>;
String _$completedTasksHash() => r'f707ca605f580a364d29b7b19cca26b01556e8f4';

/// Provider for completed tasks
///
/// Copied from [completedTasks].
@ProviderFor(completedTasks)
final completedTasksProvider = AutoDisposeFutureProvider<List<Task>>.internal(
  completedTasks,
  name: r'completedTasksProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$completedTasksHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef CompletedTasksRef = AutoDisposeFutureProviderRef<List<Task>>;
String _$watchCompletedTasksHash() =>
    r'39ad7f60dc6942a6482fa605db2f28eb2c20a4b7';

/// Provider for a stream of completed tasks (reactive)
///
/// Copied from [watchCompletedTasks].
@ProviderFor(watchCompletedTasks)
final watchCompletedTasksProvider =
    AutoDisposeStreamProvider<List<Task>>.internal(
      watchCompletedTasks,
      name: r'watchCompletedTasksProvider',
      debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
          ? null
          : _$watchCompletedTasksHash,
      dependencies: null,
      allTransitiveDependencies: null,
    );

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef WatchCompletedTasksRef = AutoDisposeStreamProviderRef<List<Task>>;
String _$sessionsByDayHash() => r'0510ff301610e56ea6f042ee8f8ea5cf9147451e';

/// Provider for today's sessions
///
/// Copied from [sessionsByDay].
@ProviderFor(sessionsByDay)
const sessionsByDayProvider = SessionsByDayFamily();

/// Provider for today's sessions
///
/// Copied from [sessionsByDay].
class SessionsByDayFamily extends Family<AsyncValue<List<Session>>> {
  /// Provider for today's sessions
  ///
  /// Copied from [sessionsByDay].
  const SessionsByDayFamily();

  /// Provider for today's sessions
  ///
  /// Copied from [sessionsByDay].
  SessionsByDayProvider call(DateTime? date) {
    return SessionsByDayProvider(date);
  }

  @override
  SessionsByDayProvider getProviderOverride(
    covariant SessionsByDayProvider provider,
  ) {
    return call(provider.date);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'sessionsByDayProvider';
}

/// Provider for today's sessions
///
/// Copied from [sessionsByDay].
class SessionsByDayProvider extends AutoDisposeFutureProvider<List<Session>> {
  /// Provider for today's sessions
  ///
  /// Copied from [sessionsByDay].
  SessionsByDayProvider(DateTime? date)
    : this._internal(
        (ref) => sessionsByDay(ref as SessionsByDayRef, date),
        from: sessionsByDayProvider,
        name: r'sessionsByDayProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$sessionsByDayHash,
        dependencies: SessionsByDayFamily._dependencies,
        allTransitiveDependencies:
            SessionsByDayFamily._allTransitiveDependencies,
        date: date,
      );

  SessionsByDayProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.date,
  }) : super.internal();

  final DateTime? date;

  @override
  Override overrideWith(
    FutureOr<List<Session>> Function(SessionsByDayRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: SessionsByDayProvider._internal(
        (ref) => create(ref as SessionsByDayRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        date: date,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<Session>> createElement() {
    return _SessionsByDayProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is SessionsByDayProvider && other.date == date;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, date.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin SessionsByDayRef on AutoDisposeFutureProviderRef<List<Session>> {
  /// The parameter `date` of this provider.
  DateTime? get date;
}

class _SessionsByDayProviderElement
    extends AutoDisposeFutureProviderElement<List<Session>>
    with SessionsByDayRef {
  _SessionsByDayProviderElement(super.provider);

  @override
  DateTime? get date => (origin as SessionsByDayProvider).date;
}

String _$watchSessionsByDayHash() =>
    r'f90b1c17d236e01d8af4191c2f34b8400cd28fd8';

/// See also [watchSessionsByDay].
@ProviderFor(watchSessionsByDay)
const watchSessionsByDayProvider = WatchSessionsByDayFamily();

/// See also [watchSessionsByDay].
class WatchSessionsByDayFamily extends Family<AsyncValue<List<Session>>> {
  /// See also [watchSessionsByDay].
  const WatchSessionsByDayFamily();

  /// See also [watchSessionsByDay].
  WatchSessionsByDayProvider call(DateTime? date) {
    return WatchSessionsByDayProvider(date);
  }

  @override
  WatchSessionsByDayProvider getProviderOverride(
    covariant WatchSessionsByDayProvider provider,
  ) {
    return call(provider.date);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'watchSessionsByDayProvider';
}

/// See also [watchSessionsByDay].
class WatchSessionsByDayProvider
    extends AutoDisposeStreamProvider<List<Session>> {
  /// See also [watchSessionsByDay].
  WatchSessionsByDayProvider(DateTime? date)
    : this._internal(
        (ref) => watchSessionsByDay(ref as WatchSessionsByDayRef, date),
        from: watchSessionsByDayProvider,
        name: r'watchSessionsByDayProvider',
        debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
            ? null
            : _$watchSessionsByDayHash,
        dependencies: WatchSessionsByDayFamily._dependencies,
        allTransitiveDependencies:
            WatchSessionsByDayFamily._allTransitiveDependencies,
        date: date,
      );

  WatchSessionsByDayProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.date,
  }) : super.internal();

  final DateTime? date;

  @override
  Override overrideWith(
    Stream<List<Session>> Function(WatchSessionsByDayRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: WatchSessionsByDayProvider._internal(
        (ref) => create(ref as WatchSessionsByDayRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        date: date,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<List<Session>> createElement() {
    return _WatchSessionsByDayProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is WatchSessionsByDayProvider && other.date == date;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, date.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin WatchSessionsByDayRef on AutoDisposeStreamProviderRef<List<Session>> {
  /// The parameter `date` of this provider.
  DateTime? get date;
}

class _WatchSessionsByDayProviderElement
    extends AutoDisposeStreamProviderElement<List<Session>>
    with WatchSessionsByDayRef {
  _WatchSessionsByDayProviderElement(super.provider);

  @override
  DateTime? get date => (origin as WatchSessionsByDayProvider).date;
}

String _$watchSessionsWithoutTasksHash() =>
    r'b3e052c0844bd4ece18ccacfa6723faab06c374f';

/// Provider for a stream of today's sessions without tasks (reactive)
///
/// Copied from [watchSessionsWithoutTasks].
@ProviderFor(watchSessionsWithoutTasks)
final watchSessionsWithoutTasksProvider =
    AutoDisposeStreamProvider<List<Session>>.internal(
      watchSessionsWithoutTasks,
      name: r'watchSessionsWithoutTasksProvider',
      debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
          ? null
          : _$watchSessionsWithoutTasksHash,
      dependencies: null,
      allTransitiveDependencies: null,
    );

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef WatchSessionsWithoutTasksRef =
    AutoDisposeStreamProviderRef<List<Session>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package

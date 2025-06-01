// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

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

String _$watchTasksHash() => r'18852d2be60dd919e112c0937abac51835f49657';

/// See also [watchTasks].
@ProviderFor(watchTasks)
const watchTasksProvider = WatchTasksFamily();

/// See also [watchTasks].
class WatchTasksFamily extends Family<AsyncValue<List<Task>>> {
  /// See also [watchTasks].
  const WatchTasksFamily();

  /// See also [watchTasks].
  WatchTasksProvider call({
    bool showCompleted = false,
    bool recentFirst = true,
    bool showRoutineGenerated = false,
  }) {
    return WatchTasksProvider(
      showCompleted: showCompleted,
      recentFirst: recentFirst,
      showRoutineGenerated: showRoutineGenerated,
    );
  }

  @override
  WatchTasksProvider getProviderOverride(
    covariant WatchTasksProvider provider,
  ) {
    return call(
      showCompleted: provider.showCompleted,
      recentFirst: provider.recentFirst,
      showRoutineGenerated: provider.showRoutineGenerated,
    );
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'watchTasksProvider';
}

/// See also [watchTasks].
class WatchTasksProvider extends AutoDisposeStreamProvider<List<Task>> {
  /// See also [watchTasks].
  WatchTasksProvider({
    bool showCompleted = false,
    bool recentFirst = true,
    bool showRoutineGenerated = false,
  }) : this._internal(
         (ref) => watchTasks(
           ref as WatchTasksRef,
           showCompleted: showCompleted,
           recentFirst: recentFirst,
           showRoutineGenerated: showRoutineGenerated,
         ),
         from: watchTasksProvider,
         name: r'watchTasksProvider',
         debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
             ? null
             : _$watchTasksHash,
         dependencies: WatchTasksFamily._dependencies,
         allTransitiveDependencies: WatchTasksFamily._allTransitiveDependencies,
         showCompleted: showCompleted,
         recentFirst: recentFirst,
         showRoutineGenerated: showRoutineGenerated,
       );

  WatchTasksProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.showCompleted,
    required this.recentFirst,
    required this.showRoutineGenerated,
  }) : super.internal();

  final bool showCompleted;
  final bool recentFirst;
  final bool showRoutineGenerated;

  @override
  Override overrideWith(
    Stream<List<Task>> Function(WatchTasksRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: WatchTasksProvider._internal(
        (ref) => create(ref as WatchTasksRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        showCompleted: showCompleted,
        recentFirst: recentFirst,
        showRoutineGenerated: showRoutineGenerated,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<List<Task>> createElement() {
    return _WatchTasksProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is WatchTasksProvider &&
        other.showCompleted == showCompleted &&
        other.recentFirst == recentFirst &&
        other.showRoutineGenerated == showRoutineGenerated;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, showCompleted.hashCode);
    hash = _SystemHash.combine(hash, recentFirst.hashCode);
    hash = _SystemHash.combine(hash, showRoutineGenerated.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin WatchTasksRef on AutoDisposeStreamProviderRef<List<Task>> {
  /// The parameter `showCompleted` of this provider.
  bool get showCompleted;

  /// The parameter `recentFirst` of this provider.
  bool get recentFirst;

  /// The parameter `showRoutineGenerated` of this provider.
  bool get showRoutineGenerated;
}

class _WatchTasksProviderElement
    extends AutoDisposeStreamProviderElement<List<Task>>
    with WatchTasksRef {
  _WatchTasksProviderElement(super.provider);

  @override
  bool get showCompleted => (origin as WatchTasksProvider).showCompleted;
  @override
  bool get recentFirst => (origin as WatchTasksProvider).recentFirst;
  @override
  bool get showRoutineGenerated =>
      (origin as WatchTasksProvider).showRoutineGenerated;
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
String _$watchActiveTasksHash() => r'ab922142d1dd37de1cd573091b1a8859328d0784';

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
    r'f058286b5e434aac162309128609af740347db65';

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
String _$completedTasksWithTodaysSessionsHash() =>
    r'c15c8759551d893a0832023a7cd2ce59932af67a';

/// See also [completedTasksWithTodaysSessions].
@ProviderFor(completedTasksWithTodaysSessions)
final completedTasksWithTodaysSessionsProvider =
    AutoDisposeStreamProvider<List<Task>>.internal(
      completedTasksWithTodaysSessions,
      name: r'completedTasksWithTodaysSessionsProvider',
      debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
          ? null
          : _$completedTasksWithTodaysSessionsHash,
      dependencies: null,
      allTransitiveDependencies: null,
    );

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef CompletedTasksWithTodaysSessionsRef =
    AutoDisposeStreamProviderRef<List<Task>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package

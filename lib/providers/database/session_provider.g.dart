// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'session_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

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

String _$sessionsByDayHash() => r'0510ff301610e56ea6f042ee8f8ea5cf9147451e';

/// Provider for sessions by day
///
/// Copied from [sessionsByDay].
@ProviderFor(sessionsByDay)
const sessionsByDayProvider = SessionsByDayFamily();

/// Provider for sessions by day
///
/// Copied from [sessionsByDay].
class SessionsByDayFamily extends Family<AsyncValue<List<Session>>> {
  /// Provider for sessions by day
  ///
  /// Copied from [sessionsByDay].
  const SessionsByDayFamily();

  /// Provider for sessions by day
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

/// Provider for sessions by day
///
/// Copied from [sessionsByDay].
class SessionsByDayProvider extends AutoDisposeFutureProvider<List<Session>> {
  /// Provider for sessions by day
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
    r'68d6896037617d4f5b1c1dd4b861e8e00fbd5df0';

/// Provider for a stream of sessions by day (reactive)
///
/// Copied from [watchSessionsByDay].
@ProviderFor(watchSessionsByDay)
const watchSessionsByDayProvider = WatchSessionsByDayFamily();

/// Provider for a stream of sessions by day (reactive)
///
/// Copied from [watchSessionsByDay].
class WatchSessionsByDayFamily extends Family<AsyncValue<List<Session>>> {
  /// Provider for a stream of sessions by day (reactive)
  ///
  /// Copied from [watchSessionsByDay].
  const WatchSessionsByDayFamily();

  /// Provider for a stream of sessions by day (reactive)
  ///
  /// Copied from [watchSessionsByDay].
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

/// Provider for a stream of sessions by day (reactive)
///
/// Copied from [watchSessionsByDay].
class WatchSessionsByDayProvider
    extends AutoDisposeStreamProvider<List<Session>> {
  /// Provider for a stream of sessions by day (reactive)
  ///
  /// Copied from [watchSessionsByDay].
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

/// Provider for a stream of sessions without tasks (reactive)
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

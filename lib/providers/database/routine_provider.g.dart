// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'routine_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$allRoutinesHash() => r'624f90f591d367af67d0aae1e0bf83ac2013ac10';

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
String _$watchAllRoutinesHash() => r'bd60a68c8d7ab744c36033ca0fa510f71398f300';

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
String _$routineByIdHash() => r'edfc4b115377565ba68fbe1d7c38425cd6a4c5f0';

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

// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package

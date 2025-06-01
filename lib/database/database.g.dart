// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'database.dart';

// ignore_for_file: type=lint
class $RoutinesTable extends Routines
    with drift.TableInfo<$RoutinesTable, Routine> {
  @override
  final drift.GeneratedDatabase attachedDatabase;
  final String? _alias;
  $RoutinesTable(this.attachedDatabase, [this._alias]);
  static const drift.VerificationMeta _idMeta = const drift.VerificationMeta(
    'id',
  );
  @override
  late final drift.GeneratedColumn<String> id = drift.GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    clientDefault: () => const Uuid().v4(),
  );
  static const drift.VerificationMeta _titleMeta = const drift.VerificationMeta(
    'title',
  );
  @override
  late final drift.GeneratedColumn<String> title =
      drift.GeneratedColumn<String>(
        'title',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: true,
      );
  static const drift.VerificationMeta _recurrenceRuleMeta =
      const drift.VerificationMeta('recurrenceRule');
  @override
  late final drift.GeneratedColumn<String> recurrenceRule =
      drift.GeneratedColumn<String>(
        'recurrence_rule',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: true,
      );
  static const drift.VerificationMeta _estimatedDurationMeta =
      const drift.VerificationMeta('estimatedDuration');
  @override
  late final drift.GeneratedColumn<String> estimatedDuration =
      drift.GeneratedColumn<String>(
        'estimated_duration',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _startTimeMeta =
      const drift.VerificationMeta('startTime');
  @override
  late final drift.GeneratedColumn<String> startTime =
      drift.GeneratedColumn<String>(
        'start_time',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _dueTimeMeta =
      const drift.VerificationMeta('dueTime');
  @override
  late final drift.GeneratedColumn<String> dueTime =
      drift.GeneratedColumn<String>(
        'due_time',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _noteMeta = const drift.VerificationMeta(
    'note',
  );
  @override
  late final drift.GeneratedColumn<String> note = drift.GeneratedColumn<String>(
    'note',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const drift.VerificationMeta _createdAtMeta =
      const drift.VerificationMeta('createdAt');
  @override
  late final drift.GeneratedColumn<String> createdAt =
      drift.GeneratedColumn<String>(
        'created_at',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
        clientDefault: () => DateTime.now().toIso8601String(),
      );
  static const drift.VerificationMeta _updatedAtMeta =
      const drift.VerificationMeta('updatedAt');
  @override
  late final drift.GeneratedColumn<String> updatedAt =
      drift.GeneratedColumn<String>(
        'updated_at',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
        clientDefault: () => DateTime.now().toIso8601String(),
      );
  @override
  List<drift.GeneratedColumn> get $columns => [
    id,
    title,
    recurrenceRule,
    estimatedDuration,
    startTime,
    dueTime,
    note,
    createdAt,
    updatedAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'routines';
  @override
  drift.VerificationContext validateIntegrity(
    drift.Insertable<Routine> instance, {
    bool isInserting = false,
  }) {
    final context = drift.VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('recurrence_rule')) {
      context.handle(
        _recurrenceRuleMeta,
        recurrenceRule.isAcceptableOrUnknown(
          data['recurrence_rule']!,
          _recurrenceRuleMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_recurrenceRuleMeta);
    }
    if (data.containsKey('estimated_duration')) {
      context.handle(
        _estimatedDurationMeta,
        estimatedDuration.isAcceptableOrUnknown(
          data['estimated_duration']!,
          _estimatedDurationMeta,
        ),
      );
    }
    if (data.containsKey('start_time')) {
      context.handle(
        _startTimeMeta,
        startTime.isAcceptableOrUnknown(data['start_time']!, _startTimeMeta),
      );
    }
    if (data.containsKey('due_time')) {
      context.handle(
        _dueTimeMeta,
        dueTime.isAcceptableOrUnknown(data['due_time']!, _dueTimeMeta),
      );
    }
    if (data.containsKey('note')) {
      context.handle(
        _noteMeta,
        note.isAcceptableOrUnknown(data['note']!, _noteMeta),
      );
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    }
    if (data.containsKey('updated_at')) {
      context.handle(
        _updatedAtMeta,
        updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta),
      );
    }
    return context;
  }

  @override
  Set<drift.GeneratedColumn> get $primaryKey => {id};
  @override
  Routine map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Routine(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      recurrenceRule: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}recurrence_rule'],
      )!,
      estimatedDuration: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}estimated_duration'],
      ),
      startTime: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}start_time'],
      ),
      dueTime: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}due_time'],
      ),
      note: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}note'],
      ),
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}created_at'],
      )!,
      updatedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}updated_at'],
      )!,
    );
  }

  @override
  $RoutinesTable createAlias(String alias) {
    return $RoutinesTable(attachedDatabase, alias);
  }
}

class Routine extends drift.DataClass implements drift.Insertable<Routine> {
  final String id;
  final String title;
  final String recurrenceRule;
  final String? estimatedDuration;
  final String? startTime;
  final String? dueTime;
  final String? note;
  final String createdAt;
  final String updatedAt;
  const Routine({
    required this.id,
    required this.title,
    required this.recurrenceRule,
    this.estimatedDuration,
    this.startTime,
    this.dueTime,
    this.note,
    required this.createdAt,
    required this.updatedAt,
  });
  @override
  Map<String, drift.Expression> toColumns(bool nullToAbsent) {
    final map = <String, drift.Expression>{};
    map['id'] = drift.Variable<String>(id);
    map['title'] = drift.Variable<String>(title);
    map['recurrence_rule'] = drift.Variable<String>(recurrenceRule);
    if (!nullToAbsent || estimatedDuration != null) {
      map['estimated_duration'] = drift.Variable<String>(estimatedDuration);
    }
    if (!nullToAbsent || startTime != null) {
      map['start_time'] = drift.Variable<String>(startTime);
    }
    if (!nullToAbsent || dueTime != null) {
      map['due_time'] = drift.Variable<String>(dueTime);
    }
    if (!nullToAbsent || note != null) {
      map['note'] = drift.Variable<String>(note);
    }
    map['created_at'] = drift.Variable<String>(createdAt);
    map['updated_at'] = drift.Variable<String>(updatedAt);
    return map;
  }

  RoutinesCompanion toCompanion(bool nullToAbsent) {
    return RoutinesCompanion(
      id: drift.Value(id),
      title: drift.Value(title),
      recurrenceRule: drift.Value(recurrenceRule),
      estimatedDuration: estimatedDuration == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(estimatedDuration),
      startTime: startTime == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(startTime),
      dueTime: dueTime == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(dueTime),
      note: note == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(note),
      createdAt: drift.Value(createdAt),
      updatedAt: drift.Value(updatedAt),
    );
  }

  factory Routine.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= drift.driftRuntimeOptions.defaultSerializer;
    return Routine(
      id: serializer.fromJson<String>(json['id']),
      title: serializer.fromJson<String>(json['title']),
      recurrenceRule: serializer.fromJson<String>(json['recurrenceRule']),
      estimatedDuration: serializer.fromJson<String?>(
        json['estimatedDuration'],
      ),
      startTime: serializer.fromJson<String?>(json['startTime']),
      dueTime: serializer.fromJson<String?>(json['dueTime']),
      note: serializer.fromJson<String?>(json['note']),
      createdAt: serializer.fromJson<String>(json['createdAt']),
      updatedAt: serializer.fromJson<String>(json['updatedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= drift.driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'title': serializer.toJson<String>(title),
      'recurrenceRule': serializer.toJson<String>(recurrenceRule),
      'estimatedDuration': serializer.toJson<String?>(estimatedDuration),
      'startTime': serializer.toJson<String?>(startTime),
      'dueTime': serializer.toJson<String?>(dueTime),
      'note': serializer.toJson<String?>(note),
      'createdAt': serializer.toJson<String>(createdAt),
      'updatedAt': serializer.toJson<String>(updatedAt),
    };
  }

  Routine copyWith({
    String? id,
    String? title,
    String? recurrenceRule,
    drift.Value<String?> estimatedDuration = const drift.Value.absent(),
    drift.Value<String?> startTime = const drift.Value.absent(),
    drift.Value<String?> dueTime = const drift.Value.absent(),
    drift.Value<String?> note = const drift.Value.absent(),
    String? createdAt,
    String? updatedAt,
  }) => Routine(
    id: id ?? this.id,
    title: title ?? this.title,
    recurrenceRule: recurrenceRule ?? this.recurrenceRule,
    estimatedDuration: estimatedDuration.present
        ? estimatedDuration.value
        : this.estimatedDuration,
    startTime: startTime.present ? startTime.value : this.startTime,
    dueTime: dueTime.present ? dueTime.value : this.dueTime,
    note: note.present ? note.value : this.note,
    createdAt: createdAt ?? this.createdAt,
    updatedAt: updatedAt ?? this.updatedAt,
  );
  Routine copyWithCompanion(RoutinesCompanion data) {
    return Routine(
      id: data.id.present ? data.id.value : this.id,
      title: data.title.present ? data.title.value : this.title,
      recurrenceRule: data.recurrenceRule.present
          ? data.recurrenceRule.value
          : this.recurrenceRule,
      estimatedDuration: data.estimatedDuration.present
          ? data.estimatedDuration.value
          : this.estimatedDuration,
      startTime: data.startTime.present ? data.startTime.value : this.startTime,
      dueTime: data.dueTime.present ? data.dueTime.value : this.dueTime,
      note: data.note.present ? data.note.value : this.note,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('Routine(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('recurrenceRule: $recurrenceRule, ')
          ..write('estimatedDuration: $estimatedDuration, ')
          ..write('startTime: $startTime, ')
          ..write('dueTime: $dueTime, ')
          ..write('note: $note, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    title,
    recurrenceRule,
    estimatedDuration,
    startTime,
    dueTime,
    note,
    createdAt,
    updatedAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is Routine &&
          other.id == this.id &&
          other.title == this.title &&
          other.recurrenceRule == this.recurrenceRule &&
          other.estimatedDuration == this.estimatedDuration &&
          other.startTime == this.startTime &&
          other.dueTime == this.dueTime &&
          other.note == this.note &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt);
}

class RoutinesCompanion extends drift.UpdateCompanion<Routine> {
  final drift.Value<String> id;
  final drift.Value<String> title;
  final drift.Value<String> recurrenceRule;
  final drift.Value<String?> estimatedDuration;
  final drift.Value<String?> startTime;
  final drift.Value<String?> dueTime;
  final drift.Value<String?> note;
  final drift.Value<String> createdAt;
  final drift.Value<String> updatedAt;
  final drift.Value<int> rowid;
  const RoutinesCompanion({
    this.id = const drift.Value.absent(),
    this.title = const drift.Value.absent(),
    this.recurrenceRule = const drift.Value.absent(),
    this.estimatedDuration = const drift.Value.absent(),
    this.startTime = const drift.Value.absent(),
    this.dueTime = const drift.Value.absent(),
    this.note = const drift.Value.absent(),
    this.createdAt = const drift.Value.absent(),
    this.updatedAt = const drift.Value.absent(),
    this.rowid = const drift.Value.absent(),
  });
  RoutinesCompanion.insert({
    this.id = const drift.Value.absent(),
    required String title,
    required String recurrenceRule,
    this.estimatedDuration = const drift.Value.absent(),
    this.startTime = const drift.Value.absent(),
    this.dueTime = const drift.Value.absent(),
    this.note = const drift.Value.absent(),
    this.createdAt = const drift.Value.absent(),
    this.updatedAt = const drift.Value.absent(),
    this.rowid = const drift.Value.absent(),
  }) : title = drift.Value(title),
       recurrenceRule = drift.Value(recurrenceRule);
  static drift.Insertable<Routine> custom({
    drift.Expression<String>? id,
    drift.Expression<String>? title,
    drift.Expression<String>? recurrenceRule,
    drift.Expression<String>? estimatedDuration,
    drift.Expression<String>? startTime,
    drift.Expression<String>? dueTime,
    drift.Expression<String>? note,
    drift.Expression<String>? createdAt,
    drift.Expression<String>? updatedAt,
    drift.Expression<int>? rowid,
  }) {
    return drift.RawValuesInsertable({
      if (id != null) 'id': id,
      if (title != null) 'title': title,
      if (recurrenceRule != null) 'recurrence_rule': recurrenceRule,
      if (estimatedDuration != null) 'estimated_duration': estimatedDuration,
      if (startTime != null) 'start_time': startTime,
      if (dueTime != null) 'due_time': dueTime,
      if (note != null) 'note': note,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  RoutinesCompanion copyWith({
    drift.Value<String>? id,
    drift.Value<String>? title,
    drift.Value<String>? recurrenceRule,
    drift.Value<String?>? estimatedDuration,
    drift.Value<String?>? startTime,
    drift.Value<String?>? dueTime,
    drift.Value<String?>? note,
    drift.Value<String>? createdAt,
    drift.Value<String>? updatedAt,
    drift.Value<int>? rowid,
  }) {
    return RoutinesCompanion(
      id: id ?? this.id,
      title: title ?? this.title,
      recurrenceRule: recurrenceRule ?? this.recurrenceRule,
      estimatedDuration: estimatedDuration ?? this.estimatedDuration,
      startTime: startTime ?? this.startTime,
      dueTime: dueTime ?? this.dueTime,
      note: note ?? this.note,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, drift.Expression> toColumns(bool nullToAbsent) {
    final map = <String, drift.Expression>{};
    if (id.present) {
      map['id'] = drift.Variable<String>(id.value);
    }
    if (title.present) {
      map['title'] = drift.Variable<String>(title.value);
    }
    if (recurrenceRule.present) {
      map['recurrence_rule'] = drift.Variable<String>(recurrenceRule.value);
    }
    if (estimatedDuration.present) {
      map['estimated_duration'] = drift.Variable<String>(
        estimatedDuration.value,
      );
    }
    if (startTime.present) {
      map['start_time'] = drift.Variable<String>(startTime.value);
    }
    if (dueTime.present) {
      map['due_time'] = drift.Variable<String>(dueTime.value);
    }
    if (note.present) {
      map['note'] = drift.Variable<String>(note.value);
    }
    if (createdAt.present) {
      map['created_at'] = drift.Variable<String>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = drift.Variable<String>(updatedAt.value);
    }
    if (rowid.present) {
      map['rowid'] = drift.Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('RoutinesCompanion(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('recurrenceRule: $recurrenceRule, ')
          ..write('estimatedDuration: $estimatedDuration, ')
          ..write('startTime: $startTime, ')
          ..write('dueTime: $dueTime, ')
          ..write('note: $note, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $TasksTable extends Tasks with drift.TableInfo<$TasksTable, Task> {
  @override
  final drift.GeneratedDatabase attachedDatabase;
  final String? _alias;
  $TasksTable(this.attachedDatabase, [this._alias]);
  static const drift.VerificationMeta _idMeta = const drift.VerificationMeta(
    'id',
  );
  @override
  late final drift.GeneratedColumn<String> id = drift.GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    clientDefault: () => const Uuid().v4(),
  );
  static const drift.VerificationMeta _routineIdMeta =
      const drift.VerificationMeta('routineId');
  @override
  late final drift.GeneratedColumn<String> routineId =
      drift.GeneratedColumn<String>(
        'routine_id',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
        defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES routines (id)',
        ),
      );
  static const drift.VerificationMeta _titleMeta = const drift.VerificationMeta(
    'title',
  );
  @override
  late final drift.GeneratedColumn<String> title =
      drift.GeneratedColumn<String>(
        'title',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: true,
      );
  static const drift.VerificationMeta _statusMeta =
      const drift.VerificationMeta('status');
  @override
  late final drift.GeneratedColumn<String> status =
      drift.GeneratedColumn<String>(
        'status',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
        defaultValue: const drift.Constant('inbox'),
      );
  static const drift.VerificationMeta _estimatedDurationMeta =
      const drift.VerificationMeta('estimatedDuration');
  @override
  late final drift.GeneratedColumn<String> estimatedDuration =
      drift.GeneratedColumn<String>(
        'estimated_duration',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _startTimeMeta =
      const drift.VerificationMeta('startTime');
  @override
  late final drift.GeneratedColumn<String> startTime =
      drift.GeneratedColumn<String>(
        'start_time',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _dueTimeMeta =
      const drift.VerificationMeta('dueTime');
  @override
  late final drift.GeneratedColumn<String> dueTime =
      drift.GeneratedColumn<String>(
        'due_time',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _noteMeta = const drift.VerificationMeta(
    'note',
  );
  @override
  late final drift.GeneratedColumn<String> note = drift.GeneratedColumn<String>(
    'note',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const drift.VerificationMeta _createdAtMeta =
      const drift.VerificationMeta('createdAt');
  @override
  late final drift.GeneratedColumn<String> createdAt =
      drift.GeneratedColumn<String>(
        'created_at',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
        clientDefault: () => DateTime.now().toIso8601String(),
      );
  static const drift.VerificationMeta _updatedAtMeta =
      const drift.VerificationMeta('updatedAt');
  @override
  late final drift.GeneratedColumn<String> updatedAt =
      drift.GeneratedColumn<String>(
        'updated_at',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
        clientDefault: () => DateTime.now().toIso8601String(),
      );
  @override
  List<drift.GeneratedColumn> get $columns => [
    id,
    routineId,
    title,
    status,
    estimatedDuration,
    startTime,
    dueTime,
    note,
    createdAt,
    updatedAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'tasks';
  @override
  drift.VerificationContext validateIntegrity(
    drift.Insertable<Task> instance, {
    bool isInserting = false,
  }) {
    final context = drift.VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('routine_id')) {
      context.handle(
        _routineIdMeta,
        routineId.isAcceptableOrUnknown(data['routine_id']!, _routineIdMeta),
      );
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('status')) {
      context.handle(
        _statusMeta,
        status.isAcceptableOrUnknown(data['status']!, _statusMeta),
      );
    }
    if (data.containsKey('estimated_duration')) {
      context.handle(
        _estimatedDurationMeta,
        estimatedDuration.isAcceptableOrUnknown(
          data['estimated_duration']!,
          _estimatedDurationMeta,
        ),
      );
    }
    if (data.containsKey('start_time')) {
      context.handle(
        _startTimeMeta,
        startTime.isAcceptableOrUnknown(data['start_time']!, _startTimeMeta),
      );
    }
    if (data.containsKey('due_time')) {
      context.handle(
        _dueTimeMeta,
        dueTime.isAcceptableOrUnknown(data['due_time']!, _dueTimeMeta),
      );
    }
    if (data.containsKey('note')) {
      context.handle(
        _noteMeta,
        note.isAcceptableOrUnknown(data['note']!, _noteMeta),
      );
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    }
    if (data.containsKey('updated_at')) {
      context.handle(
        _updatedAtMeta,
        updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta),
      );
    }
    return context;
  }

  @override
  Set<drift.GeneratedColumn> get $primaryKey => {id};
  @override
  Task map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Task(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      routineId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}routine_id'],
      ),
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      status: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}status'],
      )!,
      estimatedDuration: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}estimated_duration'],
      ),
      startTime: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}start_time'],
      ),
      dueTime: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}due_time'],
      ),
      note: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}note'],
      ),
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}created_at'],
      )!,
      updatedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}updated_at'],
      )!,
    );
  }

  @override
  $TasksTable createAlias(String alias) {
    return $TasksTable(attachedDatabase, alias);
  }
}

class Task extends drift.DataClass implements drift.Insertable<Task> {
  final String id;
  final String? routineId;
  final String title;
  final String status;
  final String? estimatedDuration;
  final String? startTime;
  final String? dueTime;
  final String? note;
  final String createdAt;
  final String updatedAt;
  const Task({
    required this.id,
    this.routineId,
    required this.title,
    required this.status,
    this.estimatedDuration,
    this.startTime,
    this.dueTime,
    this.note,
    required this.createdAt,
    required this.updatedAt,
  });
  @override
  Map<String, drift.Expression> toColumns(bool nullToAbsent) {
    final map = <String, drift.Expression>{};
    map['id'] = drift.Variable<String>(id);
    if (!nullToAbsent || routineId != null) {
      map['routine_id'] = drift.Variable<String>(routineId);
    }
    map['title'] = drift.Variable<String>(title);
    map['status'] = drift.Variable<String>(status);
    if (!nullToAbsent || estimatedDuration != null) {
      map['estimated_duration'] = drift.Variable<String>(estimatedDuration);
    }
    if (!nullToAbsent || startTime != null) {
      map['start_time'] = drift.Variable<String>(startTime);
    }
    if (!nullToAbsent || dueTime != null) {
      map['due_time'] = drift.Variable<String>(dueTime);
    }
    if (!nullToAbsent || note != null) {
      map['note'] = drift.Variable<String>(note);
    }
    map['created_at'] = drift.Variable<String>(createdAt);
    map['updated_at'] = drift.Variable<String>(updatedAt);
    return map;
  }

  TasksCompanion toCompanion(bool nullToAbsent) {
    return TasksCompanion(
      id: drift.Value(id),
      routineId: routineId == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(routineId),
      title: drift.Value(title),
      status: drift.Value(status),
      estimatedDuration: estimatedDuration == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(estimatedDuration),
      startTime: startTime == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(startTime),
      dueTime: dueTime == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(dueTime),
      note: note == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(note),
      createdAt: drift.Value(createdAt),
      updatedAt: drift.Value(updatedAt),
    );
  }

  factory Task.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= drift.driftRuntimeOptions.defaultSerializer;
    return Task(
      id: serializer.fromJson<String>(json['id']),
      routineId: serializer.fromJson<String?>(json['routineId']),
      title: serializer.fromJson<String>(json['title']),
      status: serializer.fromJson<String>(json['status']),
      estimatedDuration: serializer.fromJson<String?>(
        json['estimatedDuration'],
      ),
      startTime: serializer.fromJson<String?>(json['startTime']),
      dueTime: serializer.fromJson<String?>(json['dueTime']),
      note: serializer.fromJson<String?>(json['note']),
      createdAt: serializer.fromJson<String>(json['createdAt']),
      updatedAt: serializer.fromJson<String>(json['updatedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= drift.driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'routineId': serializer.toJson<String?>(routineId),
      'title': serializer.toJson<String>(title),
      'status': serializer.toJson<String>(status),
      'estimatedDuration': serializer.toJson<String?>(estimatedDuration),
      'startTime': serializer.toJson<String?>(startTime),
      'dueTime': serializer.toJson<String?>(dueTime),
      'note': serializer.toJson<String?>(note),
      'createdAt': serializer.toJson<String>(createdAt),
      'updatedAt': serializer.toJson<String>(updatedAt),
    };
  }

  Task copyWith({
    String? id,
    drift.Value<String?> routineId = const drift.Value.absent(),
    String? title,
    String? status,
    drift.Value<String?> estimatedDuration = const drift.Value.absent(),
    drift.Value<String?> startTime = const drift.Value.absent(),
    drift.Value<String?> dueTime = const drift.Value.absent(),
    drift.Value<String?> note = const drift.Value.absent(),
    String? createdAt,
    String? updatedAt,
  }) => Task(
    id: id ?? this.id,
    routineId: routineId.present ? routineId.value : this.routineId,
    title: title ?? this.title,
    status: status ?? this.status,
    estimatedDuration: estimatedDuration.present
        ? estimatedDuration.value
        : this.estimatedDuration,
    startTime: startTime.present ? startTime.value : this.startTime,
    dueTime: dueTime.present ? dueTime.value : this.dueTime,
    note: note.present ? note.value : this.note,
    createdAt: createdAt ?? this.createdAt,
    updatedAt: updatedAt ?? this.updatedAt,
  );
  Task copyWithCompanion(TasksCompanion data) {
    return Task(
      id: data.id.present ? data.id.value : this.id,
      routineId: data.routineId.present ? data.routineId.value : this.routineId,
      title: data.title.present ? data.title.value : this.title,
      status: data.status.present ? data.status.value : this.status,
      estimatedDuration: data.estimatedDuration.present
          ? data.estimatedDuration.value
          : this.estimatedDuration,
      startTime: data.startTime.present ? data.startTime.value : this.startTime,
      dueTime: data.dueTime.present ? data.dueTime.value : this.dueTime,
      note: data.note.present ? data.note.value : this.note,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('Task(')
          ..write('id: $id, ')
          ..write('routineId: $routineId, ')
          ..write('title: $title, ')
          ..write('status: $status, ')
          ..write('estimatedDuration: $estimatedDuration, ')
          ..write('startTime: $startTime, ')
          ..write('dueTime: $dueTime, ')
          ..write('note: $note, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    routineId,
    title,
    status,
    estimatedDuration,
    startTime,
    dueTime,
    note,
    createdAt,
    updatedAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is Task &&
          other.id == this.id &&
          other.routineId == this.routineId &&
          other.title == this.title &&
          other.status == this.status &&
          other.estimatedDuration == this.estimatedDuration &&
          other.startTime == this.startTime &&
          other.dueTime == this.dueTime &&
          other.note == this.note &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt);
}

class TasksCompanion extends drift.UpdateCompanion<Task> {
  final drift.Value<String> id;
  final drift.Value<String?> routineId;
  final drift.Value<String> title;
  final drift.Value<String> status;
  final drift.Value<String?> estimatedDuration;
  final drift.Value<String?> startTime;
  final drift.Value<String?> dueTime;
  final drift.Value<String?> note;
  final drift.Value<String> createdAt;
  final drift.Value<String> updatedAt;
  final drift.Value<int> rowid;
  const TasksCompanion({
    this.id = const drift.Value.absent(),
    this.routineId = const drift.Value.absent(),
    this.title = const drift.Value.absent(),
    this.status = const drift.Value.absent(),
    this.estimatedDuration = const drift.Value.absent(),
    this.startTime = const drift.Value.absent(),
    this.dueTime = const drift.Value.absent(),
    this.note = const drift.Value.absent(),
    this.createdAt = const drift.Value.absent(),
    this.updatedAt = const drift.Value.absent(),
    this.rowid = const drift.Value.absent(),
  });
  TasksCompanion.insert({
    this.id = const drift.Value.absent(),
    this.routineId = const drift.Value.absent(),
    required String title,
    this.status = const drift.Value.absent(),
    this.estimatedDuration = const drift.Value.absent(),
    this.startTime = const drift.Value.absent(),
    this.dueTime = const drift.Value.absent(),
    this.note = const drift.Value.absent(),
    this.createdAt = const drift.Value.absent(),
    this.updatedAt = const drift.Value.absent(),
    this.rowid = const drift.Value.absent(),
  }) : title = drift.Value(title);
  static drift.Insertable<Task> custom({
    drift.Expression<String>? id,
    drift.Expression<String>? routineId,
    drift.Expression<String>? title,
    drift.Expression<String>? status,
    drift.Expression<String>? estimatedDuration,
    drift.Expression<String>? startTime,
    drift.Expression<String>? dueTime,
    drift.Expression<String>? note,
    drift.Expression<String>? createdAt,
    drift.Expression<String>? updatedAt,
    drift.Expression<int>? rowid,
  }) {
    return drift.RawValuesInsertable({
      if (id != null) 'id': id,
      if (routineId != null) 'routine_id': routineId,
      if (title != null) 'title': title,
      if (status != null) 'status': status,
      if (estimatedDuration != null) 'estimated_duration': estimatedDuration,
      if (startTime != null) 'start_time': startTime,
      if (dueTime != null) 'due_time': dueTime,
      if (note != null) 'note': note,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  TasksCompanion copyWith({
    drift.Value<String>? id,
    drift.Value<String?>? routineId,
    drift.Value<String>? title,
    drift.Value<String>? status,
    drift.Value<String?>? estimatedDuration,
    drift.Value<String?>? startTime,
    drift.Value<String?>? dueTime,
    drift.Value<String?>? note,
    drift.Value<String>? createdAt,
    drift.Value<String>? updatedAt,
    drift.Value<int>? rowid,
  }) {
    return TasksCompanion(
      id: id ?? this.id,
      routineId: routineId ?? this.routineId,
      title: title ?? this.title,
      status: status ?? this.status,
      estimatedDuration: estimatedDuration ?? this.estimatedDuration,
      startTime: startTime ?? this.startTime,
      dueTime: dueTime ?? this.dueTime,
      note: note ?? this.note,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, drift.Expression> toColumns(bool nullToAbsent) {
    final map = <String, drift.Expression>{};
    if (id.present) {
      map['id'] = drift.Variable<String>(id.value);
    }
    if (routineId.present) {
      map['routine_id'] = drift.Variable<String>(routineId.value);
    }
    if (title.present) {
      map['title'] = drift.Variable<String>(title.value);
    }
    if (status.present) {
      map['status'] = drift.Variable<String>(status.value);
    }
    if (estimatedDuration.present) {
      map['estimated_duration'] = drift.Variable<String>(
        estimatedDuration.value,
      );
    }
    if (startTime.present) {
      map['start_time'] = drift.Variable<String>(startTime.value);
    }
    if (dueTime.present) {
      map['due_time'] = drift.Variable<String>(dueTime.value);
    }
    if (note.present) {
      map['note'] = drift.Variable<String>(note.value);
    }
    if (createdAt.present) {
      map['created_at'] = drift.Variable<String>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = drift.Variable<String>(updatedAt.value);
    }
    if (rowid.present) {
      map['rowid'] = drift.Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('TasksCompanion(')
          ..write('id: $id, ')
          ..write('routineId: $routineId, ')
          ..write('title: $title, ')
          ..write('status: $status, ')
          ..write('estimatedDuration: $estimatedDuration, ')
          ..write('startTime: $startTime, ')
          ..write('dueTime: $dueTime, ')
          ..write('note: $note, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $SessionsTable extends Sessions
    with drift.TableInfo<$SessionsTable, Session> {
  @override
  final drift.GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SessionsTable(this.attachedDatabase, [this._alias]);
  static const drift.VerificationMeta _idMeta = const drift.VerificationMeta(
    'id',
  );
  @override
  late final drift.GeneratedColumn<String> id = drift.GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    clientDefault: () => const Uuid().v4(),
  );
  static const drift.VerificationMeta _taskIdMeta =
      const drift.VerificationMeta('taskId');
  @override
  late final drift.GeneratedColumn<String> taskId =
      drift.GeneratedColumn<String>(
        'task_id',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
        defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES tasks (id)',
        ),
      );
  static const drift.VerificationMeta _titleMeta = const drift.VerificationMeta(
    'title',
  );
  @override
  late final drift.GeneratedColumn<String> title =
      drift.GeneratedColumn<String>(
        'title',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _startTimeMeta =
      const drift.VerificationMeta('startTime');
  @override
  late final drift.GeneratedColumn<String> startTime =
      drift.GeneratedColumn<String>(
        'start_time',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _endTimeMeta =
      const drift.VerificationMeta('endTime');
  @override
  late final drift.GeneratedColumn<String> endTime =
      drift.GeneratedColumn<String>(
        'end_time',
        aliasedName,
        true,
        type: DriftSqlType.string,
        requiredDuringInsert: false,
      );
  static const drift.VerificationMeta _durationMeta =
      const drift.VerificationMeta('duration');
  @override
  late final drift.GeneratedColumn<String> duration =
      drift.GeneratedColumn<String>(
        'duration',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: true,
      );
  static const drift.VerificationMeta _typeMeta = const drift.VerificationMeta(
    'type',
  );
  @override
  late final drift.GeneratedColumn<String> type = drift.GeneratedColumn<String>(
    'type',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    defaultValue: const drift.Constant('free'),
  );
  static const drift.VerificationMeta _moodMeta = const drift.VerificationMeta(
    'mood',
  );
  @override
  late final drift.GeneratedColumn<String> mood = drift.GeneratedColumn<String>(
    'mood',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    defaultValue: const drift.Constant('okay'),
  );
  static const drift.VerificationMeta _noteMeta = const drift.VerificationMeta(
    'note',
  );
  @override
  late final drift.GeneratedColumn<String> note = drift.GeneratedColumn<String>(
    'note',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  @override
  List<drift.GeneratedColumn> get $columns => [
    id,
    taskId,
    title,
    startTime,
    endTime,
    duration,
    type,
    mood,
    note,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'sessions';
  @override
  drift.VerificationContext validateIntegrity(
    drift.Insertable<Session> instance, {
    bool isInserting = false,
  }) {
    final context = drift.VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('task_id')) {
      context.handle(
        _taskIdMeta,
        taskId.isAcceptableOrUnknown(data['task_id']!, _taskIdMeta),
      );
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    }
    if (data.containsKey('start_time')) {
      context.handle(
        _startTimeMeta,
        startTime.isAcceptableOrUnknown(data['start_time']!, _startTimeMeta),
      );
    }
    if (data.containsKey('end_time')) {
      context.handle(
        _endTimeMeta,
        endTime.isAcceptableOrUnknown(data['end_time']!, _endTimeMeta),
      );
    }
    if (data.containsKey('duration')) {
      context.handle(
        _durationMeta,
        duration.isAcceptableOrUnknown(data['duration']!, _durationMeta),
      );
    } else if (isInserting) {
      context.missing(_durationMeta);
    }
    if (data.containsKey('type')) {
      context.handle(
        _typeMeta,
        type.isAcceptableOrUnknown(data['type']!, _typeMeta),
      );
    }
    if (data.containsKey('mood')) {
      context.handle(
        _moodMeta,
        mood.isAcceptableOrUnknown(data['mood']!, _moodMeta),
      );
    }
    if (data.containsKey('note')) {
      context.handle(
        _noteMeta,
        note.isAcceptableOrUnknown(data['note']!, _noteMeta),
      );
    }
    return context;
  }

  @override
  Set<drift.GeneratedColumn> get $primaryKey => {id};
  @override
  Session map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Session(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      taskId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}task_id'],
      ),
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      ),
      startTime: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}start_time'],
      ),
      endTime: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}end_time'],
      ),
      duration: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}duration'],
      )!,
      type: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}type'],
      )!,
      mood: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}mood'],
      ),
      note: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}note'],
      ),
    );
  }

  @override
  $SessionsTable createAlias(String alias) {
    return $SessionsTable(attachedDatabase, alias);
  }
}

class Session extends drift.DataClass implements drift.Insertable<Session> {
  final String id;
  final String? taskId;
  final String? title;
  final String? startTime;
  final String? endTime;
  final String duration;
  final String type;
  final String? mood;
  final String? note;
  const Session({
    required this.id,
    this.taskId,
    this.title,
    this.startTime,
    this.endTime,
    required this.duration,
    required this.type,
    this.mood,
    this.note,
  });
  @override
  Map<String, drift.Expression> toColumns(bool nullToAbsent) {
    final map = <String, drift.Expression>{};
    map['id'] = drift.Variable<String>(id);
    if (!nullToAbsent || taskId != null) {
      map['task_id'] = drift.Variable<String>(taskId);
    }
    if (!nullToAbsent || title != null) {
      map['title'] = drift.Variable<String>(title);
    }
    if (!nullToAbsent || startTime != null) {
      map['start_time'] = drift.Variable<String>(startTime);
    }
    if (!nullToAbsent || endTime != null) {
      map['end_time'] = drift.Variable<String>(endTime);
    }
    map['duration'] = drift.Variable<String>(duration);
    map['type'] = drift.Variable<String>(type);
    if (!nullToAbsent || mood != null) {
      map['mood'] = drift.Variable<String>(mood);
    }
    if (!nullToAbsent || note != null) {
      map['note'] = drift.Variable<String>(note);
    }
    return map;
  }

  SessionsCompanion toCompanion(bool nullToAbsent) {
    return SessionsCompanion(
      id: drift.Value(id),
      taskId: taskId == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(taskId),
      title: title == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(title),
      startTime: startTime == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(startTime),
      endTime: endTime == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(endTime),
      duration: drift.Value(duration),
      type: drift.Value(type),
      mood: mood == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(mood),
      note: note == null && nullToAbsent
          ? const drift.Value.absent()
          : drift.Value(note),
    );
  }

  factory Session.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= drift.driftRuntimeOptions.defaultSerializer;
    return Session(
      id: serializer.fromJson<String>(json['id']),
      taskId: serializer.fromJson<String?>(json['taskId']),
      title: serializer.fromJson<String?>(json['title']),
      startTime: serializer.fromJson<String?>(json['startTime']),
      endTime: serializer.fromJson<String?>(json['endTime']),
      duration: serializer.fromJson<String>(json['duration']),
      type: serializer.fromJson<String>(json['type']),
      mood: serializer.fromJson<String?>(json['mood']),
      note: serializer.fromJson<String?>(json['note']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= drift.driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'taskId': serializer.toJson<String?>(taskId),
      'title': serializer.toJson<String?>(title),
      'startTime': serializer.toJson<String?>(startTime),
      'endTime': serializer.toJson<String?>(endTime),
      'duration': serializer.toJson<String>(duration),
      'type': serializer.toJson<String>(type),
      'mood': serializer.toJson<String?>(mood),
      'note': serializer.toJson<String?>(note),
    };
  }

  Session copyWith({
    String? id,
    drift.Value<String?> taskId = const drift.Value.absent(),
    drift.Value<String?> title = const drift.Value.absent(),
    drift.Value<String?> startTime = const drift.Value.absent(),
    drift.Value<String?> endTime = const drift.Value.absent(),
    String? duration,
    String? type,
    drift.Value<String?> mood = const drift.Value.absent(),
    drift.Value<String?> note = const drift.Value.absent(),
  }) => Session(
    id: id ?? this.id,
    taskId: taskId.present ? taskId.value : this.taskId,
    title: title.present ? title.value : this.title,
    startTime: startTime.present ? startTime.value : this.startTime,
    endTime: endTime.present ? endTime.value : this.endTime,
    duration: duration ?? this.duration,
    type: type ?? this.type,
    mood: mood.present ? mood.value : this.mood,
    note: note.present ? note.value : this.note,
  );
  Session copyWithCompanion(SessionsCompanion data) {
    return Session(
      id: data.id.present ? data.id.value : this.id,
      taskId: data.taskId.present ? data.taskId.value : this.taskId,
      title: data.title.present ? data.title.value : this.title,
      startTime: data.startTime.present ? data.startTime.value : this.startTime,
      endTime: data.endTime.present ? data.endTime.value : this.endTime,
      duration: data.duration.present ? data.duration.value : this.duration,
      type: data.type.present ? data.type.value : this.type,
      mood: data.mood.present ? data.mood.value : this.mood,
      note: data.note.present ? data.note.value : this.note,
    );
  }

  @override
  String toString() {
    return (StringBuffer('Session(')
          ..write('id: $id, ')
          ..write('taskId: $taskId, ')
          ..write('title: $title, ')
          ..write('startTime: $startTime, ')
          ..write('endTime: $endTime, ')
          ..write('duration: $duration, ')
          ..write('type: $type, ')
          ..write('mood: $mood, ')
          ..write('note: $note')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    taskId,
    title,
    startTime,
    endTime,
    duration,
    type,
    mood,
    note,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is Session &&
          other.id == this.id &&
          other.taskId == this.taskId &&
          other.title == this.title &&
          other.startTime == this.startTime &&
          other.endTime == this.endTime &&
          other.duration == this.duration &&
          other.type == this.type &&
          other.mood == this.mood &&
          other.note == this.note);
}

class SessionsCompanion extends drift.UpdateCompanion<Session> {
  final drift.Value<String> id;
  final drift.Value<String?> taskId;
  final drift.Value<String?> title;
  final drift.Value<String?> startTime;
  final drift.Value<String?> endTime;
  final drift.Value<String> duration;
  final drift.Value<String> type;
  final drift.Value<String?> mood;
  final drift.Value<String?> note;
  final drift.Value<int> rowid;
  const SessionsCompanion({
    this.id = const drift.Value.absent(),
    this.taskId = const drift.Value.absent(),
    this.title = const drift.Value.absent(),
    this.startTime = const drift.Value.absent(),
    this.endTime = const drift.Value.absent(),
    this.duration = const drift.Value.absent(),
    this.type = const drift.Value.absent(),
    this.mood = const drift.Value.absent(),
    this.note = const drift.Value.absent(),
    this.rowid = const drift.Value.absent(),
  });
  SessionsCompanion.insert({
    this.id = const drift.Value.absent(),
    this.taskId = const drift.Value.absent(),
    this.title = const drift.Value.absent(),
    this.startTime = const drift.Value.absent(),
    this.endTime = const drift.Value.absent(),
    required String duration,
    this.type = const drift.Value.absent(),
    this.mood = const drift.Value.absent(),
    this.note = const drift.Value.absent(),
    this.rowid = const drift.Value.absent(),
  }) : duration = drift.Value(duration);
  static drift.Insertable<Session> custom({
    drift.Expression<String>? id,
    drift.Expression<String>? taskId,
    drift.Expression<String>? title,
    drift.Expression<String>? startTime,
    drift.Expression<String>? endTime,
    drift.Expression<String>? duration,
    drift.Expression<String>? type,
    drift.Expression<String>? mood,
    drift.Expression<String>? note,
    drift.Expression<int>? rowid,
  }) {
    return drift.RawValuesInsertable({
      if (id != null) 'id': id,
      if (taskId != null) 'task_id': taskId,
      if (title != null) 'title': title,
      if (startTime != null) 'start_time': startTime,
      if (endTime != null) 'end_time': endTime,
      if (duration != null) 'duration': duration,
      if (type != null) 'type': type,
      if (mood != null) 'mood': mood,
      if (note != null) 'note': note,
      if (rowid != null) 'rowid': rowid,
    });
  }

  SessionsCompanion copyWith({
    drift.Value<String>? id,
    drift.Value<String?>? taskId,
    drift.Value<String?>? title,
    drift.Value<String?>? startTime,
    drift.Value<String?>? endTime,
    drift.Value<String>? duration,
    drift.Value<String>? type,
    drift.Value<String?>? mood,
    drift.Value<String?>? note,
    drift.Value<int>? rowid,
  }) {
    return SessionsCompanion(
      id: id ?? this.id,
      taskId: taskId ?? this.taskId,
      title: title ?? this.title,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
      duration: duration ?? this.duration,
      type: type ?? this.type,
      mood: mood ?? this.mood,
      note: note ?? this.note,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, drift.Expression> toColumns(bool nullToAbsent) {
    final map = <String, drift.Expression>{};
    if (id.present) {
      map['id'] = drift.Variable<String>(id.value);
    }
    if (taskId.present) {
      map['task_id'] = drift.Variable<String>(taskId.value);
    }
    if (title.present) {
      map['title'] = drift.Variable<String>(title.value);
    }
    if (startTime.present) {
      map['start_time'] = drift.Variable<String>(startTime.value);
    }
    if (endTime.present) {
      map['end_time'] = drift.Variable<String>(endTime.value);
    }
    if (duration.present) {
      map['duration'] = drift.Variable<String>(duration.value);
    }
    if (type.present) {
      map['type'] = drift.Variable<String>(type.value);
    }
    if (mood.present) {
      map['mood'] = drift.Variable<String>(mood.value);
    }
    if (note.present) {
      map['note'] = drift.Variable<String>(note.value);
    }
    if (rowid.present) {
      map['rowid'] = drift.Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SessionsCompanion(')
          ..write('id: $id, ')
          ..write('taskId: $taskId, ')
          ..write('title: $title, ')
          ..write('startTime: $startTime, ')
          ..write('endTime: $endTime, ')
          ..write('duration: $duration, ')
          ..write('type: $type, ')
          ..write('mood: $mood, ')
          ..write('note: $note, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends drift.GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $RoutinesTable routines = $RoutinesTable(this);
  late final $TasksTable tasks = $TasksTable(this);
  late final $SessionsTable sessions = $SessionsTable(this);
  @override
  Iterable<drift.TableInfo<drift.Table, Object?>> get allTables =>
      allSchemaEntities.whereType<drift.TableInfo<drift.Table, Object?>>();
  @override
  List<drift.DatabaseSchemaEntity> get allSchemaEntities => [
    routines,
    tasks,
    sessions,
  ];
}

typedef $$RoutinesTableCreateCompanionBuilder =
    RoutinesCompanion Function({
      drift.Value<String> id,
      required String title,
      required String recurrenceRule,
      drift.Value<String?> estimatedDuration,
      drift.Value<String?> startTime,
      drift.Value<String?> dueTime,
      drift.Value<String?> note,
      drift.Value<String> createdAt,
      drift.Value<String> updatedAt,
      drift.Value<int> rowid,
    });
typedef $$RoutinesTableUpdateCompanionBuilder =
    RoutinesCompanion Function({
      drift.Value<String> id,
      drift.Value<String> title,
      drift.Value<String> recurrenceRule,
      drift.Value<String?> estimatedDuration,
      drift.Value<String?> startTime,
      drift.Value<String?> dueTime,
      drift.Value<String?> note,
      drift.Value<String> createdAt,
      drift.Value<String> updatedAt,
      drift.Value<int> rowid,
    });

final class $$RoutinesTableReferences
    extends drift.BaseReferences<_$AppDatabase, $RoutinesTable, Routine> {
  $$RoutinesTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static drift.MultiTypedResultKey<$TasksTable, List<Task>> _tasksRefsTable(
    _$AppDatabase db,
  ) => drift.MultiTypedResultKey.fromTable(
    db.tasks,
    aliasName: drift.$_aliasNameGenerator(db.routines.id, db.tasks.routineId),
  );

  $$TasksTableProcessedTableManager get tasksRefs {
    final manager = $$TasksTableTableManager(
      $_db,
      $_db.tasks,
    ).filter((f) => f.routineId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(_tasksRefsTable($_db));
    return drift.ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }
}

class $$RoutinesTableFilterComposer
    extends drift.Composer<_$AppDatabase, $RoutinesTable> {
  $$RoutinesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get recurrenceRule => $composableBuilder(
    column: $table.recurrenceRule,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get estimatedDuration => $composableBuilder(
    column: $table.estimatedDuration,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get startTime => $composableBuilder(
    column: $table.startTime,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get dueTime => $composableBuilder(
    column: $table.dueTime,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get note => $composableBuilder(
    column: $table.note,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.Expression<bool> tasksRefs(
    drift.Expression<bool> Function($$TasksTableFilterComposer f) f,
  ) {
    final $$TasksTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.tasks,
      getReferencedColumn: (t) => t.routineId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$TasksTableFilterComposer(
            $db: $db,
            $table: $db.tasks,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$RoutinesTableOrderingComposer
    extends drift.Composer<_$AppDatabase, $RoutinesTable> {
  $$RoutinesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get recurrenceRule => $composableBuilder(
    column: $table.recurrenceRule,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get estimatedDuration => $composableBuilder(
    column: $table.estimatedDuration,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get startTime => $composableBuilder(
    column: $table.startTime,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get dueTime => $composableBuilder(
    column: $table.dueTime,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get note => $composableBuilder(
    column: $table.note,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => drift.ColumnOrderings(column),
  );
}

class $$RoutinesTableAnnotationComposer
    extends drift.Composer<_$AppDatabase, $RoutinesTable> {
  $$RoutinesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  drift.GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  drift.GeneratedColumn<String> get recurrenceRule => $composableBuilder(
    column: $table.recurrenceRule,
    builder: (column) => column,
  );

  drift.GeneratedColumn<String> get estimatedDuration => $composableBuilder(
    column: $table.estimatedDuration,
    builder: (column) => column,
  );

  drift.GeneratedColumn<String> get startTime =>
      $composableBuilder(column: $table.startTime, builder: (column) => column);

  drift.GeneratedColumn<String> get dueTime =>
      $composableBuilder(column: $table.dueTime, builder: (column) => column);

  drift.GeneratedColumn<String> get note =>
      $composableBuilder(column: $table.note, builder: (column) => column);

  drift.GeneratedColumn<String> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  drift.GeneratedColumn<String> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  drift.Expression<T> tasksRefs<T extends Object>(
    drift.Expression<T> Function($$TasksTableAnnotationComposer a) f,
  ) {
    final $$TasksTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.tasks,
      getReferencedColumn: (t) => t.routineId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$TasksTableAnnotationComposer(
            $db: $db,
            $table: $db.tasks,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$RoutinesTableTableManager
    extends
        drift.RootTableManager<
          _$AppDatabase,
          $RoutinesTable,
          Routine,
          $$RoutinesTableFilterComposer,
          $$RoutinesTableOrderingComposer,
          $$RoutinesTableAnnotationComposer,
          $$RoutinesTableCreateCompanionBuilder,
          $$RoutinesTableUpdateCompanionBuilder,
          (Routine, $$RoutinesTableReferences),
          Routine,
          drift.PrefetchHooks Function({bool tasksRefs})
        > {
  $$RoutinesTableTableManager(_$AppDatabase db, $RoutinesTable table)
    : super(
        drift.TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$RoutinesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$RoutinesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$RoutinesTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                drift.Value<String> id = const drift.Value.absent(),
                drift.Value<String> title = const drift.Value.absent(),
                drift.Value<String> recurrenceRule = const drift.Value.absent(),
                drift.Value<String?> estimatedDuration =
                    const drift.Value.absent(),
                drift.Value<String?> startTime = const drift.Value.absent(),
                drift.Value<String?> dueTime = const drift.Value.absent(),
                drift.Value<String?> note = const drift.Value.absent(),
                drift.Value<String> createdAt = const drift.Value.absent(),
                drift.Value<String> updatedAt = const drift.Value.absent(),
                drift.Value<int> rowid = const drift.Value.absent(),
              }) => RoutinesCompanion(
                id: id,
                title: title,
                recurrenceRule: recurrenceRule,
                estimatedDuration: estimatedDuration,
                startTime: startTime,
                dueTime: dueTime,
                note: note,
                createdAt: createdAt,
                updatedAt: updatedAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                drift.Value<String> id = const drift.Value.absent(),
                required String title,
                required String recurrenceRule,
                drift.Value<String?> estimatedDuration =
                    const drift.Value.absent(),
                drift.Value<String?> startTime = const drift.Value.absent(),
                drift.Value<String?> dueTime = const drift.Value.absent(),
                drift.Value<String?> note = const drift.Value.absent(),
                drift.Value<String> createdAt = const drift.Value.absent(),
                drift.Value<String> updatedAt = const drift.Value.absent(),
                drift.Value<int> rowid = const drift.Value.absent(),
              }) => RoutinesCompanion.insert(
                id: id,
                title: title,
                recurrenceRule: recurrenceRule,
                estimatedDuration: estimatedDuration,
                startTime: startTime,
                dueTime: dueTime,
                note: note,
                createdAt: createdAt,
                updatedAt: updatedAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$RoutinesTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: ({tasksRefs = false}) {
            return drift.PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [if (tasksRefs) db.tasks],
              addJoins: null,
              getPrefetchedDataCallback: (items) async {
                return [
                  if (tasksRefs)
                    await drift.$_getPrefetchedData<
                      Routine,
                      $RoutinesTable,
                      Task
                    >(
                      currentTable: table,
                      referencedTable: $$RoutinesTableReferences
                          ._tasksRefsTable(db),
                      managerFromTypedResult: (p0) =>
                          $$RoutinesTableReferences(db, table, p0).tasksRefs,
                      referencedItemsForCurrentItem: (item, referencedItems) =>
                          referencedItems.where((e) => e.routineId == item.id),
                      typedResults: items,
                    ),
                ];
              },
            );
          },
        ),
      );
}

typedef $$RoutinesTableProcessedTableManager =
    drift.ProcessedTableManager<
      _$AppDatabase,
      $RoutinesTable,
      Routine,
      $$RoutinesTableFilterComposer,
      $$RoutinesTableOrderingComposer,
      $$RoutinesTableAnnotationComposer,
      $$RoutinesTableCreateCompanionBuilder,
      $$RoutinesTableUpdateCompanionBuilder,
      (Routine, $$RoutinesTableReferences),
      Routine,
      drift.PrefetchHooks Function({bool tasksRefs})
    >;
typedef $$TasksTableCreateCompanionBuilder =
    TasksCompanion Function({
      drift.Value<String> id,
      drift.Value<String?> routineId,
      required String title,
      drift.Value<String> status,
      drift.Value<String?> estimatedDuration,
      drift.Value<String?> startTime,
      drift.Value<String?> dueTime,
      drift.Value<String?> note,
      drift.Value<String> createdAt,
      drift.Value<String> updatedAt,
      drift.Value<int> rowid,
    });
typedef $$TasksTableUpdateCompanionBuilder =
    TasksCompanion Function({
      drift.Value<String> id,
      drift.Value<String?> routineId,
      drift.Value<String> title,
      drift.Value<String> status,
      drift.Value<String?> estimatedDuration,
      drift.Value<String?> startTime,
      drift.Value<String?> dueTime,
      drift.Value<String?> note,
      drift.Value<String> createdAt,
      drift.Value<String> updatedAt,
      drift.Value<int> rowid,
    });

final class $$TasksTableReferences
    extends drift.BaseReferences<_$AppDatabase, $TasksTable, Task> {
  $$TasksTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static $RoutinesTable _routineIdTable(_$AppDatabase db) =>
      db.routines.createAlias(
        drift.$_aliasNameGenerator(db.tasks.routineId, db.routines.id),
      );

  $$RoutinesTableProcessedTableManager? get routineId {
    final $_column = $_itemColumn<String>('routine_id');
    if ($_column == null) return null;
    final manager = $$RoutinesTableTableManager(
      $_db,
      $_db.routines,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_routineIdTable($_db));
    if (item == null) return manager;
    return drift.ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }

  static drift.MultiTypedResultKey<$SessionsTable, List<Session>>
  _sessionsRefsTable(_$AppDatabase db) => drift.MultiTypedResultKey.fromTable(
    db.sessions,
    aliasName: drift.$_aliasNameGenerator(db.tasks.id, db.sessions.taskId),
  );

  $$SessionsTableProcessedTableManager get sessionsRefs {
    final manager = $$SessionsTableTableManager(
      $_db,
      $_db.sessions,
    ).filter((f) => f.taskId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(_sessionsRefsTable($_db));
    return drift.ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }
}

class $$TasksTableFilterComposer
    extends drift.Composer<_$AppDatabase, $TasksTable> {
  $$TasksTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get status => $composableBuilder(
    column: $table.status,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get estimatedDuration => $composableBuilder(
    column: $table.estimatedDuration,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get startTime => $composableBuilder(
    column: $table.startTime,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get dueTime => $composableBuilder(
    column: $table.dueTime,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get note => $composableBuilder(
    column: $table.note,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => drift.ColumnFilters(column),
  );

  $$RoutinesTableFilterComposer get routineId {
    final $$RoutinesTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.routineId,
      referencedTable: $db.routines,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$RoutinesTableFilterComposer(
            $db: $db,
            $table: $db.routines,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  drift.Expression<bool> sessionsRefs(
    drift.Expression<bool> Function($$SessionsTableFilterComposer f) f,
  ) {
    final $$SessionsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.sessions,
      getReferencedColumn: (t) => t.taskId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SessionsTableFilterComposer(
            $db: $db,
            $table: $db.sessions,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$TasksTableOrderingComposer
    extends drift.Composer<_$AppDatabase, $TasksTable> {
  $$TasksTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get status => $composableBuilder(
    column: $table.status,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get estimatedDuration => $composableBuilder(
    column: $table.estimatedDuration,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get startTime => $composableBuilder(
    column: $table.startTime,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get dueTime => $composableBuilder(
    column: $table.dueTime,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get note => $composableBuilder(
    column: $table.note,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => drift.ColumnOrderings(column),
  );

  $$RoutinesTableOrderingComposer get routineId {
    final $$RoutinesTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.routineId,
      referencedTable: $db.routines,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$RoutinesTableOrderingComposer(
            $db: $db,
            $table: $db.routines,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$TasksTableAnnotationComposer
    extends drift.Composer<_$AppDatabase, $TasksTable> {
  $$TasksTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  drift.GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  drift.GeneratedColumn<String> get status =>
      $composableBuilder(column: $table.status, builder: (column) => column);

  drift.GeneratedColumn<String> get estimatedDuration => $composableBuilder(
    column: $table.estimatedDuration,
    builder: (column) => column,
  );

  drift.GeneratedColumn<String> get startTime =>
      $composableBuilder(column: $table.startTime, builder: (column) => column);

  drift.GeneratedColumn<String> get dueTime =>
      $composableBuilder(column: $table.dueTime, builder: (column) => column);

  drift.GeneratedColumn<String> get note =>
      $composableBuilder(column: $table.note, builder: (column) => column);

  drift.GeneratedColumn<String> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  drift.GeneratedColumn<String> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  $$RoutinesTableAnnotationComposer get routineId {
    final $$RoutinesTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.routineId,
      referencedTable: $db.routines,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$RoutinesTableAnnotationComposer(
            $db: $db,
            $table: $db.routines,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  drift.Expression<T> sessionsRefs<T extends Object>(
    drift.Expression<T> Function($$SessionsTableAnnotationComposer a) f,
  ) {
    final $$SessionsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.sessions,
      getReferencedColumn: (t) => t.taskId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SessionsTableAnnotationComposer(
            $db: $db,
            $table: $db.sessions,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$TasksTableTableManager
    extends
        drift.RootTableManager<
          _$AppDatabase,
          $TasksTable,
          Task,
          $$TasksTableFilterComposer,
          $$TasksTableOrderingComposer,
          $$TasksTableAnnotationComposer,
          $$TasksTableCreateCompanionBuilder,
          $$TasksTableUpdateCompanionBuilder,
          (Task, $$TasksTableReferences),
          Task,
          drift.PrefetchHooks Function({bool routineId, bool sessionsRefs})
        > {
  $$TasksTableTableManager(_$AppDatabase db, $TasksTable table)
    : super(
        drift.TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$TasksTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$TasksTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$TasksTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                drift.Value<String> id = const drift.Value.absent(),
                drift.Value<String?> routineId = const drift.Value.absent(),
                drift.Value<String> title = const drift.Value.absent(),
                drift.Value<String> status = const drift.Value.absent(),
                drift.Value<String?> estimatedDuration =
                    const drift.Value.absent(),
                drift.Value<String?> startTime = const drift.Value.absent(),
                drift.Value<String?> dueTime = const drift.Value.absent(),
                drift.Value<String?> note = const drift.Value.absent(),
                drift.Value<String> createdAt = const drift.Value.absent(),
                drift.Value<String> updatedAt = const drift.Value.absent(),
                drift.Value<int> rowid = const drift.Value.absent(),
              }) => TasksCompanion(
                id: id,
                routineId: routineId,
                title: title,
                status: status,
                estimatedDuration: estimatedDuration,
                startTime: startTime,
                dueTime: dueTime,
                note: note,
                createdAt: createdAt,
                updatedAt: updatedAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                drift.Value<String> id = const drift.Value.absent(),
                drift.Value<String?> routineId = const drift.Value.absent(),
                required String title,
                drift.Value<String> status = const drift.Value.absent(),
                drift.Value<String?> estimatedDuration =
                    const drift.Value.absent(),
                drift.Value<String?> startTime = const drift.Value.absent(),
                drift.Value<String?> dueTime = const drift.Value.absent(),
                drift.Value<String?> note = const drift.Value.absent(),
                drift.Value<String> createdAt = const drift.Value.absent(),
                drift.Value<String> updatedAt = const drift.Value.absent(),
                drift.Value<int> rowid = const drift.Value.absent(),
              }) => TasksCompanion.insert(
                id: id,
                routineId: routineId,
                title: title,
                status: status,
                estimatedDuration: estimatedDuration,
                startTime: startTime,
                dueTime: dueTime,
                note: note,
                createdAt: createdAt,
                updatedAt: updatedAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) =>
                    (e.readTable(table), $$TasksTableReferences(db, table, e)),
              )
              .toList(),
          prefetchHooksCallback: ({routineId = false, sessionsRefs = false}) {
            return drift.PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [if (sessionsRefs) db.sessions],
              addJoins:
                  <
                    T extends drift.TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic
                    >
                  >(state) {
                    if (routineId) {
                      state =
                          state.withJoin(
                                currentTable: table,
                                currentColumn: table.routineId,
                                referencedTable: $$TasksTableReferences
                                    ._routineIdTable(db),
                                referencedColumn: $$TasksTableReferences
                                    ._routineIdTable(db)
                                    .id,
                              )
                              as T;
                    }

                    return state;
                  },
              getPrefetchedDataCallback: (items) async {
                return [
                  if (sessionsRefs)
                    await drift.$_getPrefetchedData<Task, $TasksTable, Session>(
                      currentTable: table,
                      referencedTable: $$TasksTableReferences
                          ._sessionsRefsTable(db),
                      managerFromTypedResult: (p0) =>
                          $$TasksTableReferences(db, table, p0).sessionsRefs,
                      referencedItemsForCurrentItem: (item, referencedItems) =>
                          referencedItems.where((e) => e.taskId == item.id),
                      typedResults: items,
                    ),
                ];
              },
            );
          },
        ),
      );
}

typedef $$TasksTableProcessedTableManager =
    drift.ProcessedTableManager<
      _$AppDatabase,
      $TasksTable,
      Task,
      $$TasksTableFilterComposer,
      $$TasksTableOrderingComposer,
      $$TasksTableAnnotationComposer,
      $$TasksTableCreateCompanionBuilder,
      $$TasksTableUpdateCompanionBuilder,
      (Task, $$TasksTableReferences),
      Task,
      drift.PrefetchHooks Function({bool routineId, bool sessionsRefs})
    >;
typedef $$SessionsTableCreateCompanionBuilder =
    SessionsCompanion Function({
      drift.Value<String> id,
      drift.Value<String?> taskId,
      drift.Value<String?> title,
      drift.Value<String?> startTime,
      drift.Value<String?> endTime,
      required String duration,
      drift.Value<String> type,
      drift.Value<String?> mood,
      drift.Value<String?> note,
      drift.Value<int> rowid,
    });
typedef $$SessionsTableUpdateCompanionBuilder =
    SessionsCompanion Function({
      drift.Value<String> id,
      drift.Value<String?> taskId,
      drift.Value<String?> title,
      drift.Value<String?> startTime,
      drift.Value<String?> endTime,
      drift.Value<String> duration,
      drift.Value<String> type,
      drift.Value<String?> mood,
      drift.Value<String?> note,
      drift.Value<int> rowid,
    });

final class $$SessionsTableReferences
    extends drift.BaseReferences<_$AppDatabase, $SessionsTable, Session> {
  $$SessionsTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static $TasksTable _taskIdTable(_$AppDatabase db) => db.tasks.createAlias(
    drift.$_aliasNameGenerator(db.sessions.taskId, db.tasks.id),
  );

  $$TasksTableProcessedTableManager? get taskId {
    final $_column = $_itemColumn<String>('task_id');
    if ($_column == null) return null;
    final manager = $$TasksTableTableManager(
      $_db,
      $_db.tasks,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_taskIdTable($_db));
    if (item == null) return manager;
    return drift.ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }
}

class $$SessionsTableFilterComposer
    extends drift.Composer<_$AppDatabase, $SessionsTable> {
  $$SessionsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get startTime => $composableBuilder(
    column: $table.startTime,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get endTime => $composableBuilder(
    column: $table.endTime,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get duration => $composableBuilder(
    column: $table.duration,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get type => $composableBuilder(
    column: $table.type,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get mood => $composableBuilder(
    column: $table.mood,
    builder: (column) => drift.ColumnFilters(column),
  );

  drift.ColumnFilters<String> get note => $composableBuilder(
    column: $table.note,
    builder: (column) => drift.ColumnFilters(column),
  );

  $$TasksTableFilterComposer get taskId {
    final $$TasksTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.taskId,
      referencedTable: $db.tasks,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$TasksTableFilterComposer(
            $db: $db,
            $table: $db.tasks,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$SessionsTableOrderingComposer
    extends drift.Composer<_$AppDatabase, $SessionsTable> {
  $$SessionsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get startTime => $composableBuilder(
    column: $table.startTime,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get endTime => $composableBuilder(
    column: $table.endTime,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get duration => $composableBuilder(
    column: $table.duration,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get type => $composableBuilder(
    column: $table.type,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get mood => $composableBuilder(
    column: $table.mood,
    builder: (column) => drift.ColumnOrderings(column),
  );

  drift.ColumnOrderings<String> get note => $composableBuilder(
    column: $table.note,
    builder: (column) => drift.ColumnOrderings(column),
  );

  $$TasksTableOrderingComposer get taskId {
    final $$TasksTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.taskId,
      referencedTable: $db.tasks,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$TasksTableOrderingComposer(
            $db: $db,
            $table: $db.tasks,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$SessionsTableAnnotationComposer
    extends drift.Composer<_$AppDatabase, $SessionsTable> {
  $$SessionsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  drift.GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  drift.GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  drift.GeneratedColumn<String> get startTime =>
      $composableBuilder(column: $table.startTime, builder: (column) => column);

  drift.GeneratedColumn<String> get endTime =>
      $composableBuilder(column: $table.endTime, builder: (column) => column);

  drift.GeneratedColumn<String> get duration =>
      $composableBuilder(column: $table.duration, builder: (column) => column);

  drift.GeneratedColumn<String> get type =>
      $composableBuilder(column: $table.type, builder: (column) => column);

  drift.GeneratedColumn<String> get mood =>
      $composableBuilder(column: $table.mood, builder: (column) => column);

  drift.GeneratedColumn<String> get note =>
      $composableBuilder(column: $table.note, builder: (column) => column);

  $$TasksTableAnnotationComposer get taskId {
    final $$TasksTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.taskId,
      referencedTable: $db.tasks,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$TasksTableAnnotationComposer(
            $db: $db,
            $table: $db.tasks,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$SessionsTableTableManager
    extends
        drift.RootTableManager<
          _$AppDatabase,
          $SessionsTable,
          Session,
          $$SessionsTableFilterComposer,
          $$SessionsTableOrderingComposer,
          $$SessionsTableAnnotationComposer,
          $$SessionsTableCreateCompanionBuilder,
          $$SessionsTableUpdateCompanionBuilder,
          (Session, $$SessionsTableReferences),
          Session,
          drift.PrefetchHooks Function({bool taskId})
        > {
  $$SessionsTableTableManager(_$AppDatabase db, $SessionsTable table)
    : super(
        drift.TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$SessionsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$SessionsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$SessionsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                drift.Value<String> id = const drift.Value.absent(),
                drift.Value<String?> taskId = const drift.Value.absent(),
                drift.Value<String?> title = const drift.Value.absent(),
                drift.Value<String?> startTime = const drift.Value.absent(),
                drift.Value<String?> endTime = const drift.Value.absent(),
                drift.Value<String> duration = const drift.Value.absent(),
                drift.Value<String> type = const drift.Value.absent(),
                drift.Value<String?> mood = const drift.Value.absent(),
                drift.Value<String?> note = const drift.Value.absent(),
                drift.Value<int> rowid = const drift.Value.absent(),
              }) => SessionsCompanion(
                id: id,
                taskId: taskId,
                title: title,
                startTime: startTime,
                endTime: endTime,
                duration: duration,
                type: type,
                mood: mood,
                note: note,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                drift.Value<String> id = const drift.Value.absent(),
                drift.Value<String?> taskId = const drift.Value.absent(),
                drift.Value<String?> title = const drift.Value.absent(),
                drift.Value<String?> startTime = const drift.Value.absent(),
                drift.Value<String?> endTime = const drift.Value.absent(),
                required String duration,
                drift.Value<String> type = const drift.Value.absent(),
                drift.Value<String?> mood = const drift.Value.absent(),
                drift.Value<String?> note = const drift.Value.absent(),
                drift.Value<int> rowid = const drift.Value.absent(),
              }) => SessionsCompanion.insert(
                id: id,
                taskId: taskId,
                title: title,
                startTime: startTime,
                endTime: endTime,
                duration: duration,
                type: type,
                mood: mood,
                note: note,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$SessionsTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: ({taskId = false}) {
            return drift.PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins:
                  <
                    T extends drift.TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic
                    >
                  >(state) {
                    if (taskId) {
                      state =
                          state.withJoin(
                                currentTable: table,
                                currentColumn: table.taskId,
                                referencedTable: $$SessionsTableReferences
                                    ._taskIdTable(db),
                                referencedColumn: $$SessionsTableReferences
                                    ._taskIdTable(db)
                                    .id,
                              )
                              as T;
                    }

                    return state;
                  },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ),
      );
}

typedef $$SessionsTableProcessedTableManager =
    drift.ProcessedTableManager<
      _$AppDatabase,
      $SessionsTable,
      Session,
      $$SessionsTableFilterComposer,
      $$SessionsTableOrderingComposer,
      $$SessionsTableAnnotationComposer,
      $$SessionsTableCreateCompanionBuilder,
      $$SessionsTableUpdateCompanionBuilder,
      (Session, $$SessionsTableReferences),
      Session,
      drift.PrefetchHooks Function({bool taskId})
    >;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$RoutinesTableTableManager get routines =>
      $$RoutinesTableTableManager(_db, _db.routines);
  $$TasksTableTableManager get tasks =>
      $$TasksTableTableManager(_db, _db.tasks);
  $$SessionsTableTableManager get sessions =>
      $$SessionsTableTableManager(_db, _db.sessions);
}

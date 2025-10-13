package dev.tireless.abun.core.time

import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun currentEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

@OptIn(ExperimentalTime::class)
fun currentInstant(): Instant = Clock.System.now()

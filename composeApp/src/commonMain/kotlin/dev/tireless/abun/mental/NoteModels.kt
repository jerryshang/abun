package dev.tireless.abun.mental

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Note(
  val id: Long,
  val title: String,
  val content: String,
  val createdAt: Instant,
  val updatedAt: Instant,
)

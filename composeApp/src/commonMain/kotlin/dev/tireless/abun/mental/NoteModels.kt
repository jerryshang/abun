package dev.tireless.abun.mental

import kotlinx.datetime.Instant

data class Note(
  val id: Long,
  val title: String,
  val content: String,
  val createdAt: Instant,
  val updatedAt: Instant,
)

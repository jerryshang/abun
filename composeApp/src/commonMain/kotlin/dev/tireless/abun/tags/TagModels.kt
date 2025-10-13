package dev.tireless.abun.tags

import dev.tireless.abun.core.time.currentInstant
import kotlinx.datetime.Instant

/**
 * Represents the domains within the app that a tag can apply to.
 */
enum class TagDomain {
  Tasks,
  Notes,
  Finance,
  All,
}

/**
 * Core tag model shared across modules.
 */
data class Tag(
  val id: Long,
  val name: String,
  val path: String,
  val colorHex: String,
  val domains: Set<TagDomain> = setOf(TagDomain.All),
  val description: String? = null,
  val createdAt: Instant = currentInstant(),
  val updatedAt: Instant = currentInstant(),
)

data class TagDraft(
  val name: String,
  val path: String,
  val colorHex: String,
  val domains: Set<TagDomain>,
  val description: String? = null,
)

data class TagUpdate(
  val id: Long,
  val name: String,
  val path: String,
  val colorHex: String,
  val domains: Set<TagDomain>,
  val description: String? = null,
)

fun Tag.appliesTo(domain: TagDomain): Boolean {
  return TagDomain.All in domains || domain == TagDomain.All || domain in domains
}

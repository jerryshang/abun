package dev.tireless.abun.model

data class Category(
    val id: Long,
    val name: String,
    val color: String,
    val createdAt: String,
    val updatedAt: String
)

data class Task(
    val id: Long,
    val name: String,
    val description: String?,
    val categoryId: Long,
    val strategy: String,
    val createdAt: String,
    val updatedAt: String,
    val categoryName: String? = null,
    val categoryColor: String? = null
)

data class Timeblock(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val taskId: Long,
    val createdAt: String,
    val updatedAt: String,
    val taskName: String? = null,
    val taskDescription: String? = null,
    val taskStrategy: String? = null,
    val categoryName: String? = null,
    val categoryColor: String? = null
)

data class Alarm(
    val id: Long,
    val timeblockId: Long,
    val minutesBefore: Long,
    val isEnabled: Boolean,
    val createdAt: String
)
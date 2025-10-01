package dev.tireless.abun.time

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.time.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val database: AppDatabase
) {

    suspend fun initializeDefaultData() {
        withContext(Dispatchers.IO) {
            try {
                println("CategoryRepository: Checking for existing categories...")
                database.transaction {
                    // Check if the categories table exists
                    try {
                        val existingCategories = database.timeblockQueries.selectAllCategories().executeAsList()
                        println("CategoryRepository: Found ${existingCategories.size} existing categories")

                        if (existingCategories.isEmpty()) {
                            println("CategoryRepository: No categories found, inserting default data...")
                            val now = getCurrentTimestamp()

                            // Insert categories
                            database.timeblockQueries.insertCategory("Life", "#4CAF50", now, now)
                            database.timeblockQueries.insertCategory("Work", "#2196F3", now, now)
                            database.timeblockQueries.insertCategory("Side Project", "#FF9800", now, now)
                            println("CategoryRepository: Inserted 3 default categories")

                            // Get the inserted categories to create default tasks
                            val categories = database.timeblockQueries.selectAllCategories().executeAsList()
                            println("CategoryRepository: Retrieved ${categories.size} categories for task creation")

                            val lifeCategory = categories.find { it.name == "Life" }
                            val workCategory = categories.find { it.name == "Work" }
                            val sideProjectCategory = categories.find { it.name == "Side Project" }

                            // Insert default tasks
                            lifeCategory?.let {
                                database.timeblockQueries.insertTask("Morning Routine", "Daily morning activities", it.id, "do", now, now)
                                database.timeblockQueries.insertTask("Exercise", "Physical activity and fitness", it.id, "todo", now, now)
                                println("CategoryRepository: Inserted tasks for Life category (id: ${it.id})")
                            }
                            workCategory?.let {
                                database.timeblockQueries.insertTask("Meeting", "Work meetings and discussions", it.id, "check", now, now)
                                database.timeblockQueries.insertTask("Focus Work", "Deep work sessions", it.id, "do", now, now)
                                println("CategoryRepository: Inserted tasks for Work category (id: ${it.id})")
                            }
                            sideProjectCategory?.let {
                                database.timeblockQueries.insertTask("Development", "Coding and development work", it.id, "do", now, now)
                                database.timeblockQueries.insertTask("Planning", "Project planning and design", it.id, "plan", now, now)
                                println("CategoryRepository: Inserted tasks for Side Project category (id: ${it.id})")
                            }
                            println("CategoryRepository: Default data initialization completed successfully")
                        } else {
                            println("CategoryRepository: Categories already exist, skipping initialization")
                        }
                    } catch (e: Exception) {
                        println("CategoryRepository: Error accessing categories table: ${e.message}")
                        // The table might not exist, which is okay - SQLDelight should create it
                        throw e
                    }
                }
            } catch (e: Exception) {
                // Log the error for debugging
                println("Error initializing default data: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    fun getAllCategories(): Flow<List<Category>> =
        database.timeblockQueries
            .selectAllCategories()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { categoriesData ->
                categoriesData.map { categoryData ->
                    Category(
                        id = categoryData.id,
                        name = categoryData.name,
                        color = categoryData.color,
                        createdAt = categoryData.created_at,
                        updatedAt = categoryData.updated_at
                    )
                }
            }

    suspend fun getCategoryById(id: Long): Category? =
        withContext(Dispatchers.IO) {
            database.timeblockQueries
                .selectCategoryById(id)
                .asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .first()
                ?.let { categoryData ->
                    Category(
                        id = categoryData.id,
                        name = categoryData.name,
                        color = categoryData.color,
                        createdAt = categoryData.created_at,
                        updatedAt = categoryData.updated_at
                    )
                }
        }

    suspend fun insertCategory(name: String, color: String): Long? {
        return withContext(Dispatchers.IO) {
            val now = getCurrentTimestamp()
            database.timeblockQueries.insertCategory(name, color, now, now)
            // Get the last inserted row ID
            database.timeblockQueries.selectAllCategories().executeAsList().lastOrNull()?.id
        }
    }

    suspend fun updateCategory(id: Long, name: String, color: String) {
        withContext(Dispatchers.IO) {
            val now = getCurrentTimestamp()
            database.timeblockQueries.updateCategory(name, color, now, id)
        }
    }

    suspend fun deleteCategory(id: Long) {
        withContext(Dispatchers.IO) {
            database.timeblockQueries.deleteCategory(id)
        }
    }

    private fun getCurrentTimestamp(): String {
        return "2024-01-01T00:00:00" // Simplified for KMP
    }
}
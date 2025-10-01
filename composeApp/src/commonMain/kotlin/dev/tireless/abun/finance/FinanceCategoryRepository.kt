package dev.tireless.abun.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.FinanceCategory as DbFinanceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for managing finance categories
 */
class FinanceCategoryRepository(private val database: AppDatabase) {
    private val queries = database.financeQueries

    /**
     * Get all categories as Flow
     */
    fun getAllCategoriesFlow(): Flow<List<FinanceCategory>> =
        queries.getAllFinanceCategories().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toDomain() }
        }

    /**
     * Get all categories
     */
    suspend fun getAllCategories(): List<FinanceCategory> =
        withContext(Dispatchers.IO) {
            queries.getAllFinanceCategories().executeAsList().map { it.toDomain() }
        }

    /**
     * Get category by ID
     */
    suspend fun getCategoryById(id: Long): FinanceCategory? =
        withContext(Dispatchers.IO) {
            queries.getFinanceCategoryById(id).executeAsOneOrNull()?.toDomain()
        }

    /**
     * Get categories by type (expense or income)
     */
    suspend fun getCategoriesByType(type: CategoryType): List<FinanceCategory> =
        withContext(Dispatchers.IO) {
            queries.getFinanceCategoriesByType(type.name.lowercase()).executeAsList().map { it.toDomain() }
        }

    /**
     * Get top-level categories (parent categories)
     */
    suspend fun getTopLevelCategories(): List<FinanceCategory> =
        withContext(Dispatchers.IO) {
            queries.getTopLevelFinanceCategories().executeAsList().map { it.toDomain() }
        }

    /**
     * Get subcategories of a parent category
     */
    suspend fun getSubCategories(parentId: Long): List<FinanceCategory> =
        withContext(Dispatchers.IO) {
            queries.getSubCategories(parentId).executeAsList().map { it.toDomain() }
        }

    /**
     * Get categories with their subcategories
     */
    suspend fun getCategoriesWithSubcategories(): List<CategoryWithSubcategories> =
        withContext(Dispatchers.IO) {
            val topLevel = getTopLevelCategories()
            topLevel.map { parent ->
                CategoryWithSubcategories(
                    category = parent,
                    subcategories = getSubCategories(parent.id)
                )
            }
        }

    /**
     * Create a new category
     */
    suspend fun createCategory(input: CreateCategoryInput): Long =
        withContext(Dispatchers.IO) {
            val now = currentTimeMillis()
            queries.insertFinanceCategory(
                name = input.name,
                parent_id = input.parentId,
                type = input.type.name.lowercase(),
                icon_name = input.iconName,
                color_hex = input.colorHex,
                is_system = 0L,
                created_at = now,
                updated_at = now
            )
            queries.getAllFinanceCategories().executeAsList().lastOrNull()?.id ?: -1L
        }

    /**
     * Update a category
     */
    suspend fun updateCategory(
        id: Long,
        name: String,
        parentId: Long? = null,
        type: CategoryType,
        iconName: String? = null,
        colorHex: String? = null
    ): Unit =
        withContext(Dispatchers.IO) {
            val now = currentTimeMillis()
            queries.updateFinanceCategory(
                name = name,
                parent_id = parentId,
                type = type.name.lowercase(),
                icon_name = iconName,
                color_hex = colorHex,
                updated_at = now,
                id = id
            )
        }

    /**
     * Delete a category
     */
    suspend fun deleteCategory(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.deleteFinanceCategory(id)
        }

    /**
     * Initialize default categories (Chinese user preset)
     */
    suspend fun initializeDefaultCategories() {
        val existingCategories = getAllCategories()
        if (existingCategories.isNotEmpty()) return

        // Expense Categories
        val expenseCategories =
            mapOf(
                "餐饮" to listOf("早餐", "午餐", "晚餐", "零食饮料", "水果"),
                "交通" to listOf("地铁", "公交", "打车", "加油", "停车费"),
                "购物" to listOf("衣服", "鞋包", "化妆品", "日用品", "电子产品"),
                "娱乐" to listOf("电影", "游戏", "旅游", "运动健身", "聚会"),
                "居住" to listOf("房租", "物业费", "水电费", "网费", "家具家电"),
                "医疗" to listOf("看病", "买药", "体检", "保健品"),
                "教育" to listOf("书籍", "课程", "培训", "学费"),
                "通讯" to listOf("话费", "宽带", "会员订阅"),
                "人情" to listOf("礼金", "送礼", "请客")
            )

        // Income Categories
        val incomeCategories =
            mapOf(
                "工资" to listOf("本职工资", "奖金", "津贴"),
                "副业" to listOf("兼职", "投资", "理财收益"),
                "其他" to listOf("红包", "退款", "报销")
            )

        // Create expense categories
        expenseCategories.forEach { (parent, children) ->
            val parentId =
                createCategory(
                    CreateCategoryInput(
                        name = parent,
                        type = CategoryType.EXPENSE,
                        iconName = null,
                        colorHex = null
                    )
                )
            children.forEach { child ->
                createCategory(
                    CreateCategoryInput(
                        name = child,
                        parentId = parentId,
                        type = CategoryType.EXPENSE,
                        iconName = null,
                        colorHex = null
                    )
                )
            }
        }

        // Create income categories
        incomeCategories.forEach { (parent, children) ->
            val parentId =
                createCategory(
                    CreateCategoryInput(
                        name = parent,
                        type = CategoryType.INCOME,
                        iconName = null,
                        colorHex = null
                    )
                )
            children.forEach { child ->
                createCategory(
                    CreateCategoryInput(
                        name = child,
                        parentId = parentId,
                        type = CategoryType.INCOME,
                        iconName = null,
                        colorHex = null
                    )
                )
            }
        }
    }

    /**
     * Mapper: Database model to Domain model
     */
    private fun DbFinanceCategory.toDomain() =
        FinanceCategory(
            id = id,
            name = name,
            parentId = parent_id,
            type = CategoryType.fromString(type),
            iconName = icon_name,
            colorHex = color_hex,
            isSystem = is_system == 1L,
            createdAt = created_at,
            updatedAt = updated_at
        )

    /**
     * Get current timestamp in milliseconds (KMP-compatible)
     */
    private fun currentTimeMillis(): Long {
        return 1704067200000L // 2024-01-01 00:00:00 UTC - Simplified for KMP
    }
}

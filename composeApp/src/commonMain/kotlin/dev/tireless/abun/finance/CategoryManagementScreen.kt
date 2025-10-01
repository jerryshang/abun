package dev.tireless.abun.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * Category Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: FinanceCategoryViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedParentId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedParentId = null
                    showAddCategoryDialog = true
                }
            ) {
                Icon(Icons.Default.Add, "添加分类")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val expenseCategories = categories.filter { it.category.type == CategoryType.EXPENSE }
                    val incomeCategories = categories.filter { it.category.type == CategoryType.INCOME }

                    if (expenseCategories.isNotEmpty()) {
                        item {
                            Text(
                                text = "支出分类",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(expenseCategories) { categoryWithSubs ->
                            CategoryGroupCard(
                                categoryWithSubs = categoryWithSubs,
                                onAddSubcategory = {
                                    selectedParentId = categoryWithSubs.category.id
                                    showAddCategoryDialog = true
                                },
                                onDelete = { viewModel.deleteCategory(it) }
                            )
                        }
                    }

                    if (incomeCategories.isNotEmpty()) {
                        item {
                            Text(
                                text = "收入分类",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(incomeCategories) { categoryWithSubs ->
                            CategoryGroupCard(
                                categoryWithSubs = categoryWithSubs,
                                onAddSubcategory = {
                                    selectedParentId = categoryWithSubs.category.id
                                    showAddCategoryDialog = true
                                },
                                onDelete = { viewModel.deleteCategory(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            parentId = selectedParentId,
            onDismiss = {
                showAddCategoryDialog = false
                selectedParentId = null
            },
            onConfirm = { input ->
                viewModel.createCategory(input)
                showAddCategoryDialog = false
                selectedParentId = null
            }
        )
    }
}

/**
 * Category Group Card (parent with subcategories)
 */
@Composable
fun CategoryGroupCard(
    categoryWithSubs: CategoryWithSubcategories,
    onAddSubcategory: () -> Unit,
    onDelete: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryWithSubs.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddSubcategory) {
                    Text("添加子分类")
                }
            }

            if (categoryWithSubs.subcategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categoryWithSubs.subcategories.forEach { subCategory ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${subCategory.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Add Category Dialog
 */
@Composable
fun AddCategoryDialog(
    parentId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (CreateCategoryInput) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (parentId == null) "添加分类" else "添加子分类") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (parentId == null) {
                    Text("分类类型", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedType == CategoryType.EXPENSE,
                            onClick = { selectedType = CategoryType.EXPENSE },
                            label = { Text("支出") }
                        )
                        FilterChip(
                            selected = selectedType == CategoryType.INCOME,
                            onClick = { selectedType = CategoryType.INCOME },
                            label = { Text("收入") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        CreateCategoryInput(
                            name = name,
                            parentId = parentId,
                            type = selectedType
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

package dev.tireless.abun.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * Dialog for adding or editing a transaction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    transaction: Transaction? = null,
    accounts: List<Account>,
    categories: List<FinanceCategory> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (CreateTransactionInput) -> Unit,
    categoryViewModel: FinanceCategoryViewModel = koinInject()
) {
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var selectedAccountId by remember { mutableStateOf(transaction?.accountId ?: accounts.firstOrNull()?.id ?: 0L) }
    var selectedToAccountId by remember { mutableStateOf<Long?>(transaction?.toAccountId) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(transaction?.categoryId) }
    var payee by remember { mutableStateOf(transaction?.payee ?: "") }
    var member by remember { mutableStateOf(transaction?.member ?: "") }
    var notes by remember { mutableStateOf(transaction?.notes ?: "") }

    val allCategories by categoryViewModel.categories.collectAsState()

    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (transaction == null) "添加交易" else "编辑交易")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "关闭")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transaction Type Selector
                Text("交易类型", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    when (type) {
                                        TransactionType.EXPENSE -> "支出"
                                        TransactionType.INCOME -> "收入"
                                        TransactionType.TRANSFER -> "转账"
                                    }
                                )
                            }
                        )
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("¥") }
                )

                // Account Selector
                var expandedAccounts by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAccounts,
                    onExpandedChange = { expandedAccounts = it }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("账户") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccounts) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccounts,
                        onDismissRequest = { expandedAccounts = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    selectedAccountId = account.id
                                    expandedAccounts = false
                                }
                            )
                        }
                    }
                }

                // Transfer To Account (if transfer)
                if (selectedType == TransactionType.TRANSFER) {
                    var expandedToAccounts by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedToAccounts,
                        onExpandedChange = { expandedToAccounts = it }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedToAccountId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("转入账户") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedToAccounts) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedToAccounts,
                            onDismissRequest = { expandedToAccounts = false }
                        ) {
                            accounts.filter { it.id != selectedAccountId }.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text(account.name) },
                                    onClick = {
                                        selectedToAccountId = account.id
                                        expandedToAccounts = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category Selector (not for transfers)
                if (selectedType != TransactionType.TRANSFER) {
                    var expandedCategories by remember { mutableStateOf(false) }
                    val filteredCategories = allCategories.filter {
                        it.category.type.name == selectedType.name
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedCategories,
                        onExpandedChange = { expandedCategories = it }
                    ) {
                        OutlinedTextField(
                            value = filteredCategories
                                .flatMap { listOf(it.category) + it.subcategories }
                                .find { it.id == selectedCategoryId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("分类") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategories) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategories,
                            onDismissRequest = { expandedCategories = false }
                        ) {
                            filteredCategories.forEach { categoryWithSubs ->
                                // Parent category
                                DropdownMenuItem(
                                    text = { Text(categoryWithSubs.category.name) },
                                    onClick = {
                                        selectedCategoryId = categoryWithSubs.category.id
                                        expandedCategories = false
                                    }
                                )
                                // Subcategories
                                categoryWithSubs.subcategories.forEach { subCategory ->
                                    DropdownMenuItem(
                                        text = { Text("  ${subCategory.name}") },
                                        onClick = {
                                            selectedCategoryId = subCategory.id
                                            expandedCategories = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Payee/Source
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text(if (selectedType == TransactionType.INCOME) "来源" else "商家") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Member
                OutlinedTextField(
                    value = member,
                    onValueChange = { member = it },
                    label = { Text("成员") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0) {
                        onConfirm(
                            CreateTransactionInput(
                                amount = amountValue,
                                type = selectedType,
                                transactionDate = 1704067200000L, // 2024-01-01 00:00:00 UTC - Simplified for KMP
                                categoryId = selectedCategoryId,
                                accountId = selectedAccountId,
                                toAccountId = selectedToAccountId,
                                payee = payee.ifBlank { null },
                                member = member.ifBlank { null },
                                notes = notes.ifBlank { null },
                                tagIds = emptyList()
                            )
                        )
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
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

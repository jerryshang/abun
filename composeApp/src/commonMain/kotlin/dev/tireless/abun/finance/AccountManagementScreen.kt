package dev.tireless.abun.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Dot
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import dev.tireless.abun.navigation.Route
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject

/**
 * Account Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun AccountManagementScreen(
  navController: NavHostController,
  viewModel: AccountViewModel = koinInject(),
) {
  val accounts by viewModel.accounts.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  val accountsByParent = remember(accounts) { accounts.groupBy { it.parentId } }
  val expandedAccounts = remember { mutableStateMapOf<Long, Boolean>() }

  LaunchedEffect(accounts) {
    val ids = accounts.map { it.id }.toSet()
    expandedAccounts.keys.retainAll(ids)
    ids.forEach { id ->
      if (expandedAccounts[id] == null) {
        expandedAccounts[id] = false
      }
    }
  }

  val displayItems =
    remember(accounts, accountsByParent, expandedAccounts.toMap()) {
      buildAccountDisplayItems(
        accounts = accounts,
        accountsByParent = accountsByParent,
        expandedAccounts = expandedAccounts.toMap(),
      )
    }

  Scaffold(
    topBar = {
      TopAppBar(
        windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        title = { Text("Account Management") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.ArrowBack, "Back")
          }
        },
        actions = {
          IconButton(onClick = { navController.navigate(Route.AccountEdit(null)) }) {
            Icon(Lucide.Plus, contentDescription = "Add Account")
          }
        },
      )
    },
  ) { paddingValues ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues),
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center),
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(displayItems.size) { index ->
            val item = displayItems[index]
            AccountCard(
              account = item.account,
              depth = item.depth,
              hasChildren = item.hasChildren,
              isExpanded = item.isExpanded,
              onToggleExpand = {
                if (item.hasChildren) {
                  expandedAccounts[item.account.id] = !(expandedAccounts[item.account.id] ?: false)
                }
              },
              onClick = { navController.navigate(Route.AccountEdit(item.account.id)) },
            )
          }
        }
      }
    }
  }
}

/**
 * Account Card
 */
@Composable
fun AccountCard(
  account: AccountWithBalance,
  depth: Int,
  hasChildren: Boolean,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onClick: () -> Unit,
) {
  val accentColor = hexToColorOrNull(account.colorHex) ?: MaterialTheme.colorScheme.secondary
  val containerColor = accentColor.copy(alpha = 0.08f)
  val indent = 20.dp * depth

  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(start = indent)
        .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = containerColor),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (hasChildren) {
          IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
            Icon(
              imageVector = if (isExpanded) Lucide.ChevronDown else Lucide.ChevronRight,
              contentDescription = if (isExpanded) "Collapse" else "Expand",
            )
          }
        } else {
          Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Lucide.Dot,
              contentDescription = null,
            )
          }
        }
        Column {
          Text(
            text = account.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = account.currency,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (!account.isActive) {
        Text(
          text = "Disabled",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

private data class AccountDisplayItem(
  val account: AccountWithBalance,
  val depth: Int,
  val hasChildren: Boolean,
  val isExpanded: Boolean,
)

private fun buildAccountDisplayItems(
  accounts: List<AccountWithBalance>,
  accountsByParent: Map<Long?, List<AccountWithBalance>>,
  expandedAccounts: Map<Long, Boolean>,
): List<AccountDisplayItem> {
  val orderedItems = mutableListOf<AccountDisplayItem>()
  val visited = mutableSetOf<Long>()
  val accountLookup = accounts.associateBy { it.id }

  fun traverse(
    account: AccountWithBalance,
    depth: Int,
  ) {
    if (!visited.add(account.id)) return
    val children = accountsByParent[account.id]?.sortedBy { it.name } ?: emptyList()
    val hasChildren = children.isNotEmpty()
    val isExpanded = expandedAccounts[account.id] ?: false
    orderedItems += AccountDisplayItem(account, depth, hasChildren, isExpanded)
    if (hasChildren && isExpanded) {
      children.forEach { child -> traverse(child, depth + 1) }
    }
  }

  val rootAccounts = accountsByParent[null]?.sortedBy { it.name } ?: emptyList()
  rootAccounts.forEach { traverse(it, 0) }

  val orphanAccounts =
    accounts
      .asSequence()
      .filter { account ->
        val parentId = account.parentId
        parentId != null && parentId !in accountLookup
      }.sortedBy { it.name }
      .toList()

  orphanAccounts.forEach { traverse(it, 0) }

  return orderedItems
}

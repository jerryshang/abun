package dev.tireless.abun.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

/**
 * Filter type for account hierarchy selector
 */
enum class AccountFilter {
  /** Show all accounts */
  ALL,

  /** Show only Asset and Liability accounts (normal accounts) */
  NORMAL_ACCOUNTS,

  /** Show only Expense accounts */
  EXPENSES,

  /** Show only Revenue/Income accounts */
  REVENUE,
}

/**
 * Reusable hierarchical account selector with filtering capability
 *
 * @param accounts List of all accounts
 * @param filter Type of accounts to display
 * @param selectedAccountId Currently selected account ID (null for "All")
 * @param onAccountSelect Callback when an account is selected (null for "All")
 * @param expanded Whether the dropdown is expanded
 * @param onExpandedChange Callback when expansion state changes
 * @param showAllOption Whether to show "All" option (default true)
 */
@Composable
fun AccountHierarchySelector(
  accounts: List<AccountWithBalance>,
  filter: AccountFilter,
  selectedAccountId: Long?,
  onAccountSelect: (Long?) -> Unit,
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit,
  showAllOption: Boolean = true,
  allLabel: String = "All",
  modifier: Modifier = Modifier,
  menuWidthPx: Int? = null,
  accountLabel: (AccountWithBalance) -> String = { it.name },
  isAccountEnabled: (AccountWithBalance, Boolean) -> Boolean = { _, _ -> true },
  showRootLabels: Boolean = true,
) {
  val groupedAccounts = remember(accounts, filter) { buildGroupedHierarchy(accounts, filter) }
  val expansionState =
    remember(groupedAccounts) {
      mutableStateMapOf<String, Boolean>().apply {
        groupedAccounts.forEach { group ->
          put(group.groupKey, false)
        }
      }
    }

  val visibleItemsState =
    remember(groupedAccounts, showAllOption, allLabel, showRootLabels) {
      androidx.compose.runtime.derivedStateOf {
        buildVisibleItems(groupedAccounts, expansionState, showAllOption, allLabel, showRootLabels)
      }
    }
  val visibleItems by visibleItemsState

  val accountById = remember(accounts) { accounts.associateBy { it.id } }

  val density = LocalDensity.current
  var dropdownModifier = modifier
  if (menuWidthPx != null && menuWidthPx > 0) {
    dropdownModifier = dropdownModifier.width(with(density) { menuWidthPx.toDp() })
  }
  dropdownModifier = dropdownModifier.heightIn(max = 320.dp)

  val scrollState = rememberScrollState()

  LaunchedEffect(selectedAccountId, groupedAccounts) {
    val targetId = selectedAccountId ?: return@LaunchedEffect
    val rootIds = setOf(
      RootAccountIds.ASSET,
      RootAccountIds.LIABILITY,
      RootAccountIds.EQUITY,
      RootAccountIds.REVENUE,
      RootAccountIds.EXPENSE,
    )
    var currentAccount = accountById[targetId]
    while (currentAccount != null) {
      expansionState[accountKey(currentAccount.id)] = true
      val parentId = currentAccount.parentId
      if (parentId == null) {
        currentAccount = null
      } else if (parentId in rootIds) {
        expansionState[groupKey(parentId)] = true
        currentAccount = null
      } else {
        expansionState[accountKey(parentId)] = true
        currentAccount = accountById[parentId]
      }
    }
  }

  LaunchedEffect(expanded, selectedAccountId, visibleItems) {
    if (expanded) {
      val selectedIndex =
        visibleItems.indexOfFirst { item ->
          item is HierarchyMenuItem.Account && item.account.id == selectedAccountId
        }
      if (selectedIndex >= 0) {
        val approxItemHeightPx = with(density) { 48.dp.toPx() }
        val targetScroll = (selectedIndex * approxItemHeightPx).toInt().coerceAtLeast(0)
        val clamped = targetScroll.coerceIn(0, scrollState.maxValue)
        scrollState.scrollTo(clamped)
      }
    } else {
      scrollState.scrollTo(0)
    }
  }

  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { onExpandedChange(false) },
    modifier = dropdownModifier,
    properties = PopupProperties(focusable = true),
  ) {
    Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(scrollState)) {
      visibleItems.forEach { item ->
        when (item) {
          is HierarchyMenuItem.All ->
            DropdownMenuItem(
              text = {
                Text(
                  item.label,
                  fontWeight = if (selectedAccountId == null) FontWeight.Bold else FontWeight.Normal,
                )
              },
              onClick = {
                onAccountSelect(null)
                onExpandedChange(false)
              },
            )

          is HierarchyMenuItem.Group ->
            DropdownMenuItem(
              text = {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Start,
                ) {
                  Icon(
                    imageVector = if (item.expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (item.expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    item.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
              },
              onClick = {
                expansionState[item.groupKey] = !item.expanded
              },
            )

          is HierarchyMenuItem.Account -> {
            val accountEnabled = isAccountEnabled(item.account, item.hasChildren)
            DropdownMenuItem(
              text = {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Spacer(modifier = Modifier.width((item.depth * 16).dp))
                  if (item.hasChildren) {
                    IconButton(
                      onClick = {
                        expansionState[item.key] = !item.expanded
                      },
                      modifier = Modifier.size(32.dp),
                    ) {
                      Icon(
                        imageVector = if (item.expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = if (item.expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  } else {
                    Spacer(modifier = Modifier.width(32.dp))
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = accountLabel(item.account),
                    fontWeight = if (selectedAccountId == item.account.id) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                      if (accountEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                  )
                }
              },
              onClick = {
                if (accountEnabled) {
                  onAccountSelect(item.account.id)
                  onExpandedChange(false)
                }
              },
            )
          }
        }
      }
    }
  }
}

private data class AccountHierarchyGroup(
  val label: String,
  val rootId: Long,
  val nodes: List<AccountHierarchyNode>,
)

private data class AccountHierarchyNode(
  val account: AccountWithBalance,
  val children: List<AccountHierarchyNode>,
)

private sealed interface HierarchyMenuItem {
  val key: String

  data class All(val label: String) : HierarchyMenuItem {
    override val key: String = "all"
  }

  data class Group(
    val label: String,
    val groupKey: String,
    val expanded: Boolean,
  ) : HierarchyMenuItem {
    override val key: String = groupKey
  }

  data class Account(
    val account: AccountWithBalance,
    val depth: Int,
    val hasChildren: Boolean,
    val expanded: Boolean,
  ) : HierarchyMenuItem {
    override val key: String = "account-${account.id}"
  }
}

private fun buildGroupedHierarchy(
  accounts: List<AccountWithBalance>,
  filter: AccountFilter,
): List<AccountHierarchyGroup> {
  val accountsByParent = accounts.groupBy { it.parentId }

  fun buildNode(account: AccountWithBalance): AccountHierarchyNode {
    val children = accountsByParent[account.id].orEmpty().sortedBy { it.id }.map(::buildNode)
    return AccountHierarchyNode(account, children)
  }

  fun group(label: String, rootId: Long): AccountHierarchyGroup? {
    val roots = accountsByParent[rootId].orEmpty().sortedBy { it.id }.map(::buildNode)
    if (roots.isEmpty()) return null
    return AccountHierarchyGroup(label, rootId, roots)
  }

  return when (filter) {
    AccountFilter.ALL ->
      listOfNotNull(
        group("Assets", RootAccountIds.ASSET),
        group("Liabilities", RootAccountIds.LIABILITY),
        group("Revenue", RootAccountIds.REVENUE),
        group("Expenses", RootAccountIds.EXPENSE),
      )

    AccountFilter.NORMAL_ACCOUNTS ->
      listOfNotNull(
        group("Assets", RootAccountIds.ASSET),
        group("Liabilities", RootAccountIds.LIABILITY),
      )

    AccountFilter.EXPENSES ->
      listOfNotNull(group("Expenses", RootAccountIds.EXPENSE))

    AccountFilter.REVENUE ->
      listOfNotNull(group("Revenue", RootAccountIds.REVENUE))
  }
}

private fun buildVisibleItems(
  groups: List<AccountHierarchyGroup>,
  expansionState: Map<String, Boolean>,
  showAllOption: Boolean,
  allLabel: String,
  showRootLabels: Boolean,
): List<HierarchyMenuItem> {
  val items = mutableListOf<HierarchyMenuItem>()
  if (showAllOption) {
    items += HierarchyMenuItem.All(allLabel)
  }

  groups.forEach { group ->
    val groupExpanded = if (showRootLabels) expansionState[group.groupKey] ?: false else true
    if (showRootLabels) {
      items += HierarchyMenuItem.Group(group.label, group.groupKey, groupExpanded)
    }
    if (groupExpanded) {
      group.nodes.forEach { node ->
        addAccountItems(items, node, expansionState, depth = 0)
      }
    }
  }

  return items
}

private fun addAccountItems(
  target: MutableList<HierarchyMenuItem>,
  node: AccountHierarchyNode,
  expansionState: Map<String, Boolean>,
  depth: Int,
) {
  val expanded = expansionState[node.accountKey] ?: false
  val hasChildren = node.children.isNotEmpty()
  target += HierarchyMenuItem.Account(
    account = node.account,
    depth = depth,
    hasChildren = hasChildren,
    expanded = expanded,
  )
  if (hasChildren && expanded) {
    node.children.forEach { child ->
      addAccountItems(target, child, expansionState, depth + 1)
    }
  }
}

private val AccountHierarchyGroup.groupKey: String
  get() = groupKey(rootId)

private val AccountHierarchyNode.accountKey: String
  get() = accountKey(account.id)

private fun groupKey(rootId: Long): String = "group-$rootId"

private fun accountKey(accountId: Long): String = "account-$accountId"

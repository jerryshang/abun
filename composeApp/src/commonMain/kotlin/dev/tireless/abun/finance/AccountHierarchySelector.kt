package dev.tireless.abun.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
) {
  val groupedAccounts = remember(accounts, filter) { buildGroupedHierarchy(accounts, filter) }
  val groupsList = groupedAccounts.entries.toList()
  var expandedGroup by remember(groupsList) {
    mutableStateOf(if (groupsList.size <= 1) groupsList.firstOrNull()?.key else null)
  }

  val density = LocalDensity.current
  var dropdownModifier = modifier
  if (menuWidthPx != null && menuWidthPx > 0) {
    dropdownModifier = dropdownModifier.width(with(density) { menuWidthPx.toDp() })
  }
  dropdownModifier = dropdownModifier.heightIn(max = 320.dp)

  val visibleItems =
    remember(groupsList, expandedGroup, showAllOption, allLabel) {
      buildList<HierarchyMenuItem> {
        if (showAllOption) {
          add(HierarchyMenuItem.All(allLabel))
        }

        groupsList.forEach { (groupName, accountsInGroup) ->
          val showHeader = groupsList.size > 1
          if (showHeader) {
            add(HierarchyMenuItem.Header(groupName))
          }

          val shouldShowAccounts = expandedGroup == groupName || groupsList.size == 1
          if (shouldShowAccounts) {
            accountsInGroup.forEach { hierarchyItem ->
              add(HierarchyMenuItem.Account(hierarchyItem.account, hierarchyItem.depth))
            }
          }
        }
      }
    }

  val scrollState = rememberScrollState()

  LaunchedEffect(expanded, selectedAccountId, groupsList, visibleItems) {
    if (expanded) {
      val targetGroup =
        groupsList.firstOrNull { (_, items) ->
          items.any { it.account.id == selectedAccountId }
        }?.key
      if (targetGroup != null && expandedGroup != targetGroup) {
        expandedGroup = targetGroup
        return@LaunchedEffect
      }

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

          is HierarchyMenuItem.Header ->
            DropdownMenuItem(
              text = {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    item.groupName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                  Icon(
                    imageVector = if (expandedGroup == item.groupName) Icons.Default.KeyboardArrowDown else Icons.Default.ArrowDropDown,
                    contentDescription = if (expandedGroup == item.groupName) "Collapse" else "Expand",
                    modifier =
                      Modifier
                        .size(20.dp)
                        .rotate(if (expandedGroup == item.groupName) 0f else -90f),
                  )
                }
              },
              onClick = {
                expandedGroup = if (expandedGroup == item.groupName) null else item.groupName
              },
            )

          is HierarchyMenuItem.Account ->
            DropdownMenuItem(
              text = {
                Text(
                  text = item.account.name,
                  fontWeight = if (selectedAccountId == item.account.id) FontWeight.Bold else FontWeight.Normal,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.padding(start = (item.depth * 16).dp),
                )
              },
              onClick = {
                onAccountSelect(item.account.id)
                onExpandedChange(false)
              },
            )
        }
      }
    }
  }
}

private data class AccountHierarchyItem(val account: AccountWithBalance, val depth: Int)

private sealed interface HierarchyMenuItem {
  val key: String

  data class All(val label: String) : HierarchyMenuItem {
    override val key: String = "all"
  }

  data class Header(val groupName: String) : HierarchyMenuItem {
    override val key: String = "header-$groupName"
  }

  data class Account(val account: AccountWithBalance, val depth: Int) : HierarchyMenuItem {
    override val key: String = "account-${account.id}"
  }
}

private fun buildGroupedHierarchy(
  accounts: List<AccountWithBalance>,
  filter: AccountFilter,
): Map<String, List<AccountHierarchyItem>> {
  val accountsByParent = accounts.groupBy { it.parentId }

  fun buildChain(
    parents: List<AccountWithBalance>,
    depth: Int,
  ): List<AccountHierarchyItem> {
    if (parents.isEmpty()) return emptyList()
    return parents
      .sortedBy { it.name }
      .flatMap { account ->
        val children = accountsByParent[account.id].orEmpty()
        listOf(AccountHierarchyItem(account, depth)) + buildChain(children, depth + 1)
      }
  }

  fun group(label: String, rootId: Long): Pair<String, List<AccountHierarchyItem>>? {
    val parents = accountsByParent[rootId].orEmpty()
    if (parents.isEmpty()) return null
    return label to buildChain(parents, 0)
  }

  return when (filter) {
    AccountFilter.ALL ->
      listOfNotNull(
        group("Assets", RootAccountIds.ASSET),
        group("Liabilities", RootAccountIds.LIABILITY),
        group("Revenue", RootAccountIds.REVENUE),
        group("Expenses", RootAccountIds.EXPENSE),
      ).toMap()

    AccountFilter.NORMAL_ACCOUNTS ->
      listOfNotNull(
        group("Assets", RootAccountIds.ASSET),
        group("Liabilities", RootAccountIds.LIABILITY),
      ).toMap()

    AccountFilter.EXPENSES ->
      listOfNotNull(group("Expenses", RootAccountIds.EXPENSE)).toMap()

    AccountFilter.REVENUE ->
      listOfNotNull(group("Revenue", RootAccountIds.REVENUE)).toMap()
  }
}

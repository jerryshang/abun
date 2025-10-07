package dev.tireless.abun.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * @param onAccountSelected Callback when an account is selected (null for "All")
 * @param expanded Whether the dropdown is expanded
 * @param onExpandedChange Callback when expansion state changes
 * @param showAllOption Whether to show "All" option (default true)
 */
@Composable
fun AccountHierarchySelector(
  accounts: List<AccountWithBalance>,
  filter: AccountFilter,
  selectedAccountId: Long?,
  onAccountSelected: (Long?) -> Unit,
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit,
  showAllOption: Boolean = true,
  allLabel: String = "All",
  modifier: Modifier = Modifier,
  menuWidthPx: Int? = null,
) {
  // Filter accounts based on the filter type
  val filteredAccounts =
    remember(accounts, filter) {
      when (filter) {
        AccountFilter.ALL -> accounts
        AccountFilter.NORMAL_ACCOUNTS -> {
          accounts.filter { account ->
            account.parentId == RootAccountIds.ASSET || account.parentId == RootAccountIds.LIABILITY
          }
        }

        AccountFilter.EXPENSES -> {
          accounts.filter { account ->
            account.parentId == RootAccountIds.EXPENSE
          }
        }

        AccountFilter.REVENUE -> {
          accounts.filter { account ->
            account.parentId == RootAccountIds.REVENUE
          }
        }
      }
    }

  // Group accounts by parent category
  val groupedAccounts = remember(filteredAccounts, filter) {
    when (filter) {
      AccountFilter.ALL ->
        buildMap<String, List<AccountWithBalance>> {
          val assets = filteredAccounts.filter { it.parentId == RootAccountIds.ASSET }
          if (assets.isNotEmpty()) put("Assets", assets)
          val liabilities = filteredAccounts.filter { it.parentId == RootAccountIds.LIABILITY }
          if (liabilities.isNotEmpty()) put("Liabilities", liabilities)
          val revenue = filteredAccounts.filter { it.parentId == RootAccountIds.REVENUE }
          if (revenue.isNotEmpty()) put("Revenue", revenue)
          val expenses = filteredAccounts.filter { it.parentId == RootAccountIds.EXPENSE }
          if (expenses.isNotEmpty()) put("Expenses", expenses)
        }

      AccountFilter.NORMAL_ACCOUNTS ->
        buildMap {
          val assets = filteredAccounts.filter { it.parentId == RootAccountIds.ASSET }
          if (assets.isNotEmpty()) put("Assets", assets)
          val liabilities = filteredAccounts.filter { it.parentId == RootAccountIds.LIABILITY }
          if (liabilities.isNotEmpty()) put("Liabilities", liabilities)
        }

      AccountFilter.EXPENSES -> mapOf("Expenses" to filteredAccounts)
      AccountFilter.REVENUE -> mapOf("Revenue" to filteredAccounts)
    }
  }

  val groupKeys = groupedAccounts.keys.toList()
  var expandedGroup by remember(groupKeys) { mutableStateOf<String?>(null) }

  val density = LocalDensity.current
  val dropdownModifier = if (menuWidthPx != null && menuWidthPx > 0) {
    modifier.width(with(density) { menuWidthPx.toDp() })
  } else {
    modifier
  }

  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { onExpandedChange(false) },
    modifier = dropdownModifier,
  ) {
    // "All" option (if enabled)
    if (showAllOption) {
      DropdownMenuItem(
        text = {
          Text(
            allLabel,
            fontWeight = if (selectedAccountId == null) FontWeight.Bold else FontWeight.Normal,
          )
        },
        onClick = {
          onAccountSelected(null)
          onExpandedChange(false)
        },
      )
    }

    // Group sections with expandable children
    groupedAccounts.forEach { (groupName, groupAccounts) ->
      // Only show group header if there are multiple groups
      if (groupedAccounts.size > 1) {
        DropdownMenuItem(
          text = {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                groupName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
              )
              Icon(
                imageVector = if (expandedGroup == groupName) Icons.Default.KeyboardArrowDown else Icons.Default.ArrowDropDown,
                contentDescription = if (expandedGroup == groupName) "Collapse" else "Expand",
                modifier =
                Modifier
                  .size(20.dp)
                  .rotate(if (expandedGroup == groupName) 0f else -90f),
              )
            }
          },
          onClick = {
            expandedGroup = if (expandedGroup == groupName) null else groupName
          },
        )
      }

      // Show accounts if group is expanded (or if there's only one group)
      if (expandedGroup == groupName || groupedAccounts.size == 1) {
        groupAccounts.forEach { account ->
          DropdownMenuItem(
            text = {
              Text(
                text = if (groupedAccounts.size > 1) "  - ${account.name}" else account.name,
                fontWeight = if (selectedAccountId == account.id) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            },
            onClick = {
              onAccountSelected(account.id)
              onExpandedChange(false)
            },
          )
        }
      }
    }
  }
}

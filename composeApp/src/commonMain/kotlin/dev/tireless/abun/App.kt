package dev.tireless.abun

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Library
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package2
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Trash2
import dev.tireless.abun.finance.AccountEditScreen
import dev.tireless.abun.finance.AccountManagementScreen
import dev.tireless.abun.finance.AccountViewModel
import dev.tireless.abun.finance.ExpenseEditScreen
import dev.tireless.abun.finance.FinanceScreen
import dev.tireless.abun.finance.FutureViewScreen
import dev.tireless.abun.finance.LoanEditScreen
import dev.tireless.abun.finance.PriceComparator
import dev.tireless.abun.finance.RevenueEditScreen
import dev.tireless.abun.finance.SplitExpenseDraft
import dev.tireless.abun.finance.TransactionViewModel
import dev.tireless.abun.finance.TransferEditScreen
import dev.tireless.abun.finance.toEditPayload
import dev.tireless.abun.navigation.Route
import dev.tireless.abun.tags.Tag
import dev.tireless.abun.tags.TagDomain
import dev.tireless.abun.tags.TagDraft
import dev.tireless.abun.tags.TagManagementViewModel
import dev.tireless.abun.tags.TagUpdate
import dev.tireless.abun.tasks.TaskDashboardScreen
import dev.tireless.abun.notes.NotesHomeScreen
import dev.tireless.abun.time.CategoryManagementScreen
import dev.tireless.abun.time.TimeWorkspaceScreen
import dev.tireless.abun.ui.AppTheme
import dev.tireless.abun.ui.ThemePreference
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
  var themePreference by rememberSaveable { mutableStateOf(ThemePreference.SYSTEM) }

  AppTheme(themePreference = themePreference) {
    val navController = rememberNavController()

    Scaffold(
      bottomBar = {
        BottomNavigationBar(navController)
      },
    ) { paddingValues ->
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(paddingValues),
      ) {
        NavHost(
          navController = navController,
          startDestination = Route.Home,
        ) {
          // Main tabs
          composable<Route.Home> {
            HomeScreen(navController)
          }
          composable<Route.Material> {
            FinanceScreen(navController)
          }
          composable<Route.Mental> {
            NotesHomeScreen(navController)
          }
          composable<Route.Time> {
            TimeWorkspaceScreen(navController)
          }
          composable<Route.Settings> {
            SettingsScreen(
              navController = navController,
              themePreference = themePreference,
              onThemePreferenceChange = { themePreference = it },
            )
          }

          composable<Route.TaskPlanner> {
            TaskDashboardScreen(navController = navController)
          }

          // Finance sub-screens
          composable<Route.AccountManagement> {
            AccountManagementScreen(navController)
          }
          composable<Route.AccountEdit> { backStackEntry ->
            val route: Route.AccountEdit = backStackEntry.toRoute()
            val viewModel: AccountViewModel = koinInject()
            AccountEditScreen(
              navController = navController,
              accountId = route.accountId,
              viewModel = viewModel,
            )
          }
          composable<Route.ExpenseEdit> { backStackEntry ->
            val route: Route.ExpenseEdit = backStackEntry.toRoute()
            val viewModel: TransactionViewModel = koinInject()
            val accounts by viewModel.accounts.collectAsState()
            val loadedDraft by produceState<SplitExpenseDraft?>(
              initialValue = null,
              key1 = route.transactionId,
            ) {
              value = route.transactionId?.let { id -> viewModel.getSplitExpenseDraft(id) }
            }
            ExpenseEditScreen(
              navController = navController,
              accounts = accounts,
              existingDraft = loadedDraft,
              transactionId = route.transactionId,
              onCreate = { draft ->
                viewModel.createSplitExpense(draft)
              },
              onUpdate = { draft ->
                viewModel.updateSplitExpense(draft)
              },
            )
          }
          composable<Route.RevenueEdit> { backStackEntry ->
            val route: Route.RevenueEdit = backStackEntry.toRoute()
            val viewModel: TransactionViewModel = koinInject()
            val accounts by viewModel.accounts.collectAsState()
            val transactions by viewModel.transactions.collectAsState()
            val existingTransaction =
              route.transactionId?.let { id ->
                transactions.find { it.transaction.id == id }?.toEditPayload()
              }
            RevenueEditScreen(
              navController = navController,
              accounts = accounts,
              existingTransaction = existingTransaction,
              onCreate = { input ->
                viewModel.createTransaction(input)
              },
              onUpdate = { input ->
                viewModel.updateTransaction(input)
              },
            )
          }
          composable<Route.TransferEdit> { backStackEntry ->
            val route: Route.TransferEdit = backStackEntry.toRoute()
            val viewModel: TransactionViewModel = koinInject()
            val accounts by viewModel.accounts.collectAsState()
            val transactions by viewModel.transactions.collectAsState()
            val existingTransaction =
              route.transactionId?.let { id ->
                transactions.find { it.transaction.id == id }?.toEditPayload()
              }
            TransferEditScreen(
              navController = navController,
              accounts = accounts,
              existingTransaction = existingTransaction,
              onCreate = { input ->
                viewModel.createTransaction(input)
              },
              onUpdate = { input ->
                viewModel.updateTransaction(input)
              },
            )
          }
          composable<Route.LoanEdit> {
            val viewModel: TransactionViewModel = koinInject()
            val accounts by viewModel.accounts.collectAsState()
            LoanEditScreen(
              navController = navController,
              onConfirm = { input ->
                viewModel.createLoan(input)
              },
              accounts = accounts,
            )
          }
          composable<Route.PriceComparison> {
            PriceComparator(navController)
          }
          composable<Route.FutureView> {
            FutureViewScreen(navController)
          }

          // Timeblock sub-screens
          composable<Route.TimeCategoryManagement> {
            CategoryManagementScreen(navController)
          }
        }
      }
    }
  }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    contentColor = MaterialTheme.colorScheme.onSurface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    tonalElevation = 4.dp,
    shadowElevation = 16.dp,
  ) {
    val items = BottomNavItems
    val selectedIndex =
      items.indexOfFirst { item -> currentDestination.matchesAny(item.matchingRoutes) }
        .takeIf { it >= 0 } ?: 0

    NavigationBar(
      containerColor = Color.Transparent,
    ) {
      items.forEachIndexed { index, item ->
        NavigationBarItem(
          selected = index == selectedIndex,
          onClick = {
            if (!currentDestination.matchesAny(item.matchingRoutes)) {
              navController.navigate(item.target) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
              }
            }
          },
          icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
          alwaysShowLabel = false,
        )
      }
    }
  }
}

private data class BottomNavItem(
  val label: String,
  val icon: ImageVector,
  val target: Route,
  val matchingRoutes: Set<String>,
)

private fun NavDestination?.matchesAny(routes: Set<String>) = routes.any { matchesRoute(it) }

private fun NavDestination?.matchesRoute(route: String?): Boolean {
  if (route.isNullOrEmpty()) return false
  return this?.hierarchy?.any { destination ->
    destination.route?.startsWith(route) == true
  } ?: false
}

private val HomeRoutes = setOfNotNull(Route.Home::class.qualifiedName)
private val MaterialRoutes =
  setOfNotNull(
    Route.Material::class.qualifiedName,
    Route.AccountManagement::class.qualifiedName,
    Route.ExpenseEdit::class.qualifiedName,
    Route.RevenueEdit::class.qualifiedName,
    Route.TransferEdit::class.qualifiedName,
    Route.LoanEdit::class.qualifiedName,
    Route.PriceComparison::class.qualifiedName,
    Route.FutureView::class.qualifiedName,
    Route.AccountDetails::class.qualifiedName,
  )
private val MentalRoutes = setOfNotNull(Route.Mental::class.qualifiedName)
private val TimeRoutes =
  setOfNotNull(
    Route.Time::class.qualifiedName,
    Route.TimeCategoryManagement::class.qualifiedName,
    Route.TaskPlanner::class.qualifiedName,
  )
private val SettingsRoutes = setOfNotNull(Route.Settings::class.qualifiedName)

private val BottomNavItems = listOf(
  BottomNavItem(
    label = "Home",
    icon = Lucide.House,
    target = Route.Home,
    matchingRoutes = HomeRoutes,
  ),
  BottomNavItem(
    label = "Material",
    icon = Lucide.Package2,
    target = Route.Material,
    matchingRoutes = MaterialRoutes,
  ),
  BottomNavItem(
    label = "Mental",
    icon = Lucide.Library,
    target = Route.Mental,
    matchingRoutes = MentalRoutes,
  ),
  BottomNavItem(
    label = "Time",
    icon = Lucide.Calendar,
    target = Route.Time,
    matchingRoutes = TimeRoutes,
  ),
  BottomNavItem(
    label = "Settings",
    icon = Lucide.Settings,
    target = Route.Settings,
    matchingRoutes = SettingsRoutes,
  ),
)

@Composable
private fun HomeScreen(navController: NavHostController) {
  Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      Text("Welcome back", style = MaterialTheme.typography.headlineLarge)
      Text(
        "Choose where you'd like to focus today.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      HomeActionCard(
        title = "Review your schedule",
        description = "Check today's timeblocks and adjust upcoming plans.",
        icon = Lucide.Calendar,
        actionLabel = "Open time",
        onClick = { navController.navigate(Route.Time) },
      )

      HomeActionCard(
        title = "Capture quick notes",
        description = "Write ideas, meeting minutes, and references in one place.",
        icon = Lucide.Library,
        actionLabel = "Go to notes",
        onClick = { navController.navigate(Route.Mental) },
      )

      HomeActionCard(
        title = "Stay on top of finances",
        description = "Log transactions, track budgets, and review accounts.",
        icon = Lucide.Package2,
        actionLabel = "Open finance",
        onClick = { navController.navigate(Route.Material) },
      )

      Spacer(Modifier.weight(1f))

      Text(
        "Tip: you can switch between sections anytime using the bottom navigation.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun HomeActionCard(
  title: String,
  description: String,
  icon: ImageVector,
  actionLabel: String,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium)
      }
      Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      FilledTonalButton(onClick = onClick) {
        Text(actionLabel)
      }
    }
  }
}

@Composable
private fun SettingsScreen(
  navController: NavHostController,
  themePreference: ThemePreference,
  onThemePreferenceChange: (ThemePreference) -> Unit,
) {
  val tagViewModel: TagManagementViewModel = koinViewModel()

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      "Settings",
      style = MaterialTheme.typography.headlineLarge,
      modifier = Modifier.padding(bottom = 32.dp),
    )

    // Appearance Section
    Card(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
      ) {
        Text(
          "Appearance",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
          "Choose how the app adapts to light or dark mode.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 16.dp),
        )
        listOf(
          ThemePreference.SYSTEM,
          ThemePreference.LIGHT,
          ThemePreference.DARK,
        ).forEach { preference ->
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onThemePreferenceChange(preference) },
            verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(
              selected = themePreference == preference,
              onClick = { onThemePreferenceChange(preference) },
            )
            Column(
              modifier = Modifier.padding(start = 12.dp),
            ) {
              val title =
                when (preference) {
                  ThemePreference.SYSTEM -> "Use system theme"
                  ThemePreference.LIGHT -> "Light theme"
                  ThemePreference.DARK -> "Dark theme"
                }
              val description =
                when (preference) {
                  ThemePreference.SYSTEM -> "Match your device setting"
                  ThemePreference.LIGHT -> "Always use the light palette"
                  ThemePreference.DARK -> "Always use the dark palette"
                }
              Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
              )
              Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }

    Card(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text("Category Management", style = MaterialTheme.typography.titleLarge)
        Text(
          "Manage your timeblock categories and colors",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = { navController.navigate(Route.TimeCategoryManagement) }, modifier = Modifier.fillMaxWidth()) {
          Text("Manage Categories")
        }
      }
    }

    Text(
      "Other settings will be available here.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 24.dp),
    )

    TagManagementSection(tagViewModel)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagManagementSection(viewModel: TagManagementViewModel) {
  val tags by viewModel.tags.collectAsState()
  var editingTag by remember { mutableStateOf<Tag?>(null) }
  var showEditor by remember { mutableStateOf(false) }
  var deleteCandidate by remember { mutableStateOf<Tag?>(null) }

  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text("Tags", style = MaterialTheme.typography.titleLarge)
      Text(
        "Organize tasks and notes with reusable tags.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FilledTonalButton(
        onClick = {
          editingTag = null
          showEditor = true
        },
      ) {
        Icon(Lucide.Plus, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("New tag")
      }

      if (tags.isEmpty()) {
        Text(
          "No tags yet. Create your first tag to get started.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          tags.forEach { tag ->
            TagRow(
              tag = tag,
              onEdit = {
                editingTag = tag
                showEditor = true
              },
              onDelete = { deleteCandidate = tag },
            )
          }
        }
      }
    }
  }

  if (showEditor) {
    TagEditorDialog(
      existing = editingTag,
      onDismiss = {
        showEditor = false
        editingTag = null
      },
      onConfirm = { name, path, color, domains, description ->
        if (editingTag == null) {
          viewModel.createTag(
            TagDraft(
              name = name,
              path = path,
              colorHex = color,
              domains = domains,
              description = description,
            ),
          )
        } else {
          viewModel.updateTag(
            TagUpdate(
              id = editingTag!!.id,
              name = name,
              path = path,
              colorHex = color,
              domains = domains,
              description = description,
            ),
          )
        }
        showEditor = false
        editingTag = null
      },
    )
  }

  deleteCandidate?.let { tag ->
    TagDeleteDialog(
      tag = tag,
      onDismiss = { deleteCandidate = null },
      onConfirm = {
        viewModel.deleteTag(tag.id)
        deleteCandidate = null
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditorDialog(
  existing: Tag?,
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, Set<TagDomain>, String?) -> Unit,
) {
  var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
  var path by rememberSaveable { mutableStateOf(existing?.path ?: "") }
  var color by rememberSaveable { mutableStateOf(existing?.colorHex ?: "#1E88E5") }
  var description by rememberSaveable { mutableStateOf(existing?.description ?: "") }
  var domains by remember { mutableStateOf(existing?.domains ?: setOf(TagDomain.All)) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (existing == null) "New tag" else "Edit tag") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(value = path, onValueChange = { path = it }, label = { Text("Path") })
        OutlinedTextField(
          value = color,
          onValueChange = { input ->
            color = buildColorInput(input)
          },
          label = { Text("Color (hex)") },
          leadingIcon = {
            Box(
              modifier =
                Modifier
                  .height(20.dp)
                  .width(20.dp)
                  .background(colorFromHex(color), shape = MaterialTheme.shapes.small),
            )
          },
        )
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") })

        Text("Usage", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TagDomain.entries.forEach { domain ->
            AssistChip(
              onClick = {
                domains = toggleDomainSelection(domains, domain)
              },
              label = { Text(domain.displayName()) },
              colors = AssistChipDefaults.assistChipColors(containerColor = if (domain in domains || (TagDomain.All in domains && domain == TagDomain.All)) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val normalizedDomains = normalizeDomains(domains)
          val normalizedColor = normalizeHex(color)
          onConfirm(name.trim(), path.trim(), normalizedColor, normalizedDomains, description.trim().takeIf { it.isNotEmpty() })
        },
        enabled = name.isNotBlank() && path.isNotBlank(),
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
private fun TagDeleteDialog(
  tag: Tag,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Delete tag") },
    text = { Text("Are you sure you want to delete \"${tag.name}\"?") },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text("Delete") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagRow(
  tag: Tag,
  onEdit: (Tag) -> Unit,
  onDelete: (Tag) -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
        .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier
            .height(16.dp)
            .width(16.dp)
            .background(colorFromHex(tag.colorHex), MaterialTheme.shapes.small),
      )
      Column(Modifier.padding(start = 12.dp).weight(1f)) {
        Text(tag.name, style = MaterialTheme.typography.titleMedium)
        Text(tag.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      IconButton(onClick = { onEdit(tag) }) {
        Icon(Lucide.Pencil, contentDescription = "Edit tag")
      }
      IconButton(onClick = { onDelete(tag) }) {
        Icon(Lucide.Trash2, contentDescription = "Delete tag")
      }
    }

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      normalizeDomains(tag.domains).forEach { domain ->
        AssistChip(onClick = {}, label = { Text(domain.displayName()) })
      }
    }

    tag.description?.let {
      Text(it, style = MaterialTheme.typography.bodySmall)
    }
  }
}

private fun TagDomain.displayName(): String =
  when (this) {
    TagDomain.Tasks -> "Tasks"
    TagDomain.Notes -> "Notes"
    TagDomain.Finance -> "Finance"
    TagDomain.All -> "All modules"
  }

private fun toggleDomainSelection(current: Set<TagDomain>, domain: TagDomain): Set<TagDomain> {
  return when (domain) {
    TagDomain.All -> setOf(TagDomain.All)
    else -> {
      val next = current.toMutableSet()
      if (!next.add(domain)) {
        next.remove(domain)
      }
      next.remove(TagDomain.All)
      if (next.isEmpty()) setOf(TagDomain.All) else next
    }
  }
}

private fun normalizeDomains(domains: Set<TagDomain>): Set<TagDomain> {
  return if (domains.isEmpty() || TagDomain.All in domains) {
    setOf(TagDomain.All)
  } else {
    domains
  }
}

private fun buildColorInput(input: String): String {
  val cleaned = input.trim().removePrefix("#")
  val filtered = cleaned.filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
  val truncated = filtered.take(6)
  return "#${truncated.uppercase()}"
}

private fun normalizeHex(input: String): String {
  val cleaned = input.removePrefix("#")
  val filtered = cleaned.filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
  val padded = filtered.uppercase().padEnd(6, '0')
  return "#${padded.take(6)}"
}

private fun colorFromHex(hex: String): Color {
  val cleaned = hex.removePrefix("#")
  val parsed = cleaned.toLongOrNull(16) ?: return Color(0xFF888888)
  val argb = when (cleaned.length) {
    6 -> 0xFF000000L or parsed
    8 -> parsed
    else -> 0xFF888888
  }
  return Color(argb.toInt())
}

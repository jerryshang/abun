package dev.tireless.abun

import abun.composeapp.generated.resources.Res
import abun.composeapp.generated.resources.compose_multiplatform
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Library
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package2
import com.composables.icons.lucide.Settings
import dev.tireless.abun.finance.AccountDetailsScreen
import dev.tireless.abun.finance.AccountManagementScreen
import dev.tireless.abun.finance.ExpenseEditScreen
import dev.tireless.abun.finance.FinanceScreen
import dev.tireless.abun.finance.FutureViewScreen
import dev.tireless.abun.finance.LoanEditScreen
import dev.tireless.abun.finance.PriceComparator
import dev.tireless.abun.finance.RevenueEditScreen
import dev.tireless.abun.finance.SplitExpenseDraft
import dev.tireless.abun.finance.TransactionViewModel
import dev.tireless.abun.finance.toEditPayload
import dev.tireless.abun.finance.TransferEditScreen
import dev.tireless.abun.mental.QuoteViewModel
import dev.tireless.abun.mental.QuickNoteScreen
import dev.tireless.abun.navigation.Route
import dev.tireless.abun.time.CategoryManagementScreen
import dev.tireless.abun.time.TimeblockScreen
import dev.tireless.abun.material.AppTheme
import dev.tireless.abun.material.ThemePreference
import org.jetbrains.compose.resources.painterResource
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
          startDestination = Route.Material
        ) {
          // Main tabs
          composable<Route.Home> {
            HomeScreen()
          }
          composable<Route.Material> {
            FinanceScreen(navController)
          }
          composable<Route.Mental> {
            QuickNoteScreen()
          }
          composable<Route.Time> {
            TimeblockScreen(navController)
          }
          composable<Route.Settings> {
            SettingsScreen(
              navController = navController,
              themePreference = themePreference,
              onThemePreferenceChange = { themePreference = it }
            )
          }

          // Finance sub-screens
          composable<Route.AccountManagement> {
            AccountManagementScreen(navController)
          }
          composable<Route.ExpenseEdit> { backStackEntry ->
            val route: Route.ExpenseEdit = backStackEntry.toRoute()
            val viewModel: TransactionViewModel = koinInject()
            val accounts by viewModel.accounts.collectAsState()
            val loadedDraft by produceState<SplitExpenseDraft?>(
              initialValue = null,
              key1 = route.transactionId
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
              }
            )
          }
          composable<Route.RevenueEdit> { backStackEntry ->
            val route: Route.RevenueEdit = backStackEntry.toRoute()
            val viewModel: TransactionViewModel = koinInject()
            val accounts by viewModel.accounts.collectAsState()
            val transactions by viewModel.transactions.collectAsState()
            val existingTransaction = route.transactionId?.let { id ->
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
              }
            )
          }
          composable<Route.TransferEdit> { backStackEntry ->
            val route: Route.TransferEdit = backStackEntry.toRoute()
            val viewModel: TransactionViewModel = koinInject()
            val accounts by viewModel.accounts.collectAsState()
            val transactions by viewModel.transactions.collectAsState()
            val existingTransaction = route.transactionId?.let { id ->
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
              }
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
              accounts = accounts
            )
          }
          composable<Route.PriceComparison> {
            PriceComparator(navController)
          }
          composable<Route.FutureView> {
            FutureViewScreen(navController)
          }
          composable<Route.AccountDetails> { backStackEntry ->
            val accountDetails: Route.AccountDetails = backStackEntry.toRoute()
            AccountDetailsScreen(
              accountId = accountDetails.accountId,
              navController = navController
            )
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
    shadowElevation = 16.dp
  ) {
    NavigationBar(
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.onSurface
    ) {
      NavigationBarItem(
        icon = { Icon(imageVector = Lucide.House, contentDescription = "Home") },
        selected = currentDestination.matchesAny(HomeRoutes),
        onClick = {
          if (!currentDestination.matchesAny(HomeRoutes)) {
            navController.navigate(Route.Home) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        },
      )
      NavigationBarItem(
        icon = { Icon(imageVector = Lucide.Package2, contentDescription = "Material") },
        selected = currentDestination.matchesAny(MaterialRoutes),
        onClick = {
          if (!currentDestination.matchesAny(MaterialRoutes)) {
            navController.navigate(Route.Material) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        },
      )
      NavigationBarItem(
        icon = { Icon(imageVector = Lucide.Library, contentDescription = "Mental") },
        selected = currentDestination.matchesAny(MentalRoutes),
        onClick = {
          if (!currentDestination.matchesAny(MentalRoutes)) {
            navController.navigate(Route.Mental) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        },
      )
      NavigationBarItem(
        icon = { Icon(imageVector = Lucide.Calendar, contentDescription = "Time") },
        selected = currentDestination.matchesAny(TimeRoutes),
        onClick = {
          if (!currentDestination.matchesAny(TimeRoutes)) {
            navController.navigate(Route.Time) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        },
      )
      NavigationBarItem(
        icon = { Icon(imageVector = Lucide.Settings, contentDescription = "Settings") },
        selected = currentDestination.matchesAny(SettingsRoutes),
        onClick = {
          if (!currentDestination.matchesAny(SettingsRoutes)) {
            navController.navigate(Route.Settings) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        },
      )
    }
  }
}

private fun NavDestination?.matchesAny(routes: Set<String>) =
  routes.any { matchesRoute(it) }

private fun NavDestination?.matchesRoute(route: String?): Boolean {
  if (route.isNullOrEmpty()) return false
  return this?.hierarchy?.any { destination ->
    destination.route?.startsWith(route) == true
  } ?: false
}

private val HomeRoutes = setOfNotNull(Route.Home::class.qualifiedName)
private val MaterialRoutes = setOfNotNull(
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
private val TimeRoutes = setOfNotNull(
  Route.Time::class.qualifiedName,
  Route.TimeCategoryManagement::class.qualifiedName,
)
private val SettingsRoutes = setOfNotNull(Route.Settings::class.qualifiedName)

@Composable
private fun HomeScreen() {
  val greeting: Greeting = koinInject()
  val quoteViewModel: QuoteViewModel = koinViewModel()

  var showContent by remember { mutableStateOf(false) }

  val currentQuote by quoteViewModel.currentQuote.collectAsState()
  val isLoading by quoteViewModel.isLoading.collectAsState()

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Button(
        onClick = {
          showContent = !showContent
          quoteViewModel.loadRandomQuote()
        },
      ) {
        if (isLoading) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        } else {
          Text("Get New Quote!")
        }
      }

      AnimatedVisibility(showContent) {
        val greetingText = remember { greeting.greet() }
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Image(painterResource(Res.drawable.compose_multiplatform), null)
            Text(
              "Compose: $greetingText",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.padding(top = 12.dp)
            )
          }
        }
      }

      currentQuote?.let { quote ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "${quote.content}",
              style = MaterialTheme.typography.bodyLarge,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
            quote.source?.let { source ->
              Text(
                text = "— $source",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                  .padding(top = 8.dp)
                  .fillMaxWidth(),
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SettingsScreen(
  navController: NavHostController,
  themePreference: ThemePreference,
  onThemePreferenceChange: (ThemePreference) -> Unit
) {
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
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          "Appearance",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
          "Choose how the app adapts to light or dark mode.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        listOf(
          ThemePreference.SYSTEM,
          ThemePreference.LIGHT,
          ThemePreference.DARK
        ).forEach { preference ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
              .clickable { onThemePreferenceChange(preference) },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = themePreference == preference,
              onClick = { onThemePreferenceChange(preference) }
            )
            Column(
              modifier = Modifier.padding(start = 12.dp)
            ) {
              val title = when (preference) {
                ThemePreference.SYSTEM -> "Use system theme"
                ThemePreference.LIGHT -> "Light theme"
                ThemePreference.DARK -> "Dark theme"
              }
              val description = when (preference) {
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

    // Category Management Section
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          "Category Management",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
          "Manage your timeblock categories and colors",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
          onClick = { navController.navigate(Route.TimeCategoryManagement) },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Manage Categories")
        }
      }
    }

    Text(
      "Other settings will be available here.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(16.dp),
    )
  }
}

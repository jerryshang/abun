package dev.tireless.abun

import abun.composeapp.generated.resources.Res
import abun.composeapp.generated.resources.compose_multiplatform
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.tireless.abun.time.CategoryManagementScreen
import dev.tireless.abun.mental.QuoteViewModel
import dev.tireless.abun.material.PriceScreen
import dev.tireless.abun.time.TimeblockScreen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
  MaterialTheme {
    var selectedTab by remember { mutableIntStateOf(1) }

    Scaffold(
      bottomBar = {
        NavigationBar {
          NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Financial") },
            label = { Text("Financial") },
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.Schedule, contentDescription = "Timeblock") },
            label = { Text("Timeblock") },
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
          )
        }
      },
    ) { paddingValues ->
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(paddingValues),
      ) {
        when (selectedTab) {
          0 -> HomeScreen()
          1 -> PriceScreen()
          2 -> TimeblockScreen()
          3 -> SettingsScreen()
        }
      }
    }
  }
}

@Composable
private fun HomeScreen() {
  val greeting: Greeting = koinInject()
  val quoteViewModel: QuoteViewModel = koinViewModel()

  var showContent by remember { mutableStateOf(false) }

  val currentQuote by quoteViewModel.currentQuote.collectAsState()
  val isLoading by quoteViewModel.isLoading.collectAsState()

  Column(
    modifier =
      Modifier
        .background(MaterialTheme.colorScheme.primaryContainer)
        .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Button(
      onClick = {
        showContent = !showContent
        quoteViewModel.loadRandomQuote()
      },
    ) {
      if (isLoading) {
        CircularProgressIndicator()
      } else {
        Text("Get New Quote!")
      }
    }

    AnimatedVisibility(showContent) {
      val greetingText = remember { greeting.greet() }
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Image(painterResource(Res.drawable.compose_multiplatform), null)
        Text("Compose: $greetingText")
      }
    }

    // Display current quote
    currentQuote?.let { quote ->
      Text(
        text = "${quote.content} — ${quote.source ?: "Unknown"}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun SettingsScreen() {
  var showCategoryManagement by remember { mutableStateOf(false) }

  if (showCategoryManagement) {
    CategoryManagementScreen(
      onNavigateBack = { showCategoryManagement = false }
    )
  } else {
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
            onClick = { showCategoryManagement = true },
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
}

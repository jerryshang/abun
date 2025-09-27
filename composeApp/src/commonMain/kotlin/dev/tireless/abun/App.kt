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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
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
import dev.tireless.abun.viewmodel.QuoteViewModel
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
            onClick = { selectedTab = 0 }
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Financial") },
            label = { Text("Financial") },
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 }
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 }
          )
        }
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        when (selectedTab) {
          0 -> HomeScreen()
          1 -> PriceScreen()
          2 -> SettingsScreen()
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
      }
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
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      "Settings",
      style = MaterialTheme.typography.headlineLarge,
      modifier = Modifier.padding(top = 32.dp)
    )
    Text(
      "App settings and preferences will be available here.",
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(16.dp)
    )
  }
}

package dev.tireless.abun

import abun.composeapp.generated.resources.Res
import abun.composeapp.generated.resources.compose_multiplatform
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.tireless.abun.viewmodel.QuoteViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
  MaterialTheme {
    AppContent()
  }
}

@Composable
private fun AppContent() {
  val greeting: Greeting = koinInject()
  val quoteViewModel: QuoteViewModel = koinViewModel()

  var showContent by remember { mutableStateOf(false) }

  val currentQuote by quoteViewModel.currentQuote.collectAsState()
  val isLoading by quoteViewModel.isLoading.collectAsState()

  Column(
    modifier =
      Modifier
        .background(MaterialTheme.colorScheme.primaryContainer)
        .safeContentPadding()
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

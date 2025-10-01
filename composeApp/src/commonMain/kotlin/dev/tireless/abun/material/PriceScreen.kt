package dev.tireless.abun.material

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class PriceItem(
  val price: String = "",
  val quantity: String = "",
  val unitPrice: Double = 0.0,
)

@Composable
fun PriceScreen() {
  var priceItems by remember { mutableStateOf(listOf(PriceItem(), PriceItem())) }
  val focusManager = LocalFocusManager.current

  val validItems =
    priceItems.filter {
      it.price.isNotEmpty() && it.quantity.isNotEmpty() &&
        it.price.toDoubleOrNull() != null && it.quantity.toDoubleOrNull() != null &&
        it.quantity.toDoubleOrNull() != 0.0
    }

  val allInputsFilled =
    priceItems.all {
      it.price.isNotEmpty() && it.quantity.isNotEmpty() &&
        it.price.toDoubleOrNull() != null && it.quantity.toDoubleOrNull() != null
    }

  val minUnitPrice = validItems.minByOrNull { it.unitPrice }?.unitPrice
  val maxUnitPrice = validItems.maxByOrNull { it.unitPrice }?.unitPrice

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
        .clickable(
          indication = null,
          interactionSource = remember { MutableInteractionSource() },
        ) {
          focusManager.clearFocus()
        },
  ) {
    Text(
      "Price Comparison",
      style = MaterialTheme.typography.headlineLarge,
      modifier = Modifier.padding(bottom = 16.dp),
    )

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            "Price",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            "Quantity",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            "Unit Price",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
          Box(modifier = Modifier.width(48.dp))
        }
      }

      itemsIndexed(priceItems) { index, item ->
        PriceItemRow(
          item = item,
          onItemChange = { newItem ->
            priceItems =
              priceItems.toMutableList().apply {
                this[index] = newItem
              }
          },
          onDelete =
            if (priceItems.size > 2) {
              {
                priceItems =
                  priceItems.toMutableList().apply {
                    removeAt(index)
                  }
              }
            } else {
              null
            },
          backgroundColor =
            when {
              validItems.size > 1 && item.unitPrice == minUnitPrice && item.unitPrice > 0 ->
                Color.Green.copy(
                  alpha = 0.3f,
                )

              validItems.size > 1 && item.unitPrice == maxUnitPrice && item.unitPrice > 0 ->
                Color.Yellow.copy(
                  alpha = 0.3f,
                )

              else -> Color.Transparent
            },
        )
      }
    }

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Button(
        onClick = {
          focusManager.clearFocus()
          priceItems = priceItems + PriceItem()
        },
        enabled = allInputsFilled,
        modifier = Modifier.weight(1f),
      ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Row")
      }

      OutlinedButton(
        onClick = {
          focusManager.clearFocus()
          priceItems = listOf(PriceItem(), PriceItem())
        },
        modifier = Modifier.weight(1f),
      ) {
        Icon(Icons.Default.Clear, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Clear All")
      }
    }
  }
}

@Composable
private fun PriceItemRow(
  item: PriceItem,
  onItemChange: (PriceItem) -> Unit,
  onDelete: (() -> Unit)? = null,
  backgroundColor: Color = Color.Transparent,
) {
  val focusManager = LocalFocusManager.current

  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(backgroundColor),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(backgroundColor)
          .padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = item.price,
        onValueChange = { newPrice ->
          val price = newPrice.toDoubleOrNull() ?: 0.0
          val quantity = item.quantity.toDoubleOrNull() ?: 0.0
          val unitPrice = if (quantity != 0.0) price / quantity else 0.0
          onItemChange(item.copy(price = newPrice, unitPrice = unitPrice))
        },
        placeholder = { Text("0.00") },
        keyboardOptions =
          KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
          ),
        keyboardActions =
          KeyboardActions(
            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Right) },
            onDone = { focusManager.clearFocus() },
          ),
        singleLine = true,
        modifier = Modifier.weight(1f),
      )

      OutlinedTextField(
        value = item.quantity,
        onValueChange = { newQuantity ->
          val price = item.price.toDoubleOrNull() ?: 0.0
          val quantity = newQuantity.toDoubleOrNull() ?: 0.0
          val unitPrice = if (quantity != 0.0) price / quantity else 0.0
          onItemChange(item.copy(quantity = newQuantity, unitPrice = unitPrice))
        },
        placeholder = { Text("1") },
        keyboardOptions =
          KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
          ),
        keyboardActions =
          KeyboardActions(
            onDone = { focusManager.clearFocus() },
          ),
        singleLine = true,
        modifier = Modifier.weight(1f),
      )

      Text(
        text =
          if (item.unitPrice > 0) {
            val rounded = kotlin.math.round(item.unitPrice * 100) / 100.0
            "$rounded"
          } else {
            "-"
          },
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
      )

      if (onDelete != null) {
        IconButton(
          onClick = {
            focusManager.clearFocus()
            onDelete()
          },
          modifier = Modifier.size(48.dp),
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
      } else {
        Box(modifier = Modifier.size(48.dp))
      }
    }
  }
}

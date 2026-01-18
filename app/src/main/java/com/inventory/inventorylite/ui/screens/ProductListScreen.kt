package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventory.inventorylite.ui.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    vm: InventoryViewModel,
    onOpenProduct: (Long) -> Unit,
    onAddProduct: () -> Unit
) {
    val products by vm.products.collectAsStateWithLifecycle()
    var q by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Products") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(12.dp)
        ) {
            TextField(
                value = q,
                onValueChange = {
                    q = it
                    vm.setSearchQuery(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by name or SKU") }
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products, key = { it.id }) { p ->
                    val lowStock = p.stockOnHand <= p.reorderPoint
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProduct(p.id) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(p.name)
                                Text("Stock: ${p.stockOnHand}")
                            }
                            Text("SKU: ${p.sku}")
                            if (p.reorderPoint > 0) {
                                Text("Reorder point: ${p.reorderPoint}")
                            }
                            if (lowStock) {
                                Text("⚠ Low stock")
                            }
                        }
                    }
                }
            }
        }
    }
}

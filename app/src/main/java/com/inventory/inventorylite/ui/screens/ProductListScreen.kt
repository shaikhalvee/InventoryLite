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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventory.inventorylite.data.ProductWithStock
import com.inventory.inventorylite.ui.InventoryViewModel

@Composable
fun ProductListScreen(
    vm: InventoryViewModel,
    onOpenProduct: (Long) -> Unit,
    onAddProduct: () -> Unit,
    onOpenUsers: () -> Unit
) {
    val products by vm.products.collectAsStateWithLifecycle()
    var q by remember { mutableStateOf("") }

    ProductListContent(
        products = products,
        searchQuery = q,
        onSearchQueryChange = {
            q = it
            vm.setSearchQuery(it)
        },
        onSignOut = { vm.signOut() },
        onOpenUsers = onOpenUsers,
        onOpenProduct = onOpenProduct,
        onAddProduct = onAddProduct
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListContent(
    products: List<ProductWithStock>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSignOut: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onAddProduct: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                actions = {
                    TextButton(onClick = onSignOut) { Text("Logout") }
                    TextButton(onClick = onOpenUsers) { Text("Users") }
                }
            )
        },
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
                value = searchQuery,
                onValueChange = onSearchQueryChange,
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

@Preview(showBackground = true)
@Composable
fun ProductListPreview() {
    Surface {
        ProductListContent(
            products = listOf(
                ProductWithStock(1, "SKU001", "Laptop Pro", "Description", 1200.0, 5, true, 0, 0, 15),
                ProductWithStock(2, "SKU002", "Wireless Mouse", "Description", 25.0, 10, true, 0, 0, 4),
                ProductWithStock(3, "SKU003", "USB-C Cable", "Description", 15.0, 0, true, 0, 0, 50)
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            onSignOut = {},
            onOpenUsers = {},
            onOpenProduct = {},
            onAddProduct = {}
        )
    }
}

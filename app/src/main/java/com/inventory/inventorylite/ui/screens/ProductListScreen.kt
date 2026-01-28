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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventory.inventorylite.data.ProductWithStock
import com.inventory.inventorylite.data.Role
import com.inventory.inventorylite.ui.InventoryViewModel

@Composable
fun ProductListScreen(
    vm: InventoryViewModel,
    onOpenProduct: (Long) -> Unit,
    onAddProduct: () -> Unit,
    onOpenUsers: () -> Unit
) {
    val products by vm.products.collectAsStateWithLifecycle()
    val session by vm.sessionUser.collectAsStateWithLifecycle()
    var q by remember { mutableStateOf("") }

    val aiUi by vm.aiUi.collectAsState()
    val filtered by vm.aiFilteredProducts.collectAsState()
    val changes by vm.changes.collectAsState()

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
        onAddProduct = onAddProduct,
        canAddProduct = session?.role != Role.VIEWER,
        canManageUsers = session?.role == Role.ADMIN
    )
}

@Composable
fun AiQueryBar(
    query: String,
    error: String?,
    onQueryChange: (String) -> Unit,
    onRun: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Ask inventory") },
            placeholder = { Text("e.g., Show low-stock items under 10 units") },
            singleLine = true
        )
        Row {
            Button(onClick = onRun) { Text("Run") }
        }
        if (error != null) {
            Text(text = error)
        }
    }
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
    onAddProduct: () -> Unit,
    canAddProduct: Boolean = true,
    canManageUsers: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Products",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSignOut) { Text("Logout") }
                    if (canManageUsers) {
                        TextButton(onClick = onOpenUsers) { Text("Users") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (canAddProduct) {
                FloatingActionButton(onClick = onAddProduct) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { pad ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search by name or SKU") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { p ->
                        val lowStock = p.reorderPoint > 0 && p.stockOnHand <= p.reorderPoint
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenProduct(p.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = p.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Stock ${p.stockOnHand}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "SKU: ${p.sku}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (p.reorderPoint > 0) {
                                    Text(
                                        text = "Reorder point: ${p.reorderPoint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (lowStock) {
                                    Text(
                                        text = "Low stock",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
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

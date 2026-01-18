package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventory.inventorylite.ui.InventoryViewModel
import com.inventory.inventorylite.ui.OpResult
import com.inventory.inventorylite.ui.ProductDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    vm: InventoryViewModel,
    productId: Long?,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val title = if (productId == null) "Add product" else "Edit product"

    val productFlow = remember(productId) {
        productId?.let { vm.observeProductWithStock(it) }
    }
    val product by productFlow?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }

    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var unitCost by remember { mutableStateOf("0.0") }
    var reorderPoint by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId, product) {
        if (productId != null && product != null) {
            sku = product!!.sku
            name = product!!.name
            description = product!!.description
            unitCost = product!!.unitCost.toString()
            reorderPoint = product!!.reorderPoint.toString()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(12.dp)
        ) {
            TextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU (unique)") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / notes") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = unitCost,
                onValueChange = { unitCost = it },
                label = { Text("Unit cost") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = reorderPoint,
                onValueChange = { reorderPoint = it },
                label = { Text("Reorder point") },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text("Error: $error")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val cost = unitCost.trim().toDoubleOrNull() ?: -1.0
                    val rp = reorderPoint.trim().toIntOrNull() ?: Int.MIN_VALUE
                    vm.saveProduct(
                        ProductDraft(
                            id = productId,
                            sku = sku,
                            name = name,
                            description = description,
                            unitCost = cost,
                            reorderPoint = rp
                        )
                    ) { res ->
                        when (res) {
                            is OpResult.Ok -> {
                                error = null
                                onDone()
                            }
                            is OpResult.Error -> error = res.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

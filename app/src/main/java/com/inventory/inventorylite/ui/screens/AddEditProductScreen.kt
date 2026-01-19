package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventory.inventorylite.ui.InventoryViewModel
import com.inventory.inventorylite.ui.OpResult
import com.inventory.inventorylite.ui.ProductDraft

@Composable
fun AddEditProductScreen(
    vm: InventoryViewModel,
    productId: Long?,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
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

    AddEditProductContent(
        title = if (productId == null) "Add product" else "Edit product",
        sku = sku,
        onSkuChange = { sku = it },
        name = name,
        onNameChange = { name = it },
        description = description,
        onDescriptionChange = { description = it },
        unitCost = unitCost,
        onUnitCostChange = { unitCost = it },
        reorderPoint = reorderPoint,
        onReorderPointChange = { reorderPoint = it },
        error = error,
        onSave = {
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
        onCancel = onCancel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductContent(
    title: String,
    sku: String,
    onSkuChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    unitCost: String,
    onUnitCostChange: (String) -> Unit,
    reorderPoint: String,
    onReorderPointChange: (String) -> Unit,
    error: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Helper to block Enter unless Shift is held
    val shiftEnterModifier = Modifier.onPreviewKeyEvent {
        if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
            !it.isShiftPressed // Return true (handled) if shift is NOT pressed
        } else false
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
                onValueChange = onSkuChange,
                label = { Text("SKU (unique)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth().then(shiftEnterModifier)
            )
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description / notes") },
                modifier = Modifier.fillMaxWidth().then(shiftEnterModifier)
            )
            TextField(
                value = unitCost,
                onValueChange = onUnitCostChange,
                label = { Text("Unit cost") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = reorderPoint,
                onValueChange = onReorderPointChange,
                label = { Text("Reorder point") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text("Error: $error", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSave,
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

@Preview(showBackground = true)
@Composable
fun AddProductPreview() {
    Surface {
        AddEditProductContent(
            title = "Add product",
            sku = "",
            onSkuChange = {},
            name = "",
            onNameChange = {},
            description = "",
            onDescriptionChange = {},
            unitCost = "0.0",
            onUnitCostChange = {},
            reorderPoint = "0",
            onReorderPointChange = {},
            error = null,
            onSave = {},
            onCancel = {}
        )
    }
}

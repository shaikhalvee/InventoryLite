package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.inventory.inventorylite.data.MovementType
import com.inventory.inventorylite.ui.InventoryViewModel
import com.inventory.inventorylite.ui.OpResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    vm: InventoryViewModel,
    productId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val productFlow = remember(productId) { vm.observeProductWithStock(productId) }
    val movementsFlow = remember(productId) { vm.observeMovements(productId) }

    val product by productFlow.collectAsStateWithLifecycle(initialValue = null)
    val movements by movementsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var showDelete by remember { mutableStateOf(false) }
    var movementType by remember { mutableStateOf<MovementType?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    if (product == null) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Product") }) }
        ) { pad ->
            Column(Modifier.padding(pad).padding(12.dp)) {
                Text("Product not found (it may have been deleted).")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Back") }
            }
        }
        return
    }

    val p = product!!

    if (movementType != null) {
        MovementDialog(
            type = movementType!!,
            onDismiss = { movementType = null },
            onSubmit = { qty, note ->
                vm.addMovement(productId, movementType!!, qty, note) { res ->
                    when (res) {
                        is OpResult.Ok -> {
                            movementType = null
                            error = null
                        }
                        is OpResult.Error -> error = res.message
                    }
                }
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete product?") },
            text = { Text("This will also delete its movement history.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteProduct(productId, onResult = { res ->
                        when (res) {
                            is OpResult.Ok -> {
                                error = null
                                showDelete = false
                                onBack()
                            }
                            is OpResult.Error -> {
                                error = res.message
                                showDelete = false
                            }
                        }
                    })
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(p.name) }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(12.dp)
        ) {
            Text("SKU: ${p.sku}")
            Text("Stock on hand: ${p.stockOnHand}")
            if (p.reorderPoint > 0) Text("Reorder point: ${p.reorderPoint}")
            if (p.description.isNotBlank()) Text("Notes: ${p.description}")
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { movementType = MovementType.IN }, modifier = Modifier.weight(1f)) { Text("IN") }
                Button(onClick = { movementType = MovementType.OUT }, modifier = Modifier.weight(1f)) { Text("OUT") }
                Button(onClick = { movementType = MovementType.ADJUST }, modifier = Modifier.weight(1f)) { Text("ADJUST") }
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
                Button(onClick = { showDelete = true }, modifier = Modifier.weight(1f)) { Text("Delete") }
                Button(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            }

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text("Error: $error")
            }

            Spacer(Modifier.height(14.dp))
            Text("Movement history")
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movements, key = { it.id }) { m ->
                    val sign = when (m.type) {
                        MovementType.OUT -> "-"
                        else -> if (m.quantity >= 0) "+" else ""
                    }
                    Text("${m.type}: $sign${m.quantity}  ${if (m.note.isBlank()) "" else "(${m.note})"}")
                }
            }
        }
    }
}


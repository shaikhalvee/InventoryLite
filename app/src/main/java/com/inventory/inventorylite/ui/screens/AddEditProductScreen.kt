package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
        title = if (productId == null) "Add Product" else "Edit Product",
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
    val scrollState = rememberScrollState()

    // Helper to block Enter unless Shift is held
    val shiftEnterModifier = Modifier.onPreviewKeyEvent {
        if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
            !it.isShiftPressed
        } else false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { pad ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(pad)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Product Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = sku,
                            onValueChange = onSkuChange,
                            label = { Text("SKU (unique)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = onNameChange,
                            label = { Text("Product Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(shiftEnterModifier),
                            leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                            shape = MaterialTheme.shapes.medium,
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = onDescriptionChange,
                            label = { Text("Description / Notes") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(shiftEnterModifier),
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            shape = MaterialTheme.shapes.medium,
                            minLines = 3,
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = unitCost,
                                onValueChange = onUnitCostChange,
                                label = { Text("Unit Cost") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    autoCorrect = false,
                                    imeAction = ImeAction.Next
                                ),
                                shape = MaterialTheme.shapes.medium
                            )

                            OutlinedTextField(
                                value = reorderPoint,
                                onValueChange = onReorderPointChange,
                                label = { Text("Reorder Pt") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.NotificationImportant, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    autoCorrect = false,
                                    imeAction = ImeAction.Done
                                ),
                                shape = MaterialTheme.shapes.medium
                            )
                        }
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Save")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductPreview() {
    MaterialTheme {
        AddEditProductContent(
            title = "Add Product",
            sku = "SKU123",
            onSkuChange = {},
            name = "Test Product",
            onNameChange = {},
            description = "This is a test description for the product.",
            onDescriptionChange = {},
            unitCost = "19.99",
            onUnitCostChange = {},
            reorderPoint = "10",
            onReorderPointChange = {},
            error = "Invalid SKU provided",
            onSave = {},
            onCancel = {}
        )
    }
}

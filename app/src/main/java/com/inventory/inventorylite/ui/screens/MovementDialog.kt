package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.inventory.inventorylite.data.MovementType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementDialog(
    type: MovementType,
    onDismiss: () -> Unit,
    onSubmit: (quantity: Int, note: String) -> Unit
) {
    var qtyText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val title = when (type) {
        MovementType.IN -> "Receive stock (IN)"
        MovementType.OUT -> "Issue stock (OUT)"
        MovementType.ADJUST -> "Adjust stock (±)"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity") },
                    placeholder = {
                        Text(if (type == MovementType.ADJUST) "e.g., -3 or 5" else "e.g., 10")
                    }
                )
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val q = qtyText.trim().toIntOrNull()
                if (q != null) onSubmit(q, note)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

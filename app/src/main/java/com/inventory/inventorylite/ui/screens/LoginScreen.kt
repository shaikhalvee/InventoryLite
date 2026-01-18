package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inventory.inventorylite.ui.InventoryViewModel
import com.inventory.inventorylite.ui.OpResult

@Composable
fun LoginScreen(vm: InventoryViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Login", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(onClick = {
            error = null
            vm.signIn(username, password.toCharArray()) { res ->
                if (res is OpResult.Error) error = res.message
            }
        }) { Text("Sign in") }

        Text("Default admin: admin / admin123")
    }
}

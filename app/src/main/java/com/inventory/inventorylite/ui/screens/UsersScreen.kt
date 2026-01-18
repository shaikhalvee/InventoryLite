package com.inventory.inventorylite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventory.inventorylite.data.Role
import com.inventory.inventorylite.ui.InventoryViewModel
import com.inventory.inventorylite.ui.OpResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(vm: InventoryViewModel, onBack: () -> Unit) {
    val users by vm.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.VIEWER) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users (Admin)") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    TextButton(onClick = { vm.signOut() }) { Text("Logout") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Create user", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = role == Role.VIEWER, onClick = { role = Role.VIEWER }, label = { Text("VIEWER") })
                FilterChip(selected = role == Role.CLERK, onClick = { role = Role.CLERK }, label = { Text("CLERK") })
                FilterChip(selected = role == Role.ADMIN, onClick = { role = Role.ADMIN }, label = { Text("ADMIN") })
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = {
                error = null
                vm.createUser(username, password.toCharArray(), role) { res ->
                    when (res) {
                        is OpResult.Ok -> { username = ""; password = "" }
                        is OpResult.Error -> error = res.message
                    }
                }
            }) { Text("Create") }

            Divider()

            Text("Existing users", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users) { u ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(u.username)
                            Text("Role: ${u.role}  Active: ${u.isActive}")
                        }
                    }
                }
            }
        }
    }
}

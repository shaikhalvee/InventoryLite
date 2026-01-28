package com.inventory.inventorylite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventory.inventorylite.ui.InventoryViewModel
import com.inventory.inventorylite.ui.theme.InventoryLiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppGraph.init(applicationContext)

        setContent {
            InventoryLiteTheme {
                val vm: InventoryViewModel = viewModel(factory = InventoryViewModel.Factory)
                InventoryApp(vm)
            }
        }
    }
}

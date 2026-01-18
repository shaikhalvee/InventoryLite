package com.inventory.inventorylite

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.inventory.inventorylite.ui.InventoryViewModel
import com.inventory.inventorylite.ui.screens.AddEditProductScreen
import com.inventory.inventorylite.ui.screens.ProductDetailScreen
import com.inventory.inventorylite.ui.screens.ProductListScreen

private object Routes {
    const val PRODUCTS = "products"
    const val PRODUCT_DETAIL = "product/{id}"
    const val PRODUCT_EDIT = "product/edit/{id}"
    const val PRODUCT_NEW = "product/new"
}

@Composable
fun InventoryApp(vm: InventoryViewModel) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.PRODUCTS) {
        composable(Routes.PRODUCTS) {
            ProductListScreen(
                vm = vm,
                onOpenProduct = { id -> nav.navigate("product/$id") },
                onAddProduct = { nav.navigate(Routes.PRODUCT_NEW) }
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: return@composable
            ProductDetailScreen(
                vm = vm,
                productId = id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("product/edit/$id") }
            )
        }

        composable(Routes.PRODUCT_NEW) {
            AddEditProductScreen(
                vm = vm,
                productId = null,
                onDone = { nav.popBackStack() },
                onCancel = { nav.popBackStack() }
            )
        }

        composable(
            route = Routes.PRODUCT_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: return@composable
            AddEditProductScreen(
                vm = vm,
                productId = id,
                onDone = { nav.popBackStack() },
                onCancel = { nav.popBackStack() }
            )
        }
    }
}

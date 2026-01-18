package com.inventory.inventorylite

import android.content.Context
import com.inventory.inventorylite.data.AppDatabase
import com.inventory.inventorylite.data.AuthRepository
import com.inventory.inventorylite.data.InventoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppGraph {
    private lateinit var db: AppDatabase
    lateinit var repo: InventoryRepository
        private set
    lateinit var auth: AuthRepository
        private set

    fun init(context: Context) {
        db = AppDatabase.build(context.applicationContext)
        repo = InventoryRepository(db.inventoryDao())
        auth = AuthRepository(db.authDao())

        CoroutineScope(Dispatchers.IO).launch {
            auth.ensureDefaultAdmin()
        }
    }
}

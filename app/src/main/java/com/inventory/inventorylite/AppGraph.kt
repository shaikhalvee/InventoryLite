package com.inventory.inventorylite

import android.content.Context
import com.inventory.inventorylite.data.AppDatabase
import com.inventory.inventorylite.data.InventoryRepository

object AppGraph {
    private lateinit var db: AppDatabase
    lateinit var repo: InventoryRepository
        private set

    fun init(context: Context) {
        db = AppDatabase.build(context)
        repo = InventoryRepository(db.inventoryDao())
    }
}
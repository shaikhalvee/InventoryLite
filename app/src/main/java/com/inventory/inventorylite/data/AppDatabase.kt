package com.inventory.inventorylite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProductEntity::class, StockMovementEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "inventorylite.db")
                .addCallback(SeedCallback(context.applicationContext))
                .build()
        }
    }

    private class SeedCallback(
        private val appContext: Context
    ) : Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed using a one-off DB instance built from the same name, but without callback recursion.
            CoroutineScope(Dispatchers.IO).launch {
                val seededDb = Room.databaseBuilder(appContext, AppDatabase::class.java, "inventorylite.db")
                    .build()
                val dao = seededDb.inventoryDao()

                val now = System.currentTimeMillis()
                val p1 = ProductEntity(
                    sku = "SKU-1001",
                    name = "USB-C Cable 1m",
                    description = "Basic charging/data cable",
                    unitCost = 3.50,
                    reorderPoint = 10,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
                val p2 = ProductEntity(
                    sku = "SKU-2001",
                    name = "Notebook A5",
                    description = "200 pages",
                    unitCost = 1.20,
                    reorderPoint = 25,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
                val id1 = dao.insertProduct(p1)
                val id2 = dao.insertProduct(p2)

                dao.insertMovement(
                    StockMovementEntity(
                        productId = id1,
                        type = MovementType.IN,
                        quantity = 50,
                        note = "Initial stock",
                        timestampEpochMs = now
                    )
                )
                dao.insertMovement(
                    StockMovementEntity(
                        productId = id2,
                        type = MovementType.IN,
                        quantity = 80,
                        note = "Initial stock",
                        timestampEpochMs = now
                    )
                )

                seededDb.close()
            }
        }
    }
}


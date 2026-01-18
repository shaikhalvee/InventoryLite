package com.inventory.inventorylite.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MovementType { IN, OUT, ADJUST }

@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val name: String,
    val description: String = "",
    val unitCost: Double = 0.0,
    val reorderPoint: Int = 0,
    val isActive: Boolean = true,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: MovementType,
    /**
     * IN: positive
     * OUT: positive (we subtract it in stock computation)
     * ADJUST: can be positive or negative
     */
    val quantity: Int,
    val note: String = "",
    val timestampEpochMs: Long
)

data class ProductWithStock(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String,
    val unitCost: Double,
    val reorderPoint: Int,
    val isActive: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val stockOnHand: Int
)
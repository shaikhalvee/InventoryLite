package com.inventory.inventorylite.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // Stock-on-hand is computed from movements:
    // OUT subtracts quantity; IN and ADJUST add quantity (ADJUST can be negative)
    @Query(
        """
        SELECT 
          p.id, p.sku, p.name, p.description, p.unitCost, p.reorderPoint, p.isActive, 
          p.createdAtEpochMs, p.updatedAtEpochMs,
          COALESCE(SUM(
            CASE 
              WHEN m.type = 'OUT' THEN -m.quantity
              ELSE m.quantity
            END
          ), 0) AS stockOnHand
        FROM products p
        LEFT JOIN stock_movements m ON m.productId = p.id
        GROUP BY p.id
        ORDER BY p.name COLLATE NOCASE
        """
    )
    fun observeProductsWithStock(): Flow<List<ProductWithStock>>

    @Query(
        """
        SELECT 
          p.id, p.sku, p.name, p.description, p.unitCost, p.reorderPoint, p.isActive, 
          p.createdAtEpochMs, p.updatedAtEpochMs,
          COALESCE(SUM(
            CASE 
              WHEN m.type = 'OUT' THEN -m.quantity
              ELSE m.quantity
            END
          ), 0) AS stockOnHand
        FROM products p
        LEFT JOIN stock_movements m ON m.productId = p.id
        WHERE p.id = :productId
        GROUP BY p.id
        LIMIT 1
        """
    )
    fun observeProductWithStock(productId: Long): Flow<ProductWithStock?>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun productCount(): Int


    @Query(
        """
        SELECT * FROM stock_movements
        WHERE productId = :productId
        ORDER BY timestampEpochMs DESC
        """
    )
    fun observeMovements(productId: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMovement(movement: StockMovementEntity)
}
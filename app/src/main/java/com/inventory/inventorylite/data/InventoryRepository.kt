package com.inventory.inventorylite.data

import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val dao: InventoryDao
) {
    fun observeProductsWithStock(): Flow<List<ProductWithStock>> = dao.observeProductsWithStock()
    fun observeProductWithStock(productId: Long): Flow<ProductWithStock?> = dao.observeProductWithStock(productId)
    fun observeMovements(productId: Long): Flow<List<StockMovementEntity>> = dao.observeMovements(productId)

    suspend fun getProductById(productId: Long): ProductEntity? = dao.getProductById(productId)

    suspend fun insertProduct(product: ProductEntity): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    suspend fun insertMovement(movement: StockMovementEntity) = dao.insertMovement(movement)
}

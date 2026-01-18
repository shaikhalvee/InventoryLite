package com.inventory.inventorylite.ui

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventory.inventorylite.AppGraph
import com.inventory.inventorylite.data.InventoryRepository
import com.inventory.inventorylite.data.MovementType
import com.inventory.inventorylite.data.ProductEntity
import com.inventory.inventorylite.data.ProductWithStock
import com.inventory.inventorylite.data.StockMovementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductDraft(
    val id: Long? = null,
    val sku: String,
    val name: String,
    val description: String,
    val unitCost: Double,
    val reorderPoint: Int,
    val isActive: Boolean = true
)

sealed interface OpResult {
    data object Ok : OpResult
    data class Error(val message: String) : OpResult
}

class InventoryViewModel(
    private val repo: InventoryRepository
) : ViewModel() {

    private val search = MutableStateFlow("")

    val products = repo.observeProductsWithStock()
        .combine(search) { list, q ->
            val t = q.trim()
            if (t.isEmpty()) list
            else list.filter {
                it.name.contains(t, ignoreCase = true) || it.sku.contains(t, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(q: String) {
        search.value = q
    }

    fun observeProductWithStock(productId: Long): Flow<ProductWithStock?> =
        repo.observeProductWithStock(productId)

    fun observeMovements(productId: Long): Flow<List<StockMovementEntity>> =
        repo.observeMovements(productId)

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            val entity = repo.getProductById(productId) ?: return@launch
            repo.deleteProduct(entity)
        }
    }

    fun saveProduct(draft: ProductDraft, onResult: (OpResult) -> Unit) {
        viewModelScope.launch {
            val sku = draft.sku.trim()
            val name = draft.name.trim()

            if (sku.isEmpty()) {
                onResult(OpResult.Error("SKU is required."))
                return@launch
            }
            if (name.isEmpty()) {
                onResult(OpResult.Error("Name is required."))
                return@launch
            }
            if (draft.reorderPoint < 0) {
                onResult(OpResult.Error("Reorder point cannot be negative."))
                return@launch
            }
            if (draft.unitCost < 0.0) {
                onResult(OpResult.Error("Unit cost cannot be negative."))
                return@launch
            }

            val now = System.currentTimeMillis()

            try {
                if (draft.id == null) {
                    repo.insertProduct(
                        ProductEntity(
                            sku = sku,
                            name = name,
                            description = draft.description.trim(),
                            unitCost = draft.unitCost,
                            reorderPoint = draft.reorderPoint,
                            isActive = draft.isActive,
                            createdAtEpochMs = now,
                            updatedAtEpochMs = now
                        )
                    )
                } else {
                    val existing = repo.getProductById(draft.id) ?: run {
                        onResult(OpResult.Error("Product not found."))
                        return@launch
                    }
                    repo.updateProduct(
                        existing.copy(
                            sku = sku,
                            name = name,
                            description = draft.description.trim(),
                            unitCost = draft.unitCost,
                            reorderPoint = draft.reorderPoint,
                            isActive = draft.isActive,
                            updatedAtEpochMs = now
                        )
                    )
                }
                onResult(OpResult.Ok)
            } catch (e: SQLiteConstraintException) {
                onResult(OpResult.Error("SKU already exists (must be unique)."))
            } catch (e: Exception) {
                onResult(OpResult.Error("Save failed: ${e.message ?: "unknown error"}"))
            }
        }
    }

    fun addMovement(
        productId: Long,
        type: MovementType,
        quantityRaw: Int,
        note: String,
        onResult: (OpResult) -> Unit
    ) {
        viewModelScope.launch {
            if (type != MovementType.ADJUST && quantityRaw <= 0) {
                onResult(OpResult.Error("Quantity must be > 0 for IN/OUT."))
                return@launch
            }
            if (type == MovementType.ADJUST && quantityRaw == 0) {
                onResult(OpResult.Error("Adjustment cannot be 0."))
                return@launch
            }

            val qty = when (type) {
                MovementType.IN -> quantityRaw
                MovementType.OUT -> kotlin.math.abs(quantityRaw)
                MovementType.ADJUST -> quantityRaw
            }

            try {
                repo.insertMovement(
                    StockMovementEntity(
                        productId = productId,
                        type = type,
                        quantity = qty,
                        note = note.trim(),
                        timestampEpochMs = System.currentTimeMillis()
                    )
                )
                onResult(OpResult.Ok)
            } catch (e: Exception) {
                onResult(OpResult.Error("Movement failed: ${e.message ?: "unknown error"}"))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InventoryViewModel(AppGraph.repo) as T
            }
        }
    }
}


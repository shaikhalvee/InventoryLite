package com.inventory.inventorylite.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiInterpretRequest(
    val query: String,
    val nowEpochMs: Long,
    val timezone: String
)

@Serializable
sealed class AiIntent {
    @Serializable
    @SerialName("products")
    data class Products(
        val filter: ProductFilter = ProductFilter(),
        val sort: ProductSort = ProductSort.STOCK_ASC,
        val limit: Int? = null
    ) : AiIntent()

    @Serializable
    @SerialName("changes")
    data class Changes(
        val sinceEpochMs: Long
    ) : AiIntent()
}

@Serializable
data class ProductFilter(
    val lowStock: Boolean = false,
    val maxStockOnHand: Int? = null,
    val skuContains: String? = null,
    val nameContains: String? = null,
    val activeOnly: Boolean = true
)

@Serializable
enum class ProductSort { STOCK_ASC, STOCK_DESC, NAME_ASC, NAME_DESC }

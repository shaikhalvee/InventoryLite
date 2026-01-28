package com.inventory.inventorylite.ai

/**
 * Deterministic parser for the two assignment queries.
 * Keeps the app working even if LLM backend is unavailable.
 */
object LocalIntentParser {

    private val lowStockRegex =
        Regex("\\blow\\s*stock\\b(?:.*?\\bunder\\s*(\\d+)\\b)?", RegexOption.IGNORE_CASE)

    private val changedTodayRegex =
        Regex("\\b(what\\s*changed\\s*today|changed\\s*today|today\\s*changes)\\b", RegexOption.IGNORE_CASE)

    fun tryParse(query: String, todayStartEpochMs: Long): AiIntent? {
        val q = query.trim()

        lowStockRegex.find(q)?.let { m ->
            val max = m.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }?.toIntOrNull()
            return AiIntent.Products(
                filter = ProductFilter(
                    lowStock = true,
                    maxStockOnHand = max,
                    activeOnly = true
                ),
                sort = ProductSort.STOCK_ASC
            )
        }

        if (changedTodayRegex.containsMatchIn(q)) {
            return AiIntent.Changes(sinceEpochMs = todayStartEpochMs)
        }

        return null
    }
}

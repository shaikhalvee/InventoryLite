package com.inventory.inventorylite.ai

/**
 * AiRepository turns a user's natural-language query into an [AiIntent].
 *
 * Strategy:
 *  1) Try deterministic parsing (LocalIntentParser).
 *  2) Otherwise call remote service (AiService) for structured AiIntent.
 *  3) If remote is disabled or fails, fall back to name search.
 */
class AiRepository(
    private val service: AiService?,
    private val timezone: String = "America/Chicago",
    private val remoteEnabled: Boolean = true
) {

    suspend fun interpret(
        query: String,
        nowEpochMs: Long,
        todayStartEpochMs: Long
    ): AiIntent {
        val q = query.trim()
        if (q.isEmpty()) return AiIntent.Products()

        // Fast path for your assignment queries
        LocalIntentParser.tryParse(q, todayStartEpochMs)?.let { return it }

        val fallback = fallbackIntent(q)
        if (!remoteEnabled || service == null) return fallback

        return try {
            service.interpret(
                AiInterpretRequest(
                    query = q,
                    nowEpochMs = nowEpochMs,
                    timezone = timezone
                )
            )
        } catch (_: Exception) {
            fallback
        }
    }

    private fun fallbackIntent(q: String): AiIntent {
        return AiIntent.Products(
            filter = ProductFilter(nameContains = q, activeOnly = true),
            sort = ProductSort.NAME_ASC
        )
    }
}

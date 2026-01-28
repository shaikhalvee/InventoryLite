package com.inventory.inventorylite.ai

interface AiService {
    suspend fun interpret(request: AiInterpretRequest): AiIntent
}

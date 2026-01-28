package com.inventory.inventorylite.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.client.request.contentType

class HttpAiService(
    private val client: HttpClient,
    private val baseUrl: String, // e.g., "https://YOUR_BACKEND_DOMAIN"
) : AiService {

    override suspend fun interpret(request: AiInterpretRequest): AiIntent {
        return client.post("$baseUrl/api/ai/interpret") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

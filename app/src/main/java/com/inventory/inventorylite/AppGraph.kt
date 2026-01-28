package com.inventory.inventorylite

import android.content.Context
import com.inventory.inventorylite.ai.AiRepository
import com.inventory.inventorylite.ai.HttpAiService
import com.inventory.inventorylite.ai.HttpClientFactory
import com.inventory.inventorylite.data.AppDatabase
import com.inventory.inventorylite.data.AuthRepository
import com.inventory.inventorylite.data.InventoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppGraph {
    private lateinit var db: AppDatabase

    lateinit var repo: InventoryRepository
        private set

    lateinit var auth: AuthRepository
        private set

    lateinit var ai: AiRepository
        private set

    /**
     * Leave placeholder if you don't have a backend yet.
     * The assignment queries will still work via LocalIntentParser.
     */
    private const val AI_BASE_URL = "https://YOUR_BACKEND_DOMAIN"

    fun init(context: Context) {
        db = AppDatabase.build(context.applicationContext)

        repo = InventoryRepository(db.inventoryDao())
        auth = AuthRepository(db.authDao())

        val httpClient = HttpClientFactory.create()
        val aiService = HttpAiService(httpClient, baseUrl = AI_BASE_URL)

        val remoteEnabled = AI_BASE_URL != "https://YOUR_BACKEND_DOMAIN"
        ai = AiRepository(
            service = aiService,
            timezone = "America/Chicago",
            remoteEnabled = remoteEnabled
        )

        CoroutineScope(Dispatchers.IO).launch {
            auth.ensureDefaultAdmin()
        }
    }
}

package com.example.tfg

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Storage

object AppwriteClient {
    lateinit var client: Client
    lateinit var storage: Storage

    fun init(context: Context) {
        client = Client(context)
            .setEndpoint("https://<TU_ENDPOINT>/v1")
            .setProject("<TU_PROJECT_ID>")
            .setSelfSigned(true) // Solo en desarrollo

        storage = Storage(client)
    }
}

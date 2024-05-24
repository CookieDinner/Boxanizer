package com.cookiedinner.boxanizer.core.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiClient {
    private val client = HttpClient().config {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun checkForUpdates(): List<ReleaseInfo> {
        val response = client.get("https://api.github.com/repos/CookieDinner/Boxanizer/releases") {
            contentType(ContentType.Application.Json)
        }
        return response.body<List<ReleaseInfo>>()
    }
}
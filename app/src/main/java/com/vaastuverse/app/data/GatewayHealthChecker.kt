package com.vaastuverse.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object GatewayHealthChecker {
    suspend fun ping(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(ApiConfig.healthUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                requestMethod = "GET"
            }
            try {
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?.take(500)
                    ?: ""
                if (code !in 200..299) {
                    error("HTTP $code — $body")
                }
                body.ifBlank { "HTTP $code OK" }
            } finally {
                connection.disconnect()
            }
        }
    }
}

package com.vaastuverse.app.data

import android.os.Build
import com.vaastuverse.app.BuildConfig

/**
 * Base URL for **api-gateway** (Docker on Windows desktop).
 *
 * - **Emulator** on the same PC as Docker: `http://10.0.2.2:8080`
 * - **Physical device** on Wi‑Fi: `http://<PC-LAN-IP>:8080` via [BuildConfig.GATEWAY_HOST]
 *
 * Set `vaastuverse.gatewayHost` in `local.properties`. See [CONNECT_ANDROID_WINDOWS.md].
 */
object ApiConfig {
    private const val GATEWAY_PORT = 8080

    val gatewayBaseUrl: String
        get() {
            val host = if (isEmulator()) "10.0.2.2" else BuildConfig.GATEWAY_HOST
            return "http://$host:$GATEWAY_PORT"
        }

    val healthUrl: String get() = "$gatewayBaseUrl/actuator/health"

    val authDocsUrl: String get() = "$gatewayBaseUrl/auth-docs/swagger-ui/index.html"

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        return fingerprint.startsWith("generic")
            || fingerprint.startsWith("unknown")
            || Build.MODEL.contains("google_sdk", ignoreCase = true)
            || Build.MODEL.contains("Emulator", ignoreCase = true)
            || Build.MODEL.contains("Android SDK built for x86", ignoreCase = true)
            || Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
            || Build.HARDWARE.contains("goldfish", ignoreCase = true)
            || Build.HARDWARE.contains("ranchu", ignoreCase = true)
            || Build.PRODUCT.contains("sdk_gphone", ignoreCase = true)
    }
}

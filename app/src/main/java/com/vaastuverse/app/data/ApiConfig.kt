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
            val configured = BuildConfig.GATEWAY_HOST.trim()
            // Emulator default is host PC (10.0.2.2). If local.properties points at a remote
            // staging IP (EC2), use that on emulator too — otherwise OTP never hits AWS.
            val host = if (isEmulator() && !isRemoteStagingHost(configured)) "10.0.2.2" else configured
            return "http://$host:$GATEWAY_PORT"
        }

    /** True when gatewayHost is a public/staging host, not a LAN dev machine. */
    private fun isRemoteStagingHost(host: String): Boolean {
        if (host.isBlank()) return false
        if (host == "10.0.2.2" || host == "localhost" || host == "127.0.0.1") return false
        if (host.startsWith("192.168.") || (host.startsWith("10.") && host != "10.0.2.2")) return false
        // Numeric public IPv4 from local.properties (e.g. EC2 elastic IP)
        return host.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))
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

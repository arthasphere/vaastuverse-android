package com.vaastuverse.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vaastuverse.app.data.ApiConfig
import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.dto.PropertyFieldSuggestionsResponse
import com.vaastuverse.app.data.dto.PropertyUploadResponse
import com.vaastuverse.app.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class PropertyUploadRepository(private val context: Context) {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun upload(
        session: StoredSession,
        fieldKey: String,
        uriString: String,
        onProgress: (Int) -> Unit,
    ): PropertyUploadResponse = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val tempFile = copyToTemp(uri)
        try {
            val fileBody = tempFile.asRequestBody(guessMime(uri).toMediaTypeOrNull())
            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fieldKey", fieldKey)
                .addFormDataPart("file", tempFile.name, fileBody)
                .build()

            val request = Request.Builder()
                .url("${ApiConfig.gatewayBaseUrl}/api/v1/users/${session.userId}/property-uploads")
                .header("Authorization", ApiClient.bearer(session.accessToken))
                .post(multipart)
                .build()

            onProgress(10)
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                onProgress(100)
                if (!response.isSuccessful) {
                    val message = parseError(body) ?: "Upload failed (${response.code})"
                    throw IllegalStateException(message)
                }
                gson.fromJson(body, PropertyUploadResponse::class.java)
                    ?: throw IllegalStateException("Invalid upload response")
            }
        } finally {
            runCatching { tempFile.delete() }
        }
    }

    private fun copyToTemp(uri: Uri): File {
        val suffix = context.contentResolver.getType(uri)?.substringAfter('/') ?: "bin"
        val temp = File.createTempFile("vv-upload-", ".$suffix", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            temp.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Could not read selected file")
        return temp
    }

    private fun guessMime(uri: Uri): String =
        context.contentResolver.getType(uri) ?: "application/octet-stream"

    suspend fun suggestDetails(
        session: StoredSession,
        uploadId: String,
        propertyType: String,
    ): PropertyFieldSuggestionsResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(
                "${ApiConfig.gatewayBaseUrl}/api/v1/users/${session.userId}/property-uploads/$uploadId/suggest-details?propertyType=$propertyType"
            )
            .header("Authorization", ApiClient.bearer(session.accessToken))
            .post(okhttp3.RequestBody.create(null, ByteArray(0)))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = parseError(body) ?: "Could not read document suggestions (${response.code})"
                throw IllegalStateException(message)
            }
            gson.fromJson(body, PropertyFieldSuggestionsResponse::class.java)
                ?: PropertyFieldSuggestionsResponse(uploadId, "", emptyMap())
        }
    }

    private fun parseError(body: String): String? = runCatching {
        val json = gson.fromJson(body, JsonObject::class.java)
        json?.get("message")?.asString ?: json?.get("error")?.asString
    }.getOrNull()
}

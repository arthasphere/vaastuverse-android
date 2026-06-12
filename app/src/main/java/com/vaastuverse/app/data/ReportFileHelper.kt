package com.vaastuverse.app.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object ReportFileHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun downloadAndSave(context: Context, downloadUrl: String, orderId: String): Uri =
        withContext(Dispatchers.IO) {
            val response = client.newCall(Request.Builder().url(downloadUrl).get().build()).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Download failed (${response.code})")
            }
            val bytes = response.body?.bytes() ?: throw IllegalStateException("Empty report file")
            val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
            val localFile = File(reportsDir, "vaastu-report-$orderId.pdf")
            localFile.writeBytes(bytes)
            copyToDownloads(context, localFile, orderId)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile,
            )
        }

    private fun copyToDownloads(context: Context, source: File, orderId: String) {
        val displayName = "VaastuVerse-Report-$orderId.pdf"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
        resolver.openOutputStream(uri)?.use { out ->
            source.inputStream().use { input -> input.copyTo(out) }
        }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }

    fun openLocalPdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openPreviewUrl(context: Context, previewUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(previewUrl), "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun findCachedReport(context: Context, orderId: String): Uri? {
        val file = File(File(context.filesDir, "reports"), "vaastu-report-$orderId.pdf")
        if (!file.exists() || file.length() == 0L) return null
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}

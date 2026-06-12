package com.vaastuverse.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.vaastuverse.app.data.ApiConfig
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.GuruTier
import com.vaastuverse.app.data.FeatureFlags
import com.vaastuverse.app.data.OrderKind
import com.vaastuverse.app.data.OrderPage
import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.dto.CustomerOrderRequest
import com.vaastuverse.app.data.dto.LinkPropertyRequest
import com.vaastuverse.app.data.dto.CustomerOrderResponse
import com.vaastuverse.app.data.PropertyValidationException
import com.vaastuverse.app.data.dto.DeliverReportRequest
import com.vaastuverse.app.data.isClosed
import com.vaastuverse.app.data.isLocalOnly
import com.vaastuverse.app.data.isOpen
import com.vaastuverse.app.data.newLocalOrderId
import com.vaastuverse.app.data.normalized
import com.vaastuverse.app.data.network.ApiClient
import com.vaastuverse.app.ui.customer.CustomerUseCaseId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import android.net.Uri
import com.vaastuverse.app.data.ReportFileHelper
import java.io.File
import java.util.concurrent.TimeUnit

private val Context.orderDataStore by preferencesDataStore("customer_orders")

class OrderRepository(private val context: Context) {
    private val gson = Gson()
    private val api get() = ApiClient.api
    private val uploadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .build()

    fun ordersFlow(userId: String): Flow<List<CustomerOrder>> =
        context.orderDataStore.data.map { prefs ->
            sortedOrders(decode(prefs[stringPreferencesKey(key(userId))]))
        }

    fun openOrdersPageFlow(userId: String, limit: Int = ORDER_PAGE_SIZE): Flow<List<CustomerOrder>> =
        ordersFlow(userId).map { orders ->
            orders.filter { it.isOpen() }.take(limit)
        }

    suspend fun getOrderPage(
        userId: String,
        open: Boolean,
        page: Int,
        pageSize: Int = ORDER_PAGE_SIZE,
    ): OrderPage {
        val filtered = loadSorted(userId).filter { order ->
            if (open) order.isOpen() else order.isClosed()
        }
        val start = page * pageSize
        val items = filtered.drop(start).take(pageSize)
        return OrderPage(
            items = items,
            page = page,
            hasMore = start + items.size < filtered.size,
            totalCount = filtered.size,
        )
    }

    suspend fun loadSorted(userId: String): List<CustomerOrder> =
        sortedOrders(decode(context.orderDataStore.data.first()[stringPreferencesKey(key(userId))]))

    suspend fun syncFromServer(session: StoredSession): List<CustomerOrder> {
        return try {
            refreshFeatureFlags()
            val local = loadSorted(session.userId)
            local.filter { it.isLocalOnly() }.forEach { order ->
                pushToServer(session, order, create = true)?.let { upsertLocal(session.userId, it) }
            }
            val remote = api.listCustomerOrders(
                session.userId,
                ApiClient.bearer(session.accessToken),
            ).map(::fromResponse)
            mergeRemoteWithLocal(session.userId, remote)
            loadSorted(session.userId)
        } catch (_: Exception) {
            loadSorted(session.userId)
        }
    }

    suspend fun addOrder(session: StoredSession, order: CustomerOrder): CustomerOrder {
        val now = System.currentTimeMillis()
        val local = order.normalized().copy(
            id = if (order.id.isBlank()) newLocalOrderId() else order.id,
            lastUpdatedAt = now,
            createdAt = if (order.createdAt > 0L) order.createdAt else now,
        )
        val saved = pushToServer(session, local, create = true) ?: local
        upsertLocal(session.userId, saved)
        return saved
    }

    suspend fun updateOrder(
        session: StoredSession,
        orderId: String,
        transform: (CustomerOrder) -> CustomerOrder,
    ): CustomerOrder? {
        val current = loadSorted(session.userId).find { it.id == orderId } ?: return null
        val now = System.currentTimeMillis()
        val updated = transform(current).normalized().copy(lastUpdatedAt = now)
        val saved = pushToServer(session, updated, create = updated.isLocalOnly())
            ?: throw PropertyValidationException("Could not save order update")
        upsertLocal(session.userId, saved)
        return saved
    }

    suspend fun linkPropertyToOrder(
        session: StoredSession,
        orderId: String,
        propertyId: String,
        buyerDifferentFromUser: Boolean,
        buyerFullName: String?,
        buyerDateOfBirth: String?,
    ): CustomerOrder {
        return try {
            val response = api.linkPropertyToOrder(
                session.userId,
                orderId,
                ApiClient.bearer(session.accessToken),
                LinkPropertyRequest(
                    propertyId = propertyId,
                    buyerDifferentFromUser = buyerDifferentFromUser,
                    buyerFullName = buyerFullName,
                    buyerDateOfBirth = buyerDateOfBirth,
                ),
            )
            val linked = fromResponse(response)
            upsertLocal(session.userId, linked)
            linked
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string().orEmpty()
            val message = parseApiError(body) ?: "Could not link property to order"
            if (e.code() in 400..499) throw PropertyValidationException(message)
            throw IllegalStateException(message)
        }
    }

    suspend fun upgradeToGurujiValidation(session: StoredSession, orderId: String): CustomerOrder? {
        return try {
            val response = api.upgradeOrderToGuruji(
                session.userId,
                orderId,
                ApiClient.bearer(session.accessToken),
            )
            val upgraded = fromResponse(response)
            upsertLocal(session.userId, upgraded)
            loadSorted(session.userId).find { it.id == orderId }?.let { source ->
                upsertLocal(session.userId, source.copy(upgradeEligible = false))
            }
            upgraded
        } catch (_: Exception) {
            null
        }
    }

    suspend fun refreshFeatureFlags() {
        runCatching {
            FeatureFlags.updateGuruMatching(api.getGuruMatchingFeatures())
        }
    }

    suspend fun listGurujiPendingOrders(session: StoredSession): List<CustomerOrder> {
        return api.listGurujiPendingOrders(ApiClient.bearer(session.accessToken))
            .map(::fromResponse)
    }

    suspend fun listGurujiAssignedReportOrders(session: StoredSession): List<CustomerOrder> {
        return try {
            api.listGurujiAssignedOrders(ApiClient.bearer(session.accessToken))
                .map(::fromResponse)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun listGurujiConsultationOrders(session: StoredSession): List<CustomerOrder> {
        return try {
            api.listGurujiConsultationOrders(ApiClient.bearer(session.accessToken))
                .map(::fromResponse)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun submitGuruRating(session: StoredSession, orderId: String, rating: Int): Boolean {
        return try {
            api.submitGuruRating(
                session.userId,
                orderId,
                ApiClient.bearer(session.accessToken),
                com.vaastuverse.app.data.dto.SubmitGuruRatingRequest(rating),
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getGuruRating(session: StoredSession, orderId: String): Int? {
        return try {
            api.getGuruRating(
                session.userId,
                orderId,
                ApiClient.bearer(session.accessToken),
            )?.rating
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getReportAccessUrl(
        session: StoredSession,
        orderId: String,
        mode: String,
    ): String? {
        return try {
            api.getOrderReportUrl(
                session.userId,
                orderId,
                mode,
                ApiClient.bearer(session.accessToken),
            ).url
        } catch (_: Exception) {
            null
        }
    }

    suspend fun downloadReportToDevice(
        session: StoredSession,
        orderId: String,
    ): Uri? = withContext(Dispatchers.IO) {
        val downloadUrl = getReportAccessUrl(session, orderId, "download")
            ?: return@withContext ReportFileHelper.findCachedReport(context, orderId)
        try {
            ReportFileHelper.downloadAndSave(context, downloadUrl, orderId)
        } catch (_: Exception) {
            ReportFileHelper.findCachedReport(context, orderId)
        }
    }

    suspend fun deliverReport(session: StoredSession, orderId: String, pdfUrl: String): CustomerOrder {
        val response = api.deliverOrderReport(
            orderId,
            ApiClient.bearer(session.accessToken),
            DeliverReportRequest(pdfUrl),
        )
        return fromResponse(response)
    }

    suspend fun deliverReportPdf(
        session: StoredSession,
        orderId: String,
        pdfUri: String,
        onProgress: (Int) -> Unit = {},
    ): CustomerOrder = withContext(Dispatchers.IO) {
        val uri = Uri.parse(pdfUri)
        val temp = File.createTempFile("vv-report-", ".pdf", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IllegalStateException("Could not read PDF")
            onProgress(10)
            val body = temp.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", temp.name, body)
                .build()
            val request = Request.Builder()
                .url("${ApiConfig.gatewayBaseUrl}/api/v1/guruji/orders/$orderId/deliver/upload")
                .header("Authorization", ApiClient.bearer(session.accessToken))
                .post(multipart)
                .build()
            uploadClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                onProgress(100)
                if (!response.isSuccessful) {
                    throw IllegalStateException(parseUploadError(raw) ?: "Report upload failed")
                }
                fromResponse(gson.fromJson(raw, CustomerOrderResponse::class.java)
                    ?: throw IllegalStateException("Invalid response"))
            }
        } finally {
            runCatching { temp.delete() }
        }
    }

    private fun parseUploadError(body: String): String? = runCatching {
        gson.fromJson(body, JsonObject::class.java)?.get("message")?.asString
    }.getOrNull()

    private suspend fun pushToServer(
        session: StoredSession,
        order: CustomerOrder,
        create: Boolean,
    ): CustomerOrder? {
        return try {
            val request = toRequest(order)
            val response = if (create || order.isLocalOnly()) {
                api.createCustomerOrder(
                    session.userId,
                    ApiClient.bearer(session.accessToken),
                    request.copy(id = if (order.isLocalOnly()) order.id else request.id),
                )
            } else {
                api.updateCustomerOrder(
                    session.userId,
                    order.id,
                    ApiClient.bearer(session.accessToken),
                    request,
                )
            }
            fromResponse(response)
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string().orEmpty()
            val message = parseApiError(body) ?: if (create) "Could not create order" else "Could not update order"
            if (e.code() in 400..499) {
                throw PropertyValidationException(message)
            }
            if (!create) {
                throw IllegalStateException(message)
            }
            null
        }
    }

    private fun parseApiError(body: String): String? = runCatching {
        gson.fromJson(body, JsonObject::class.java)?.let { json ->
            json.get("message")?.asString ?: json.get("error")?.asString
        }
    }.getOrNull()

    private suspend fun mergeRemoteWithLocal(userId: String, remote: List<CustomerOrder>) {
        val local = loadSorted(userId)
        val remoteIds = remote.map { it.id }.toSet()
        val unsyncedLocal = local.filter { it.isLocalOnly() && it.id !in remoteIds }
        replaceLocal(userId, (remote + unsyncedLocal).sortedByDescending { it.lastUpdatedAt })
    }

    private suspend fun upsertLocal(userId: String, order: CustomerOrder) {
        context.orderDataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey(key(userId))]).toMutableList()
            val index = current.indexOfFirst { it.id == order.id }
            if (index >= 0) current[index] = order else current.add(0, order)
            prefs[stringPreferencesKey(key(userId))] = gson.toJson(current)
        }
    }

    private suspend fun replaceLocal(userId: String, orders: List<CustomerOrder>) {
        context.orderDataStore.edit { prefs ->
            prefs[stringPreferencesKey(key(userId))] = gson.toJson(orders)
        }
    }

    private fun sortedOrders(orders: List<CustomerOrder>): List<CustomerOrder> =
        orders.map { it.normalized() }.sortedByDescending { it.lastUpdatedAt }

    private fun key(userId: String) = "orders_$userId"

    private fun decode(raw: String?): List<CustomerOrder> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CustomerOrder>>() {}.type
            gson.fromJson<List<CustomerOrder>>(raw, type)
        }.getOrElse { emptyList() }
    }

    private fun toRequest(order: CustomerOrder) = CustomerOrderRequest(
        id = order.id,
        useCaseId = order.useCaseId.name,
        packageTitle = order.packageTitle,
        priceLabel = order.priceLabel,
        kind = order.kind.name,
        linkedReportOrderId = order.linkedReportOrderId,
        guruTier = order.guruTier.name,
        propertyId = order.propertyId,
        propertyLabel = order.propertyLabel,
        propertySubmittedAt = order.propertySubmittedAt,
        buyerDifferentFromUser = order.buyerDifferentFromUser,
        buyerFullName = order.buyerFullName,
        buyerDateOfBirth = order.buyerDateOfBirth,
        status = order.status,
        createdAt = order.createdAt,
        lastUpdatedAt = order.lastUpdatedAt,
    )

    private fun fromResponse(response: CustomerOrderResponse): CustomerOrder = CustomerOrder(
        id = response.id,
        useCaseId = runCatching { CustomerUseCaseId.valueOf(response.useCaseId) }
            .getOrDefault(CustomerUseCaseId.HOME),
        packageTitle = response.packageTitle,
        priceLabel = response.priceLabel,
        kind = runCatching { OrderKind.valueOf(response.kind) }.getOrDefault(OrderKind.REPORT),
        guruTier = runCatching { GuruTier.valueOf(response.guruTier ?: "GURUJI_VALIDATED") }
            .getOrDefault(GuruTier.GURUJI_VALIDATED),
        upgradedFromOrderId = response.upgradedFromOrderId,
        linkedReportOrderId = response.linkedReportOrderId,
        upgradeEligible = response.upgradeEligible == true,
        assignedGurujiId = response.assignedGurujiId,
        assignedGurujiName = response.assignedGurujiName,
        propertyId = response.propertyId,
        propertyLabel = response.propertyLabel,
        propertySubmittedAt = response.propertySubmittedAt,
        buyerDifferentFromUser = response.buyerDifferentFromUser == true,
        buyerFullName = response.buyerFullName,
        buyerDateOfBirth = response.buyerDateOfBirth,
        status = response.status,
        publishedReportId = response.publishedReportId,
        reportStatus = response.reportStatus,
        reportPdfUrl = response.reportPdfUrl,
        reportDeliveredAt = response.reportDeliveredAt,
        reportExpiresAt = response.reportExpiresAt,
        createdAt = response.createdAt,
        lastUpdatedAt = response.lastUpdatedAt,
    )

    companion object {
        const val ORDER_PAGE_SIZE = 10
    }
}

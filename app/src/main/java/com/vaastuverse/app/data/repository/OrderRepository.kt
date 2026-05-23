package com.vaastuverse.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.OrderPage
import com.vaastuverse.app.data.isClosed
import com.vaastuverse.app.data.isOpen
import com.vaastuverse.app.data.normalized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.orderDataStore by preferencesDataStore("customer_orders")

class OrderRepository(private val context: Context) {
    private val gson = Gson()

    fun ordersFlow(userId: String): Flow<List<CustomerOrder>> =
        context.orderDataStore.data.map { prefs ->
            sortedOrders(decode(prefs[stringPreferencesKey(key(userId))]))
        }

    /** Latest open orders for home — sorted by last update, one page. */
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

    private fun sortedOrders(orders: List<CustomerOrder>): List<CustomerOrder> =
        orders
            .map { it.normalized() }
            .sortedByDescending { it.lastUpdatedAt }

    suspend fun addOrder(userId: String, order: CustomerOrder) {
        val now = System.currentTimeMillis()
        val toSave = order.normalized().copy(
            lastUpdatedAt = now,
            createdAt = if (order.createdAt > 0L) order.createdAt else now,
        )
        context.orderDataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey(key(userId))]).toMutableList()
            current.add(0, toSave)
            prefs[stringPreferencesKey(key(userId))] = gson.toJson(current)
        }
    }

    private fun key(userId: String) = "orders_$userId"

    private fun decode(raw: String?): List<CustomerOrder> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CustomerOrder>>() {}.type
            gson.fromJson<List<CustomerOrder>>(raw, type)
        }.getOrElse { emptyList() }
    }

    companion object {
        const val ORDER_PAGE_SIZE = 10
    }
}

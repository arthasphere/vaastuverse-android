package com.vaastuverse.app.data

import com.vaastuverse.app.ui.customer.CustomerUseCaseId
import java.util.UUID

enum class OrderKind {
    REPORT,
    CONSULTATION,
}

enum class OrderLifecycle {
    OPEN,
    CLOSED,
}

data class CustomerOrder(
    val id: String = UUID.randomUUID().toString(),
    val useCaseId: CustomerUseCaseId,
    val packageTitle: String,
    val priceLabel: String,
    val kind: OrderKind = OrderKind.REPORT,
    val status: String = OrderStatus.ONGOING,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
)

object OrderStatus {
    const val ONGOING = "ONGOING"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val COMPLETED = "COMPLETED"
    const val DELIVERED = "DELIVERED"
    const val CLOSED = "CLOSED"
    const val CANCELLED = "CANCELLED"
}

fun CustomerOrder.lifecycle(): OrderLifecycle = when (status.uppercase()) {
    OrderStatus.COMPLETED,
    OrderStatus.DELIVERED,
    OrderStatus.CLOSED,
    OrderStatus.CANCELLED,
    -> OrderLifecycle.CLOSED
    else -> OrderLifecycle.OPEN
}

fun CustomerOrder.isOpen(): Boolean = lifecycle() == OrderLifecycle.OPEN

fun CustomerOrder.isClosed(): Boolean = lifecycle() == OrderLifecycle.CLOSED

/** Normalizes legacy rows missing [lastUpdatedAt]. */
fun CustomerOrder.normalized(): CustomerOrder {
    val updated = if (lastUpdatedAt > 0L) lastUpdatedAt else createdAt
    return if (updated == lastUpdatedAt) this else copy(lastUpdatedAt = updated)
}

data class PaymentCheckout(
    val useCaseId: CustomerUseCaseId,
    val packageTitle: String,
    val priceLabel: String,
    val kind: OrderKind = OrderKind.REPORT,
)

data class OrderPage(
    val items: List<CustomerOrder>,
    val page: Int,
    val hasMore: Boolean,
    val totalCount: Int,
)

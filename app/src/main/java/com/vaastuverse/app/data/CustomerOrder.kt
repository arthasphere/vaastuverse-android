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
    val guruTier: GuruTier = GuruTier.GURUJI_VALIDATED,
    val upgradedFromOrderId: String? = null,
    /** Report order id when this row is a consultation purchase for that report. */
    val linkedReportOrderId: String? = null,
    val upgradeEligible: Boolean = false,
    val assignedGurujiId: String? = null,
    val assignedGurujiName: String? = null,
    val propertyId: String? = null,
    val propertyLabel: String? = null,
    /** When property details were first linked to this order (starts 15-min edit window). */
    val propertySubmittedAt: Long? = null,
    val status: String = OrderStatus.ONGOING,
    val buyerDifferentFromUser: Boolean = false,
    val buyerFullName: String? = null,
    val buyerDateOfBirth: String? = null,
    val reportPdfUrl: String? = null,
    val publishedReportId: String? = null,
    val reportStatus: String? = null,
    val reportDeliveredAt: Long? = null,
    val reportExpiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
)

fun CustomerOrder.isLocalOnly(): Boolean = id.startsWith("local-")

fun newLocalOrderId(): String = "local-${UUID.randomUUID()}"

object OrderStatus {
    const val AWAITING_DETAILS = "AWAITING_DETAILS"
    const val ONGOING = "ONGOING"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val WAITING_ASSIGNMENT = "WAITING_ASSIGNMENT"
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

fun CustomerOrder.needsPropertyDetails(): Boolean =
    status == OrderStatus.AWAITING_DETAILS || (propertyId.isNullOrBlank() && isOpen())

fun CustomerOrder.isClosed(): Boolean = lifecycle() == OrderLifecycle.CLOSED

/** Normalizes legacy rows missing [lastUpdatedAt] or [propertySubmittedAt]. */
fun CustomerOrder.normalized(): CustomerOrder {
    val updated = if (lastUpdatedAt > 0L) lastUpdatedAt else createdAt
    var order = if (updated == lastUpdatedAt) this else copy(lastUpdatedAt = updated)
    if (order.hasPropertyAttached() && order.propertySubmittedAt == null && order.status != OrderStatus.AWAITING_DETAILS) {
        order = order.copy(propertySubmittedAt = order.lastUpdatedAt)
    }
    return order
}

data class PaymentCheckout(
    val useCaseId: CustomerUseCaseId,
    val packageTitle: String,
    val priceLabel: String,
    val kind: OrderKind = OrderKind.REPORT,
    val guruTier: GuruTier = GuruTier.GURUJI_VALIDATED,
    val property: SavedProperty? = null,
    val linkedReportOrderId: String? = null,
    val linkedReportTitle: String? = null,
    val consultationOffer: ConsultationOffer? = null,
)

data class OrderPage(
    val items: List<CustomerOrder>,
    val page: Int,
    val hasMore: Boolean,
    val totalCount: Int,
)

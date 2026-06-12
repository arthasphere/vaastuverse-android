package com.vaastuverse.app.data

const val PROPERTY_EDIT_WINDOW_MS = 0L

fun CustomerOrder.hasPropertyAttached(): Boolean = !propertyId.isNullOrBlank()

fun CustomerOrder.submittedAtMs(): Long? = propertySubmittedAt?.takeIf { it > 0L }

fun CustomerOrder.canEditPropertyDetails(nowMs: Long = System.currentTimeMillis()): Boolean {
    if (!hasPropertyAttached()) return false
    if (status == OrderStatus.COMPLETED || status == OrderStatus.DELIVERED) return false
    val submitted = submittedAtMs() ?: return false
    return nowMs - submitted < PROPERTY_EDIT_WINDOW_MS
}

fun CustomerOrder.isReportGenerationLocked(nowMs: Long = System.currentTimeMillis()): Boolean {
    if (!hasPropertyAttached()) return false
    if (status == OrderStatus.COMPLETED || status == OrderStatus.DELIVERED) return false
    return !canEditPropertyDetails(nowMs) && isOpen()
}

fun CustomerOrder.isOrderViewable(): Boolean = isOpen() || isClosed()

fun CustomerOrder.propertyEditMinutesRemaining(nowMs: Long = System.currentTimeMillis()): Int? {
    if (!canEditPropertyDetails(nowMs)) return null
    val submitted = submittedAtMs() ?: return null
    val remaining = PROPERTY_EDIT_WINDOW_MS - (nowMs - submitted)
    return ((remaining + 59_999) / 60_000).toInt().coerceAtLeast(1)
}

fun CustomerOrder.orderStatusLine(nowMs: Long = System.currentTimeMillis()): String = when {
    needsPropertyDetails() -> "Tap to add property details"
    canEditPropertyDetails(nowMs) -> {
        val mins = propertyEditMinutesRemaining(nowMs)
        if (mins != null) "Tap to view · edit open $mins min"
        else "Tap to view order"
    }
    isReportGenerationLocked(nowMs) -> "Tap to view · report in progress"
    status == OrderStatus.DELIVERED -> "Report delivered · tap to view"
    status == OrderStatus.COMPLETED -> "Report completed · tap to view"
    isOpen() -> "Tap to view order"
    else -> status.replace('_', ' ')
}

fun isPropertyLockedForEdit(
    propertyId: String,
    orders: List<CustomerOrder>,
    nowMs: Long = System.currentTimeMillis(),
): Boolean = orders.any { order ->
    order.propertyId == propertyId && order.isReportGenerationLocked(nowMs)
}

enum class ReportProgressStepState { DONE, ACTIVE, PENDING }

data class ReportProgressStep(
    val label: String,
    val state: ReportProgressStepState,
)

fun CustomerOrder.isReportDelivered(): Boolean =
    kind == OrderKind.REPORT && (
        status == OrderStatus.DELIVERED
            || status == OrderStatus.COMPLETED
            || status == OrderStatus.CLOSED
            || reportDeliveredAt != null
        )

fun CustomerOrder.isAskGurujiReport(): Boolean =
    kind == OrderKind.REPORT && isReportDelivered()

fun List<CustomerOrder>.consultationsForReport(reportOrderId: String): List<CustomerOrder> =
    filter { it.kind == OrderKind.CONSULTATION && it.linkedReportOrderId == reportOrderId }
        .sortedByDescending { it.createdAt }

fun CustomerOrder.reportProgressSteps(nowMs: Long = System.currentTimeMillis()): List<ReportProgressStep> {
    val paymentDone = status != OrderStatus.AWAITING_DETAILS || hasPropertyAttached()
    val propertyDone = hasPropertyAttached()
    val delivered = isReportDelivered()
    val generating = propertyDone && !delivered && isOpen()

    return listOf(
        ReportProgressStep("Payment received", ReportProgressStepState.DONE),
        ReportProgressStep(
            label = if (propertyDone) "Property details submitted" else "Property details pending",
            state = when {
                propertyDone -> ReportProgressStepState.DONE
                needsPropertyDetails() -> ReportProgressStepState.ACTIVE
                else -> ReportProgressStepState.PENDING
            },
        ),
        ReportProgressStep(
            label = when {
                delivered -> "Report generated & shared"
                generating -> "Guruji preparing your report"
                else -> "Report generation"
            },
            state = when {
                delivered -> ReportProgressStepState.DONE
                generating -> ReportProgressStepState.ACTIVE
                else -> ReportProgressStepState.PENDING
            },
        ),
    )
}

fun CustomerOrder.reportProgressSummary(nowMs: Long = System.currentTimeMillis()): String = when {
    needsPropertyDetails() -> "Waiting for property details to start your report."
    canEditPropertyDetails(nowMs) -> {
        val mins = propertyEditMinutesRemaining(nowMs)
        "Property submitted. You can edit for ${mins ?: 15} more minutes while we queue your report."
    }
    isReportGenerationLocked(nowMs) -> "Your report is being prepared. Property edits are locked until delivery."
    status == OrderStatus.DELIVERED -> "Your Vaastu report has been delivered."
    status == OrderStatus.COMPLETED -> "Your Vaastu report is ready."
    else -> "Your order is in progress."
}

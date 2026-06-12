package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.ConsultationOffer
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.OrderKind
import com.vaastuverse.app.data.OrderStatus
import com.vaastuverse.app.data.consultationsForReport
import com.vaastuverse.app.data.isAskGurujiReport
import com.vaastuverse.app.data.isOpen
import com.vaastuverse.app.data.repository.OrderRepository
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerAskGurujiScreen(
    modifier: Modifier = Modifier,
    userId: String?,
    leadGuideName: String,
    orderRepo: OrderRepository,
    onBuyConsultation: (CustomerOrder, ConsultationOffer) -> Unit,
    onViewReport: (CustomerOrder) -> Unit,
) {
    var orders by remember { mutableStateOf<List<CustomerOrder>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        scope.launch {
            orderRepo.ordersFlow(id).collect { orders = it }
        }
    }

    // All report orders that have been delivered/completed/closed — the full history
    val reportOrders = orders.filter { it.isAskGurujiReport() }
        .sortedByDescending { it.reportDeliveredAt ?: it.lastUpdatedAt }

    val activeConsultations = orders.filter { it.kind == OrderKind.CONSULTATION && it.isOpen() }
        .sortedByDescending { it.lastUpdatedAt }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Discuss your reports with $leadGuideName. Book a call straight from any card below.",
            style = VvType.body(12, VvColors.Ink2),
        )

        if (activeConsultations.isNotEmpty()) {
            SectionLabel("CONSULTATIONS IN PROGRESS")
            activeConsultations.forEach { consultation ->
                ConsultationInProgressCard(order = consultation)
            }
        }

        SectionLabel("YOUR REPORTS")

        if (reportOrders.isEmpty()) {
            Text(
                "Your delivered reports will appear here. Tap \"Book\" on any report to schedule a call with $leadGuideName.",
                style = VvType.body(12, VvColors.Ink3),
            )
        } else {
            reportOrders.forEach { report ->
                ReportWithConsultationCard(
                    report = report,
                    consultations = orders.consultationsForReport(report.id),
                    leadGuideName = leadGuideName,
                    onViewReport = { onViewReport(report) },
                    onBuyConsultation = { offer -> onBuyConsultation(report, offer) },
                )
            }
        }
    }
}

// ── Private composables ────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp,
        color = VvColors.Ink3,
    )
}

/**
 * Card shown for every delivered / completed / closed report order.
 * The two consultation offers are the primary action — tappable directly
 * without navigating into the detail screen.
 */
@Composable
private fun ReportWithConsultationCard(
    report: CustomerOrder,
    consultations: List<CustomerOrder>,
    leadGuideName: String,
    onViewReport: () -> Unit,
    onBuyConsultation: (ConsultationOffer) -> Unit,
) {
    val useCase = CustomerUseCases.get(report.useCaseId)
    val gurujiName = report.assignedGurujiName ?: leadGuideName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(14.dp)),
    ) {
        // ── Report identity row ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewReport)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${useCase.icon} ${report.packageTitle}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = VvColors.Ink,
                )
                report.propertyLabel?.takeIf { it.isNotBlank() }?.let { label ->
                    Text(label, fontSize = 11.sp, color = VvColors.Ink3)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReportStatusChip(report)
                Text(
                    "View ›",
                    fontSize = 11.sp,
                    color = VvColors.Jade,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        HorizontalDivider(color = VvColors.Border, thickness = 0.5.dp)

        // ── "Book a call" section — the hero ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VvColors.Cream)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Book a call with $gurujiName",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = VvColors.Ink2,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ConsultationOffer.entries.forEach { offer ->
                    ConsultationOfferTile(
                        offer = offer,
                        modifier = Modifier.weight(1f),
                        onBuy = { onBuyConsultation(offer) },
                    )
                }
            }
        }

        // ── Past bookings footer (only when bookings exist) ──────────────────
        if (consultations.isNotEmpty()) {
            HorizontalDivider(color = VvColors.Border, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${consultations.size} call${if (consultations.size == 1) "" else "s"} booked",
                    fontSize = 10.sp,
                    color = VvColors.Ink3,
                )
                // Most recent booking label
                consultations.firstOrNull()?.let { latest ->
                    Text(
                        "Last: ${latest.packageTitle} · ${latest.priceLabel}",
                        fontSize = 10.sp,
                        color = VvColors.Ink3,
                    )
                }
            }
        }
    }
}

/**
 * Single consultation offer tile — shows duration, label, price and a "Book" button.
 * Tapping anywhere (tile or button) triggers the purchase.
 */
@Composable
private fun ConsultationOfferTile(
    offer: ConsultationOffer,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, VvColors.Jade.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .clickable(onClick = onBuy)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "${offer.durationMinutes} min",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = VvColors.Jade,
        )
        Text(
            offer.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = VvColors.Ink,
        )
        Text(
            offer.subtitle,
            fontSize = 9.sp,
            color = VvColors.Ink3,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Button(
            onClick = onBuy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VvColors.Jade),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp),
        ) {
            Text(
                offer.priceLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

/**
 * Status chip shown on the report card header.
 * Maps each terminal status to an appropriate label + color.
 */
@Composable
private fun ReportStatusChip(order: CustomerOrder) {
    val (label, color) = when (order.status.uppercase()) {
        OrderStatus.DELIVERED -> "Delivered" to VvColors.Jade
        OrderStatus.COMPLETED -> "Ready" to VvColors.Jade
        OrderStatus.CLOSED    -> "Closed" to VvColors.Ink3
        else -> if (order.reportDeliveredAt != null) "Generated" to VvColors.Jade
                else "Done" to VvColors.Ink3
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.3.sp,
        )
    }
}

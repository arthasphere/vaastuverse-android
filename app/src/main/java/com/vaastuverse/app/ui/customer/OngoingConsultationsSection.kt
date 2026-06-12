package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.isOrderViewable
import com.vaastuverse.app.data.orderStatusLine
import com.vaastuverse.app.ui.VvColors

@Composable
fun ConsultationsInProgressSection(
    consultations: List<CustomerOrder>,
    leadGuideName: String,
    orderNowMs: Long = System.currentTimeMillis(),
    onOrderAction: (CustomerOrder) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (consultations.isEmpty()) "YOUR CONSULTATIONS" else "CONSULTATIONS IN PROGRESS",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
            color = VvColors.Ink3,
        )

        if (consultations.isEmpty()) {
            GurujiValidatesBanner(leadGuideName = leadGuideName)
            Text(
                "No active consultations yet — pick a package below to get started.",
                fontSize = 10.sp,
                color = VvColors.Ink3,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        } else {
            OrderCardsViewport(orders = consultations) { order ->
                ConsultationInProgressCard(
                    order = order,
                    nowMs = orderNowMs,
                    onOrderAction = if (order.isOrderViewable()) {
                        { onOrderAction(order) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun GurujiValidatesBanner(leadGuideName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(
                Brush.horizontalGradient(listOf(VvColors.GoldLight, Color(0xFFFFFAEE))),
            )
            .border(1.dp, VvColors.Gold.copy(alpha = 0.25f), RoundedCornerShape(9.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("👑", fontSize = 20.sp)
        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
            Text(
                "$leadGuideName personally validates every report",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VvColors.Gold,
            )
            Text(
                "Not AI alone — a named expert who signs off on your order",
                fontSize = 8.sp,
                color = VvColors.Ink3,
            )
        }
        Text("Only here ✦", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = VvColors.Gold)
    }
}

/**
 * Shows open report orders (kind = REPORT) in a fixed-height viewport that shows 2 cards
 * at a time and scrolls vertically when there are more. Hidden when [reports] is empty.
 */
@Composable
fun ReportsInProgressSection(
    reports: List<CustomerOrder>,
    orderNowMs: Long = System.currentTimeMillis(),
    onOrderAction: (CustomerOrder) -> Unit = {},
) {
    if (reports.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "REPORTS IN PROGRESS",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
            color = VvColors.Ink3,
        )
        OrderCardsViewport(orders = reports) { order ->
            ReportOrderCard(
                order = order,
                nowMs = orderNowMs,
                onOrderAction = if (order.isOrderViewable()) {
                    { onOrderAction(order) }
                } else {
                    null
                },
            )
        }
    }
}

/** @deprecated Use [ConsultationsInProgressSection] */
@Composable
fun OrdersInProgressSection(
    orders: List<CustomerOrder>,
    leadGuideName: String,
) = ConsultationsInProgressSection(consultations = orders, leadGuideName = leadGuideName)

/** @deprecated Use [ConsultationsInProgressSection] */
@Composable
fun OngoingConsultationsSection(orders: List<CustomerOrder>) =
    ConsultationsInProgressSection(consultations = orders, leadGuideName = "Guruji")

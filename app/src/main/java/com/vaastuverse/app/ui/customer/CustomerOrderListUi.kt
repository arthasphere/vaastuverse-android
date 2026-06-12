package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.OrderKind
import com.vaastuverse.app.data.isOpen
import com.vaastuverse.app.data.isOrderViewable
import com.vaastuverse.app.data.orderStatusLine
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val OrderCardHeight = 78.dp
val OrderCardSpacing = 8.dp
const val OrderCardsVisibleCount = 2

val OrderCardsViewportHeight =
    OrderCardHeight * OrderCardsVisibleCount + OrderCardSpacing * (OrderCardsVisibleCount - 1)

@Composable
fun OrderCardsViewport(
    orders: List<CustomerOrder>,
    modifier: Modifier = Modifier,
    card: @Composable (CustomerOrder) -> Unit = { order -> ConsultationInProgressCard(order) },
) {
    val scrollState = rememberScrollState()
    val showScrollbar = orders.size > OrderCardsVisibleCount

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(OrderCardsViewportHeight),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = if (showScrollbar) 10.dp else 0.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(OrderCardSpacing),
        ) {
            orders.forEach { order -> card(order) }
        }
        if (showScrollbar) {
            VerticalScrollIndicator(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun VerticalScrollIndicator(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val trackColor = VvColors.Ink3.copy(alpha = 0.22f)
    val thumbColor = VvColors.Jade.copy(alpha = 0.7f)
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(4.dp),
    ) {
        val trackHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val maxScroll = scrollState.maxValue.toFloat()
        val thumbHeightPx = if (maxScroll > 0f) {
            (trackHeightPx * (trackHeightPx / (trackHeightPx + maxScroll))).coerceIn(24f, trackHeightPx)
        } else {
            trackHeightPx * 0.45f
        }
        val thumbOffsetPx = if (maxScroll > 0f) {
            (trackHeightPx - thumbHeightPx) * (scrollState.value / maxScroll)
        } else {
            0f
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(trackColor),
        )
        Box(
            modifier = Modifier
                .offset(y = with(density) { thumbOffsetPx.toDp() })
                .fillMaxWidth()
                .height(with(density) { thumbHeightPx.toDp() })
                .clip(RoundedCornerShape(2.dp))
                .background(thumbColor),
        )
    }
}

@Composable
fun ConsultationInProgressCard(
    order: CustomerOrder,
    nowMs: Long = System.currentTimeMillis(),
    onOrderAction: (() -> Unit)? = null,
) {
    OrderSummaryCard(
        order = order,
        statusLine = order.orderStatusLine(nowMs),
        statusColor = if (order.isOpen()) VvColors.Jade else VvColors.Ink3,
        onClick = if (order.isOrderViewable()) onOrderAction else null,
    )
}

@Composable
fun ReportOrderCard(
    order: CustomerOrder,
    nowMs: Long = System.currentTimeMillis(),
    onOrderAction: (() -> Unit)? = null,
) {
    OrderSummaryCard(
        order = order,
        statusLine = order.orderStatusLine(nowMs),
        statusColor = if (order.isOpen()) VvColors.Jade else VvColors.Ink3,
        onClick = if (order.isOrderViewable()) onOrderAction else null,
    )
}

@Composable
private fun OrderSummaryCard(
    order: CustomerOrder,
    statusLine: String,
    statusColor: Color,
    onClick: (() -> Unit)? = null,
) {
    val useCase = CustomerUseCases.get(order.useCaseId)
    val dateLabel = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        .format(Date(order.lastUpdatedAt))
    val kindLabel = if (order.kind == OrderKind.CONSULTATION) "Consultation" else "Report"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(OrderCardHeight)
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Jade.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(useCase.icon, fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                order.packageTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = VvColors.Ink,
                maxLines = 1,
            )
            Text(
                buildString {
                    append(kindLabel)
                    append(" · ")
                    append(order.priceLabel)
                    order.propertyLabel?.takeIf { it.isNotBlank() }?.let {
                        append(" · ")
                        append(it)
                    }
                    append(" · Updated ")
                    append(dateLabel)
                },
                fontSize = 9.sp,
                color = VvColors.Ink3,
                maxLines = 2,
            )
            Text(
                statusLine,
                fontSize = 9.sp,
                color = statusColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun OrderListEmptyMessage(message: String) {
    Text(message, style = VvType.body(12, VvColors.Ink3))
}

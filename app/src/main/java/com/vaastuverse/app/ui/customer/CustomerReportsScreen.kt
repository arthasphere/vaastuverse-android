package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.OrderKind
import com.vaastuverse.app.data.isOrderViewable
import com.vaastuverse.app.data.repository.OrderRepository
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import kotlinx.coroutines.launch

private enum class ReportsTab {
    Open,
    Closed,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerReportsScreen(
    modifier: Modifier = Modifier,
    userId: String?,
    orderRepo: OrderRepository,
    orderNowMs: Long = System.currentTimeMillis(),
    onOrderAction: (CustomerOrder) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    var reportsTab by remember { mutableIntStateOf(0) }
    var displayedOrders by remember { mutableStateOf<List<CustomerOrder>>(emptyList()) }
    var nextPage by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var lastLoadTriggeredAtPage by remember { mutableIntStateOf(-1) }

    fun loadNextPage(reset: Boolean) {
        val id = userId ?: return
        if (isLoading) return
        scope.launch {
            isLoading = true
            val pageToLoad = if (reset) 0 else nextPage
            val result = orderRepo.getOrderPage(
                userId = id,
                open = reportsTab == ReportsTab.Open.ordinal,
                page = pageToLoad,
            )
            val reportItems = result.items.filter { it.kind == OrderKind.REPORT }
            displayedOrders = if (reset) reportItems else displayedOrders + reportItems
            hasMore = result.hasMore
            nextPage = pageToLoad + 1
            isLoading = false
        }
    }

    LaunchedEffect(userId, reportsTab) {
        val id = userId ?: return@LaunchedEffect
        lastLoadTriggeredAtPage = -1
        isLoading = true
        val result = orderRepo.getOrderPage(
            userId = id,
            open = reportsTab == ReportsTab.Open.ordinal,
            page = 0,
        )
        displayedOrders = result.items.filter { it.kind == OrderKind.REPORT }
        hasMore = result.hasMore
        nextPage = 1
        lastLoadTriggeredAtPage = 0
        isLoading = false
    }

    LaunchedEffect(scroll.value, scroll.maxValue, hasMore, isLoading, nextPage, userId, reportsTab) {
        if (userId == null || !hasMore || isLoading || scroll.maxValue <= 0) return@LaunchedEffect
        val nearEnd = scroll.value > 0 && scroll.value >= scroll.maxValue - 120
        if (nearEnd && lastLoadTriggeredAtPage < nextPage) {
            lastLoadTriggeredAtPage = nextPage
            loadNextPage(reset = false)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CustomerMiddleThemes.reportsBackground)
            .verticalScroll(scroll)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(VvColors.JadeLight)
                .border(1.dp, VvColors.Jade.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(10.dp),
        ) {
            Text("📱", modifier = Modifier.padding(end = 8.dp))
            Text(
                CustomerBranding.reportRetentionMessage,
                fontSize = 9.sp,
                color = VvColors.Jade,
            )
        }

        TabRow(
            selectedTabIndex = reportsTab,
            containerColor = CustomerMiddleThemes.reportsBackground,
            contentColor = VvColors.Ink,
            indicator = { tabPositions ->
                if (reportsTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[reportsTab]),
                        color = VvColors.Saffron,
                    )
                }
            },
        ) {
            Tab(
                selected = reportsTab == ReportsTab.Open.ordinal,
                onClick = { reportsTab = ReportsTab.Open.ordinal },
                text = { Text("Open") },
            )
            Tab(
                selected = reportsTab == ReportsTab.Closed.ordinal,
                onClick = { reportsTab = ReportsTab.Closed.ordinal },
                text = { Text("Closed") },
            )
        }

        when {
            displayedOrders.isEmpty() && !isLoading -> {
                OrderListEmptyMessage(
                    if (reportsTab == ReportsTab.Open.ordinal) {
                        "No open orders yet."
                    } else {
                        "No closed orders yet."
                    },
                )
            }
            displayedOrders.isNotEmpty() -> {
                Text(
                    if (reportsTab == ReportsTab.Open.ordinal) "Open orders" else "Closed orders",
                    style = VvType.title(14),
                )
                Column(verticalArrangement = Arrangement.spacedBy(OrderCardSpacing)) {
                    displayedOrders.forEach { order ->
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
        }

        if (isLoading && displayedOrders.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = VvColors.Jade,
                    strokeWidth = 2.dp,
                )
                Text("Loading more…", style = VvType.body(12, VvColors.Ink3))
            }
        }
    }
}

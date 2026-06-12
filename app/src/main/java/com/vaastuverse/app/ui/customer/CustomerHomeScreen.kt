package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors

data class UseCaseCard(
    val icon: String,
    val title: String,
    val subtitle: String,
    val strikePrice: String,
    val price: String,
)

private val useCaseSamples = listOf(
    UseCaseCard("🏠", "New Flat / Home", "Zone analysis · remedies · muhurat · renders", "₹5,500", "₹399"),
    UseCaseCard("🏢", "Office / Workspace", "Desk flow · cash counter · MD cabin · entrance", "₹8,000", "₹599"),
    UseCaseCard("🏪", "Shop / Showroom", "Entrance · cash box · display placement · flow", "₹10,000", "₹599"),
    UseCaseCard("🏭", "Factory / Warehouse", "Machinery · raw material · dispatch zone · flow", "₹25,000", "₹999"),
)

@Composable
fun CustomerHomeScreen(
    session: UserSessionViewModel,
    ongoingReports: List<CustomerOrder> = emptyList(),
    ongoingConsultations: List<CustomerOrder> = emptyList(),
    onUseCaseClick: (CustomerUseCaseId) -> Unit,
    onQuickBuy: (CustomerUseCaseId) -> Unit,
    orderNowMs: Long = System.currentTimeMillis(),
    onOrderAction: (CustomerOrder) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ReportsInProgressSection(
            reports = ongoingReports,
            orderNowMs = orderNowMs,
            onOrderAction = onOrderAction,
        )

        ConsultationsInProgressSection(
            consultations = ongoingConsultations,
            leadGuideName = session.leadGuideShortName,
            orderNowMs = orderNowMs,
            onOrderAction = onOrderAction,
        )

        Text(
            "WHAT DO YOU NEED VAASTU FOR?",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = VvColors.Ink3,
        )

        useCaseSamples.forEachIndexed { index, card ->
            val useCaseId = useCaseIdForLandingIndex(index)
            UseCaseCardRow(
                card = card,
                onClick = { onUseCaseClick(useCaseId) },
                onQuickBuy = { onQuickBuy(useCaseId) },
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

private fun useCaseIdForLandingIndex(index: Int): CustomerUseCaseId = when (index) {
    0 -> CustomerUseCaseId.HOME
    1 -> CustomerUseCaseId.OFFICE
    2 -> CustomerUseCaseId.SHOP
    else -> CustomerUseCaseId.FACTORY
}

@Composable
private fun UseCaseCardRow(
    card: UseCaseCard,
    onClick: () -> Unit,
    onQuickBuy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(11.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(card.icon, fontSize = 22.sp, modifier = Modifier.padding(end = 10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(card.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink)
                Text(card.subtitle, fontSize = 9.sp, color = VvColors.Ink3)
            }
            Text(
                card.strikePrice,
                fontSize = 8.sp,
                color = VvColors.Ink3,
                textDecoration = TextDecoration.LineThrough,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
        Button(
            onClick = onQuickBuy,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VvColors.Jade,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("Buy now", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(card.price, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

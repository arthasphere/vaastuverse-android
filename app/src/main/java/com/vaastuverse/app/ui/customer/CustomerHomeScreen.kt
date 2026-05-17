package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

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
fun CustomerHomeScreen(session: UserSessionViewModel) {
    val scroll = rememberScrollState()
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF8F0),
            Color(0xFFFFF0E0),
            VvColors.Cream,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .verticalScroll(scroll)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.namasteGreetingLine,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    color = VvColors.Saffron,
                )
                Text("Your Vaastu,", style = VvType.title(17))
                Text(
                    "your ${session.leadGuideShortName}.",
                    style = VvType.titleItalicSaffron(17),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VvColors.JadeLight)
                    .border(2.dp, VvColors.Jade, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("🧘", fontSize = 18.sp)
            }
        }

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
                    "${session.leadGuideShortName} personally validates every report",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = VvColors.Gold,
                )
                Text(
                    "Not AI alone — a named expert who signs off",
                    fontSize = 8.sp,
                    color = VvColors.Ink3,
                )
            }
            Text("Only here ✦", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = VvColors.Gold)
        }

        Text(
            "WHAT DO YOU NEED VAASTU FOR?",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = VvColors.Ink3,
        )

        useCaseSamples.forEach { card ->
            UseCaseCardRow(card)
        }

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
                "Your report stays on your phone forever — revisit any time, share with family, book a follow-up in seconds. No other Vaastu service gives you this.",
                fontSize = 9.sp,
                color = VvColors.Jade,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun UseCaseCardRow(card: UseCaseCard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(11.dp))
            .clickable { }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(card.icon, fontSize = 22.sp, modifier = Modifier.padding(end = 10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(card.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink)
            Text(card.subtitle, fontSize = 9.sp, color = VvColors.Ink3)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(card.strikePrice, fontSize = 8.sp, color = VvColors.Ink3, textDecoration = TextDecoration.LineThrough)
            Text(card.price, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VvColors.Jade)
        }
    }
}

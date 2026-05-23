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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.dto.DiscoverablePartnerResponse
import com.vaastuverse.app.ui.VvColors

@Composable
fun CustomerDiscoverScreen(
    partners: List<DiscoverablePartnerResponse>,
    onFilter: (String) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf("ALL") }
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VvColors.Cream)
            .verticalScroll(scroll)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "FIND A PARTNER",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = VvColors.Ink3,
        )
        Text(
            "Gurujis, designers, and channel partners available on VaastuVerse.",
            fontSize = 12.sp,
            color = VvColors.Ink2,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL" to "All", "GURUJI" to "Guruji", "DESIGNER" to "Designer", "CHANNEL_PARTNER" to "Channel").forEach { (value, label) ->
                FilterChip(
                    selected = filter == value,
                    onClick = {
                        filter = value
                        onFilter(value)
                    },
                    label = { Text(label, fontSize = 11.sp) },
                )
            }
        }
        if (partners.isEmpty()) {
            Text("No partners listed yet. Check back after onboarding completes.", color = VvColors.Ink3, fontSize = 12.sp)
        } else {
            partners.forEach { partner ->
                PartnerDiscoverCard(partner)
            }
        }
    }
}

@Composable
private fun PartnerDiscoverCard(partner: DiscoverablePartnerResponse) {
    val emoji = when (partner.partnerType) {
        "GURUJI" -> "🕉"
        "DESIGNER" -> "🎨"
        "CHANNEL_PARTNER" -> "🤝"
        else -> "✦"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(emoji, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    partner.displayName ?: partner.businessName ?: "Partner",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = VvColors.Ink,
                )
                Text(partnerTypeLabel(partner.partnerType), fontSize = 10.sp, color = VvColors.Jade)
            }
            Text(partner.kycStatus, fontSize = 9.sp, color = VvColors.Saffron)
        }
        partner.city?.let { Text(it, fontSize = 10.sp, color = VvColors.Ink3) }
        Text(partner.serviceSummary, fontSize = 10.sp, color = VvColors.Ink2)
        partner.businessName?.let {
            if (it != partner.displayName) {
                Text(it, fontSize = 9.sp, color = VvColors.Ink3)
            }
        }
    }
}

private fun partnerTypeLabel(type: String): String = when (type) {
    "GURUJI" -> "Vaastu Guruji"
    "DESIGNER" -> "Interior designer"
    "CHANNEL_PARTNER" -> "Channel partner"
    else -> type
}

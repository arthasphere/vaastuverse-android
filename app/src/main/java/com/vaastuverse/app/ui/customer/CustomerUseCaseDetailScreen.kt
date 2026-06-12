package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerUseCaseDetailScreen(
    useCase: CustomerUseCaseDetail,
    session: UserSessionViewModel,
    aiGuruEnabled: Boolean = false,
    onViewSampleReport: () -> Unit,
    onOrderReport: () -> Unit = {},
    onOrderAiReport: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VvColors.Ink)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(useCase.icon, fontSize = 22.sp)
                Column {
                    Text(useCase.headerTitle, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(useCase.headerSubtitle, color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                }
            }

            Text(
                useCase.badge,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(VvColors.GoldLight)
                    .border(1.dp, VvColors.Gold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VvColors.Gold,
            )

            Text(useCase.intro, style = VvType.body(12, VvColors.Ink2), lineHeight = 18.sp)

            Text(
                useCase.analysisHeading,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = VvColors.Ink3,
            )

            useCase.analysisPoints.forEach { point ->
                AnalysisPointRow(point)
            }

            SampleReportLinkCard(
                title = useCase.templateTitle,
                description = useCase.templateDescription,
                onClick = onViewSampleReport,
            )

        PricingCard(
            useCase = useCase,
            expertName = session.leadGuideShortName,
            onOrder = onOrderReport,
        )

        if (aiGuruEnabled) {
            AiPricingCard(
                useCase = useCase,
                onOrder = onOrderAiReport,
            )
        }
    }
}

@Composable
private fun AnalysisPointRow(point: AnalysisPoint) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(9.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(point.icon, fontSize = 16.sp)
        Column {
            Text(point.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VvColors.Ink)
            Text(point.detail, fontSize = 9.sp, color = VvColors.Ink3)
        }
    }
}

@Composable
private fun SampleReportLinkCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(VvColors.JadeLight)
            .border(1.dp, VvColors.Jade.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Description,
            contentDescription = null,
            tint = VvColors.Jade,
            modifier = Modifier.padding(end = 10.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = VvColors.Jade)
            Text(description, fontSize = 10.sp, color = VvColors.Ink2, modifier = Modifier.padding(top = 2.dp))
        }
        TextButton(onClick = onClick) {
            Text("View sample", fontWeight = FontWeight.Bold, color = VvColors.Jade)
        }
    }
}

@Composable
private fun PricingCard(
    useCase: CustomerUseCaseDetail,
    expertName: String,
    onOrder: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Text(
            useCase.priceLabel,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = VvColors.Jade,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(useCase.price, style = VvType.title(26).copy(color = VvColors.Jade))
                Text(useCase.marketCompare, fontSize = 9.sp, color = VvColors.Ink3)
            }
            Text(
                "Includes:\n${useCase.includes}",
                fontSize = 9.sp,
                color = VvColors.Ink3,
            )
        }
        Text(
            "Validated by $expertName before delivery.",
            fontSize = 9.sp,
            color = VvColors.Ink3,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Button(
            onClick = onOrder,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VvColors.Jade),
        ) {
            Text(useCase.ctaLabel)
        }
    }
}

@Composable
private fun AiPricingCard(
    useCase: CustomerUseCaseDetail,
    onOrder: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VvColors.Ink.copy(alpha = 0.04f))
            .border(1.dp, VvColors.Border, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "AI REPORT · QUICK START",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = VvColors.Ink3,
        )
        Text(
            CustomerUseCases.aiPriceLabel(),
            style = VvType.title(26).copy(color = VvColors.Ink),
        )
        Text(
            "Instant AI Vaastu report for ${useCase.headerTitle.lowercase()}. No Guruji iteration — upgrade later for verification.",
            fontSize = 10.sp,
            color = VvColors.Ink3,
            lineHeight = 15.sp,
        )
        Button(
            onClick = onOrder,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VvColors.Ink),
        ) {
            Text("Get AI report (${CustomerUseCases.aiPriceLabel()})")
        }
    }
}

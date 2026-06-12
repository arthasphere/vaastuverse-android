package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.ConsultationOffer
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.ReportProgressStepState
import com.vaastuverse.app.data.SavedProperty
import com.vaastuverse.app.data.canEditPropertyDetails
import com.vaastuverse.app.data.displaySubtitle
import com.vaastuverse.app.data.GuruTier
import com.vaastuverse.app.data.isReportDelivered
import com.vaastuverse.app.data.needsPropertyDetails
import com.vaastuverse.app.data.reportProgressSteps
import com.vaastuverse.app.data.reportProgressSummary
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerOrderDetailScreen(
    order: CustomerOrder,
    property: SavedProperty?,
    nowMs: Long = System.currentTimeMillis(),
    onAddPropertyDetails: () -> Unit,
    onEditProperty: () -> Unit,
    onPreviewReport: () -> Unit = {},
    onDownloadReport: () -> Unit = {},
    onUpgradeToGuruji: () -> Unit = {},
    upgradePlanEnabled: Boolean = false,
    onBookConsultation: (ConsultationOffer) -> Unit = {},
    guruRating: Int? = null,
    onSubmitGuruRating: (Int) -> Unit = {},
) {
    val useCase = CustomerUseCases.get(order.useCaseId)
    val canEdit = order.canEditPropertyDetails(nowMs)
    val needsDetails = order.needsPropertyDetails()
    val reportDelivered = order.isReportDelivered()
    val showUpgrade = upgradePlanEnabled
            && reportDelivered
            && order.guruTier == GuruTier.AI
            && order.upgradeEligible

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, VvColors.Border, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("${useCase.icon} ${order.packageTitle}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text("Amount paid: ${order.priceLabel}", fontSize = 12.sp, color = VvColors.Jade)
            Text(order.reportProgressSummary(nowMs), style = VvType.body(12, VvColors.Ink2))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PROPERTY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink3, letterSpacing = 1.sp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (needsDetails) {
                    Text("Not submitted yet", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        "Add property details to start report generation.",
                        style = VvType.body(11, VvColors.Ink3),
                    )
                } else {
                    Text(
                        order.propertyLabel ?: property?.label ?: "Property linked",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                    property?.let {
                        Text(it.displaySubtitle(), fontSize = 10.sp, color = VvColors.Ink3)
                    } ?: order.propertyLabel?.let {
                        Text(it, fontSize = 10.sp, color = VvColors.Ink3)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("REPORT PROGRESS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink3, letterSpacing = 1.sp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                order.reportProgressSteps(nowMs).forEach { step ->
                    ProgressStepRow(step.label, step.state)
                }
                if (reportDelivered) {
                    Text(
                        "Your Vaastu report is ready.",
                        fontSize = 11.sp,
                        color = VvColors.Jade,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onPreviewReport,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Preview")
                        }
                        Button(
                            onClick = onDownloadReport,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = VvColors.Jade),
                        ) {
                            Text("Download")
                        }
                    }
                    Text(
                        "Preview uses a short-lived link. Download saves the PDF on your phone.",
                        fontSize = 10.sp,
                        color = VvColors.Ink3,
                    )
                    if (showUpgrade) {
                        Button(
                            onClick = onUpgradeToGuruji,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = VvColors.Gold),
                        ) {
                            Text("Upgrade to Guruji verification")
                        }
                        Text(
                            "Get your AI report reviewed by a named Guruji with consultation support.",
                            fontSize = 10.sp,
                            color = VvColors.Ink3,
                        )
                    }
                }
            }
        }

        if (needsDetails) {
            Button(
                onClick = onAddPropertyDetails,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VvColors.Jade),
            ) {
                Text("Add property details")
            }
        } else if (!reportDelivered) {
            Button(
                onClick = onEditProperty,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VvColors.Jade,
                    disabledContainerColor = VvColors.Ink3.copy(alpha = 0.25f),
                    disabledContentColor = VvColors.Ink3,
                ),
            ) {
                Text(
                    if (canEdit) "Edit property details"
                    else "Edit property (locked during report generation)",
                )
            }
        }

        if (reportDelivered && !order.assignedGurujiName.isNullOrBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("RATE YOUR GURUJI", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink3, letterSpacing = 1.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "How was your experience with ${order.assignedGurujiName}?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (star in 1..5) {
                            val filled = guruRating != null && star <= guruRating
                            Text(
                                if (filled) "★" else "☆",
                                fontSize = 26.sp,
                                color = if (filled) VvColors.Gold else VvColors.Ink3,
                                modifier = Modifier.clickable { onSubmitGuruRating(star) },
                            )
                        }
                    }
                    if (guruRating != null) {
                        Text("Thanks for your feedback!", fontSize = 10.sp, color = VvColors.Jade)
                    }
                }
            }
        }

        if (reportDelivered) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "CONSULTING CALL",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = VvColors.Ink3,
                    letterSpacing = 1.sp,
                )
                Text(
                    "Discuss your report with Guruji. Book as many sessions as you need.",
                    style = VvType.body(11, VvColors.Ink3),
                )
                ConsultationOfferButtons(onBuy = onBookConsultation)
            }
        }
    }
}

@Composable
private fun ProgressStepRow(label: String, state: ReportProgressStepState) {
    val dotColor = when (state) {
        ReportProgressStepState.DONE -> VvColors.Jade
        ReportProgressStepState.ACTIVE -> VvColors.Saffron
        ReportProgressStepState.PENDING -> VvColors.Ink3.copy(alpha = 0.35f)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor),
        ) {}
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (state == ReportProgressStepState.ACTIVE) FontWeight.SemiBold else FontWeight.Normal,
            color = if (state == ReportProgressStepState.PENDING) VvColors.Ink3 else VvColors.Ink,
        )
    }
}

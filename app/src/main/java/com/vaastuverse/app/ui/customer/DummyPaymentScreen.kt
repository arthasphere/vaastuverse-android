package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.PaymentCheckout
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun DummyPaymentScreen(
    checkout: PaymentCheckout,
    onPaymentSuccess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Demo payment gateway — no real charge.",
            style = VvType.body(12, VvColors.Ink3),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, VvColors.Border, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("ORDER SUMMARY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink3)
            Text(checkout.packageTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            checkout.consultationOffer?.let { offer ->
                Text("${offer.durationMinutes}-minute Guruji consultation", fontSize = 11.sp, color = VvColors.Ink2)
            }
            checkout.linkedReportTitle?.let { title ->
                Text("For report: $title", fontSize = 11.sp, color = VvColors.Ink2)
            }
            checkout.property?.let { property ->
                Text("Property: ${property.label}", fontSize = 11.sp, color = VvColors.Ink2)
            }
            Text("Amount due: ${checkout.priceLabel}", color = VvColors.Jade, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Button(
            onClick = onPaymentSuccess,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VvColors.Jade),
        ) {
            Text("Complete payment successfully (demo)")
        }
    }
}

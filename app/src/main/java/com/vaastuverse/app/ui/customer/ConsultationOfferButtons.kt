package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.ConsultationOffer
import com.vaastuverse.app.ui.VvColors

@Composable
fun ConsultationOfferButtons(
    onBuy: (ConsultationOffer) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (compact) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ConsultationOffer.entries.forEach { offer ->
                OutlinedButton(
                    onClick = { onBuy(offer) },
                    modifier = Modifier.weight(1f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${offer.durationMinutes} min", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(offer.priceLabel, fontSize = 10.sp, color = VvColors.Saffron)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ConsultationOffer.entries.forEach { offer ->
                Button(
                    onClick = { onBuy(offer) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VvColors.Jade),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(offer.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(offer.subtitle, fontSize = 10.sp, color = VvColors.Cream.copy(alpha = 0.9f))
                        }
                        Text(offer.priceLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

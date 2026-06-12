package com.vaastuverse.app.ui.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.PartnerAccess
import com.vaastuverse.app.ui.VvColors

@Composable
fun ProfileChoiceScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    val name = state.customerProfile?.displayName ?: "there"
    val partnerReady = PartnerAccess.isOnboarded(state.session)
    val partnerPending = PartnerAccess.hasOpenApplication(state.applications) && !partnerReady
    val partnerTitle = if (partnerReady) PartnerAccess.onboardedTitle(state.session) else "Partner"

    FlowScaffold("Welcome, $name", state, coordinator::clearMessage) {
        Text(
            "Choose your VaastuVerse path. This choice is permanent for this account.",
            style = MaterialTheme.typography.bodyMedium,
            color = VvColors.Ink2,
        )
        Text(
            "You cannot switch to the other path later unless you delete this account and sign up again.",
            style = MaterialTheme.typography.bodySmall,
            color = VvColors.Ink2,
        )
        ProfileCard(
            emoji = "🏠",
            title = "Customer",
            subtitle = "Reports, muhurats, consultations, and partner discovery",
            tint = VvColors.Jade,
            onClick = { coordinator.chooseCustomer() },
        )
        ProfileCard(
            emoji = PartnerAccess.lockedTrack(state.session, state.applications)?.emoji ?: "🕉",
            title = if (partnerReady) partnerTitle else "Partner",
            subtitle = when {
                partnerReady -> "Your onboarded partner tools (one role per account)"
                partnerPending -> "Application in review — continue partner onboarding"
                else -> "Apply once as Guruji, Designer, or Channel partner"
            },
            tint = VvColors.Gold,
            onClick = { coordinator.choosePartner() },
        )
    }
}

@Composable
private fun ProfileCard(
    emoji: String,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(emoji, fontSize = 28.sp)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
            ),
            color = VvColors.Ink,
        )
        Text(subtitle, fontSize = 13.sp, color = VvColors.Ink2)
        Text("Continue →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tint)
    }
}

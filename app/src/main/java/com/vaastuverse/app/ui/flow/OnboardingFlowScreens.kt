package com.vaastuverse.app.ui.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.PartnerAccess
import com.vaastuverse.app.data.PartnerTrack
import com.vaastuverse.app.ui.VvColors

private val gurujiTiers = listOf(
    "GURUJI_T1" to "Tier 1 — Param Guruji",
    "GURUJI_T2" to "Tier 2 — Guruji",
    "GURUJI_T3" to "Tier 3 — Shishya",
)

@Composable
fun PartnerApplyScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    val available = PartnerAccess.availableTracks(state.session, state.applications)
    val locked = PartnerAccess.lockedTrack(state.session, state.applications)
    val initialPath = locked ?: available.firstOrNull() ?: PartnerTrack.GURUJI
    var selectedPath by rememberSaveable { mutableStateOf(initialPath.name) }
    var selectedTier by rememberSaveable { mutableStateOf("GURUJI_T2") }
    val path = PartnerTrack.entries.find { it.name == selectedPath } ?: initialPath

    FlowScaffold(
        title = "Partner application",
        state = state,
        onClearMessage = coordinator::clearMessage,
        onBack = if (PartnerAccess.hasOpenApplication(state.applications)) {
            { coordinator.openPartnerOnboardingGate() }
        } else {
            null
        },
    ) {
        HintText(
            "You can join as only one partner type for life of this account: Guruji, Designer, or Channel. Choose carefully.",
        )
        if (available.isEmpty()) {
            HintText("You are already onboarded as a partner. Use the menu to open your partner experience.")
        } else {
            if (locked != null) {
                HintText("Continuing your ${locked.label} application. Other partner types are not available.")
            } else {
                Text(
                    "Step 1 — Partner type",
                    fontWeight = FontWeight.SemiBold,
                    color = VvColors.Ink,
                    fontSize = 14.sp,
                )
                available.forEach { track ->
                    TrackCard(
                        track = track,
                        selected = path == track,
                        enabled = true,
                        onClick = { selectedPath = track.name },
                    )
                }
            }
            if (path == PartnerTrack.GURUJI) {
                Text(
                    "Step 2 — Guruji tier",
                    fontWeight = FontWeight.SemiBold,
                    color = VvColors.Ink,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
                gurujiTiers.forEach { (value, label) ->
                    FilterChip(
                        selected = selectedTier == value,
                        onClick = { selectedTier = value },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            val roleToSubmit = when (path) {
                PartnerTrack.GURUJI -> selectedTier
                PartnerTrack.DESIGNER -> "DESIGNER"
                PartnerTrack.CHANNEL -> "CHANNEL_PARTNER"
            }
            PrimaryButton("Submit ${path.label} application", enabled = !state.isLoading) {
                coordinator.applyForPartner(roleToSubmit)
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: PartnerTrack,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tint = when (track) {
        PartnerTrack.GURUJI -> VvColors.Gold
        PartnerTrack.DESIGNER -> VvColors.Purple
        PartnerTrack.CHANNEL -> VvColors.Teal
    }
    val borderColor = if (selected) tint else VvColors.Border
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) tint.copy(alpha = 0.08f) else Color.White)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp),
    ) {
        Text("${track.emoji} ${track.label}", fontWeight = FontWeight.SemiBold, color = VvColors.Ink)
        Text(
            when (track) {
                PartnerTrack.GURUJI -> "Teach, review reports, resolve knowledge conflicts"
                PartnerTrack.DESIGNER -> "Vaastu-aligned interior renders for clients"
                PartnerTrack.CHANNEL -> "Bulk reports, referrals, and client bundles"
            },
            fontSize = 12.sp,
            color = VvColors.Ink2,
        )
    }
}

@Composable
fun PartnerStatusBody(
    state: AppUiState,
    message: String,
    showActions: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HintText(message)
        PartnerAccess.lockedTrack(state.session, state.applications)?.let { track ->
            HintText("Partner type for this account: ${track.label} only.")
        }
        if (state.applications.isEmpty()) {
            HintText("No applications yet.")
        } else {
            state.applications.forEach { app ->
                val track = PartnerTrack.fromRequestedRole(app.requestedRole)?.label ?: app.requestedRole
                Text(
                    "$track — ${app.status}",
                    color = when (app.status) {
                        "APPROVED" -> VvColors.Jade
                        "REJECTED" -> VvColors.Ink3
                        else -> VvColors.Saffron
                    },
                )
                app.reviewerNotes?.let { HintText(it) }
                HintText("Submitted: ${app.submittedAt ?: "—"}")
            }
        }
        if (showActions) {
            SecondaryButton("Refresh status", onClick = onRefresh)
        }
    }
}

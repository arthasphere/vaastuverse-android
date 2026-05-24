package com.vaastuverse.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppPhase
import com.vaastuverse.app.data.ExperienceMode
import com.vaastuverse.app.data.PartnerAccess
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.flow.CustomerProfileScreen
import com.vaastuverse.app.ui.flow.EmailLoginScreen
import com.vaastuverse.app.ui.flow.OtpLoginScreen
import com.vaastuverse.app.ui.flow.PartnerApplyScreen
import com.vaastuverse.app.ui.flow.PartnerOnboardingGateScreen
import com.vaastuverse.app.ui.flow.ProfileChoiceScreen
import com.vaastuverse.app.ui.flow.RegisterScreen
import com.vaastuverse.app.ui.flow.WelcomeScreen
import com.vaastuverse.app.ui.shell.ExperienceShellScreen

@Composable
fun AppRoot(
    coordinator: AppCoordinatorViewModel = viewModel(),
    session: UserSessionViewModel = viewModel(),
) {
    val state by coordinator.state.collectAsState()

    LaunchedEffect(
        state.phase,
        state.experienceMode,
        state.customerProfile,
        state.partnerProfiles,
        state.session,
    ) {
        if (state.phase != AppPhase.Experience) return@LaunchedEffect
        val profile = state.customerProfile
        when (state.experienceMode) {
            ExperienceMode.Customer -> {
                if (profile != null) {
                    session.applyCustomerProfile(profile.displayName, profile.city)
                }
            }
            ExperienceMode.Partner -> {
                val partner = state.partnerProfiles.firstOrNull() ?: return@LaunchedEffect
                session.applyPartnerProfile(
                    businessName = partner.businessName,
                    partnerDisplayName = profile?.displayName,
                    partnerRole = PartnerAccess.primaryPartnerRole(state.session),
                )
            }
        }
    }

    when (state.phase) {
        AppPhase.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        AppPhase.Welcome -> WelcomeScreen(state, coordinator)
        AppPhase.OtpLogin -> OtpLoginScreen(state, coordinator)
        AppPhase.Register -> RegisterScreen(state, coordinator)
        AppPhase.EmailLogin -> EmailLoginScreen(state, coordinator)
        AppPhase.CustomerProfile -> CustomerProfileScreen(state, coordinator)
        AppPhase.ProfileChoice -> ProfileChoiceScreen(state, coordinator)
        AppPhase.PartnerOnboardingGate -> PartnerOnboardingGateScreen(state, coordinator)
        AppPhase.PartnerApply -> PartnerApplyScreen(state, coordinator)
        AppPhase.Experience -> ExperienceShellScreen(state, coordinator, session)
    }
}

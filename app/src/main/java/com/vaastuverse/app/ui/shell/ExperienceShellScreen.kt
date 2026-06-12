package com.vaastuverse.app.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.ExperienceMode
import com.vaastuverse.app.data.PartnerAccess
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.customer.CustomerMainScreen
import com.vaastuverse.app.ui.partner.PartnerShellScreen

@Composable
fun ExperienceShellScreen(
    state: AppUiState,
    coordinator: AppCoordinatorViewModel,
    session: UserSessionViewModel,
) {
    val customerNav = remember { CustomerNavController() }
    val partnerNav = remember { PartnerNavController() }

    LaunchedEffect(state.experienceMode) {
        when (state.experienceMode) {
            ExperienceMode.Customer -> partnerNav.reset()
            ExperienceMode.Partner -> customerNav.reset()
        }
    }

    val menuActions = AppMenuActions(
        experienceMode = state.experienceMode,
        onEditProfile = {
            when (state.experienceMode) {
                ExperienceMode.Customer -> customerNav.openProfile()
                ExperienceMode.Partner -> partnerNav.openProfile()
            }
        },
        onOpenSettings = {
            when (state.experienceMode) {
                ExperienceMode.Customer -> customerNav.openSettings()
                ExperienceMode.Partner -> partnerNav.openSettings()
            }
        },
        onManageProperties = { customerNav.openProperties() },
        partnerOnboardingInProgress = PartnerAccess.hasOpenApplication(state.applications)
            && !PartnerAccess.isOnboarded(state.session),
        onViewPartnerApplication = { coordinator.openPartnerOnboardingGate() },
        onDeleteAccount = { coordinator.deleteAccount() },
        onSignOut = { coordinator.logout() },
    )

    when (state.experienceMode) {
        ExperienceMode.Customer -> CustomerMainScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            coordinator = coordinator,
            session = session,
            customerNav = customerNav,
            menuActions = menuActions,
        )
        ExperienceMode.Partner -> PartnerShellScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            coordinator = coordinator,
            session = session,
            menuActions = menuActions,
            partnerNav = partnerNav,
            initialPersona = state.partnerPersona,
            stats = state.partnerStats,
            lockToOnboardedPersona = true,
        )
    }
}

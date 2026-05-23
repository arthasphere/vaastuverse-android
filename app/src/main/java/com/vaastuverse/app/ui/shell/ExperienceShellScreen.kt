package com.vaastuverse.app.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.ExperienceMode
import com.vaastuverse.app.data.PartnerAccess
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.customer.CustomerMainScreen
import com.vaastuverse.app.ui.partner.PartnerShellScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceShellScreen(
    state: AppUiState,
    coordinator: AppCoordinatorViewModel,
    session: UserSessionViewModel,
) {
    val customerNav = remember { CustomerNavController() }

    LaunchedEffect(state.experienceMode) {
        if (state.experienceMode != ExperienceMode.Customer) {
            customerNav.reset()
        }
    }

    val menuActions = AppMenuActions(
        experienceMode = state.experienceMode,
        onEditProfile = { customerNav.openProfile() },
        onManageProperties = { customerNav.openProperties() },
        onSwitchToCustomer = { coordinator.chooseCustomer() },
        onSwitchToPartner = { coordinator.choosePartner() },
        partnerOnboardingInProgress = PartnerAccess.hasOpenApplication(state.applications)
            && !PartnerAccess.isOnboarded(state.session),
        onViewPartnerApplication = { coordinator.openPartnerOnboardingGate() },
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
        ExperienceMode.Partner -> {
            val showBack = false
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = VvColors.Cream,
                topBar = {
                    TopAppBar(
                        title = { Text("Partner", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            if (showBack) {
                                IconButton(onClick = {}) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        actions = {
                            UserAvatarMenuButton(actions = menuActions)
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = VvColors.Cream),
                    )
                },
            ) { padding ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    PartnerShellScreen(
                        session = session,
                        initialPersona = state.partnerPersona,
                        stats = state.partnerStats,
                        lockToOnboardedPersona = true,
                    )
                }
            }
        }
    }
}

package com.vaastuverse.app.ui.flow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.ExperienceMode
import com.vaastuverse.app.data.PartnerAccess
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.shell.AppMenuActions
import com.vaastuverse.app.ui.shell.UserAvatarMenuButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerOnboardingGateScreen(state: AppUiState, coordinator: AppCoordinatorViewModel) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.error, state.info) {
        state.error?.let { snackbar.showSnackbar(it) }
        state.info?.let { snackbar.showSnackbar(it) }
        coordinator.clearMessage()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = VvColors.Cream,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Partner onboarding",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                    )
                },
                actions = {
                    UserAvatarMenuButton(
                        actions = AppMenuActions(
                            experienceMode = ExperienceMode.Partner,
                            onEditProfile = {},
                            onManageProperties = {},
                            onSwitchToCustomer = { coordinator.chooseCustomer() },
                            onSwitchToPartner = {},
                            partnerOnboardingInProgress = true,
                            onViewPartnerApplication = null,
                            onSignOut = { coordinator.logout() },
                        ),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VvColors.Cream),
            )
        },
    ) { padding ->
        PartnerStatusBody(
            state = state,
            message = PartnerAccess.partnerGateMessage(state.applications),
            showActions = true,
            onRefresh = { coordinator.refreshApplications() },
            modifier = Modifier.padding(padding).padding(horizontal = 20.dp, vertical = 8.dp),
        )
        if (state.isLoading) {
            Text("Updating…", modifier = Modifier.padding(padding).padding(20.dp))
        }
    }
}

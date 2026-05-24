package com.vaastuverse.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.ExperienceMode
import com.vaastuverse.app.ui.VvColors

data class AppMenuActions(
    val experienceMode: ExperienceMode,
    val onEditProfile: () -> Unit,
    val onOpenSettings: () -> Unit = {},
    val onManageProperties: () -> Unit,
    val onSwitchToCustomer: () -> Unit,
    val onSwitchToPartner: () -> Unit,
    val partnerOnboardingInProgress: Boolean,
    val onViewPartnerApplication: (() -> Unit)?,
    val onSignOut: () -> Unit,
)

@Composable
fun UserAvatarMenuButton(
    actions: AppMenuActions,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(VvColors.JadeLight)
                .border(2.dp, VvColors.Jade, CircleShape)
                .clickable { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            Text("🧘", fontSize = (size.value * 0.45f).sp)
        }
        AppMenuDropdown(
            expanded = expanded,
            onDismiss = { expanded = false },
            actions = actions,
        )
    }
}

/** Legacy hamburger — prefer [UserAvatarMenuButton]. */
@Composable
fun AppMenuButton(actions: AppMenuActions) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.Menu, contentDescription = "Menu")
    }
    AppMenuDropdown(
        expanded = expanded,
        onDismiss = { expanded = false },
        actions = actions,
    )
}

@Composable
private fun AppMenuDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    actions: AppMenuActions,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        MenuRow(Icons.Default.Person, "My profile") {
            onDismiss()
            actions.onEditProfile()
        }
        MenuRow(Icons.Default.Settings, "Settings") {
            onDismiss()
            actions.onOpenSettings()
        }
        if (actions.experienceMode == ExperienceMode.Customer) {
            MenuRow(Icons.Default.Place, "My properties") {
                onDismiss()
                actions.onManageProperties()
            }
        }
        HorizontalDivider()
        if (actions.experienceMode == ExperienceMode.Customer) {
            MenuRow(Icons.Default.Work, "Switch to partner") {
                onDismiss()
                actions.onSwitchToPartner()
            }
        } else {
            MenuRow(Icons.Default.Home, "Switch to customer") {
                onDismiss()
                actions.onSwitchToCustomer()
            }
        }
        if (actions.partnerOnboardingInProgress && actions.onViewPartnerApplication != null) {
            MenuRow(Icons.Default.SwapHoriz, "Partner application status") {
                onDismiss()
                actions.onViewPartnerApplication()
            }
        }
        HorizontalDivider()
        MenuRow(Icons.AutoMirrored.Filled.Logout, "Sign out") {
            onDismiss()
            actions.onSignOut()
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

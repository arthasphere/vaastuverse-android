package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import com.vaastuverse.app.ui.shell.AppMenuActions
import com.vaastuverse.app.ui.shell.UserAvatarMenuButton

sealed interface CustomerTopBarMode {
    data object HomeWelcome : CustomerTopBarMode
    data class TabTitle(val label: String) : CustomerTopBarMode
    data class SubPage(val title: String, val onBack: () -> Unit) : CustomerTopBarMode
}

@Composable
fun CustomerTopBar(
    session: UserSessionViewModel,
    menuActions: AppMenuActions,
    mode: CustomerTopBarMode,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VvColors.Cream)
            .statusBarsPadding()
            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (mode) {
            is CustomerTopBarMode.SubPage -> {
                IconButton(onClick = mode.onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    mode.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    color = VvColors.Ink,
                )
            }
            is CustomerTopBarMode.TabTitle -> {
                Text(
                    mode.label,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    fontSize = 15.sp,
                    color = VvColors.Ink,
                )
            }
            CustomerTopBarMode.HomeWelcome -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 2.dp),
                ) {
                    Text(
                        session.namasteGreetingLine,
                        fontSize = 8.sp,
                        letterSpacing = 1.2.sp,
                        color = VvColors.Saffron,
                    )
                    Text("Your Vaastu,", style = VvType.title(16))
                    Text(
                        "your ${session.leadGuideShortName}.",
                        style = VvType.titleItalicSaffron(16),
                    )
                }
            }
        }
        UserAvatarMenuButton(actions = menuActions, size = 38.dp)
    }
}

@Composable
fun CustomerBottomBar(
    tabs: List<CustomerTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    NavigationBar(
        containerColor = VvColors.Cream,
        windowInsets = WindowInsets.navigationBars,
        tonalElevation = 0.dp,
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = VvColors.JadeLight,
                ),
            )
        }
    }
}

data class CustomerTabItem(
    val label: String,
    val icon: ImageVector,
)

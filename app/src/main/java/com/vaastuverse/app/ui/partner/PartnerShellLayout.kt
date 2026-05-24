package com.vaastuverse.app.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.shell.AppMenuActions
import com.vaastuverse.app.ui.shell.UserAvatarMenuButton

sealed interface PartnerTopBarMode {
    data object Home : PartnerTopBarMode
    data class SubPage(val title: String, val onBack: () -> Unit) : PartnerTopBarMode
}

@Composable
fun PartnerTopBar(
    session: UserSessionViewModel,
    menuActions: AppMenuActions,
    mode: PartnerTopBarMode = PartnerTopBarMode.Home,
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
            is PartnerTopBarMode.SubPage -> {
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
            PartnerTopBarMode.Home -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(VvColors.JadeLight)
                            .border(1.5.dp, VvColors.Jade.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(session.partnerTrackEmoji, fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            session.namasteGreetingLine,
                            fontSize = 8.sp,
                            letterSpacing = 1.1.sp,
                            color = VvColors.Saffron,
                        )
                        Text(
                            session.partnerBadgeLine,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = VvColors.Jade,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(VvColors.JadeLight)
                                .border(1.dp, VvColors.Jade.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
        UserAvatarMenuButton(actions = menuActions, size = 38.dp)
    }
}

data class PartnerTabItem(
    val label: String,
    val icon: ImageVector,
)

@Composable
fun PartnerBottomBar(
    tabs: List<PartnerTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    accentColor: Color = VvColors.Saffron,
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
                    selectedIconColor = accentColor,
                    selectedTextColor = accentColor,
                    indicatorColor = VvColors.JadeLight,
                ),
            )
        }
    }
}

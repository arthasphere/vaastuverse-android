package com.vaastuverse.app.ui.customer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors

@Composable
fun CustomerMainScreen(session: UserSessionViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val askLabel = "Ask ${session.leadGuideShortName}"

    val tabs = listOf(
        TabSpec("Home", Icons.Default.Home) { CustomerHomeScreen(session = session) },
        TabSpec("Reports", Icons.Default.Description) { CustomerReportsScreen() },
        TabSpec("Muhurats", Icons.Default.CalendarMonth) { SimplePlaceholderScreen("Muhurats") },
        TabSpec(askLabel, Icons.Default.Phone) { SimplePlaceholderScreen(askLabel) },
    )

    Scaffold(
        containerColor = VvColors.Cream,
        bottomBar = {
            NavigationBar(containerColor = VvColors.Cream) {
                tabs.forEachIndexed { index, spec ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(spec.icon, contentDescription = spec.label) },
                        label = { Text(spec.label, maxLines = 1) },
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            tabs[tab].content()
        }
    }
}

private data class TabSpec(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit,
)

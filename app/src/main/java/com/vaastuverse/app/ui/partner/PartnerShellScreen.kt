package com.vaastuverse.app.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.PartnerDashboardStats
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import com.vaastuverse.app.ui.customer.SimplePlaceholderScreen

enum class PartnerPersona { Guruji, Designer, Channel }

@Composable
fun PartnerShellScreen(
    session: UserSessionViewModel,
    initialPersona: PartnerPersona = PartnerPersona.Guruji,
    stats: PartnerDashboardStats = PartnerDashboardStats(),
    lockToOnboardedPersona: Boolean = true,
) {
    var persona by remember(initialPersona) { mutableStateOf(initialPersona) }
    val activePersona = if (lockToOnboardedPersona) initialPersona else persona

    LaunchedEffect(activePersona) {
        session.applyPartnerDevProfile(activePersona)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!lockToOnboardedPersona) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                PartnerPersona.entries.forEachIndexed { index, p ->
                    SegmentedButton(
                        selected = persona == p,
                        onClick = { persona = p },
                        shape = SegmentedButtonDefaults.itemShape(index, PartnerPersona.entries.size),
                    ) {
                        Text(p.name)
                    }
                }
            }
        }

        when (activePersona) {
            PartnerPersona.Guruji -> GurujiTabShell(session, stats)
            PartnerPersona.Designer -> DesignerTabShell(session)
            PartnerPersona.Channel -> ChannelTabShell(session)
        }
    }
}

@Composable
private fun GurujiTabShell(session: UserSessionViewModel, stats: PartnerDashboardStats) {
    var tab by remember { mutableIntStateOf(0) }
    val gold = VvColors.DarkGold
    val tabs = listOf(
        Tab("Dashboard", Icons.Default.BarChart) { GurujiDashboard(session, stats, gold) },
        Tab("Teach", Icons.Default.Chat) {
            PartnerPlaceholder("Teach", "${stats.knowledgeEntries} knowledge entries", dark = true)
        },
        Tab("Conflicts", Icons.Default.Bolt) {
            PartnerPlaceholder("Conflicts", "${stats.openConflicts} open conflicts", dark = true)
        },
        Tab("Earnings", Icons.Default.AttachMoney) { PartnerPlaceholder("Earnings", "Revenue share", dark = true) },
    )
    PartnerScaffold(tabs, tab, { tab = it }, containerColor = VvColors.DarkBg, selectedColor = gold)
}

@Composable
private fun DesignerTabShell(session: UserSessionViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Tab("Home", Icons.Default.Home) { DesignerHome(session) },
        Tab("Renders", Icons.Default.Photo) { PartnerPlaceholder("Renders", "render-service") },
        Tab("Vaastu", Icons.Default.Description) { PartnerPlaceholder("Vaastu", "report-service") },
        Tab("Earnings", Icons.Default.AttachMoney) { PartnerPlaceholder("Earnings", "Margins + referrals") },
    )
    PartnerScaffold(tabs, tab, { tab = it }, containerColor = VvColors.PartnerBg, selectedColor = VvColors.Purple)
}

@Composable
private fun ChannelTabShell(session: UserSessionViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Tab("Home", Icons.Default.Home) { ChannelHome(session) },
        Tab("Buy", Icons.Default.ShoppingCart) { PartnerPlaceholder("Buy Credits", "Wholesale packs") },
        Tab("Bundle", Icons.Default.Description) { PartnerPlaceholder("Bundle", "Vaastu + renders") },
        Tab("Earnings", Icons.Default.AttachMoney) { PartnerPlaceholder("Earnings", "Margins + bundles") },
    )
    PartnerScaffold(tabs, tab, { tab = it }, containerColor = VvColors.PartnerBg, selectedColor = VvColors.Teal)
}

private data class Tab(val label: String, val icon: ImageVector, val content: @Composable () -> Unit)

@Composable
private fun PartnerScaffold(
    tabs: List<Tab>,
    selected: Int,
    onSelect: (Int) -> Unit,
    containerColor: Color,
    selectedColor: Color,
) {
    Scaffold(
        containerColor = containerColor,
        bottomBar = {
            NavigationBar(containerColor = if (containerColor == VvColors.DarkBg) VvColors.DarkElevated else Color.White) {
                tabs.forEachIndexed { index, spec ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { onSelect(index) },
                        icon = { Icon(spec.icon, contentDescription = spec.label) },
                        label = { Text(spec.label, maxLines = 1) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            indicatorColor = selectedColor.copy(alpha = 0.15f),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            tabs[selected].content()
        }
    }
}

@Composable
private fun GurujiDashboard(session: UserSessionViewModel, stats: PartnerDashboardStats, gold: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VvColors.DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(gold, gold.copy(alpha = 0.7f)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("🕉", fontSize = 18.sp)
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(session.displayName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    "👑 TIER 1 · PARAM GURUJI",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = gold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(gold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }

        Text("YOUR KNOWLEDGE IMPACT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.3f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCell(stats.knowledgeEntries.toString(), "Knowledge entries", gold)
            StatCell(stats.reportsInfluenced.toString(), "Reports influenced", Color(0xFF6CC4B4))
            StatCell(stats.formattedMonthlyEarnings(), "Earned this month", Color(0xFFF5C76A))
        }

        Text("TODAY'S SESSIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
        SessionCard(
            "💬 Consultations",
            "${stats.consultationBookings} bookings on your calendar",
            gold,
            if (stats.consultationBookings > 0) "View" else "—",
        )
        SessionCard(
            "⚡ Conflict Queue",
            "${stats.openConflicts} open conflicts",
            Color(0xFFFCA5A5),
            if (stats.openConflicts > 0) "${stats.openConflicts} open" else "Clear",
        )
        SessionCard(
            "📋 Report Reviews",
            "${stats.reportsPendingReview} reports awaiting review",
            Color(0xFF6CC4B4),
            if (stats.reportsPendingReview > 0) "Review" else "—",
        )
    }
}

@Composable
private fun StatCell(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = color, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
        Text(label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f))
    }
}

@Composable
private fun SessionCard(title: String, subtitle: String, tint: Color, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.08f))
            .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tint)
            Text(subtitle, fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
        }
        Text(
            action,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            modifier = Modifier
                .background(tint.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun DesignerHome(session: UserSessionViewModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Brush.linearGradient(listOf(VvColors.Purple, Color(0xFF7B5CC6)))),
        ) {
            Column(modifier = Modifier.padding(14.dp).align(Alignment.BottomStart)) {
                Text(session.designerPartnerHeroSubtitle, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                Text(session.morningGreetingLine, style = VvType.title(16).copy(color = Color.White))
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    MiniStat("8", "Render credits")
                    MiniStat("3", "Vaastu credits")
                    MiniStat("₹9.2K", "This month")
                }
            }
        }
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CreditBar("🖼 Render Credits", "8 remaining", 0.8f, VvColors.Purple)
            CreditBar("📋 Vaastu Credits", "3 remaining", 0.6f, VvColors.Teal)
        }
    }
}

@Composable
private fun ChannelHome(session: UserSessionViewModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Brush.linearGradient(listOf(VvColors.Teal, Color(0xFF0A6558)))),
        ) {
            Column(modifier = Modifier.padding(14.dp).align(Alignment.BottomStart)) {
                Text(session.channelPartnerHeroSubtitle, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                Text(session.morningGreetingLine, style = VvType.title(16).copy(color = Color.White))
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    MiniStat("14", "Vaastu credits")
                    MiniStat("4", "Interior credits")
                    MiniStat("₹12.7K", "This month")
                }
            }
        }
        Column(modifier = Modifier.padding(14.dp)) {
            CreditBar("📋 Vaastu Credits", "14 left", 0.7f, VvColors.Teal)
            Spacer(modifier = Modifier.height(10.dp))
            CreditBar("🖼 Interior Credits", "4 left", 0.4f, VvColors.Purple)
        }
    }
}

@Composable
private fun RowScope.MiniStat(value: String, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun CreditBar(title: String, value: String, progress: Float, tint: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E6ED), RoundedCornerShape(11.dp))
            .padding(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = tint)
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tint)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFE0E6ED)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .background(Brush.horizontalGradient(listOf(tint, tint.copy(alpha = 0.7f)))),
            )
        }
    }
}

@Composable
private fun PartnerPlaceholder(title: String, subtitle: String, dark: Boolean = false) {
  if (dark) {
    Box(
      modifier = Modifier.fillMaxSize().background(VvColors.DarkBg),
      contentAlignment = Alignment.Center,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = VvType.title(20).copy(color = Color.White))
        Text(subtitle, style = VvType.body(13, Color.White.copy(alpha = 0.55f)), modifier = Modifier.padding(top = 8.dp))
      }
    }
  } else {
    SimplePlaceholderScreen(title, subtitle)
  }
}

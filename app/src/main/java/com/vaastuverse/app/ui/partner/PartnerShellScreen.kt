package com.vaastuverse.app.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.PartnerDashboardStats
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import androidx.compose.ui.platform.LocalContext
import com.vaastuverse.app.data.repository.ConsultationRepository
import com.vaastuverse.app.data.repository.OrderRepository
import com.vaastuverse.app.data.repository.ReportReviewRepository
import com.vaastuverse.app.ui.customer.CustomerMiddleScrollSection
import com.vaastuverse.app.ui.customer.CustomerMiddleThemes
import com.vaastuverse.app.ui.customer.SimplePlaceholderScreen
import com.vaastuverse.app.ui.shared.CommunicationSettingsScreen
import com.vaastuverse.app.ui.shared.UserAccountProfileScreen
import com.vaastuverse.app.ui.shell.AppMenuActions
import com.vaastuverse.app.ui.shell.PartnerNavController

enum class PartnerPersona { Guruji, Designer, Channel }

private enum class PartnerOverlay {
    None,
    Profile,
    Settings,
}

@Composable
fun PartnerShellScreen(
    modifier: Modifier = Modifier,
    state: AppUiState,
    coordinator: AppCoordinatorViewModel,
    session: UserSessionViewModel,
    menuActions: AppMenuActions,
    partnerNav: PartnerNavController,
    initialPersona: PartnerPersona = PartnerPersona.Guruji,
    stats: PartnerDashboardStats = PartnerDashboardStats(),
    lockToOnboardedPersona: Boolean = true,
) {
    var persona by remember(initialPersona) { mutableStateOf(initialPersona) }
    var overlay by remember { mutableStateOf(PartnerOverlay.None) }
    val activePersona = if (lockToOnboardedPersona) initialPersona else persona

    LaunchedEffect(activePersona) {
        if (!lockToOnboardedPersona) {
            session.applyPartnerDevProfile(activePersona)
        }
    }

    LaunchedEffect(partnerNav.openProfileRequest) {
        if (partnerNav.openProfileRequest > 0) {
            coordinator.refreshAccount()
            overlay = PartnerOverlay.Profile
        }
    }

    LaunchedEffect(partnerNav.openSettingsRequest) {
        if (partnerNav.openSettingsRequest > 0) {
            overlay = PartnerOverlay.Settings
        }
    }

    val topBarMode: PartnerTopBarMode = when (overlay) {
        PartnerOverlay.Profile -> PartnerTopBarMode.SubPage(
            title = "My profile",
            onBack = { overlay = PartnerOverlay.None },
        )
        PartnerOverlay.Settings -> PartnerTopBarMode.SubPage(
            title = "Settings",
            onBack = { overlay = PartnerOverlay.None },
        )
        PartnerOverlay.None -> PartnerTopBarMode.Home
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VvColors.Cream),
    ) {
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

        PartnerTopBar(session = session, menuActions = menuActions, mode = topBarMode)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (overlay) {
                PartnerOverlay.Profile -> {
                    CustomerMiddleScrollSection {
                        UserAccountProfileScreen(
                            account = state.account,
                            profile = state.customerProfile,
                            isLoading = state.isLoading,
                            onSave = { name, city, dob ->
                                coordinator.saveCustomerProfileInExperience(name, city, dob)
                                coordinator.refreshAccount()
                                overlay = PartnerOverlay.None
                            },
                        )
                    }
                }
                PartnerOverlay.Settings -> {
                    CustomerMiddleScrollSection {
                        CommunicationSettingsScreen(
                            preferences = state.communicationPreferences,
                            onChange = coordinator::updateCommunicationPreferences,
                        )
                    }
                }
                PartnerOverlay.None -> when (activePersona) {
                    PartnerPersona.Guruji -> GurujiTabShell(
                        session = session,
                        stats = stats,
                        storedSession = state.session,
                        onNotify = coordinator::showUserMessage,
                    )
                    PartnerPersona.Designer -> DesignerTabShell(session)
                    PartnerPersona.Channel -> ChannelTabShell(session)
                }
            }
        }
    }
}

@Composable
private fun GurujiTabShell(
    session: UserSessionViewModel,
    stats: PartnerDashboardStats,
    storedSession: com.vaastuverse.app.data.StoredSession?,
    onNotify: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val orderRepo = remember { OrderRepository(context.applicationContext) }
    val reviewRepo = remember { ReportReviewRepository() }
    val consultationRepo = remember { ConsultationRepository() }
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        PartnerTabItem("Dashboard", Icons.Default.BarChart),
        PartnerTabItem("Reports", Icons.Default.Description),
        PartnerTabItem("Consult", Icons.Default.CalendarMonth),
        PartnerTabItem("Conflicts", Icons.Default.Bolt),
        PartnerTabItem("Earnings", Icons.Default.AttachMoney),
    )
    PartnerTabContent(
        tabs = tabs,
        selected = tab,
        onSelect = { tab = it },
        accentColor = VvColors.Saffron,
    ) {
        when (tab) {
            0 -> GurujiDashboard(stats)
            1 -> PartnerGurujiReportsScreen(
                session = storedSession,
                orderRepo = orderRepo,
                reviewRepo = reviewRepo,
                onNotify = onNotify,
            )
            2 -> PartnerGurujiConsultationsScreen(
                session = storedSession,
                orderRepo = orderRepo,
                consultationRepo = consultationRepo,
                onNotify = onNotify,
            )
            3 -> PartnerPlaceholder("Conflicts", "${stats.openConflicts} open conflicts")
            else -> PartnerPlaceholder("Earnings", "Revenue share")
        }
    }
}

@Composable
private fun DesignerTabShell(session: UserSessionViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        PartnerTabItem("Home", Icons.Default.Home),
        PartnerTabItem("Renders", Icons.Default.Photo),
        PartnerTabItem("Vaastu", Icons.Default.Description),
        PartnerTabItem("Earnings", Icons.Default.AttachMoney),
    )
    PartnerTabContent(
        tabs = tabs,
        selected = tab,
        onSelect = { tab = it },
        accentColor = VvColors.Jade,
    ) {
        when (tab) {
            0 -> DesignerHome(session)
            1 -> PartnerPlaceholder("Renders", "render-service")
            2 -> PartnerPlaceholder("Vaastu", "report-service")
            else -> PartnerPlaceholder("Earnings", "Margins + referrals")
        }
    }
}

@Composable
private fun ChannelTabShell(session: UserSessionViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        PartnerTabItem("Home", Icons.Default.Home),
        PartnerTabItem("Buy", Icons.Default.ShoppingCart),
        PartnerTabItem("Bundle", Icons.Default.Description),
        PartnerTabItem("Earnings", Icons.Default.AttachMoney),
    )
    PartnerTabContent(
        tabs = tabs,
        selected = tab,
        onSelect = { tab = it },
        accentColor = VvColors.Teal,
    ) {
        when (tab) {
            0 -> ChannelHome(session)
            1 -> PartnerPlaceholder("Buy Credits", "Wholesale packs")
            2 -> PartnerPlaceholder("Bundle", "Vaastu + renders")
            else -> PartnerPlaceholder("Earnings", "Margins + bundles")
        }
    }
}

@Composable
private fun PartnerTabContent(
    tabs: List<PartnerTabItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                content = content,
            )
        }
        PartnerBottomBar(
            tabs = tabs,
            selectedIndex = selected,
            onTabSelected = onSelect,
            accentColor = accentColor,
        )
    }
}

@Composable
private fun GurujiDashboard(stats: PartnerDashboardStats) {
    PartnerMiddlePane(background = CustomerMiddleThemes.homeGradient) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "YOUR KNOWLEDGE IMPACT",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = VvColors.Ink3,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCell(stats.knowledgeEntries.toString(), "Knowledge entries", VvColors.Saffron)
                StatCell(stats.reportsInfluenced.toString(), "Reports influenced", VvColors.Jade)
                StatCell(stats.formattedMonthlyEarnings(), "Earned this month", VvColors.Gold)
            }

            Text(
                "TODAY'S SESSIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VvColors.Ink2,
                modifier = Modifier.padding(top = 4.dp),
            )
            SessionCard(
                "💬 Consultations",
                "${stats.consultationBookings} bookings on your calendar",
                VvColors.Saffron,
                if (stats.consultationBookings > 0) "View" else "—",
            )
            SessionCard(
                "⚡ Conflict Queue",
                "${stats.openConflicts} open conflicts",
                VvColors.Red,
                if (stats.openConflicts > 0) "${stats.openConflicts} open" else "Clear",
            )
            SessionCard(
                "📋 Report Reviews",
                "${stats.reportsPendingReview} reports awaiting review",
                VvColors.Jade,
                if (stats.reportsPendingReview > 0) "Review" else "—",
            )
        }
    }
}

@Composable
private fun PartnerMiddlePane(
    background: Brush = Brush.linearGradient(
        colors = listOf(VvColors.Cream, VvColors.Cream),
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(bottom = 8.dp),
        content = content,
    )
}

@Composable
private fun StatCell(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp),
    ) {
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
        )
        Text(label, fontSize = 8.sp, color = VvColors.Ink3)
    }
}

@Composable
private fun SessionCard(title: String, subtitle: String, tint: Color, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tint)
            Text(subtitle, fontSize = 9.sp, color = VvColors.Ink3)
        }
        Text(
            action,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(tint.copy(alpha = 0.12f))
                .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun DesignerHome(session: UserSessionViewModel) {
    PartnerMiddlePane(background = CustomerMiddleThemes.homeGradient) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(session.morningGreetingLine, style = VvType.title(18))
            Text(session.designerPartnerHeroSubtitle, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink3)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("8", "Render credits")
                MiniStat("3", "Vaastu credits")
                MiniStat("₹9.2K", "This month")
            }
            CreditBar("🖼 Render Credits", "8 remaining", 0.8f, VvColors.Jade)
            CreditBar("📋 Vaastu Credits", "3 remaining", 0.6f, VvColors.Saffron)
        }
    }
}

@Composable
private fun ChannelHome(session: UserSessionViewModel) {
    PartnerMiddlePane(background = CustomerMiddleThemes.homeGradient) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(session.morningGreetingLine, style = VvType.title(18))
            Text(session.channelPartnerHeroSubtitle, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink3)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("14", "Vaastu credits")
                MiniStat("4", "Interior credits")
                MiniStat("₹12.7K", "This month")
            }
            CreditBar("📋 Vaastu Credits", "14 left", 0.7f, VvColors.Teal)
            CreditBar("🖼 Interior Credits", "4 left", 0.4f, VvColors.Jade)
        }
    }
}

@Composable
private fun RowScope.MiniStat(value: String, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(7.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = VvColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, fontSize = 7.sp, color = VvColors.Ink3)
    }
}

@Composable
private fun CreditBar(title: String, value: String, progress: Float, tint: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(11.dp))
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
                .background(VvColors.Border.copy(alpha = 0.5f)),
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
private fun PartnerPlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VvColors.Cream),
        contentAlignment = Alignment.Center,
    ) {
        SimplePlaceholderScreen(title, subtitle)
    }
}

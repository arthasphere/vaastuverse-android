package com.vaastuverse.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.ApiConfig
import com.vaastuverse.app.data.GatewayHealthChecker
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.ui.customer.CustomerMainScreen
import com.vaastuverse.app.ui.partner.PartnerShellScreen
import kotlinx.coroutines.launch

private enum class AppShell { Customer, Partner }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootShellScreen(session: UserSessionViewModel) {
    var shell by remember { mutableIntStateOf(0) }
    var healthMessage by remember { mutableStateOf<String?>(null) }
    var healthLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "VaastuVerse",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VvColors.Cream),
            )
        },
        containerColor = VvColors.Cream,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                AppShell.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = shell == index,
                        onClick = {
                            shell = index
                            if (item == AppShell.Customer) {
                                session.applyProfile("Priya Sharma")
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, AppShell.entries.size),
                    ) {
                        Text(item.name)
                    }
                }
            }

            BackendCard(
                gatewayUrl = ApiConfig.gatewayBaseUrl,
                healthMessage = healthMessage,
                loading = healthLoading,
                onPing = {
                    scope.launch {
                        healthLoading = true
                        healthMessage = GatewayHealthChecker.ping()
                            .fold(
                                onSuccess = { "OK — $it" },
                                onFailure = { "Failed — ${it.message ?: it.toString()}" },
                            )
                        healthLoading = false
                    }
                },
            )

            when (AppShell.entries[shell]) {
                AppShell.Customer -> CustomerMainScreen(session = session)
                AppShell.Partner -> PartnerShellScreen(session = session)
            }
        }
    }
}

@Composable
private fun BackendCard(
    gatewayUrl: String,
    healthMessage: String?,
    loading: Boolean,
    onPing: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VvColors.JadeLight),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Backend (dev)", style = VvType.body(11, VvColors.Jade).copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
            Text(gatewayUrl, style = VvType.body(10, VvColors.Ink2))
            TextButton(onClick = onPing, enabled = !loading) {
                Text(if (loading) "Pinging…" else "Ping gateway")
            }
            healthMessage?.let {
                Text(it, style = VvType.body(9, VvColors.Ink3))
            }
        }
    }
}

package com.vaastuverse.app.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.CommunicationPreferences
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CommunicationSettingsScreen(
    preferences: CommunicationPreferences,
    onChange: (CommunicationPreferences) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Communication", style = VvType.title(16))
        Text(
            "Choose how VaastuVerse reaches you. Preferences are saved on this device until server sync is available.",
            style = VvType.body(12, VvColors.Ink2),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        PrefToggle(
            title = "Push notifications",
            subtitle = "Orders, consultations, and partner alerts",
            checked = preferences.pushEnabled,
            onChecked = { onChange(preferences.copy(pushEnabled = it)) },
        )
        PrefToggle(
            title = "SMS",
            subtitle = "OTP and urgent updates",
            checked = preferences.smsEnabled,
            onChecked = { onChange(preferences.copy(smsEnabled = it)) },
        )
        PrefToggle(
            title = "Email",
            subtitle = "Receipts and report delivery",
            checked = preferences.emailEnabled,
            onChecked = { onChange(preferences.copy(emailEnabled = it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Topics", style = VvType.body(13, VvColors.Ink2))

        PrefToggle(
            title = "Order & report updates",
            subtitle = "Status changes on your purchases",
            checked = preferences.orderUpdatesEnabled,
            onChecked = { onChange(preferences.copy(orderUpdatesEnabled = it)) },
        )
        PrefToggle(
            title = "Partner workspace alerts",
            subtitle = "Reviews, conflicts, and earnings",
            checked = preferences.partnerAlertsEnabled,
            onChecked = { onChange(preferences.copy(partnerAlertsEnabled = it)) },
        )
        PrefToggle(
            title = "Tips & offers",
            subtitle = "Optional marketing messages",
            checked = preferences.marketingEnabled,
            onChecked = { onChange(preferences.copy(marketingEnabled = it)) },
        )
    }
}

@Composable
private fun PrefToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = VvType.body(14, VvColors.Ink))
            Text(subtitle, style = VvType.body(11, VvColors.Ink3))
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

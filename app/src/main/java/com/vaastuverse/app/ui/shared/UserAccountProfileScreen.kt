package com.vaastuverse.app.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.dto.AccountMeResponse
import com.vaastuverse.app.data.dto.CustomerProfileResponse
import com.vaastuverse.app.data.formatDateOfBirthForDisplay
import com.vaastuverse.app.data.isValidDateOfBirthInput
import com.vaastuverse.app.data.parseDateOfBirthInput
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun UserAccountProfileScreen(
    account: AccountMeResponse?,
    profile: CustomerProfileResponse?,
    isLoading: Boolean,
    showCity: Boolean = true,
    showDateOfBirth: Boolean = true,
    onSave: (displayName: String, city: String?, dateOfBirthIso: String?) -> Unit,
) {
    var displayName by rememberSaveable(account?.userId, profile?.displayName) {
        mutableStateOf(profile?.displayName.orEmpty())
    }
    var city by rememberSaveable(account?.userId, profile?.city) {
        mutableStateOf(profile?.city.orEmpty())
    }
    var dateOfBirth by rememberSaveable(account?.userId, profile?.dateOfBirth) {
        mutableStateOf(formatDateOfBirthForDisplay(profile?.dateOfBirth))
    }

    val phoneVerified = account?.phoneVerified == true
    val emailVerified = account?.emailVerified == true
    val dobValid = dateOfBirth.isBlank() || isValidDateOfBirthInput(dateOfBirth)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Account details",
            style = VvType.title(16),
        )
        Text(
            "Verified phone and email cannot be changed here. Date of birth is required before you submit property details for a report.",
            style = VvType.body(12, VvColors.Ink2),
        )

        ProfileField(
            label = "Full name",
            value = displayName,
            onValueChange = { displayName = it },
            enabled = true,
            verified = false,
        )

        ProfileField(
            label = "Phone",
            value = account?.phone.orEmpty().ifBlank { "—" },
            onValueChange = {},
            enabled = false,
            verified = phoneVerified,
        )

        ProfileField(
            label = "Email",
            value = account?.email.orEmpty().ifBlank { "—" },
            onValueChange = {},
            enabled = false,
            verified = emailVerified,
        )

        if (showCity) {
            ProfileField(
                label = "City",
                value = city,
                onValueChange = { city = it },
                enabled = true,
                verified = false,
            )
        }

        if (showDateOfBirth) {
            ProfileField(
                label = "Date of birth (DD/MM/YYYY)",
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                enabled = true,
                verified = false,
                isError = dateOfBirth.isNotBlank() && !dobValid,
                supportingText = if (dateOfBirth.isNotBlank() && !dobValid) "Use DD/MM/YYYY format" else null,
            )
        }

        Button(
            onClick = {
                onSave(
                    displayName.trim(),
                    city.trim().ifBlank { null },
                    parseDateOfBirthInput(dateOfBirth),
                )
            },
            enabled = displayName.isNotBlank() && dobValid && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Saving…" else "Save profile")
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    verified: Boolean,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = VvType.body(11, VvColors.Ink3))
            if (verified) {
                Text(
                    "Verified",
                    style = VvType.body(10, VvColors.Jade),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            readOnly = !enabled,
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
        )
    }
}

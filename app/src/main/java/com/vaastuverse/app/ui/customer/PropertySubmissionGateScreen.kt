package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import com.vaastuverse.app.data.PropertyBuyerInfo
import com.vaastuverse.app.data.formatDateOfBirthForDisplay
import com.vaastuverse.app.data.isValidDateOfBirthInput
import com.vaastuverse.app.data.parseDateOfBirthInput
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

data class PropertySubmissionGateResult(
    val userDateOfBirthIso: String?,
    val buyerInfo: PropertyBuyerInfo,
)

@Composable
fun PropertySubmissionGateScreen(
    profileHasDob: Boolean,
    profileDateOfBirthIso: String?,
    isLoading: Boolean,
    onContinue: (PropertySubmissionGateResult) -> Unit,
) {
    var userDob by rememberSaveable { mutableStateOf("") }
    var buyerDifferent by rememberSaveable { mutableStateOf(false) }
    var buyerName by rememberSaveable { mutableStateOf("") }
    var buyerDob by rememberSaveable { mutableStateOf("") }

    val needsUserDob = !profileHasDob
    val userDobValid = !needsUserDob || isValidDateOfBirthInput(userDob)
    val buyerFieldsValid = !buyerDifferent ||
        (buyerName.isNotBlank() && isValidDateOfBirthInput(buyerDob))
    val canContinue = userDobValid && buyerFieldsValid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Before property details", style = VvType.title(16))
        Text(
            "Confirm who this Vaastu report is for. Your profile date of birth is saved once and reused.",
            style = VvType.body(12, VvColors.Ink2),
        )

        if (profileHasDob) {
            Text(
                "Your DOB on file: ${formatDateOfBirthForDisplay(profileDateOfBirthIso)}",
                style = VvType.body(12, VvColors.Jade),
            )
        } else {
            OutlinedTextField(
                value = userDob,
                onValueChange = { userDob = it },
                label = { Text("Your date of birth (DD/MM/YYYY) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = userDob.isNotBlank() && !userDobValid,
                supportingText = {
                    if (userDob.isNotBlank() && !userDobValid) Text("Use DD/MM/YYYY format")
                },
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = buyerDifferent,
                    onCheckedChange = { buyerDifferent = it },
                )
                Text(
                    "Buyer is different from me",
                    style = VvType.body(13, VvColors.Ink),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            if (buyerDifferent) {
                Text(
                    "Enter the actual buyer's details. These are stored only for this order, not your profile.",
                    style = VvType.body(11, VvColors.Ink3),
                )
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer full name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = buyerDob,
                    onValueChange = { buyerDob = it },
                    label = { Text("Buyer date of birth (DD/MM/YYYY) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = buyerDob.isNotBlank() && !isValidDateOfBirthInput(buyerDob),
                )
            }
        }

        Button(
            onClick = {
                val userIso = when {
                    profileHasDob -> profileDateOfBirthIso
                    else -> parseDateOfBirthInput(userDob)
                }
                onContinue(
                    PropertySubmissionGateResult(
                        userDateOfBirthIso = userIso,
                        buyerInfo = PropertyBuyerInfo(
                            buyerDifferentFromUser = buyerDifferent,
                            buyerFullName = buyerName.trim().ifBlank { null },
                            buyerDateOfBirthIso = if (buyerDifferent) {
                                parseDateOfBirthInput(buyerDob)
                            } else {
                                null
                            },
                        ),
                    ),
                )
            },
            enabled = canContinue && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Saving…" else "Continue to property details")
        }
    }
}

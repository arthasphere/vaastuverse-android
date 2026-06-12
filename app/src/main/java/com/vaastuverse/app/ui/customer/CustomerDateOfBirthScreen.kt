package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.isValidDateOfBirthInput
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerDateOfBirthScreen(
    isLoading: Boolean,
    onSave: (dateOfBirthInput: String) -> Unit,
) {
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    val valid = isValidDateOfBirthInput(dateOfBirth)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Date of birth required",
            style = VvType.title(16),
        )
        Text(
            "We need your date of birth before you submit property details for this order. This is not collected during sign-up.",
            style = VvType.body(12, VvColors.Ink2),
        )
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it },
            label = { Text("Date of birth (DD/MM/YYYY) *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = dateOfBirth.isNotBlank() && !valid,
            supportingText = {
                if (dateOfBirth.isNotBlank() && !valid) {
                    Text("Use DD/MM/YYYY format")
                }
            },
        )
        Button(
            onClick = { onSave(dateOfBirth.trim()) },
            enabled = valid && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Saving…" else "Continue to property details")
        }
    }
}

package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.SavedProperty
import com.vaastuverse.app.data.SavedPropertyType
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerPropertyEditorScreen(
    existing: SavedProperty?,
    defaultType: SavedPropertyType?,
    onSave: (SavedProperty) -> Unit,
) {
    var type by rememberSaveable {
        mutableStateOf(existing?.type ?: defaultType ?: SavedPropertyType.HOME)
    }
    var label by rememberSaveable { mutableStateOf(existing?.label.orEmpty()) }
    var address by rememberSaveable { mutableStateOf(existing?.address.orEmpty()) }
    var city by rememberSaveable { mutableStateOf(existing?.city.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(existing?.notes.orEmpty()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Property type", style = VvType.body(12, VvColors.Ink2))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SavedPropertyType.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text("${option.icon} ${option.label}") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Label (e.g. Flat 4B, Main office)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address / locality") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Button(
            onClick = {
                onSave(
                    SavedProperty(
                        id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                        type = type,
                        label = label.trim(),
                        address = address.trim(),
                        city = city.trim().ifBlank { null },
                        notes = notes.trim().ifBlank { null },
                    ),
                )
            },
            enabled = label.isNotBlank() && address.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (existing == null) "Save property" else "Update property")
        }
    }
}

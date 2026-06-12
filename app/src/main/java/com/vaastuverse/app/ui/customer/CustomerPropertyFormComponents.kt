package com.vaastuverse.app.ui.customer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.PropertyFileFieldSpec
import com.vaastuverse.app.data.PropertyUploadState
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun PropertySectionTitle(text: String) {
    Text(text, style = VvType.body(12, VvColors.Ink2))
}

@Composable
fun PropertyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine && minLines == 1,
        minLines = minLines,
    )
}

@Composable
fun PropertyChipGroup(
    label: String,
    options: List<String>,
    selected: String,
    columns: Int = 4,
    onSelect: (String) -> Unit,
) {
    PropertySectionTitle(label)
    options.chunked(columns).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = { Text(option) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(columns - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PropertyOptionalSection(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Optional details", style = VvType.body(11, VvColors.Ink3))
        content()
    }
}

@Composable
fun PropertyFileUploadField(
    spec: PropertyFileFieldSpec,
    currentValue: String,
    uploadState: PropertyUploadState,
    onPickAndUpload: (String) -> Unit,
    onRetry: () -> Unit,
    onClear: () -> Unit,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onPickAndUpload(it.toString()) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(spec.label, style = VvType.body(12, VvColors.Ink2))
        Text("Optional — upload or skip", style = VvType.body(10, VvColors.Ink3))
        when (uploadState) {
            is PropertyUploadState.Uploading -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(progress = { uploadState.progressPercent / 100f }, modifier = Modifier)
                    Text("Uploading… ${uploadState.progressPercent}%", style = VvType.body(11, VvColors.Ink3))
                }
            }
            is PropertyUploadState.Completed -> {
                Text("Uploaded successfully", style = VvType.body(11, VvColors.Jade))
                OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                    Text("Remove file")
                }
            }
            is PropertyUploadState.Failed -> {
                Text(uploadState.message, style = VvType.body(11, VvColors.Red))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRetry, modifier = Modifier.weight(1f)) { Text("Retry") }
                    OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Cancel") }
                }
            }
            PropertyUploadState.Idle -> {
                if (currentValue.isNotBlank() && (currentValue.startsWith("http") || currentValue.startsWith("file://"))) {
                    Text("File attached", style = VvType.body(11, VvColors.Jade))
                    OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Remove file") }
                } else {
                    OutlinedButton(
                        onClick = { picker.launch(spec.mimeTypes.toTypedArray()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Choose file")
                    }
                }
            }
        }
    }
}

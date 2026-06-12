package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.data.BhkOptions
import com.vaastuverse.app.data.CompassDirections
import com.vaastuverse.app.data.FactoryTypeOptions
import com.vaastuverse.app.data.OfficeBusinessTypeOptions
import com.vaastuverse.app.data.OfficeTypeOptions
import com.vaastuverse.app.data.PropertyFieldKeys
import com.vaastuverse.app.data.PropertyFileFields
import com.vaastuverse.app.data.PropertyUploadState
import com.vaastuverse.app.data.SavedProperty
import com.vaastuverse.app.data.SavedPropertyType
import com.vaastuverse.app.data.ShopBusinessTypeOptions
import com.vaastuverse.app.data.ShopSizeOptions
import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.buildPropertyLabel
import com.vaastuverse.app.data.hasBlockingUpload
import com.vaastuverse.app.data.hasPendingFileUploads
import com.vaastuverse.app.data.isValid
import com.vaastuverse.app.data.saveBlockerMessage
import com.vaastuverse.app.data.newLocalPropertyId
import com.vaastuverse.app.data.repository.PropertyUploadRepository
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import kotlinx.coroutines.launch

@Composable
fun CustomerPropertyEditorScreen(
    existing: SavedProperty?,
    defaultType: SavedPropertyType?,
    lockType: Boolean = false,
    session: StoredSession?,
    uploadRepo: PropertyUploadRepository,
    onNotify: (String) -> Unit = {},
    onSave: (SavedProperty) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val initialDetails = existing?.details.orEmpty()
    var typeName by rememberSaveable {
        mutableStateOf((existing?.type ?: defaultType ?: SavedPropertyType.HOME).name)
    }
    val type = runCatching { SavedPropertyType.valueOf(typeName) }
        .getOrDefault(existing?.type ?: defaultType ?: SavedPropertyType.HOME)
    var details by remember(existing?.id, defaultType) { mutableStateOf(initialDetails) }
    val uploadStates = remember { mutableStateMapOf<String, PropertyUploadState>() }
    val pendingUris = remember { mutableStateMapOf<String, String>() }

    fun field(key: String) = details[key].orEmpty()
    fun setField(key: String, value: String) {
        details = details.toMutableMap().apply { put(key, value) }
    }

    fun applySuggestions(suggested: Map<String, String>) {
        if (suggested.isEmpty()) return
        val merged = details.toMutableMap()
        var filled = 0
        suggested.forEach { (key, value) ->
            if (merged[key].orEmpty().isBlank() && value.isNotBlank()) {
                merged[key] = value
                filled++
            }
        }
        if (filled > 0) {
            details = merged
            onNotify("Pre-filled $filled field(s) from your document")
        }
    }

    fun startUpload(fieldKey: String, uri: String) {
        val currentSession = session ?: run {
            onNotify("Sign in required to upload files")
            return
        }
        pendingUris[fieldKey] = uri
        setField(fieldKey, "local-upload://$fieldKey")
        uploadStates[fieldKey] = PropertyUploadState.Uploading(0)
        scope.launch {
            runCatching {
                val response = uploadRepo.upload(currentSession, fieldKey, uri) { progress ->
                    uploadStates[fieldKey] = PropertyUploadState.Uploading(progress)
                }
                val url = response.fileUrl ?: throw IllegalStateException("Upload returned no URL")
                setField(fieldKey, url)
                uploadStates[fieldKey] = PropertyUploadState.Completed(url)
                pendingUris.remove(fieldKey)
                val label = PropertyFileFields.specsFor(type).find { it.key == fieldKey }?.label ?: "File"
                onNotify("$label uploaded")
                runCatching {
                    uploadRepo.suggestDetails(currentSession, response.id, type.name)
                }.onSuccess { suggestions ->
                    applySuggestions(suggestions.suggestedDetails.orEmpty())
                }
            }.onFailure { error ->
                setField(fieldKey, "")
                uploadStates[fieldKey] = PropertyUploadState.Failed(
                    message = error.message ?: "Upload failed",
                    retryUri = uri,
                )
                onNotify("Upload failed: ${error.message}. You can still save without the file.")
            }
        }
    }

    fun clearFile(fieldKey: String) {
        uploadStates.remove(fieldKey)
        pendingUris.remove(fieldKey)
        setField(fieldKey, "")
    }

    val draft = SavedProperty(
        id = existing?.id ?: newLocalPropertyId(),
        type = type,
        label = buildPropertyLabel(type, details),
        details = details,
    )
    val canSave = draft.isValid() && !uploadStates.hasBlockingUpload()
    val saveBlocker = draft.saveBlockerMessage(uploadStates)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!lockType) {
            PropertySectionTitle("Property type")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SavedPropertyType.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            FilterChip(
                                selected = type == option,
                                onClick = {
                                    typeName = option.name
                                    details = emptyMap()
                                    uploadStates.clear()
                                    pendingUris.clear()
                                },
                                label = { Text("${option.icon} ${option.label}") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            Text("${type.icon} ${type.label}", style = VvType.body(13, VvColors.Ink2))
        }

        when (type) {
            SavedPropertyType.HOME -> HomePropertyFields(
                ::field, ::setField, type, uploadStates, ::startUpload, ::clearFile, pendingUris,
            )
            SavedPropertyType.OFFICE -> OfficePropertyFields(
                ::field, ::setField, type, uploadStates, ::startUpload, ::clearFile, pendingUris,
            )
            SavedPropertyType.SHOP -> ShopPropertyFields(
                ::field, ::setField, type, uploadStates, ::startUpload, ::clearFile, pendingUris,
            )
            SavedPropertyType.FACTORY -> FactoryPropertyFields(
                ::field, ::setField, type, uploadStates, ::startUpload, ::clearFile, pendingUris,
            )
        }

        if (!canSave && saveBlocker != null) {
            Text(
                saveBlocker,
                style = VvType.body(11, VvColors.Saffron),
            )
        }

        Button(
            onClick = { onSave(draft) },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (existing == null) "Save property" else "Update property")
        }
    }
}

@Composable
private fun DocumentUploadSection(
    type: SavedPropertyType,
    field: (String) -> String,
    uploadStates: Map<String, PropertyUploadState>,
    onPickAndUpload: (String, String) -> Unit,
    onRetry: (String) -> Unit,
    onClear: (String) -> Unit,
    pendingUris: Map<String, String>,
) {
    PropertySectionTitle("Property documents")
    Text(
        "Upload a Floor Plan and/or Site Plan if you have them — both are optional. We'll pre-fill details where possible.",
        style = VvType.body(11, VvColors.Ink3),
    )
    FileFields(type, field, uploadStates, onPickAndUpload, onRetry, onClear, pendingUris)
}

@Composable
private fun FileFields(
    type: SavedPropertyType,
    field: (String) -> String,
    uploadStates: Map<String, PropertyUploadState>,
    onPickAndUpload: (String, String) -> Unit,
    onRetry: (String) -> Unit,
    onClear: (String) -> Unit,
    pendingUris: Map<String, String>,
) {
    PropertyFileFields.specsFor(type).forEach { spec ->
        PropertyFileUploadField(
            spec = spec,
            currentValue = field(spec.key),
            uploadState = uploadStates[spec.key] ?: PropertyUploadState.Idle,
            onPickAndUpload = { uri -> onPickAndUpload(spec.key, uri) },
            onRetry = { onRetry(spec.key) },
            onClear = { onClear(spec.key) },
        )
    }
}

@Composable
private fun HomePropertyFields(
    field: (String) -> String,
    setField: (String, String) -> Unit,
    type: SavedPropertyType,
    uploadStates: Map<String, PropertyUploadState>,
    onPickAndUpload: (String, String) -> Unit,
    onClear: (String) -> Unit,
    pendingUris: Map<String, String>,
) {
    DocumentUploadSection(type, field, uploadStates, onPickAndUpload, { key ->
        pendingUris[key]?.let { onPickAndUpload(key, it) }
    }, onClear, pendingUris)
    PropertySectionTitle("Required details")
    PropertyTextField(field(PropertyFieldKeys.PROJECT_NAME), { setField(PropertyFieldKeys.PROJECT_NAME, it) }, "Project name *")
    PropertyTextField(field(PropertyFieldKeys.TOWER_NAME), { setField(PropertyFieldKeys.TOWER_NAME, it) }, "Tower name *")
    PropertyTextField(field(PropertyFieldKeys.FLAT_NUMBER), { setField(PropertyFieldKeys.FLAT_NUMBER, it) }, "Flat number *")
    PropertyChipGroup("Configuration *", BhkOptions, field(PropertyFieldKeys.CONFIGURATION)) {
        setField(PropertyFieldKeys.CONFIGURATION, it)
    }
    PropertyChipGroup("Tower facing *", CompassDirections, field(PropertyFieldKeys.TOWER_FACING)) {
        setField(PropertyFieldKeys.TOWER_FACING, it)
    }
    PropertyTextField(field(PropertyFieldKeys.MOVE_IN_TIMELINE), { setField(PropertyFieldKeys.MOVE_IN_TIMELINE, it) }, "Move-in timeline *")
    PropertyOptionalSection {
        PropertyTextField(field(PropertyFieldKeys.EXACT_ADDRESS), { setField(PropertyFieldKeys.EXACT_ADDRESS, it) }, "Exact address")
        PropertyTextField(field(PropertyFieldKeys.INTERIOR_LAYOUT_NOTES), { setField(PropertyFieldKeys.INTERIOR_LAYOUT_NOTES, it) }, "Interior layout notes", minLines = 2)
    }
}

@Composable
private fun OfficePropertyFields(
    field: (String) -> String,
    setField: (String, String) -> Unit,
    type: SavedPropertyType,
    uploadStates: Map<String, PropertyUploadState>,
    onPickAndUpload: (String, String) -> Unit,
    onClear: (String) -> Unit,
    pendingUris: Map<String, String>,
) {
    DocumentUploadSection(type, field, uploadStates, onPickAndUpload, { key ->
        pendingUris[key]?.let { onPickAndUpload(key, it) }
    }, onClear, pendingUris)
    PropertySectionTitle("Required details")
    PropertyTextField(field(PropertyFieldKeys.BUSINESS_NAME), { setField(PropertyFieldKeys.BUSINESS_NAME, it) }, "Business name *")
    PropertyTextField(field(PropertyFieldKeys.OFFICE_ADDRESS), { setField(PropertyFieldKeys.OFFICE_ADDRESS, it) }, "Office address *", minLines = 2)
    PropertyChipGroup("Type *", OfficeTypeOptions, field(PropertyFieldKeys.OFFICE_TYPE), columns = 3) {
        setField(PropertyFieldKeys.OFFICE_TYPE, it)
    }
    PropertyTextField(field(PropertyFieldKeys.TOTAL_AREA_SQ_FT), { setField(PropertyFieldKeys.TOTAL_AREA_SQ_FT, it) }, "Total area (sq ft) *")
    PropertyTextField(field(PropertyFieldKeys.KEY_ZONES), { setField(PropertyFieldKeys.KEY_ZONES, it) }, "Key zones *", minLines = 2)
    PropertyChipGroup("Facing direction *", CompassDirections, field(PropertyFieldKeys.FACING_DIRECTION)) {
        setField(PropertyFieldKeys.FACING_DIRECTION, it)
    }
    PropertyOptionalSection {
        PropertyTextField(field(PropertyFieldKeys.TEAM_SIZE), { setField(PropertyFieldKeys.TEAM_SIZE, it) }, "Team size")
        PropertyChipGroup("Business type", OfficeBusinessTypeOptions, field(PropertyFieldKeys.OFFICE_BUSINESS_TYPE), columns = 3) {
            setField(PropertyFieldKeys.OFFICE_BUSINESS_TYPE, it)
        }
    }
}

@Composable
private fun ShopPropertyFields(
    field: (String) -> String,
    setField: (String, String) -> Unit,
    type: SavedPropertyType,
    uploadStates: Map<String, PropertyUploadState>,
    onPickAndUpload: (String, String) -> Unit,
    onClear: (String) -> Unit,
    pendingUris: Map<String, String>,
) {
    DocumentUploadSection(type, field, uploadStates, onPickAndUpload, { key ->
        pendingUris[key]?.let { onPickAndUpload(key, it) }
    }, onClear, pendingUris)
    PropertySectionTitle("Required details")
    PropertyTextField(field(PropertyFieldKeys.SHOP_NAME), { setField(PropertyFieldKeys.SHOP_NAME, it) }, "Shop name *")
    PropertyTextField(field(PropertyFieldKeys.LOCATION), { setField(PropertyFieldKeys.LOCATION, it) }, "Location *", minLines = 2)
    PropertyChipGroup("Size category *", ShopSizeOptions, field(PropertyFieldKeys.SIZE_CATEGORY), columns = 3) {
        setField(PropertyFieldKeys.SIZE_CATEGORY, it)
    }
    PropertyChipGroup("Business type *", ShopBusinessTypeOptions, field(PropertyFieldKeys.BUSINESS_TYPE), columns = 2) {
        setField(PropertyFieldKeys.BUSINESS_TYPE, it)
    }
    PropertyChipGroup("Main entrance facing *", CompassDirections, field(PropertyFieldKeys.MAIN_ENTRANCE_FACING)) {
        setField(PropertyFieldKeys.MAIN_ENTRANCE_FACING, it)
    }
    PropertyTextField(field(PropertyFieldKeys.COUNTER_LOCATION), { setField(PropertyFieldKeys.COUNTER_LOCATION, it) }, "Counter location *")
    PropertyOptionalSection {
        PropertyTextField(field(PropertyFieldKeys.PRODUCT_CATEGORY), { setField(PropertyFieldKeys.PRODUCT_CATEGORY, it) }, "Product category")
        PropertyTextField(field(PropertyFieldKeys.FOOTFALL_PATTERN), { setField(PropertyFieldKeys.FOOTFALL_PATTERN, it) }, "Expected footfall pattern")
    }
}

@Composable
private fun FactoryPropertyFields(
    field: (String) -> String,
    setField: (String, String) -> Unit,
    type: SavedPropertyType,
    uploadStates: Map<String, PropertyUploadState>,
    onPickAndUpload: (String, String) -> Unit,
    onClear: (String) -> Unit,
    pendingUris: Map<String, String>,
) {
    DocumentUploadSection(type, field, uploadStates, onPickAndUpload, { key ->
        pendingUris[key]?.let { onPickAndUpload(key, it) }
    }, onClear, pendingUris)
    PropertySectionTitle("Required details")
    PropertyTextField(field(PropertyFieldKeys.FACILITY_NAME), { setField(PropertyFieldKeys.FACILITY_NAME, it) }, "Facility name *")
    PropertyTextField(field(PropertyFieldKeys.LOCATION), { setField(PropertyFieldKeys.LOCATION, it) }, "Location *", minLines = 2)
    PropertyChipGroup("Type *", FactoryTypeOptions, field(PropertyFieldKeys.FACILITY_TYPE), columns = 3) {
        setField(PropertyFieldKeys.FACILITY_TYPE, it)
    }
    PropertyTextField(field(PropertyFieldKeys.TOTAL_AREA_SQ_FT), { setField(PropertyFieldKeys.TOTAL_AREA_SQ_FT, it) }, "Total area (sq ft) *")
    PropertyTextField(field(PropertyFieldKeys.MACHINERY_ZONES), { setField(PropertyFieldKeys.MACHINERY_ZONES, it) }, "Main machinery zones *", minLines = 2)
    PropertyTextField(field(PropertyFieldKeys.DISPATCH_AREA), { setField(PropertyFieldKeys.DISPATCH_AREA, it) }, "Dispatch / loading area *")
    PropertyChipGroup("Site orientation *", CompassDirections, field(PropertyFieldKeys.SITE_ORIENTATION)) {
        setField(PropertyFieldKeys.SITE_ORIENTATION, it)
    }
    PropertyOptionalSection {
        PropertyTextField(field(PropertyFieldKeys.EMPLOYEE_COUNT), { setField(PropertyFieldKeys.EMPLOYEE_COUNT, it) }, "Employee count")
    }
}

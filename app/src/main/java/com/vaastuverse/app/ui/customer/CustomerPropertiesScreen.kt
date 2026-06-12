package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.SavedProperty
import com.vaastuverse.app.data.SavedPropertyType
import com.vaastuverse.app.data.displaySubtitle
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerPropertiesScreen(
    properties: List<SavedProperty>,
    isPropertyEditLocked: (SavedProperty) -> Boolean = { false },
    onAdd: () -> Unit,
    onEdit: (SavedProperty) -> Unit,
    onDelete: (SavedProperty) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<SavedProperty?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Save flats, offices, shops, and factories once — reuse them when you order reports.",
            style = VvType.body(12, VvColors.Ink2),
        )
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add property", modifier = Modifier.padding(start = 6.dp))
        }

        if (properties.isEmpty()) {
            Text("No properties yet.", style = VvType.body(12, VvColors.Ink3))
        } else {
            SavedPropertyType.entries.forEach { type ->
                val group = properties.filter { it.type == type }
                if (group.isNotEmpty()) {
                    Text(
                        type.label.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = VvColors.Ink3,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    group.forEach { property ->
                        PropertyRow(
                            property = property,
                            editLocked = isPropertyEditLocked(property),
                            onEdit = { onEdit(property) },
                            onDelete = { pendingDelete = property },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { property ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete property?") },
            text = { Text("\"${property.label}\" will be removed from your saved list.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(property)
                    pendingDelete = null
                }) {
                    Text("Delete", color = VvColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PropertyRow(
    property: SavedProperty,
    editLocked: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val rowAlpha = if (editLocked) 0.72f else 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
            .then(
                if (!editLocked) Modifier.clickable(onClick = onEdit) else Modifier,
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(property.type.icon, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(property.label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(
                property.displaySubtitle(),
                fontSize = 10.sp,
                color = VvColors.Ink3,
            )
            if (editLocked) {
                Text(
                    "Locked while a report is being generated",
                    fontSize = 9.sp,
                    color = VvColors.Ink3,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        IconButton(onClick = onEdit, enabled = !editLocked) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = if (editLocked) VvColors.Ink3 else VvColors.Jade,
            )
        }
        IconButton(onClick = onDelete, enabled = !editLocked) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = if (editLocked) VvColors.Ink3 else VvColors.Red,
            )
        }
    }
}

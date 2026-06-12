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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun CustomerPropertySelectionScreen(
    useCaseTitle: String,
    propertyType: SavedPropertyType,
    properties: List<SavedProperty>,
    onSelectProperty: (SavedProperty) -> Unit,
    onAddNew: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Select a saved ${propertyType.label.lowercase()} for your $useCaseTitle, or add a new one.",
            style = VvType.body(12, VvColors.Ink2),
        )
        properties.forEach { property ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
                    .clickable { onSelectProperty(property) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(property.type.icon, fontSize = 22.sp, modifier = Modifier.padding(end = 10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(property.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        property.displaySubtitle(),
                        fontSize = 10.sp,
                        color = VvColors.Ink3,
                    )
                }
                Text("Use →", color = VvColors.Jade, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Button(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add new ${propertyType.label.lowercase()}", modifier = Modifier.padding(start = 6.dp))
        }
    }
}

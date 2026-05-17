package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerReportsScreen() {
    SimplePlaceholderScreen(
        title = "Reports",
        subtitle = "Business Vaastu report flow — wire to report-service",
    )
}

@Composable
fun SimplePlaceholderScreen(title: String, subtitle: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = VvType.title(20))
            subtitle?.let {
                Text(
                    it,
                    style = VvType.body(13, VvColors.Ink3),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vaastuverse.app.ui.VvColors

object CustomerMiddleThemes {
    val homeGradient: Brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF8F0),
            Color(0xFFFFF0E0),
            VvColors.Cream,
        ),
    )

    /** Reports tab — soft jade paper on cream shell */
    val reportsBackground: Color = Color(0xFFF4FAF7)

    val defaultBackground: Color = VvColors.Cream
}

@Composable
fun CustomerMiddlePane(
    modifier: Modifier = Modifier,
    background: Brush? = null,
    backgroundColor: Color = CustomerMiddleThemes.defaultBackground,
    content: @Composable BoxScope.() -> Unit,
) {
    val backgroundModifier = if (background != null) {
        Modifier.background(background)
    } else {
        Modifier.background(backgroundColor)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier),
        content = content,
    )
}

@Composable
fun CustomerMiddleScrollSection(
    modifier: Modifier = Modifier,
    background: Brush? = null,
    backgroundColor: Color = CustomerMiddleThemes.defaultBackground,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val backgroundModifier = if (background != null) {
        Modifier.background(background)
    } else {
        Modifier.background(backgroundColor)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        content = content,
    )
}

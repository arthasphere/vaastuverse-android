package com.vaastuverse.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Design tokens from HTML mocks (`mockup_phase1_app.html` / `mockup_phase2_partner_app.html`). */
object VvColors {
    val Saffron = Color(0xFFE8720C)
    val SaffronLight = Color(0xFFFFF0E6)
    val Jade = Color(0xFF1A7A5E)
    val JadeLight = Color(0xFFE3F4EE)
    val Gold = Color(0xFFC9A030)
    val GoldLight = Color(0xFFFFF3DC)
    val Cream = Color(0xFFFDFAF5)
    val Ink = Color(0xFF1A150A)
    val Ink2 = Color(0xFF4A3F2F)
    val Ink3 = Color(0xFF7A6F5F)
    val Border = Color(0xFFD9CEBC)
    val Teal = Color(0xFF0A7B6A)
    val TealLight = Color(0xFFE0F4F1)
    val Purple = Color(0xFF5B3FA6)
    val PurpleLight = Color(0xFFEEEDFE)
    val Amber = Color(0xFFD4860A)
    val AmberLight = Color(0xFFFFF4E0)
    val PartnerBg = Color(0xFFF5F7FA)
    val DarkBg = Color(0xFF0A0A0C)
    val DarkElevated = Color(0xFF1A1A24)
    val DarkGold = Color(0xFFD4A830)
    val Red = Color(0xFF8B2020)
}

private val LightScheme = lightColorScheme(
    primary = VvColors.Saffron,
    onPrimary = Color.White,
    secondary = VvColors.Jade,
    onSecondary = Color.White,
    background = VvColors.Cream,
    onBackground = VvColors.Ink,
    surface = Color.White,
    onSurface = VvColors.Ink,
)

@Composable
fun VaastuVerseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        content = content,
    )
}

object VvType {
    fun title(size: Int) = TextStyle(
        fontSize = size.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Serif,
        color = VvColors.Ink,
    )

    fun body(size: Int, color: Color = VvColors.Ink) = TextStyle(
        fontSize = size.sp,
        fontWeight = FontWeight.Normal,
        color = color,
    )

    fun titleItalicSaffron(size: Int) = TextStyle(
        fontSize = size.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        color = VvColors.Saffron,
    )
}

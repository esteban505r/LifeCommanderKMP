package com.esteban.ruano.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ----- helpers to derive tints/shades from anchors -----
private fun lighten(color: Color, amount: Float): Color {
    val a = amount.coerceIn(0f, 1f)
    return Color(
        red = color.red + (1f - color.red) * a,
        green = color.green + (1f - color.green) * a,
        blue = color.blue + (1f - color.blue) * a,
        alpha = color.alpha
    )
}
private fun darken(color: Color, amount: Float): Color {
    val a = amount.coerceIn(0f, 1f)
    return Color(
        red = color.red * (1f - a),
        green = color.green * (1f - a),
        blue = color.blue * (1f - a),
        alpha = color.alpha
    )
}
private fun onColorFor(bg: Color): Color =
    if (bg.luminance() < 0.5f) Color.White else Color(0xFF1E293B) // slate-800 as dark text

// ----- legacy colors (unchanged) -----
val Blue = Color(0xFF617AFA)
val Blue2 = Color(0xFF31326F)
val SoftBlue2 = Color(0xFF527bca)
val SoftGreen2 = Color(0xFF4FB7B3)
val SoftPink = Color(0xFFFF6B81)
val LightGreen2 = Color(0xFFA8FBD3)
val BlueVariant = Color(0xFF3F51B5)
val Orange =  Color(0xFFFFAA00)
val CarbColor = Color(0xFFEEFF00)
val ProteinColor = Orange
val FatColor = Color(0xFFF44336)
val LightGray = Color(0xFF808080)
val LightGray2 = Color(0xFFFAFAFA)
val LightGray3 = Color(0xFFF0F2F5)
val LightGray4 = Color(0xFFE8EBEE)
val MediumGray = Color(0xFF404040)
val DarkGray = Color(0xFF202020)
val DarkGray2 = Color(0xFF121417)
val Gray = Color(0xFF637387)
val Gray3 = Color(0xFF474F58)
val Gray2 = Color(0xFFDBE0E5)
val BrightGreen = Color(0xFF00C713)
val SoftGreen = Color(0xFF00C853)
val SoftYellow = Color(0xFFF79C2C)
val SoftRed = Color(0xFFE83C5D)
val SoftBlue = Color(0xFF3F51B5)
val DarkGreen = Color(0xFF00790C)
val TextWhite = Color(0xFFEEEEEE)

// ----- anchors you asked to use -----
val PrimaryColor = SoftBlue2
val PrimaryVariantColor = Blue2
val SecondaryColor = SoftPink

// ----- modern cross-platform design system (now anchored) -----
object LifeCommanderColors {
    // Primary from anchors
    val Primary = PrimaryColor
    val PrimaryDark = PrimaryVariantColor
    val PrimaryLight = lighten(Primary, 0.25f)

    // Secondary from anchors
    val Secondary = SecondaryColor
    val SecondaryDark = darken(Secondary, 0.30f)
    val SecondaryLight = lighten(Secondary, 0.20f)

    // Accent (kept stable; optional: tune later)
    val Accent = Color(0xFFE91E63)
    val AccentOrange = Color(0xFFFF5722)
    val AccentPurple = Color(0xFF9C27B0)

    // Backgrounds / Surfaces
    val Background = Color(0xFFF8F9FA)
    val BackgroundSecondary = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)

    // Text & “on” colors derived for accessibility
    val OnPrimary = onColorFor(Primary)
    val OnPrimaryVariant = onColorFor(PrimaryDark)
    val OnSecondary = onColorFor(Secondary)

    val OnBackground = Color(0xFF1E293B)
    val OnSurface = Color(0xFF1E293B)
    val OnSurfaceVariant = Color(0xFF64748B)
    val OnSurfaceDisabled = Color(0xFF94A3B8)

    val DangerColor = Color(0xFFB00020)
    val WarningColor = Color(0xFFFFAB00)
    val InfoColor = Color(0xFF018786)
    val SuccessColor = Color(0xFF00C853)

    // Semantic (unchanged)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFDC2626)
    val Info = Color(0xFF3B82F6)

    // Borders / Dividers
    val Border = Color(0xFFE2E8F0)
    val BorderLight = Color(0xFFF1F5F9)
    val Divider = Color(0xFFE5E7EB)

    // Categories (kept; can re-map later if desired)
    val CategoryBlue = Color(0xFF3B82F6)
    val CategoryGreen = Color(0xFF10B981)
    val CategoryYellow = Color(0xFFF59E0B)
    val CategoryRed = Color(0xFFEF4444)
    val CategoryPurple = Color(0xFF8B5CF6)
    val CategoryCyan = Color(0xFF06B6D4)
    val CategoryPink = Color(0xFFEC4899)
    val CategoryIndigo = Color(0xFF6366F1)

    // Cards
    val CardBackground = Surface
    val CardBackgroundElevated = Surface
    val CardBorder = Border
    val CardShadow = Color(0x1A000000)

    // Buttons tied to anchors
    val ButtonPrimary = Primary
    val ButtonPrimaryHover = PrimaryDark
    val ButtonOnPrimary = OnPrimary

    val ButtonSecondary = Surface
    val ButtonSecondaryBorder = Border
    val ButtonOnSecondary = OnSurface

    val ButtonDisabled = Color(0xFFF1F5F9)
    val ButtonOnDisabled = Color(0xFF94A3B8)

    // Inputs
    val InputBackground = Surface
    val InputBorder = Border
    val InputBorderFocused = Primary
    val InputBorderError = Error
    val InputPlaceholder = OnSurfaceDisabled

    // Status chips
    val StatusCompletedBackground = Color(0xFFF0FDF4)
    val StatusCompletedForeground = Success
    val StatusPendingBackground = Color(0xFFFEF3C7)
    val StatusPendingForeground = Warning
    val StatusOverdueBackground = Color(0xFFFEF2F2)
    val StatusOverdueForeground = Error
}

// ----- gradients rebuilt from anchors -----
object LifeCommanderGradients {
    val PrimaryGradient = listOf(
        LifeCommanderColors.Primary,
        LifeCommanderColors.PrimaryDark
    )
    val SecondaryGradient = listOf(
        LifeCommanderColors.Secondary,
        LifeCommanderColors.SecondaryDark
    )

    val WelcomeGradient = listOf(
        lighten(LifeCommanderColors.Primary, 0.15f),
        LifeCommanderColors.PrimaryDark
    )

    val SuccessGradient = listOf(
        LifeCommanderColors.Success,
        darken(LifeCommanderColors.Success, 0.25f)
    )
    val WarningGradient = listOf(
        LifeCommanderColors.Warning,
        darken(LifeCommanderColors.Warning, 0.25f)
    )
    val ErrorGradient = listOf(
        LifeCommanderColors.Error,
        darken(LifeCommanderColors.Error, 0.25f)
    )
    val BackgroundGradient = listOf(
        LifeCommanderColors.Primary.copy(alpha = 0.05f),
        LifeCommanderColors.Background,
        Color.White
    )

    object HomeColors {
        // Keep home palette, but align anchors for coherence
        val PrimaryBlue = LifeCommanderColors.Primary
        val PrimaryDark = LifeCommanderColors.PrimaryDark
        val AccentPink = LifeCommanderColors.Accent
        val AccentOrange = LifeCommanderColors.AccentOrange
        val SoftGreen = LifeCommanderColors.Secondary
        val SoftPurple = LifeCommanderColors.AccentPurple

        val LightBackground = LifeCommanderColors.Background
        val CardBackground = LifeCommanderColors.CardBackground

        val TextPrimary = LifeCommanderColors.OnSurface
        val TextSecondary = LifeCommanderColors.OnSurfaceVariant
        val TextTertiary = LifeCommanderColors.OnSurfaceDisabled

        val StatusHigh = LifeCommanderColors.Error
        val StatusMedium = LifeCommanderColors.Warning
        val StatusLow = LifeCommanderColors.Info

        val SurfaceLight = LifeCommanderColors.SurfaceVariant
        val SurfaceBorder = LifeCommanderColors.Border

        val CategoryBlue = LifeCommanderColors.CategoryBlue
        val CategoryGreen = LifeCommanderColors.CategoryGreen
        val CategoryYellow = LifeCommanderColors.CategoryYellow
        val CategoryRed = LifeCommanderColors.CategoryRed
        val CategoryPurple = LifeCommanderColors.CategoryPurple
        val CategoryCyan = LifeCommanderColors.CategoryCyan
    }
}

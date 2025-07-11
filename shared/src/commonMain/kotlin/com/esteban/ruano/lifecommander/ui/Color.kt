package com.esteban.ruano.ui

import androidx.compose.ui.graphics.Color

// Legacy Colors (keeping for backward compatibility)
val Blue = Color(0xFF617AFA)
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

val PrimaryColor = Blue
val PrimaryVariantColor = BlueVariant
val SecondaryColor = Orange
val DangerColor = Color(0xFFB00020)
val WarningColor = Color(0xFFFFAB00)
val InfoColor = Color(0xFF018786)
val SuccessColor = Color(0xFF00C853)

// Modern Cross-Platform Design System
object LifeCommanderColors {
    // Primary Colors
    val Primary = Color(0xFF2196F3)
    val PrimaryDark = Color(0xFF1976D2)
    val PrimaryLight = Color(0xFF64B5F6)
    
    // Secondary Colors
    val Secondary = Color(0xFF4CAF50)
    val SecondaryDark = Color(0xFF388E3C)
    val SecondaryLight = Color(0xFF81C784)
    
    // Accent Colors
    val Accent = Color(0xFFE91E63)
    val AccentOrange = Color(0xFFFF5722)
    val AccentPurple = Color(0xFF9C27B0)
    
    // Background Colors
    val Background = Color(0xFFF8F9FA)
    val BackgroundSecondary = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)
    
    // Text Colors
    val OnBackground = Color(0xFF1E293B)
    val OnSurface = Color(0xFF1E293B)
    val OnSurfaceVariant = Color(0xFF64748B)
    val OnSurfaceDisabled = Color(0xFF94A3B8)
    
    // Semantic Colors
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFDC2626)
    val Info = Color(0xFF3B82F6)
    
    // Border & Divider Colors
    val Border = Color(0xFFE2E8F0)
    val BorderLight = Color(0xFFF1F5F9)
    val Divider = Color(0xFFE5E7EB)
    
    // Category Colors (for visual differentiation)
    val CategoryBlue = Color(0xFF3B82F6)
    val CategoryGreen = Color(0xFF10B981)
    val CategoryYellow = Color(0xFFF59E0B)
    val CategoryRed = Color(0xFFEF4444)
    val CategoryPurple = Color(0xFF8B5CF6)
    val CategoryCyan = Color(0xFF06B6D4)
    val CategoryPink = Color(0xFFEC4899)
    val CategoryIndigo = Color(0xFF6366F1)
    
    // Card Colors
    val CardBackground = Color(0xFFFFFFFF)
    val CardBackgroundElevated = Color(0xFFFFFFFF)
    val CardBorder = Color(0xFFE2E8F0)
    val CardShadow = Color(0x1A000000)
    
    // Button Colors
    val ButtonPrimary = Primary
    val ButtonPrimaryHover = PrimaryDark
    val ButtonSecondary = Surface
    val ButtonSecondaryBorder = Border
    val ButtonDisabled = Color(0xFFF1F5F9)
    val ButtonOnDisabled = Color(0xFF94A3B8)
    
    // Input Colors
    val InputBackground = Surface
    val InputBorder = Border
    val InputBorderFocused = Primary
    val InputBorderError = Error
    val InputPlaceholder = OnSurfaceDisabled
    
    // Status Colors
    val StatusCompletedBackground = Color(0xFFF0FDF4)
    val StatusCompletedForeground = Success
    val StatusPendingBackground = Color(0xFFFEF3C7)
    val StatusPendingForeground = Warning
    val StatusOverdueBackground = Color(0xFFFEF2F2)
    val StatusOverdueForeground = Error
}

// Gradient Definitions for Cross-Platform Use
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
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )
    
    val SuccessGradient = listOf(
        LifeCommanderColors.Success,
        Color(0xFF059669)
    )
    
    val WarningGradient = listOf(
        LifeCommanderColors.Warning,
        Color(0xFFD97706)
    )
    
    val ErrorGradient = listOf(
        LifeCommanderColors.Error,
        Color(0xFFB91C1C)
    )
    
    val BackgroundGradient = listOf(
        LifeCommanderColors.Primary.copy(alpha = 0.05f),
        LifeCommanderColors.Background,
        Color.White
    )
}


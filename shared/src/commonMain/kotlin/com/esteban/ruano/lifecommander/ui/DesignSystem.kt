package com.esteban.ruano.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Life Commander Cross-Platform Design System
 * 
 * This design system provides consistent styling across all platforms
 * (Android, iOS, Web, Desktop) using Compose Multiplatform.
 */
object LifeCommanderDesignSystem {
    
    // Main Design System Objects
    val colors = LifeCommanderColors
    val shapes = LifeCommanderShapes
    val dimensions = LifeCommanderDimensions
    val gradients = LifeCommanderGradients
    
    // Utility Functions
    @OptIn(ExperimentalTime::class)
    @Composable
    fun getGreeting(): String {
        val hour = remember {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        }
        return when (hour) {
            in 5..11 -> "Good Morning! 👋"
            in 12..16 -> "Good Afternoon! ☀️"
            in 17..21 -> "Good Evening! 🌅"
            else -> "Good Night! 🌙"
        }
    }
    
    @Composable
    fun getCategoryColor(categoryId: String): Color {
        return when (categoryId.hashCode() % 8) {
            0 -> colors.CategoryBlue
            1 -> colors.CategoryGreen
            2 -> colors.CategoryYellow
            3 -> colors.CategoryRed
            4 -> colors.CategoryPurple
            5 -> colors.CategoryCyan
            6 -> colors.CategoryPink
            else -> colors.CategoryIndigo
        }
    }
    
    fun createGradientBrush(
        colors: List<Color>,
        direction: GradientDirection = GradientDirection.Horizontal
    ): Brush {
        return when (direction) {
            GradientDirection.Horizontal -> Brush.horizontalGradient(colors)
            GradientDirection.Vertical -> Brush.verticalGradient(colors)
            GradientDirection.Diagonal -> Brush.linearGradient(colors)
            GradientDirection.Radial -> Brush.radialGradient(colors)
        }
    }
    
    enum class GradientDirection {
        Horizontal, Vertical, Diagonal, Radial
    }
    
    // Semantic Color Helpers
    object SemanticColors {
        val success = colors.Success
        val warning = colors.Warning
        val error = colors.Error
        val info = colors.Info
        
        val successBackground = colors.StatusCompletedBackground
        val warningBackground = colors.StatusPendingBackground
        val errorBackground = colors.StatusOverdueBackground
        
        val successGradient = gradients.SuccessGradient
        val warningGradient = gradients.WarningGradient
        val errorGradient = gradients.ErrorGradient
    }
    
    // Component Presets (simplified)
    object ComponentPresets {
        // Welcome Card Preset
        val WelcomeCardBackgroundColor = Color.Transparent
        val WelcomeCardGradient = gradients.WelcomeGradient
        val WelcomeCardShape = shapes.CardWelcome
        val WelcomeCardElevation = dimensions.ElevationExtraExtraLarge
        val WelcomeCardPadding = dimensions.PaddingContent
        
        // Section Card Preset
        val SectionCardBackgroundColor = colors.CardBackground
        val SectionCardShape = shapes.CardElevated
        val SectionCardElevation = dimensions.ElevationLarge
        val SectionCardPadding = dimensions.PaddingContent
        
        // Task Card Preset
        val TaskCardBackgroundColor = colors.CardBackground
        val TaskCardShape = shapes.CardDefault
        val TaskCardElevation = dimensions.ElevationMedium
        val TaskCardPadding = dimensions.PaddingLarge
        
        // Primary Button Preset
        val PrimaryButtonBackgroundColor = colors.ButtonPrimary
        val PrimaryButtonShape = shapes.ButtonDefault
        val PrimaryButtonHeight = dimensions.HeightButton
        val PrimaryButtonGradient = gradients.PrimaryGradient
        
        // Secondary Button Preset
        val SecondaryButtonBackgroundColor = colors.ButtonSecondary
        val SecondaryButtonBorderColor = colors.ButtonSecondaryBorder
        val SecondaryButtonShape = shapes.ButtonDefault
        val SecondaryButtonHeight = dimensions.HeightButton
    }
    
    // Layout Helpers
    object Layout {
        val screenPadding = dimensions.PaddingScreen
        val contentPadding = dimensions.PaddingContent
        val sectionSpacing = dimensions.SpacingExtraLarge
        val itemSpacing = dimensions.SpacingMedium
        val smallSpacing = dimensions.SpacingSmall
    }
    
    // Animation Durations (in milliseconds)
    object Animation {
        const val Fast = 200
        const val Normal = 300
        const val Slow = 500
        const val ExtraSlow = 800
    }
    
    // Typography Helpers (can be extended with custom typography)
    object Typography {
        // These would map to MaterialTheme.typography or custom typography
        // Keeping as constants for now since typography might be platform-specific
        const val HeadlineLarge = "HeadlineLarge"
        const val HeadlineMedium = "HeadlineMedium"
        const val HeadlineSmall = "HeadlineSmall"
        const val TitleLarge = "TitleLarge"
        const val TitleMedium = "TitleMedium"
        const val TitleSmall = "TitleSmall"
        const val BodyLarge = "BodyLarge"
        const val BodyMedium = "BodyMedium"
        const val BodySmall = "BodySmall"
        const val LabelLarge = "LabelLarge"
        const val LabelMedium = "LabelMedium"
        const val LabelSmall = "LabelSmall"
    }
    
    // Platform-specific helpers
    object Platform {
        // These can be implemented with expect/actual pattern if needed
        val isAndroid: Boolean = false // Would be implemented per platform
        val isIOS: Boolean = false
        val isDesktop: Boolean = false
        val isWeb: Boolean = false
    }
}

// Convenience aliases for easier access
typealias LCColors = LifeCommanderColors
typealias LCShapes = LifeCommanderShapes
typealias LCDimensions = LifeCommanderDimensions
typealias LCGradients = LifeCommanderGradients
typealias LCDS = LifeCommanderDesignSystem 
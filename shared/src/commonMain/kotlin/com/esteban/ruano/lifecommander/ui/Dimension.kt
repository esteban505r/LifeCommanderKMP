package com.esteban.ruano.ui

import androidx.compose.ui.unit.dp

// Legacy Dimensions (keeping for backward compatibility)
val timePickerDimensionWith = 600
val timePickerDimensionHeight = 400

val datePickerDimensionWith = 700
val datePickerDimensionHeight = 700

// Modern Cross-Platform Dimension System
object LifeCommanderDimensions {
    // Spacing Scale
    val SpacingExtraSmall = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 12.dp
    val SpacingLarge = 16.dp
    val SpacingExtraLarge = 20.dp
    val SpacingExtraExtraLarge = 24.dp
    val SpacingExtraExtraExtraLarge = 32.dp
    val SpacingHuge = 40.dp
    val SpacingExtraHuge = 48.dp
    val SpacingMassive = 64.dp
    
    // Padding & Margin
    val PaddingExtraSmall = SpacingExtraSmall
    val PaddingSmall = SpacingSmall
    val PaddingMedium = SpacingMedium
    val PaddingLarge = SpacingLarge
    val PaddingExtraLarge = SpacingExtraLarge
    val PaddingExtraExtraLarge = SpacingExtraExtraLarge
    val PaddingContent = SpacingExtraLarge // Standard content padding
    val PaddingScreen = SpacingExtraLarge // Standard screen padding
    
    // Component Heights
    val HeightButton = 48.dp
    val HeightButtonSmall = 36.dp
    val HeightButtonLarge = 56.dp
    val HeightInput = 48.dp
    val HeightInputSmall = 36.dp
    val HeightInputLarge = 56.dp
    val HeightListItem = 64.dp
    val HeightListItemSmall = 48.dp
    val HeightListItemLarge = 72.dp
    val HeightAppBar = 56.dp
    val HeightTabBar = 48.dp
    val HeightBottomNavigation = 80.dp
    
    // Icon Sizes
    val IconExtraSmall = 12.dp
    val IconSmall = 16.dp
    val IconMedium = 20.dp
    val IconLarge = 24.dp
    val IconExtraLarge = 32.dp
    val IconExtraExtraLarge = 40.dp
    val IconHuge = 48.dp
    val IconAvatar = 56.dp
    val IconAvatarLarge = 72.dp
    
    // Elevation & Shadow
    val ElevationNone = 0.dp
    val ElevationSmall = 2.dp
    val ElevationMedium = 4.dp
    val ElevationLarge = 8.dp
    val ElevationExtraLarge = 12.dp
    val ElevationExtraExtraLarge = 16.dp
    val ElevationMaximum = 24.dp
    
    // Border Width
    val BorderThin = 1.dp
    val BorderMedium = 2.dp
    val BorderThick = 3.dp
    val BorderExtraThick = 4.dp
    
    // Maximum Widths for Responsive Design
    val MaxWidthMobile = 480.dp
    val MaxWidthTablet = 768.dp
    val MaxWidthDesktop = 1024.dp
    val MaxWidthContent = 1200.dp
    val MaxWidthCard = 400.dp
    val MaxWidthDialog = 560.dp
    
    // Minimum Touch Targets (for accessibility)
    val TouchMinimum = 44.dp
    val TouchRecommended = 48.dp
    val TouchLarge = 56.dp
    
    // Card Dimensions
    val CardMinHeight = 120.dp
    val CardHeroMinHeight = 200.dp
    val CardWelcomeMinHeight = 180.dp
    
    // Avatar Dimensions
    val AvatarSmall = 32.dp
    val AvatarMedium = 40.dp
    val AvatarLarge = 48.dp
    val AvatarExtraLarge = 64.dp
    val AvatarHuge = 80.dp
    val AvatarProfile = 120.dp
    
    // Logo Dimensions
    val LogoSmall = 24.dp
    val LogoMedium = 32.dp
    val LogoLarge = 48.dp
    val LogoExtraLarge = 64.dp
    val LogoHero = 120.dp
}
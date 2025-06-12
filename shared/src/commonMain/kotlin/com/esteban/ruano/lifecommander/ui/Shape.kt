package com.esteban.ruano.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

// Modern Cross-Platform Shape System
object LifeCommanderShapes {
    // Corner Radius Tokens
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(20.dp)
    val ExtraExtraLarge = RoundedCornerShape(24.dp)
    val Rounded = RoundedCornerShape(28.dp)
    
    // Card Shapes
    val CardDefault = Medium
    val CardElevated = Large
    val CardHero = ExtraExtraLarge
    val CardWelcome = Rounded
    
    // Button Shapes
    val ButtonDefault = ExtraExtraLarge
    val ButtonSmall = Medium
    val ButtonLarge = Rounded
    val ButtonPill = RoundedCornerShape(50)
    
    // Input Shapes
    val InputDefault = Medium
    val InputSmall = Small
    val InputLarge = Large
    
    // Dialog Shapes
    val DialogDefault = ExtraLarge
    val DialogBottomSheet = RoundedCornerShape(
        topStart = ExtraLarge.topStart,
        topEnd = ExtraLarge.topEnd,
        bottomStart = RoundedCornerShape(0.dp).bottomStart,
        bottomEnd = RoundedCornerShape(0.dp).bottomEnd
    )
    
    // Tag Shapes
    val TagDefault = Small
    val TagPill = RoundedCornerShape(50)
    
    // Navigation Shapes
    val NavigationBottomBar = RoundedCornerShape(
        topStart = ExtraLarge.topStart,
        topEnd = ExtraLarge.topEnd,
        bottomStart = RoundedCornerShape(0.dp).bottomStart,
        bottomEnd = RoundedCornerShape(0.dp).bottomEnd
    )
    val NavigationDrawer = RoundedCornerShape(
        topEnd = ExtraLarge.topEnd,
        bottomEnd = ExtraLarge.bottomEnd,
        bottomStart = RoundedCornerShape(0.dp).bottomStart,
        topStart = RoundedCornerShape(0.dp).topStart
    )
    
    // Version Badge Shape
    val VersionBadge = Medium
}
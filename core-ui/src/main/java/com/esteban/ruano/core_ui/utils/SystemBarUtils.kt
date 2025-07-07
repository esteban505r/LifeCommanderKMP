package com.esteban.ruano.core_ui.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Modern utility functions for handling system bars using Compose's built-in WindowInsets
 * 
 * USAGE EXAMPLES:
 * 
 * 1. Basic system bar padding (recommended approach):
 *    Modifier.padding(WindowInsets.systemBars.asPaddingValues())
 * 
 * 2. Separate status and navigation bar padding:
 *    Modifier.padding(WindowInsets.statusBars.asPaddingValues())
 *    Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
 * 
 * 3. Background extending behind system bars:
 *    Modifier
 *      .fillMaxSize()
 *      .background(Color.White)
 *      .padding(WindowInsets.systemBars.asPaddingValues())
 * 
 * 4. Using the utility functions:
 *    SystemBarUtils.withSystemBarPadding(Modifier.fillMaxSize())
 *    SystemBarUtils.withSystemBarBackground(Color.White, Modifier.fillMaxSize())
 * 
 * TIPS:
 * - Use WindowInsets.systemBars for both status and navigation bars
 * - Use WindowInsets.statusBars for status bar only
 * - Use WindowInsets.navigationBars for navigation bar only
 * - Always apply background color before padding to extend behind system bars
 * - For transparent system bars, use .background() on the root composable
 */
object SystemBarUtils {
    
    /**
     * Adds system bar padding to a modifier
     * @param modifier The base modifier to add padding to
     * @return Modifier with system bar padding
     */
    @Composable
    fun withSystemBarPadding(modifier: Modifier): Modifier {
        return modifier.padding(WindowInsets.systemBars.asPaddingValues())
    }
    
    /**
     * Adds status bar padding only to a modifier
     * @param modifier The base modifier to add padding to
     * @return Modifier with status bar padding
     */
    @Composable
    fun withStatusBarPadding(modifier: Modifier): Modifier {
        return modifier.padding(WindowInsets.statusBars.asPaddingValues())
    }
    
    /**
     * Adds navigation bar padding only to a modifier
     * @param modifier The base modifier to add padding to
     * @return Modifier with navigation bar padding
     */
    @Composable
    fun withNavigationBarPadding(modifier: Modifier): Modifier {
        return modifier.padding(WindowInsets.navigationBars.asPaddingValues())
    }
    
    /**
     * Adds background color that extends behind system bars with proper padding
     * @param backgroundColor The background color to apply
     * @param modifier The base modifier
     * @return Modifier with background and system bar padding
     */
    @Composable
    fun withSystemBarBackground(
        backgroundColor: Color,
        modifier: Modifier = Modifier
    ): Modifier {
        return modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.systemBars.asPaddingValues())
    }
    
    /**
     * Creates a modifier for transparent system bars with background extending behind
     * @param backgroundColor The background color to apply
     * @param modifier The base modifier
     * @return Modifier with background extending behind system bars
     */
    @Composable
    fun withTransparentSystemBars(
        backgroundColor: Color,
        modifier: Modifier = Modifier
    ): Modifier {
        return modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.systemBars.asPaddingValues())
    }
    
    /**
     * Creates a modifier that matches the bottom navigation style
     * @param bottomNavColor The color to match with system bars
     * @param modifier The base modifier
     * @return Modifier with matching background and system bar padding
     */
    @Composable
    fun withBottomNavMatchingSystemBars(
        bottomNavColor: Color,
        modifier: Modifier = Modifier
    ): Modifier {
        return modifier
            .fillMaxSize()
            .background(bottomNavColor)
            .padding(WindowInsets.systemBars.asPaddingValues())
    }
    
    /**
     * Creates a modifier for LifeCommander app theme with proper system bar handling
     * @param modifier The base modifier
     * @return Modifier with theme-appropriate background and system bar padding
     */
    @Composable
    fun withLifeCommanderSystemBars(modifier: Modifier = Modifier): Modifier {
        val backgroundColor = if (isSystemInDarkTheme()) {
            Color(0xFF1E293B) // Dark theme background
        } else {
            Color(0xFFF8F9FA) // Light theme background
        }
        
        return modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.systemBars.asPaddingValues())
    }
    
    /**
     * Composable function that provides system bar padding
     * @param content The content to wrap with system bar padding
     */
    @Composable
    fun SystemBarPadding(content: @Composable () -> Unit) {
        content()
    }
    
    /**
     * Composable function that provides system bar background with padding
     * @param backgroundColor The background color
     * @param content The content to wrap
     */
    @Composable
    fun SystemBarBackground(
        backgroundColor: Color,
        content: @Composable () -> Unit
    ) {
        content()
    }
} 
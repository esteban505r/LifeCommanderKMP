package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.ui.platform.LocalConfiguration

actual object PlatformConfiguration {
    actual val isLandscape: Boolean
        get() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
} 
package com.esteban.ruano.core_ui.composables.button

import androidx.compose.ui.graphics.Color
import com.esteban.ruano.core_ui.theme.DangerColor
import com.esteban.ruano.core_ui.theme.InfoColor
import com.esteban.ruano.core_ui.theme.PrimaryColor
import com.esteban.ruano.core_ui.theme.PrimaryVariantColor
import com.esteban.ruano.core_ui.theme.SuccessColor
import com.esteban.ruano.core_ui.theme.WarningColor

enum class ButtonType {
    PRIMARY,
    SECONDARY,
    DANGER,
    WARNING,
    INFO,
    SUCCESS
}

fun ButtonType.toColor(): Color {
    return when (this) {
        ButtonType.PRIMARY -> PrimaryColor
        ButtonType.SECONDARY -> PrimaryVariantColor
        ButtonType.DANGER -> DangerColor
        ButtonType.WARNING -> WarningColor
        ButtonType.INFO -> InfoColor
        ButtonType.SUCCESS ->  SuccessColor
    }
}
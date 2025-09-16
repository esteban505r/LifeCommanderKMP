package com.esteban.ruano.lifecommander.ui.components.button

import androidx.compose.ui.graphics.Color
import com.esteban.ruano.ui.DangerColor
import com.esteban.ruano.ui.InfoColor
import com.esteban.ruano.ui.PrimaryColor
import com.esteban.ruano.ui.PrimaryVariantColor
import com.esteban.ruano.ui.SuccessColor
import com.esteban.ruano.ui.WarningColor

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
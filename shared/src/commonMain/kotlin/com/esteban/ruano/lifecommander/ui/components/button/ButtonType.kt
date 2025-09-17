package com.esteban.ruano.lifecommander.ui.components.button

import androidx.compose.ui.graphics.Color
import com.esteban.ruano.ui.LifeCommanderColors.DangerColor
import com.esteban.ruano.ui.LifeCommanderColors.InfoColor
import com.esteban.ruano.ui.LifeCommanderColors.SuccessColor
import com.esteban.ruano.ui.LifeCommanderColors.WarningColor
import com.esteban.ruano.ui.PrimaryColor
import com.esteban.ruano.ui.PrimaryVariantColor

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
package com.esteban.ruano.core_ui.utils

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.Color
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.theme.FatColor

data class CustomSnackBarVisuals(
    override val message: String,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val containerColor: Color = Color.White,
    val contentColor: Color = Color.Red,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals{
    companion object {
        fun fromType(snackbarType: SnackbarType, message: String): CustomSnackBarVisuals {
            return when (snackbarType) {
                SnackbarType.ERROR -> CustomSnackBarVisuals(
                    message = message,
                    containerColor = FatColor,
                    contentColor = Color.White
                )
                SnackbarType.SUCCESS -> CustomSnackBarVisuals(
                    message = message,
                    containerColor = Color.Green,
                    contentColor = Color.White
                )
                SnackbarType.WARNING -> CustomSnackBarVisuals(
                    message = message,
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                )

                SnackbarType.INFO -> CustomSnackBarVisuals(
                    message = message,
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            }
        }
    }
}

data class CustomSnackbarVisualsWithUiText(
    val message: UiText,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val containerColor: Color = Color.White,
    val contentColor: Color = Color.Red,
    val actionLabel: String? = null,
    val withDismissAction: Boolean = false
){
    companion object {
        fun fromType(snackbarType: SnackbarType, message: UiText): CustomSnackbarVisualsWithUiText {
            return when (snackbarType) {
                SnackbarType.ERROR -> CustomSnackbarVisualsWithUiText(
                    message = message,
                    containerColor = FatColor,
                    contentColor = Color.White
                )
                SnackbarType.SUCCESS -> CustomSnackbarVisualsWithUiText(
                    message = message,
                    containerColor = Color.Green,
                    contentColor = Color.White
                )
                SnackbarType.WARNING -> CustomSnackbarVisualsWithUiText(
                    message = message,
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                )

                SnackbarType.INFO -> CustomSnackbarVisualsWithUiText(
                    message = message,
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            }
        }
    }

    fun toCustomSnackBarVisuals(context: Context): CustomSnackBarVisuals {
        return CustomSnackBarVisuals(
            message = message.asString(context),
            duration = duration,
            containerColor = containerColor,
            contentColor = contentColor,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction
        )
    }
}
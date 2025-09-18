package com.esteban.ruano.lifecommander.ui.components.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BaseButton(onClick: () -> Unit, text: String,modifier: Modifier = Modifier,buttonType: ButtonType = ButtonType.PRIMARY,enabled: Boolean = true,) {
    Button(
        enabled = enabled,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonType.toColor()
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
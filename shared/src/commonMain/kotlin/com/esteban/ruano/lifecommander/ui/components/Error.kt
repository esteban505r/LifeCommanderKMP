package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.button.BaseButton

@Composable
fun Error(message: String = "Error, try again", onRetry: (() -> Unit)? = null) {
    Column {
        Text(text = message)
        Spacer(modifier = Modifier.height(16.dp))
        if(onRetry != null) {
            BaseButton(text = "Retry", onClick = {
                onRetry()
            })
        }
    }
}
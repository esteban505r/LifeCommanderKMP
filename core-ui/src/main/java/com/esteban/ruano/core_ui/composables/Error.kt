package com.esteban.ruano.core_ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.button.BaseButton

@Composable
fun Error(message: String = stringResource(R.string.error_unknown), onRetry: (() -> Unit)? = null) {
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
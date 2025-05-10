package com.esteban.ruano.core_ui.composables.text

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable

fun TitleH4(text: String,modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.h4,
        modifier = modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun TitleH4(textResource: Int,modifier: Modifier = Modifier) {
    Text(
        stringResource(id = textResource),
        style = MaterialTheme.typography.h4,
        modifier = modifier.padding(vertical = 16.dp)
    )
}
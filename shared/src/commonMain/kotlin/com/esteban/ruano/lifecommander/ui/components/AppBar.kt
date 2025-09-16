package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun AppBar(
    title: String,
    onClose: (() -> Unit)? = null,
    modifier: Modifier? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        horizontalArrangement = if (onClose == null && actions == null) Arrangement.Center else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            ?: Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        if (onClose != null) {
            IconButton(onClick = {
                onClose()
            }) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Close")
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.h3,
            textAlign = TextAlign.Center,
        )
        if (actions != null) {
            Row {
                actions()
            }
        }
    }
}


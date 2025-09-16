package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ListTile(
    modifier: Modifier = Modifier.padding(bottom = 16.dp),
    title: String,
    subtitle: String? = null,
    prefix: (@Composable RowScope.() -> Unit)? = null,
    suffix: (@Composable RowScope.() -> Unit)? = null,
    contentWeight: Float = 1f,
    onClick: (() -> Unit)? = null
) {
    val modifierT = modifier
        .fillMaxWidth()
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = if(onClick != null) modifier.clickable { onClick() } else modifierT
    ) outerRow@{
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(contentWeight).padding(vertical = 8.dp)
        ) {
            prefix?.invoke(this@Row)
            Column(
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
        suffix?.invoke(this@outerRow)
    }
}
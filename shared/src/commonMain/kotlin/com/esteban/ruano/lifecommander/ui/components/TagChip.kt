package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.utils.UiUtils.parseHexColor
import com.lifecommander.models.Tag

@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val tagColor = tag.color?.let { parseHexColor(it) } ?: MaterialTheme.colors.primary

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            tagColor.copy(alpha = 0.2f)
        } else {
            tagColor.copy(alpha = 0.1f)
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = tagColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(tagColor, RoundedCornerShape(4.dp))
            )
            Text(
                text = tag.name,
                style = MaterialTheme.typography.caption,
                color = if (selected) tagColor else MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}


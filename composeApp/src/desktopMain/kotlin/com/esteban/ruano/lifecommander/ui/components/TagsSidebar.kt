package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.utils.UiUtils.parseHexColor
import com.lifecommander.models.Tag

@Composable
fun TagsSidebar(
    tags: List<Tag>,
    selectedTagSlug: String?,
    onTagClick: (String?) -> Unit,
    onCreateTag: () -> Unit,
    onTagLongClick: (Tag) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onCreateTag,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Tag",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // All Tasks option
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTagClick(null) },
            shape = RoundedCornerShape(8.dp),
            color = if (selectedTagSlug == null) {
                MaterialTheme.colors.primary.copy(alpha = 0.1f)
            } else {
                Color.Transparent
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (selectedTagSlug == null) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "All Tasks",
                    style = MaterialTheme.typography.body2,
                    fontWeight = if (selectedTagSlug == null) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTagSlug == null) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Divider()

        Spacer(modifier = Modifier.height(8.dp))

        // Tags list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(tags) { tag ->
                TagSidebarItem(
                    tag = tag,
                    selected = selectedTagSlug == tag.slug,
                    onClick = { onTagClick(tag.slug) },
                    onLongClick = { onTagLongClick(tag) }
                )
            }
        }
    }
}

@Composable
private fun TagSidebarItem(
    tag: Tag,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val tagColor = tag.color?.let { parseHexColor(it) } ?: MaterialTheme.colors.primary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) {
            tagColor.copy(alpha = 0.15f)
        } else {
            Color.Transparent
        },
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(1.dp, tagColor)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(tagColor, RoundedCornerShape(6.dp))
            )
            Text(
                text = tag.name,
                style = MaterialTheme.typography.body2,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) tagColor else MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}


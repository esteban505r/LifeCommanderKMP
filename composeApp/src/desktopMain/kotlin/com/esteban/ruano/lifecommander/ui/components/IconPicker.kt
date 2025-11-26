package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import coil3.Image
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun IconPicker(
    selectedIconUrl: String?,
    onIconSelected: (String?) -> Unit,
    onIconUploaded: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var iconUrlInput by remember { mutableStateOf("") }
    var showUrlInput by remember { mutableStateOf(false) }
    val context = LocalPlatformContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Icon",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )

        // Icon Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                color = MaterialTheme.colors.surface
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedIconUrl != null && selectedIconUrl.isNotBlank()) {
                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(
                                context
                            )
                                .data(selectedIconUrl)
                                .crossfade(true)
                                .build()
                        )

                        Image(
                            painter = painter,
                            contentDescription = "Selected icon",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No icon selected",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedIconUrl?.takeIf { it.isNotBlank() } ?: "No icon selected",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showUrlInput = !showUrlInput },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showUrlInput) "Cancel" else if (selectedIconUrl == null) "Add Icon URL" else "Change Icon")
            }

            if (selectedIconUrl != null && selectedIconUrl.isNotBlank()) {
                OutlinedButton(
                    onClick = { onIconSelected(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove")
                }
            }
        }

        // URL Input
        if (showUrlInput) {
            OutlinedTextField(
                value = iconUrlInput,
                onValueChange = { iconUrlInput = it },
                label = { Text("Icon URL") },
                placeholder = { Text("https://example.com/icon.png") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (iconUrlInput.isNotBlank()) {
                                onIconSelected(iconUrlInput.trim())
                                iconUrlInput = ""
                                showUrlInput = false
                            }
                        },
                        enabled = iconUrlInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Set URL")
                    }
                }
            )
        }
    }
}

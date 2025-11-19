package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.esteban.ruano.lifecommander.ui.components.TagChip
import com.esteban.ruano.lifecommander.ui.components.ColorPicker
import com.lifecommander.models.Tag
import org.koin.compose.viewmodel.koinViewModel
import ui.viewmodels.TagsViewModel
import java.awt.Dimension

@Composable
fun TagsManagementScreen(
    show: Boolean,
    onDismiss: () -> Unit,
    tagsViewModel: TagsViewModel = koinViewModel()
) {
    val tags by tagsViewModel.tags.collectAsState()
    val loading by tagsViewModel.loading.collectAsState()
    val error by tagsViewModel.error.collectAsState()
    
    var showCreateTagDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<Tag?>(null) }
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            tagsViewModel.loadTags()
        }
    }

    if (show) {
        DialogWindow(
            onCloseRequest = onDismiss,
            state = rememberDialogState(position = WindowPosition(Alignment.Center))
        ) {
            LaunchedEffect(Unit) {
                window.size = Dimension(600, 700)
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Transparent),
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Manage Tags",
                                style = MaterialTheme.typography.h5,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Create Tag Button
                        Button(
                            onClick = { 
                                tagToEdit = null
                                showCreateTagDialog = true 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Tag")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Your Tags",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (loading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (error != null) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error: $error",
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.error
                                )
                            }
                        } else if (tags.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tags yet. Create your first tag!",
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(tags) { tag ->
                                    TagManagementItem(
                                        tag = tag,
                                        onEdit = {
                                            tagToEdit = tag
                                            showCreateTagDialog = true
                                        },
                                        onDelete = {
                                            tagToDelete = tag
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create/Edit Tag Dialog
    if (showCreateTagDialog) {
        CreateEditTagDialog(
            tag = tagToEdit,
            onDismiss = {
                showCreateTagDialog = false
                tagToEdit = null
            },
            onSave = { name, color ->
                if (tagToEdit != null) {
                    tagsViewModel.updateTag(
                        tagId = tagToEdit!!.id,
                        name = name,
                        color = color
                    )
                } else {
                    tagsViewModel.createTag(name, color)
                }
                showCreateTagDialog = false
                tagToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && tagToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                tagToDelete = null
            },
            title = {
                Text(
                    "Delete Tag",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${tagToDelete!!.name}\"? This will remove the tag from all tasks.",
                    style = MaterialTheme.typography.body1
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tagsViewModel.deleteTag(tagToDelete!!.id)
                        showDeleteDialog = false
                        tagToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        tagToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TagManagementItem(
    tag: Tag,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f)),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TagChip(
                tag = tag,
                selected = false,
                onClick = { },
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Tag",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Tag",
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateEditTagDialog(
    tag: Tag?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var tagName by remember { mutableStateOf(tag?.name ?: "") }
    var tagColor by remember { mutableStateOf(tag?.color ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (tag != null) "Edit Tag" else "Create Tag",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ColorPicker(
                    selectedColor = tagColor.ifBlank { null },
                    onColorSelected = { color ->
                        tagColor = color ?: ""
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tagName.isNotBlank()) {
                        onSave(tagName, tagColor.ifBlank { null })
                    }
                },
                enabled = tagName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


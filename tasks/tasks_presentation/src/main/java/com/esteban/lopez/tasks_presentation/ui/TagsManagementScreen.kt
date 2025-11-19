package com.esteban.ruano.tasks_presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.AppBar
import com.esteban.ruano.lifecommander.ui.components.TagChip
import com.esteban.ruano.lifecommander.ui.components.text.TitleH3
import com.esteban.ruano.tasks_presentation.intent.TagIntent
import com.esteban.ruano.tasks_presentation.ui.components.ColorPicker
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TagsViewModel
import com.esteban.ruano.ui.Gray
import com.esteban.ruano.ui.Gray2
import com.lifecommander.models.Tag

@Composable
fun TagsManagementScreen(
    onNavigateUp: () -> Unit,
    tagsViewModel: TagsViewModel = hiltViewModel()
) {
    val tagsState = tagsViewModel.viewState.collectAsState()
    var showCreateTagDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<Tag?>(null) }
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tagsViewModel.performAction(TagIntent.LoadTags)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AppBar(
            title = "Manage Tags",
            onClose = onNavigateUp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Create Tag Button
        Button(
            onClick = { showCreateTagDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Tag")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TitleH3("Your Tags")

        Spacer(modifier = Modifier.height(8.dp))

        if (tagsState.value.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (tagsState.value.tags.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tags yet. Create your first tag!",
                    style = MaterialTheme.typography.body1,
                    color = Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tagsState.value.tags) { tag ->
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
                    tagsViewModel.performAction(
                        TagIntent.UpdateTag(
                            tagId = tagToEdit!!.id,
                            name = name,
                            color = color
                        )
                    )
                } else {
                    tagsViewModel.performAction(
                        TagIntent.CreateTag(
                            name = name,
                            color = color
                        )
                    )
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
                        tagsViewModel.performAction(
                            TagIntent.DeleteTag(tagToDelete!!.id)
                        )
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray2.copy(alpha = 0.3f)),
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
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = Gray2.copy(alpha = 0.3f)
                    )
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


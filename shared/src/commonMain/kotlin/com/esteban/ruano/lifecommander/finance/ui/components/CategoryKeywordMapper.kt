package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.CategoryKeyword
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector

@Composable
fun CategoryKeywordMapper(
    categoryKeywords: List<CategoryKeyword>,
    onAddKeyword: (Category, String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onDeleteMapping: (CategoryKeyword) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var newKeyword by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            color = MaterialTheme.colors.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Keywords",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Mapping")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Mapping")
                }
            }
        }

        // Category Keywords List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categoryKeywords) { mapping ->
                CategoryKeywordCard(
                    mapping = mapping,
                    onAddKeyword = onAddKeyword,
                    onRemoveKeyword = onRemoveKeyword,
                    onDeleteMapping = onDeleteMapping
                )
            }
        }
    }

    // Add Mapping Dialog
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add Category Mapping",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )

                    // Category Selection
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.subtitle1
                    )
                    EnumChipSelector(
                        enumValues = Category.entries.toTypedArray(),
                        selectedValues = selectedCategory?.let { setOf(it) } ?: emptySet(),
                        onValueSelected = { categories ->
                            selectedCategory = categories.firstOrNull()
                        },
                        multiSelect = false,
                        labelMapper = { it.name }
                    )

                    // Keyword Input
                    OutlinedTextField(
                        value = newKeyword,
                        onValueChange = { newKeyword = it },
                        label = { Text("Keyword") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                selectedCategory?.let { category ->
                                    if (newKeyword.isNotBlank()) {
                                        onAddKeyword(category, newKeyword)
                                        newKeyword = ""
                                        showAddDialog = false
                                    }
                                }
                            },
                            enabled = selectedCategory != null && newKeyword.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CategoryKeywordCard(
    mapping: CategoryKeyword,
    onAddKeyword: (Category, String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onDeleteMapping: (CategoryKeyword) -> Unit
) {
    var showAddKeyword by remember { mutableStateOf(false) }
    var newKeyword by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mapping.category.name,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onDeleteMapping(mapping) }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Mapping",
                        tint = MaterialTheme.colors.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Keywords List
            FlowRow(
                modifier = Modifier.fillMaxWidth(),

            ) {
                mapping.keywords.forEach { keyword ->
                    Chip(
                        modifier = Modifier.padding(8.dp),
                        onClick = { },
                        leadingIcon = {
                            IconButton(
                                onClick = { onRemoveKeyword(keyword.id?:"") }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove Keyword",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    ) {
                        Text(keyword.keyword)
                    }
                }

                // Add Keyword Button
                Chip(
                    onClick = { showAddKeyword = true }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Keyword",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Keyword")
                }
            }
        }
    }

    // Add Keyword Dialog
    if (showAddKeyword) {
        Dialog(onDismissRequest = { showAddKeyword = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add Keyword",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = newKeyword,
                        onValueChange = { newKeyword = it },
                        label = { Text("Keyword") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddKeyword = false }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newKeyword.isNotBlank()) {
                                    onAddKeyword(mapping.category, newKeyword)
                                    newKeyword = ""
                                    showAddKeyword = false
                                }
                            },
                            enabled = newKeyword.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
} 
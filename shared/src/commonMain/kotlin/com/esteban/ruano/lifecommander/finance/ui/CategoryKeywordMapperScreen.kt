package com.esteban.ruano.lifecommander.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.finance.ui.components.CategoryKeywordMapper
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.CategoryKeyword
import kotlinx.coroutines.launch

@Composable
fun CategoryKeywordMapperScreen(
    categoryKeywords: List<CategoryKeyword>,
    onAddKeyword: (Category, String) -> Unit,
    onRemoveKeyword: (Category, String) -> Unit,
    onDeleteMapping: (CategoryKeyword) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Category Keywords",
                        color = MaterialTheme.colors.onPrimary
                    )
                },
                backgroundColor = MaterialTheme.colors.primary,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        CategoryKeywordMapper(
            categoryKeywords = categoryKeywords,
            onAddKeyword = onAddKeyword,
            onRemoveKeyword = onRemoveKeyword,
            onDeleteMapping = onDeleteMapping,
            modifier = Modifier.padding(padding)
        )
    }
} 
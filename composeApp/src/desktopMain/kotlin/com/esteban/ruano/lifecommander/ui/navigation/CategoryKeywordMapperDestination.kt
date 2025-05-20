package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.finance.ui.CategoryKeywordMapperScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.CategoryKeywordMapperViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryKeywordMapperDestination(
    modifier: Modifier = Modifier,
    viewModel: CategoryKeywordMapperViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCategoryKeywords()
    }

    CategoryKeywordMapperScreen(
        categoryKeywords = state.categoryKeywords,
        onAddKeyword = { category, keyword ->
            viewModel.addKeyword(category, keyword)
        },
        onRemoveKeyword = { category, keyword ->
            viewModel.removeKeyword(category, keyword)
        },
        onDeleteMapping = { mapping ->
            viewModel.deleteMapping(mapping)
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
} 
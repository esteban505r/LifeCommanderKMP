package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : Enum<T>> EnumChipSelector(
    enumValues: Array<T>,
    selectedValues: Set<T>,
    onValueSelected: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    labelMapper: (T) -> String = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
    multiSelect: Boolean = false
) {
    val lazyListState = rememberLazyListState()
    ChipSelectorWrapper(modifier = Modifier.fillMaxWidth(), lazyListState) {
        LazyRow(modifier = modifier.padding(vertical = 8.dp), state = lazyListState) {
            items(enumValues.size) { index ->
                val value = enumValues[index]
                val isSelected = value in selectedValues
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection = if (multiSelect) {
                            if (isSelected) {
                                selectedValues - value
                            } else {
                                selectedValues + value
                            }
                        } else {
                            if (isSelected) {
                                emptySet()
                            } else {
                                setOf(value)
                            }
                        }
                        onValueSelected(newSelection)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    border = null,
                    colors = ChipDefaults.filterChipColors(
                        backgroundColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                        contentColor = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                    )
                ) {
                    Text(
                        text = labelMapper(value),
                        color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
}

// Single select convenience function
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : Enum<T>> EnumChipSelector(
    enumValues: Array<T>,
    selectedValue: T?,
    onValueSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    labelMapper: (T) -> String = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
) {
    EnumChipSelector(
        enumValues = enumValues,
        selectedValues = selectedValue?.let { setOf(it) } ?: emptySet(),
        onValueSelected = { newSelection -> onValueSelected(newSelection.firstOrNull()) },
        modifier = modifier,
        labelMapper = labelMapper,
        multiSelect = false
    )
}

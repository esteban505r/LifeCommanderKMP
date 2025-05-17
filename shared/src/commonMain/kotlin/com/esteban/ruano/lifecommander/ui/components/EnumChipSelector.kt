package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
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
    selectedValue: T,
    scrollState: ScrollState,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelMapper: (T) -> String = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
) {
    LazyRow(modifier = modifier.padding(vertical = 8.dp).horizontalScroll(scrollState)) {
        items(enumValues.size) { index ->
            val isSelected = enumValues[index] == selectedValue
            FilterChip(
                selected = isSelected,
                onClick = { onValueSelected(enumValues[index]) },
                modifier = Modifier.padding(horizontal = 4.dp),
                border = null,
                colors = ChipDefaults.filterChipColors(
                    backgroundColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                    contentColor = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                )
            ) {
                Text(text = labelMapper(enumValues[index]), color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface)
            }
        }
    }
}

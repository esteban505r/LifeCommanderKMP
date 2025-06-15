package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> ToggleButtons(
    selectedIndex: Int = 0,
    buttons: List<T>,
    onCheckedChange: (T) -> Unit,
    toString: (@Composable (T) -> String)? = null
) {
    if (buttons.isEmpty()) return

    var selectedIndex by remember { mutableIntStateOf(selectedIndex) }
    LazyRow {
        items(buttons.size) { index ->
            val obj = buttons[index]
            Card(
                onClick = {
                    selectedIndex = index
                    onCheckedChange(obj)
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                backgroundColor = if (selectedIndex == index) MaterialTheme.colors.primary else MaterialTheme.colors.background,
                border = if (selectedIndex == index) null else BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = toString?.invoke(obj) ?: obj.toString(),
                    modifier = Modifier.padding(16.dp),
                    color = if (selectedIndex == index) MaterialTheme.colors.surface else MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> ToggleChipsButtons(
    modifier : Modifier = Modifier,
    selectedIndex: Int,
    buttons: List<T>,
    onCheckedChange: (T) -> Unit,
    onGetStrings: @Composable ((T) -> String)? = null
) {
    if (buttons.isEmpty()) return
    LazyRow(
        modifier = modifier
    ) {
        items(buttons.size) {
            Chip(
                onClick = {
                    onCheckedChange(buttons[it])
                },
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = if (selectedIndex == it) 
                    ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.primary) 
                else 
                    ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface),
                border = if (selectedIndex == it) null else BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
            ) {
                Text(
                    text = onGetStrings?.invoke(buttons[it]) ?: buttons[it].toString(),
                    color = if (selectedIndex == it) MaterialTheme.colors.surface else MaterialTheme.colors.onSurface
                )
            }
        }
    }
} 
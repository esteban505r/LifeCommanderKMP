package ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TaskFilters


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TaskFilterChip(selectedFilter: TaskFilters, filter: TaskFilters, onClick: (TaskFilters) -> Unit) {
    Chip(
        colors = ChipDefaults.chipColors(
            backgroundColor = if (selectedFilter == filter) MaterialTheme.colors.primary else MaterialTheme.colors.surface
        ),
        border = if (selectedFilter == filter) null else BorderStroke(1.dp, MaterialTheme.colors.primary),
        modifier = Modifier.clickable { onClick(filter) }.padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = {
            onClick(filter)
        }
    ){
        Text(text = filter.value, style = MaterialTheme.typography.body2,
            color = if (selectedFilter == filter) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface)
    }
}
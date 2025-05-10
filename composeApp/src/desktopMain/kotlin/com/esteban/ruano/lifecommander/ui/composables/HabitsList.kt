package ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import services.NightBlockService
import services.habits.models.HabitResponse
import ui.screens.SectionTitle
import java.time.LocalDateTime

@Composable
fun HabitsList(
    modifier: Modifier = Modifier,
    habits: List<HabitResponse>,
    onCheckedChange: (String,Boolean) -> Unit,
    currentTime: LocalDateTime = LocalDateTime.now(),
    onNewHabitClick: () -> Unit,
    onEditHabitClick: (HabitResponse) -> Unit,
    onDeleteHabitClick: (String) -> Unit,
    nightBlockService: NightBlockService
) {

    val isNightBlockActive by nightBlockService.isNightBlockActive.collectAsState()
    val whitelistedHabits by nightBlockService.whitelistedHabits.collectAsState()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item{
            SectionTitle(title = "Habits")
        }
        item{
            Spacer(modifier = Modifier.height(55.dp))
        }
        items(habits.size) { index ->
            HabitCard(
                habit = habits[index],
                currentTime = currentTime,
                onCheckedChange = onCheckedChange,
                onEdit = {
                    onEditHabitClick(habits[index])
                },
                onDelete = {
                    onDeleteHabitClick(habits[index].id)
                },
                isEnabled = isNightBlockActive && whitelistedHabits.contains(habits[index].id) || isNightBlockActive.not()
            )

        }
        item {
            NewItemCard("New Habit", onClick = onNewHabitClick)
        }
    }
}
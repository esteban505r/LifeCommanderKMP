package com.esteban.ruano.habits_presentation.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.ui.Gray3
import com.lifecommander.models.Habit
import kotlinx.coroutines.launch

@Composable
fun HabitCard(habit: Habit? = null, modifier: Modifier = Modifier, onHabitClick: (Habit?) -> Unit = {}) {
    val context = LocalContext.current
    Card(
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.Gray
        ),
        elevation = 5.dp,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                onHabitClick(habit)
            }
    ) {
        val coroutineScope = rememberCoroutineScope()
        var timeDoing by remember { mutableStateOf("00:00") }

        LaunchedEffect(Unit) {
            habit?.let{
                coroutineScope.launch {
                    while (true) {
                        //timeDoing = habit.timeDoingIt()?.formatTime() ?: "00:00"
                        kotlinx.coroutines.delay(1000)
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = habit?.name ?: stringResource(id = R.string.you_re_not_doing_anything),
                style = MaterialTheme.typography.h2.copy(color = Gray3),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    habit?.let{
                        "getStringResourceByCurrentHabit(context,habit, timeDoing)"
                    } ?: stringResource(R.string.enjoy_your_free_time),
                    textAlign = TextAlign.Center
                )
            }

            /*Image(
                painter = painterResource(id = com.esteban.ruano.habits_presentation.R.drawable.habit_morning),
                contentScale = ContentScale.FillWidth,
                contentDescription = "Habit morning",
                modifier = Modifier
                    .height(200.dp)
                    .clip(RoundedCornerShape(
                        16.dp
                    ))
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = habit.name ?: stringResource(id = R.string.habit_without_name),
                style = MaterialTheme.typography.h5.copy(color = Gray3)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = habit.time() ?: stringResource(id = R.string.no_time),
                style = MaterialTheme.typography.h6.copy(color = Gray)
            )*/
        }
    }
}
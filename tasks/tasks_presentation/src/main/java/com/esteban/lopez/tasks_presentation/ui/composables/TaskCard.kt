package com.esteban.ruano.tasks_presentation.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.theme.Gray3
import com.esteban.ruano.tasks_domain.model.Task


@Composable
fun TaskCard(task: Task, modifier: Modifier = Modifier, onTaskClick: (Task) -> Unit = {}) {
    Card(
        border = BorderStroke(
            width = 1.dp,
            color = Color.Gray
        ),
        elevation = 5.dp,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                onTaskClick(task)
            }
    ) {
        val coroutineScope = rememberCoroutineScope()
        var timeDoing by remember { mutableStateOf("00:00") }

        LaunchedEffect(Unit) {

        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp,vertical = 24.dp)
        ) {
            Text(
                text = task.name ?: stringResource(id = R.string.task_without_name),
                style = MaterialTheme.typography.h2.copy(color = Gray3)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Tienes hasta tata para completar esta tarea",
                    textAlign = TextAlign.Center
                )
            }
            /*Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Llevas $timeDoing haciendo esta actividad",
                    textAlign = TextAlign.Center
                )
            }*/

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
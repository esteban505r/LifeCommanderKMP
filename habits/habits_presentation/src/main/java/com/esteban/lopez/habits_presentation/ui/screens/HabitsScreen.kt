package com.esteban.lopez.habits_presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.state.HabitState
import com.esteban.ruano.ui.components.HabitList
import com.esteban.ruano.utils.HabitsUtils.findCurrentHabit
import com.lifecommander.models.Habit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HabitsScreen(
    onHabitClick: (Habit) -> Unit,
    onNewHabitClick: () -> Unit,
    onNavigateUp: () -> Unit,
    state: HabitState,
    userIntent: (HabitIntent) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scaffoldState = rememberScaffoldState()


    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = {
                    onNewHabitClick()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(id = R.string.new_habit_title),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if(state.habits.isEmpty()){
                Column(
                    Modifier.fillMaxWidth().align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.empty_habits_2),
                        contentDescription = "Empty habits",
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.empty_habits),
                        style = MaterialTheme.typography.subtitle1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(400.dp)
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        state.habits.findCurrentHabit()?.name ?: "No current habit",
                        style = MaterialTheme.typography.h1.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.inProgress),
                        style = MaterialTheme.typography.h3.copy(
                            color = MaterialTheme.colors.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    /* Spacer(modifier = Modifier.height(16.dp))
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         Text("Streak")
                         Text("83%")
                     }*/
                    /*Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = currentProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Your longest streak is 9 days")
                    Spacer(modifier = Modifier.height(16.dp))*/
                    HabitList(onHabitClick = onHabitClick,
                        onCheckedChange = { habit, checked, onComplete ->
                            coroutineScope.launch {
                                userIntent(
                                    if (checked) HabitIntent.CompleteHabit(
                                        habit.id,
                                        onComplete
                                    ) else HabitIntent.UnCompleteHabit(
                                        habit.id, onComplete
                                    )
                                )
                            }
                        },
                        habitList = state.habits,
                        isRefreshing = false,
                        onPullRefresh = {
                            coroutineScope.launch {
                                userIntent(HabitIntent.FetchHabits())
                            }
                        },
                        onEdit = {_->
                            },
                        onDelete = { habit ->},
                        itemWrapper = { content, habit ->
                            Box {
                                content()
                            }
                        }
                    )
                }
            }
        }
    }
}
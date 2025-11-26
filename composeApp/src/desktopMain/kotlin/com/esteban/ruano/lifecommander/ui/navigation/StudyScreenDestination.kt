package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.screens.StudyScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.StudyViewModel

@Composable
fun StudyScreenDestination(
    studyViewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val state by studyViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        studyViewModel.loadData()
    }

            StudyScreen(
                topics = state.topics,
                items = state.items,
                sessions = state.sessions,
                disciplines = state.disciplines,
                topicsLoading = state.topicsLoading,
                itemsLoading = state.itemsLoading,
                sessionsLoading = state.sessionsLoading,
        onReload = { studyViewModel.loadData() },
        onCreateTopic = { studyViewModel.createTopic(it) },
        onUpdateTopic = { id, topic -> studyViewModel.updateTopic(id, topic) },
        onDeleteTopic = { id -> studyViewModel.deleteTopic(id) },
        onCreateItem = { studyViewModel.createItem(it) },
        onUpdateItem = { id, item -> studyViewModel.updateItem(id, item) },
        onDeleteItem = { id -> studyViewModel.deleteItem(id) },
        onCreateSession = { studyViewModel.createSession(it) },
        onUpdateSession = { id, session -> studyViewModel.updateSession(id, session) },
        onCompleteSession = { id, actualEnd, notes -> studyViewModel.completeSession(id, actualEnd, notes) },
        onDeleteSession = { id -> studyViewModel.deleteSession(id) },
        modifier = modifier
    )
}


package com.esteban.ruano.tasks_domain.di

import com.esteban.ruano.tasks_domain.repository.TasksRepository
import com.esteban.ruano.tasks_domain.use_cases.AddTask
import com.esteban.ruano.tasks_domain.use_cases.CompleteTask
import com.esteban.ruano.tasks_domain.use_cases.DeleteTask
import com.esteban.ruano.tasks_domain.use_cases.GetTask
import com.esteban.ruano.tasks_domain.use_cases.GetTasks
import com.esteban.ruano.tasks_domain.use_cases.GetTasksWithSmartFiltering
import com.esteban.ruano.tasks_domain.use_cases.GetTasksNoDueDate
import com.esteban.ruano.tasks_domain.use_cases.RescheduleTask
import com.esteban.ruano.tasks_domain.use_cases.TaskUseCases
import com.esteban.ruano.tasks_domain.use_cases.UnCompleteTask
import com.esteban.ruano.tasks_domain.use_cases.UpdateTask
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object TaskDomainModule {
    @ViewModelScoped
    @Provides
    fun provideTaskUseCases(
        repository: TasksRepository
    ): TaskUseCases {
        return TaskUseCases(
            getTasks = GetTasks(repository),
            getTasksWithSmartFiltering = GetTasksWithSmartFiltering(repository),
            completeTask = CompleteTask(repository),
            unCompleteTask = UnCompleteTask(repository),
            deleteTask = DeleteTask(repository),
            addTask = AddTask(repository),
            updateTask = UpdateTask(repository),
            getTask = GetTask(repository),
            getTaskNoDueDate = GetTasksNoDueDate(repository),
            rescheduleTask = RescheduleTask(repository)
        )
    }
}
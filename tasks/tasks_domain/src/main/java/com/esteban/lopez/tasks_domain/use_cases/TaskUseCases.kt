package com.esteban.ruano.tasks_domain.use_cases

data class TaskUseCases(
    val getTasks: GetTasks,
    val getTasksWithSmartFiltering: GetTasksWithSmartFiltering,
    val getTaskNoDueDate: GetTasksNoDueDate,
    val deleteTask: DeleteTask,
    val completeTask: CompleteTask,
    val unCompleteTask: UnCompleteTask,
    val addTask: AddTask,
    val updateTask: UpdateTask,
    val rescheduleTask: RescheduleTask,
    val getTask: GetTask
)

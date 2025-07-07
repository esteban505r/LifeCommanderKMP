package com.esteban.ruano.repository

import com.esteban.ruano.models.tasks.CreateTaskDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.tasks.UpdateTaskDTO
import com.esteban.ruano.service.TaskService
import com.esteban.ruano.utils.parseDate
import java.util.*

class TaskRepository(private val taskService: TaskService) {

    fun getAll(userId: Int, filter: String, limit: Int, offset: Long, date: String? = null, withOverdue:Boolean = true): List<TaskDTO> {

        if (date != null) {
            val tasks = taskService.fetchAll(
                userId,
                filter,
                limit,
                offset,
                parseDate(date),
                withOverdue
            )
            return tasks

        }

        return taskService.fetchAll(
            userId,
            filter,
            limit,
            offset,
        )
    }

    fun getAllByDate(
        userId: Int,
        startDate: String,
        endDate: String,
        filter: String,
        limit: Int,
        offset: Long
    ): List<TaskDTO> {
        return taskService.fetchAllByDateRange(
            userId,
            filter,
            parseDate(startDate),
            parseDate(endDate),
            limit,
            offset
        )
    }

    fun getAllByDateWithSmartFiltering(
        userId: Int,
        startDate: String,
        endDate: String,
        filter: String,
        limit: Int,
        offset: Long,
        isTodayFilter: Boolean = false
    ): List<TaskDTO> {
        return taskService.fetchAllByDateRangeWithSmartFiltering(
            userId,
            filter,
            parseDate(startDate),
            parseDate(endDate),
            limit,
            offset,
            isTodayFilter
        )
    }

    fun getAllNoDueDate(userId: Int, filter: String, limit: Int, offset: Long): List<TaskDTO> {
        return taskService.fetchAllNoDueDate(
            userId,
            filter,
            limit,
            offset,
        )
    }

    fun create(userId: Int, task: CreateTaskDTO): UUID? {
        return taskService.create(userId, task)
    }

    fun completeTask(userId: Int, id: UUID, dateTime:String): Boolean {
        return taskService.completeTask(userId,id,dateTime)
    }

    fun unCompleteTask(userId: Int, id: UUID): Boolean {
        return taskService.unCompleteTask(userId,id)
    }

    fun getByIdAndUserId(id: UUID, userId: Int): TaskDTO? {
        return taskService.getByIdAndUserId(id, userId)
    }

    fun update(userId:Int,id: UUID, task: UpdateTaskDTO): Boolean {
        return taskService.update(userId,id, task)
    }

    fun delete(userId: Int,id: UUID): Boolean {
        return taskService.delete(userId,id)
    }

    fun getByUserId(userId: Int, limit: Int, offset: Long): List<TaskDTO> {
        return taskService.getByUserId(userId, limit, offset)
    }
}
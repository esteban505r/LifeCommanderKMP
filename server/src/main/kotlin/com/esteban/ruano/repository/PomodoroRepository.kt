package com.esteban.ruano.repository

import kotlinx.datetime.*
import com.esteban.ruano.models.pomodoros.CreatePomodoroDTO
import com.esteban.ruano.models.pomodoros.PomodoroDTO
import com.esteban.ruano.models.pomodoros.UpdatePomodoroDTO
import com.esteban.ruano.service.PomodoroService
import parseDate
import java.util.UUID

class PomodoroRepository(private val pomodoroService: PomodoroService) {

    fun getAll(userId: Int, limit: Int, offset: Long): List<PomodoroDTO> {
        return pomodoroService.getByUserId(userId, limit, offset)
    }

    fun getAllByDateRange(
        userId: Int,
        startDate: String,
        endDate: String,
        limit: Int,
        offset: Long
    ): List<PomodoroDTO> {
        return pomodoroService.getByDateRange(
            userId,
            parseDate(startDate),
            parseDate(endDate),
            limit,
            offset
        )
    }

    fun create(userId: Int, pomodoro: CreatePomodoroDTO): UUID? {
        return pomodoroService.create(userId, pomodoro)
    }

    fun update(userId: Int, id: UUID, pomodoro: UpdatePomodoroDTO): Boolean {
        return pomodoroService.update(userId, id, pomodoro)
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return pomodoroService.delete(userId, id)
    }

    fun getByIdAndUserId(id: UUID, userId: Int): PomodoroDTO? {
        return pomodoroService.getByIdAndUserId(id, userId)
    }
} 
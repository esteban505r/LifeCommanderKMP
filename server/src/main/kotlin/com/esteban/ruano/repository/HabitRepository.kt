package com.esteban.ruano.repository

import com.esteban.ruano.models.habits.CreateHabitDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.habits.UpdateHabitDTO
import com.esteban.ruano.service.HabitService
import com.esteban.ruano.utils.parseDate
import java.util.*

class HabitRepository(private val habitService: HabitService) {

    fun getAll(userId: Int, filter: String, limit: Int, offset: Long, date: String? = null): List<HabitDTO> {

        if (date != null) {
            val habits = habitService.fetchAll(
                userId,
                filter,
                limit,
                offset,
                parseDate(date)
            )
            return habits

        }

        return habitService.fetchAll(
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
        offset: Long,
        excludeDaily: Boolean = false
    ): List<HabitDTO> {
        return habitService.fetchAllByDateRange(
            userId,
            filter,
            parseDate(startDate),
            parseDate(endDate),
            limit,
            offset,
            excludeDaily
        )
    }


    fun create(userId: Int, habit: CreateHabitDTO): UUID? {
        return habitService.create(userId, habit)
    }

    fun completeTask(id: String,doneDate:String, userId: Int): Boolean {
        return habitService.completeHabit(id,doneDate,userId)
    }

    fun unCompleteTask(id: String,unDoneDate:String,userId: Int): Boolean {
        return habitService.unCompleteHabit(id,unDoneDate,userId)
    }

    fun getByIdAndUserId(id: UUID, userId: Int, date:String): HabitDTO? {
        return habitService.getByIdAndUserId(id, userId,date)
    }

    fun update(userId: Int, id:UUID, habit: UpdateHabitDTO): Boolean {
        return habitService.update(userId,id, habit)
    }

    fun delete(userId:Int,id: UUID): Boolean {
        return habitService.delete(userId,id)
    }

}
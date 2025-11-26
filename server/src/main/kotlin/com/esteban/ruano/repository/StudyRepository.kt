package com.esteban.ruano.repository

import com.esteban.ruano.database.entities.StudyDiscipline
import com.esteban.ruano.models.study.*
import com.esteban.ruano.service.StudyService
import java.util.*

class StudyRepository(private val studyService: StudyService) {

    // StudyTopic operations
    fun getAllTopics(userId: Int, isActive: Boolean? = null): List<StudyTopicDTO> {
        return studyService.getAllTopics(userId, isActive)
    }

    fun getTopicById(userId: Int, id: UUID): StudyTopicDTO? {
        return studyService.getTopicById(userId, id)
    }

    fun createTopic(userId: Int, dto: CreateStudyTopicDTO): UUID? {
        return studyService.createTopic(userId, dto)
    }

    fun updateTopic(userId: Int, id: UUID, dto: UpdateStudyTopicDTO): Boolean {
        return studyService.updateTopic(userId, id, dto)
    }

    fun deleteTopic(userId: Int, id: UUID): Boolean {
        return studyService.deleteTopic(userId, id)
    }

    // StudyItem operations
    fun getAllItems(
        userId: Int,
        topicId: String? = null,
        stage: String? = null,
        search: String? = null
    ): List<StudyItemDTO> {
        val topicUuid = topicId?.let { UUID.fromString(it) }
        return studyService.getAllItems(userId, topicUuid, stage, search)
    }

    fun getItemById(userId: Int, id: UUID): StudyItemDTO? {
        return studyService.getItemById(userId, id)
    }

    fun createItem(userId: Int, dto: CreateStudyItemDTO): UUID? {
        return studyService.createItem(userId, dto)
    }

    fun updateItem(userId: Int, id: UUID, dto: UpdateStudyItemDTO): Boolean {
        return studyService.updateItem(userId, id, dto)
    }

    fun deleteItem(userId: Int, id: UUID): Boolean {
        return studyService.deleteItem(userId, id)
    }

    // StudySession operations
    fun getAllSessions(
        userId: Int,
        topicId: String? = null,
        mode: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): List<StudySessionDTO> {
        val topicUuid = topicId?.let { UUID.fromString(it) }
        return studyService.getAllSessions(userId, topicUuid, mode, startDate, endDate)
    }

    fun getSessionById(userId: Int, id: UUID): StudySessionDTO? {
        return studyService.getSessionById(userId, id)
    }

    fun createSession(userId: Int, dto: CreateStudySessionDTO): UUID? {
        return studyService.createSession(userId, dto)
    }

    fun updateSession(userId: Int, id: UUID, dto: UpdateStudySessionDTO): Boolean {
        return studyService.updateSession(userId, id, dto)
    }

    fun completeSession(userId: Int, id: UUID, actualEnd: String, notes: String? = null): Boolean {
        return studyService.completeSession(userId, id, actualEnd, notes)
    }

    fun deleteSession(userId: Int, id: UUID): Boolean {
        return studyService.deleteSession(userId, id)
    }

    fun getStats(userId: Int, startDate: String? = null, endDate: String? = null): StudyStatsDTO {
        return studyService.getStats(userId, startDate, endDate)
    }

    fun getAllDisciplines(userId: Int): List<StudyDiscipline> {
        return studyService.getAllDisciplines(userId)
    }
}


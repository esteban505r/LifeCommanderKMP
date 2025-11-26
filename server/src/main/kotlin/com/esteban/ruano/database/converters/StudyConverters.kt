package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.*
import com.esteban.ruano.models.study.*
import com.esteban.ruano.utils.formatDateTime

fun StudyDiscipline.toStudyDisciplineDTO(): StudyDisciplineDTO {
    return StudyDisciplineDTO(
        id = this.id.toString(),
        name = this.name,
        description = this.description,
        color = this.color,
        iconUrl = this.iconUrl,
        createdAt = formatDateTime(this.createdAt),
        updatedAt = formatDateTime(this.updatedAt)
    )
}

fun StudyTopic.toStudyTopicDTO(): StudyTopicDTO {
    return StudyTopicDTO(
        id = this.id.toString(),
        name = this.name,
        description = this.description,
        discipline = try {
            // Access the discipline relationship - Exposed will load it if the foreign key is set
            this.discipline?.toStudyDisciplineDTO()
        } catch (e: Exception) {
            // Handle case where discipline relationship is not loaded or null
            null
        },
        color = this.color,
        icon = this.iconUrl, // Note: DTO uses 'icon' but entity uses 'iconUrl'
        isActive = this.isActive
    )
}

fun StudyItem.toStudyItemDTO(): StudyItemDTO {
    return StudyItemDTO(
        id = this.id.toString(),
        topic = try {
            // Access the topic relationship - Exposed will load it if the foreign key is set
            this.topic?.toStudyTopicDTO()
        } catch (e: Exception) {
            // Handle case where topic relationship is not loaded or null
            null
        },
        title = this.title,
        obsidianPath = this.obsidianPath,
        stage = this.stage.name,
        modeHint = this.modeHint?.name,
        discipline = this.discipline,
        progress = this.progress,
        estimatedEffortMinutes = this.estimatedEffortMinutes,
        createdAt = formatDateTime(this.createdAt),
        updatedAt = formatDateTime(this.updatedAt)
    )
}

fun StudySession.toStudySessionDTO(): StudySessionDTO {
    return StudySessionDTO(
        id = this.id.toString(),
        topic = try {
            // Access the topic relationship - Exposed will load it if the foreign key is set
            this.topic?.toStudyTopicDTO()
        } catch (e: Exception) {
            // Handle case where topic relationship is not loaded or null
            null
        },
        studyItem = try {
            // Access the studyItem relationship - Exposed will load it if the foreign key is set
            this.studyItem?.toStudyItemDTO()
        } catch (e: Exception) {
            // Handle case where studyItem relationship is not loaded or null
            null
        },
        mode = this.mode.name,
        plannedStart = this.plannedStart?.let { formatDateTime(it) },
        plannedEnd = this.plannedEnd?.let { formatDateTime(it) },
        actualStart = this.actualStart?.let { formatDateTime(it) },
        actualEnd = this.actualEnd?.let { formatDateTime(it) },
        durationMinutes = this.durationMinutes,
        notes = this.notes,
        createdAt = formatDateTime(this.createdAt),
        updatedAt = formatDateTime(this.updatedAt)
    )
}


package com.esteban.ruano.tasks_domain.model


data class Task(
    val id: String,
    val name: String? = null,
    val done: Boolean? = false,
    val note: String? = null,
    val dueDateTime: String?=null,
    val scheduledDateTime: String?=null,
    val priority: Int?=0,
    val reminders: List<TaskReminder>?=null,
    val createdAt: String = System.currentTimeMillis().toString(),
    val updatedAt: String? = null
)
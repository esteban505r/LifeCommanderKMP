package com.lifecommander.models

data class Habit(
    val id: String,
    val name: String,
    val note: String?,
    val dateTime: String?,
    val done: Boolean?,
    val frequency: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

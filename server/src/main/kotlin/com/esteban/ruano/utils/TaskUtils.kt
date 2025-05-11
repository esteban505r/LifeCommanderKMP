package com.esteban.ruano.utils

import com.esteban.ruano.database.entities.Task

fun List<Task>.sortedByDefault(): List<Task> {
    return this.sortedWith(compareBy<Task?> { it?.priority }
        .thenBy { it?.scheduledDate }
        .thenBy { it?.dueDate }
    )
}
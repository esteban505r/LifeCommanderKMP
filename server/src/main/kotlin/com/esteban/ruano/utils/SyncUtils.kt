package com.esteban.ruano.utils

import com.esteban.ruano.database.models.DBActions
import com.esteban.ruano.models.sync.SyncItemDTO

object SyncUtils{

    fun <T> List<SyncItemDTO<T>>.sortedByAction(): List<SyncItemDTO<T>> {
        val actionOrder = listOf(DBActions.INSERT, DBActions.UPDATE, DBActions.DELETE)
        return this.sortedWith(compareBy { actionOrder.indexOf(DBActions.valueOf(it.action)) })
    }
}
package com.esteban.ruano.lifecommander.utilities

import com.esteban.ruano.core_data.local.model.DBActions
import com.esteban.ruano.core_data.local.model.SyncItemDTO

object SyncHelper {

    suspend fun <T> syncData(
        data:List<SyncItemDTO<T>>,
        onCreate: suspend (T) -> Unit,
        onUpdate: suspend (T) -> Unit,
        onDelete: suspend (T) -> Unit
    ) {
        data.forEach {
            try {
            val action = DBActions.valueOf(it.action)
            when(action){
                DBActions.INSERT -> onCreate(it.item)
                DBActions.UPDATE -> onUpdate(it.item)
                DBActions.DELETE -> onDelete(it.item)
            }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

}
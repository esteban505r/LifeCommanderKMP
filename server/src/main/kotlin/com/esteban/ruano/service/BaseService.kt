package com.esteban.ruano.service

import com.esteban.ruano.database.entities.HistoryTracks
import com.esteban.ruano.database.models.DBActions
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.insert
import java.util.*

open class BaseService {
    /**
     * Performs an operation on a table and logs it in HistoryTrack if the operation is successful.
     *
     * @param T The table type.
     * @param userId The ID of the user performing the action.
     * @param actionType The type of action being performed (e.g., INSERT, UPDATE).
     * @param operation A lambda that executes the table operation and returns an entity ID (or null if failed).
     * @return The ID of the affected row or null if the operation failed.
     */
    private fun <T: Table> T.doOperation(
        userId: Int,
        actionType: DBActions,
        lastUpdate: Long? = null,
        operation: T.() -> UUID?
    ): UUID? {
        val resultId = this.operation()  // Execute the provided table operation.

        if (resultId != null) {
            // Log the operation to HistoryTrack
            HistoryTracks.insert {
                it[entityName] = this.tableName  // Log the table name
                it[entityId] = resultId          // Log the affected entity ID
                it[action_type] = actionType.value  // Log the type of action
                it[timestamp] = lastUpdate ?: System.currentTimeMillis()  // Log the timestamp
                it[user_id] = userId             // Log the user ID
            }
            return resultId
        }

        return null
    }

    fun <T:Table> T.updateOperation(userId: Int, createdAt: Long? = null, operation: T.() -> UUID?): UUID? = doOperation(userId, DBActions.UPDATE, createdAt,operation)

    fun <T:Table> T.insertOperation(userId: Int, createdAt:Long? = null,operation: T.() -> UUID?): UUID? = doOperation(userId, DBActions.INSERT, createdAt, operation)

    fun <T:Table> T.deleteOperation(userId: Int,  createdAt:Long? = null, operation: T.() -> UUID?): UUID? = doOperation(userId, DBActions.DELETE, createdAt, operation)

}

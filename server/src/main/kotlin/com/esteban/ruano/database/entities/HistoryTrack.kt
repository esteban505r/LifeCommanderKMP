package com.esteban.ruano.database.entities

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object HistoryTracks : IntIdTable(){
    val entityName = varchar("entity_name", 255)
    val entityId = uuid("entity_id")
    val action_type = varchar("action_type", 255)
    val timestamp = long("timestamp")
    val user_id = reference("user_id", Users, ReferenceOption.CASCADE)
}

class HistoryTrack(id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<HistoryTrack>(HistoryTracks)

    var entityName by HistoryTracks.entityName
    var entityId by HistoryTracks.entityId
    var actionType by HistoryTracks.action_type
    var timestamp by HistoryTracks.timestamp
    var userId by HistoryTracks.user_id
}
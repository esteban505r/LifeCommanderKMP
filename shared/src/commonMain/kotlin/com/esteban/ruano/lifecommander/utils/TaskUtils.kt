package com.esteban.ruano.lifecommander.utils

import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUtils
import com.lifecommander.models.Task
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

object TaskUtils {

    /** Prefer an explicit due over a scheduled time; fall back to scheduled. */
    fun Task.dueAt(): LocalDateTime? =
        dueDateTime?.toLocalDateTimeUtils() ?: scheduledDateTime?.toLocalDateTimeUtils()

    @OptIn(ExperimentalTime::class)
    fun Task.dueAtMillis(tz: TimeZone = TimeZone.currentSystemDefault()): Long? =
        dueAt()?.toInstant(tz)?.toEpochMilliseconds()

    /** Overdue = has a due occurrence, time has passed, and not done. */
    @OptIn(ExperimentalTime::class)
    fun Task.isOverdue(
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): Boolean {
        if (done) return false
        val d = dueAt() ?: return false
        return d < now
    }

    /** Pending = not done and either not due yet OR no due time set. */
    @OptIn(ExperimentalTime::class)
    fun Task.isPending(
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): Boolean = !done && (dueAt()?.let { it >= now } ?: true)

    /** Convenience for grouping. */
    fun Task.isDone(): Boolean = done

}
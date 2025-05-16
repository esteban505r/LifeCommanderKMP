package com.esteban.ruano.utils

import com.esteban.ruano.MR
import com.esteban.ruano.utils.DateUIUtils.formatToTimeString
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Frequency
import com.lifecommander.models.Habit
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import kotlinx.datetime.LocalTime

import kotlinx.datetime.*
import kotlin.math.absoluteValue

import dev.icerock.moko.resources.desc.StringDesc

object HabitsUtils {

    fun Habit.time() = this.dateTime?.toLocalDateTime()?.formatToTimeString()

    fun Habit.timeDoingIt(): Long? {
        val habitTime = this.dateTime?.toLocalDateTime()?.time ?: return null
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        return (now.minutesSinceMidnight() - habitTime.minutesSinceMidnight()).toLong()
    }

    fun Habit.date() = this.dateTime?.toLocalDateTime()?.date?.formatToDateString()

    fun getStringResourceByCurrentHabit(
        habit: Habit?,
        timeDoing: String
    ): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateTime = habit?.dateTime?.toLocalDateTime() ?: return ""
        val habitTime = dateTime.time

        return if (habitTime < now.time) {
            when (habit.frequency.uppercase()) {
                Frequency.WEEKLY.value -> {
                    if (dateTime.date.dayOfWeek == now.date.dayOfWeek) {
                        StringDesc.ResourceFormatted(MR.strings.youvebeen_doing_this_for, timeDoing).toString()
                    } else {
                        StringDesc.ResourceFormatted(
                            MR.strings.youdidnt_do_this_at,
                            dateTime.date.dayOfWeek.isoDayNumber.toDayOfTheWeekStringDesc()
                        ).toString()
                    }
                }

                Frequency.MONTHLY.value -> {
                    if (dateTime.date.month == now.date.month) {
                        StringDesc.ResourceFormatted(MR.strings.youvebeen_doing_this_for, timeDoing).toString()
                    } else {
                        StringDesc.ResourceFormatted(
                            MR.strings.youdidnt_do_this_at,
                            dateTime.date.monthNumber.toMonthStringDesc()
                        ).toString()
                    }
                }

                Frequency.YEARLY.value -> {
                    if (dateTime.date.dayOfYear == now.date.dayOfYear) {
                        StringDesc.ResourceFormatted(MR.strings.youvebeen_doing_this_for, timeDoing).toString()
                    } else {
                        StringDesc.ResourceFormatted(
                            MR.strings.youdidnt_do_this_at,
                            dateTime.date.formatToDateString()
                        ).toString()
                    }
                }

                else -> StringDesc.ResourceFormatted(MR.strings.youvebeen_doing_this_for, timeDoing).toString()
            }
        } else {
            StringDesc.ResourceFormatted(MR.strings.youllstart_doing_this_in, timeDoing).toString()
        }
    }

    fun List<Habit>.findCurrentHabit(): Habit? {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val filtered = this.filter {
            it.done == false &&
                    (it.frequency?.uppercase() == Frequency.DAILY.value ||
                            (it.frequency?.uppercase() == Frequency.WEEKLY.value &&
                                    it.dateTime?.toLocalDateTime()?.date?.dayOfWeek == now.date.dayOfWeek) ||
                            (it.frequency?.uppercase() == Frequency.MONTHLY.value &&
                                    it.dateTime?.toLocalDateTime()?.date?.month == now.date.month) ||
                            (it.frequency?.uppercase() == Frequency.YEARLY.value &&
                                    it.dateTime?.toLocalDateTime()?.date?.dayOfYear == now.date.dayOfYear)) &&
                    (it.dateTime?.toLocalDateTime()?.time?.let { t -> t < now.time } == true)
        }

        return filtered.minByOrNull {
            val time = it.dateTime?.toLocalDateTime()?.time
            time?.let { t -> (t.minutesSinceMidnight() - now.time.minutesSinceMidnight()).absoluteValue } ?: Int.MIN_VALUE
        } ?: findLastOverdueHabit()
    }

    fun List<Habit>.findLastOverdueHabit(): Habit? {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        return this.firstOrNull {
            it.done == false &&
                    it.frequency?.uppercase() != Frequency.DAILY.value &&
                    it.dateTime?.toLocalDateTime()?.time?.let { t -> t < now.time } == true
        } ?: this.maxByOrNull {
            it.dateTime?.toLocalDateTime()?.time?.let { time ->
                (time.minutesSinceMidnight() - now.time.minutesSinceMidnight()).absoluteValue
            } ?: Int.MIN_VALUE
        }
    }

    fun Habit.getTimeText(): StringDesc {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val localDateTime = this.dateTime?.toLocalDateTime() ?: return StringDesc.Raw("")

        return when (Frequency.valueOf(this.frequency?.uppercase() ?: "DAILY")) {
            Frequency.DAILY -> StringDesc.Raw(localDateTime.time.formatToTimeString())
            Frequency.WEEKLY -> {
                if (localDateTime.date.dayOfWeek == now.date.dayOfWeek) {
                    StringDesc.Resource(MR.strings.today)
                } else if (localDateTime.date.dayOfWeek.isoDayNumber == now.date.dayOfWeek.isoDayNumber.plus(1)) {
                    StringDesc.Resource(MR.strings.tomorrow)
                } else {
                    StringDesc.ResourceFormatted(
                        MR.strings.on,
                        localDateTime.date.dayOfWeek.isoDayNumber.toDayOfTheWeekStringDesc()
                    )
                }
            }

            Frequency.MONTHLY -> localDateTime.date.monthNumber.toMonthStringDesc()
            Frequency.YEARLY -> StringDesc.ResourceFormatted(
                MR.strings.on,
                localDateTime.date.formatToDateString()
            )
            Frequency.ONE_TIME -> StringDesc.Raw( localDateTime.formatToFullDateTimeString())
        }
    }

    fun Habit.getDelay(): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val habitTime = dateTime?.toLocalDateTime()?.time ?: LocalTime(0, 0)

        return when (frequency?.uppercase()) {
            Frequency.WEEKLY.value -> {
                val habitDay = dateTime?.toLocalDateTime()?.date?.dayOfWeek?.isoDayNumber ?: 1
                val currentDay = now.date.dayOfWeek.isoDayNumber
                if (habitDay > currentDay) 5
                else if (habitDay < currentDay) 1
                else 0
            }

            else -> {
                val diff = habitTime.minutesSinceMidnight() - now.time.minutesSinceMidnight()
                diff / 60 // to hours
            }
        }
    }

    // Extension helpers

    private fun LocalTime.minutesSinceMidnight(): Int = hour * 60 + minute

    private fun LocalDate.formatToDateString(): String =
        "${dayOfMonth.toString().padStart(2, '0')}/${monthNumber.toString().padStart(2, '0')}/$year"

    private fun LocalTime.formatToTimeString(): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    private fun LocalDateTime.formatToFullDateTimeString(): String =
        "${date.formatToDateString()} ${time.formatToTimeString()}"

    private fun Int.toDayOfTheWeekStringDesc(): StringDesc = when (this) {
        1 -> StringDesc.Resource(MR.strings.monday)
        2 -> StringDesc.Resource(MR.strings.tuesday)
        3 -> StringDesc.Resource(MR.strings.wednesday)
        4 -> StringDesc.Resource(MR.strings.thursday)
        5 -> StringDesc.Resource(MR.strings.friday)
        6 -> StringDesc.Resource(MR.strings.saturday)
        7 -> StringDesc.Resource(MR.strings.sunday)
        else -> StringDesc.Raw("")
    }

    private fun Int.toMonthStringDesc(): StringDesc = when (this) {
        1 -> StringDesc.Resource(MR.strings.january)
        2 -> StringDesc.Resource(MR.strings.february)
        3 -> StringDesc.Resource(MR.strings.march)
        4 -> StringDesc.Resource(MR.strings.april)
        5 -> StringDesc.Resource(MR.strings.may)
        6 -> StringDesc.Resource(MR.strings.june)
        7 -> StringDesc.Resource(MR.strings.july)
        8 -> StringDesc.Resource(MR.strings.august)
        9 -> StringDesc.Resource(MR.strings.september)
        10 -> StringDesc.Resource(MR.strings.october)
        11 -> StringDesc.Resource(MR.strings.november)
        12 -> StringDesc.Resource(MR.strings.december)
        else -> StringDesc.Raw("")
    }

}

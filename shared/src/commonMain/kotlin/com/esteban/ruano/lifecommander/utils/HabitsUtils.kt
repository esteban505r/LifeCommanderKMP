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

            Frequency.BI_WEEKLY -> {
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

    fun Habit.isOverdue(
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): Boolean {
        if (this.done == true) return false

        val base = this.dateTime?.toLocalDateTime() ?: return false
        val baseTime = base.time // the anchor time-of-day for every occurrence

        return when (Frequency.valueOf(this.frequency?.uppercase() ?: "DAILY")) {
            // Occurs every day at baseTime
            Frequency.DAILY -> now.time > baseTime

            // Occurs on base.date.dayOfWeek at baseTime in the weeks that apply
            Frequency.WEEKLY -> {
                val targetDow = base.date.dayOfWeek.isoDayNumber
                val todayDow  = now.date.dayOfWeek.isoDayNumber
                when {
                    (this.done==false) -> true
                    todayDow > targetDow -> true                              // this week's slot already passed
                    todayDow < targetDow -> false                             // slot is later this week
                    else                 -> now.time > baseTime               // today is the slot: overdue if time passed
                }
            }

            // True bi-weekly: active only on weeks whose parity matches the base week,
            // and within active weeks behaves like WEEKLY.
            Frequency.BI_WEEKLY -> {
                val onCycle = isSameParityWeek(base.date, now.date)
                if (!onCycle) false else {
                    val targetDow = base.date.dayOfWeek.isoDayNumber
                    val todayDow  = now.date.dayOfWeek.isoDayNumber
                    when {
                        (this.done==false) -> true
                        todayDow > targetDow -> true
                        todayDow < targetDow -> false
                        else                 -> now.time > baseTime
                    }
                }
            }

            // Occurs each month on base.date.dayOfMonth at baseTime.
            // If current month has fewer days (e.g., 31st), we clamp to the month's last day.
            Frequency.MONTHLY -> {
                val targetDay = clampDayOfMonth(now.date.year, now.date.monthNumber, base.date.dayOfMonth)
                when {
                    now.date.dayOfMonth > targetDay -> true
                    now.date.dayOfMonth < targetDay -> false
                    else                            -> now.time > baseTime
                }
            }

            // Occurs each year on base.date.month/day at baseTime.
            // If day doesn't exist in this year (e.g., Feb 29), we clamp to last valid day of that month.
            Frequency.YEARLY -> {
                val (m, d) = base.date.monthNumber to base.date.dayOfMonth
                val targetDay = clampDayOfMonth(now.date.year, m, d)
                when {
                    now.date.monthNumber > m -> true
                    now.date.monthNumber < m -> false
                    // same month
                    now.date.dayOfMonth > targetDay -> true
                    now.date.dayOfMonth < targetDay -> false
                    else                            -> now.time > baseTime
                }
            }

            // One-time: compare full timestamp
            Frequency.ONE_TIME -> base < now
        }
    }

    /* ---------- helpers ---------- */

    private fun isSameParityWeek(baseDate: LocalDate, currentDate: LocalDate): Boolean {
        val baseWeekStart    = startOfIsoWeek(baseDate)    // Monday
        val currentWeekStart = startOfIsoWeek(currentDate)
        val daysBetween = baseWeekStart.daysUntil(currentWeekStart)
        val weeksBetween = daysBetween.floorDiv(7)
        // Same parity => occurrences happen this week
        return weeksBetween % 2 == 0
    }

    private fun startOfIsoWeek(date: LocalDate): LocalDate {
        val offset = date.dayOfWeek.isoDayNumber - 1 // Monday=1 -> 0 offset
        return date - DatePeriod(days = offset)
    }

    private fun clampDayOfMonth(year: Int, month: Int, desiredDay: Int): Int {
        val lastDay = lastDayOfMonth(year, month)
        return desiredDay.coerceAtMost(lastDay)
    }

    private fun lastDayOfMonth(year: Int, month: Int): Int {
        val firstOfMonth = LocalDate(year, month, 1)
        val firstOfNext  = firstOfMonth + DatePeriod(months = 1)
        val lastOfMonth  = firstOfNext - DatePeriod(days = 1)
        return lastOfMonth.dayOfMonth
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

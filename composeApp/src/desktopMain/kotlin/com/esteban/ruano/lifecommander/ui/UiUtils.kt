import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ui.theme.*
import utils.DateUtils.getTime
import utils.DateUtils.parseDateTime
import utils.DateUtils.timeToIntPair
import utils.DateUtils.toLocalDateTime
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

fun getColorByDelay(delay: Int): Color {
    return when {
        delay < 0 -> SoftRed
        delay < 2 -> SoftYellow
        else -> SoftGreen
    }
}

fun getColorByPriority(priority: Int): Color {
    return when (priority) {
        4 -> SoftRed
        3 -> SoftBlue
        2 -> SoftYellow
        else -> LightGray
    }
}

fun getIconByPriority(priority: Int): ImageVector {
    return when {
        priority == 3 -> Icons.Default.KeyboardArrowUp
        priority > 3 -> Icons.Default.KeyboardArrowUp
        else -> Icons.Default.KeyboardArrowDown
    }
}

fun getDelayByTime(time: String): Int {
    val now = LocalDateTime.now()
    val time = timeToIntPair(time)
    return Duration.between(
        now, LocalDateTime.of(now.year, now.month, now.dayOfMonth, time.first, time.second)
    ).toHours().toInt()
}

object ResourceStrings {
    const val AT_DAY = "At %s"
    const val LAST_DAY = "Last %s"
    const val YESTERDAY = "Yesterday"
    const val TOMORROW = "Tomorrow"
    const val TODAY_AT = "Today at %s"
    const val EMPTY = ""

    val dayOfWeekStrings = mapOf(
        DayOfWeek.MONDAY to "Monday",
        DayOfWeek.TUESDAY to "Tuesday",
        DayOfWeek.WEDNESDAY to "Wednesday",
        DayOfWeek.THURSDAY to "Thursday",
        DayOfWeek.FRIDAY to "Friday",
        DayOfWeek.SATURDAY to "Saturday",
        DayOfWeek.SUNDAY to "Sunday"
    )
}

// Extension function for LocalDate
@Composable
fun LocalDateTime.toResourceStringBasedOnNow(): Pair<String, Color> {
    val now = LocalDateTime.now()

    if (this.minusDays(7).toLocalDate() > now.toLocalDate()) {
        return Pair(this.parseDateTime(), LightGray)
    }

    if (now.toLocalDate() == this.toLocalDate()) {
        return Pair(ResourceStrings.TODAY_AT.format(this.getTime()), SoftGreen)
    }

    if(now.minusDays(1).toLocalDate() == this.toLocalDate()) {
        return Pair(ResourceStrings.YESTERDAY, SoftRed)
    }

    if(now.plusDays(1).toLocalDate() == this.toLocalDate()) {
        return Pair(ResourceStrings.TOMORROW, SoftGreen)
    }

    if (now.toLocalDate() < this.toLocalDate()) {
        val first = ResourceStrings.AT_DAY.format(
            ResourceStrings.dayOfWeekStrings[this.dayOfWeek] ?: ResourceStrings.EMPTY
        )
        return Pair(first, LightGray)
    }

    if(this.toLocalDate() < now.toLocalDate() && now.minusDays(7).toLocalDate() < this.toLocalDate()) {
        val first = ResourceStrings.LAST_DAY.format(
            ResourceStrings.dayOfWeekStrings[this.dayOfWeek] ?: ResourceStrings.EMPTY
        )
        return Pair(first, SoftRed)
    }

    return Pair(this.parseDateTime(), SoftRed)
}
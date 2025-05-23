package com.esteban.ruano.utils

import com.esteban.ruano.database.entities.ScheduledTransactions
import com.esteban.ruano.database.entities.Transactions
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.ScheduledTransactionFilters
import com.esteban.ruano.lifecommander.models.finance.SortOrder as ModelSortOrder
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.finance.model.TransactionType
import com.lifecommander.models.Frequency
import io.ktor.server.routing.RoutingCall
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import java.util.UUID


fun Query.addSortOrder(sortOrder: ModelSortOrder, column: Column<*>,
                       defaultSortOrder : Pair<Column<*>, SortOrder>? = null): Query {
    return when (sortOrder) {
        ModelSortOrder.NONE -> defaultSortOrder?.let { this.orderBy(it) } ?: this
        ModelSortOrder.ASCENDING -> this.orderBy(column to SortOrder.ASC)
        ModelSortOrder.DESCENDING -> this.orderBy(column to SortOrder.DESC)
    }
}

fun Query.addSortOrder(sortOrder: ModelSortOrder, column: Expression<*>,
                       defaultSortOrder : Pair<Column<*>, SortOrder>? = null): Query {
    return when (sortOrder) {
        ModelSortOrder.NONE -> defaultSortOrder?.let { this.orderBy(it) } ?: this
        ModelSortOrder.ASCENDING -> this.orderBy(column to SortOrder.ASC)
        ModelSortOrder.DESCENDING -> this.orderBy(column to SortOrder.DESC)
    }
}

val absAmount = CustomFunction<Double>("ABS", DecimalColumnType(10, 2), Transactions.amount)

fun ModelSortOrder.toSortOrder(): SortOrder? {
    return when (this) {
        ModelSortOrder.NONE -> SortOrder.ASC
        ModelSortOrder.ASCENDING -> SortOrder.DESC
        else -> null
    }
}

fun generateOccurrencesBetween(
    startDate: LocalDate,
    frequency: Frequency,
    interval: Int,
    from: LocalDate,
    to: LocalDate
): List<LocalDate> {
    val result = mutableListOf<LocalDate>()
    var current = startDate

    while (current < from) {
        current = advance(current, frequency, interval)
    }

    while (current <= to) {
        result += current
        current = advance(current, frequency, interval)
    }

    return result
}

fun advance(base: LocalDate, frequency: Frequency, interval: Int): LocalDate = when (frequency) {
    Frequency.DAILY -> base.plus(DatePeriod(days = interval))
    Frequency.WEEKLY -> base.plus(DatePeriod(days = interval * 7))
    Frequency.MONTHLY -> base.plus(DatePeriod(months = interval))
    Frequency.YEARLY -> base.plus(DatePeriod(years = interval))
    Frequency.ONE_TIME -> base
    Frequency.BI_WEEKLY -> base.plus(DatePeriod(days = interval * 14))
}

fun TransactionFilters.toScheduledTransactionFilters(): ScheduledTransactionFilters {
    return ScheduledTransactionFilters(
        searchPattern = this.searchPattern,
        categories = this.categories,
        startDate = this.startDate,
        endDate = this.endDate,
        types = this.types,
        minAmount = this.minAmount,
        maxAmount = this.maxAmount,
        accountIds = this.accountIds,
    )
}
fun RoutingCall.gatherTransactionFilters(): TransactionFilters{
    val call = this
    val searchPattern = call.parameters["search"]
    val categories = call.parameters.getAll("category")
    val startDate = call.parameters["startDate"]
    val startDateHour = call.parameters["startDateHour"]
    val endDate = call.parameters["endDate"]
    val endDateHour = call.parameters["endDateHour"]
    val types = call.parameters.getAll("type")?.map { TransactionType.valueOf(it) }
    val minAmount = call.parameters["minAmount"]?.toDoubleOrNull()
    val maxAmount = call.parameters["maxAmount"]?.toDoubleOrNull()
    val accountIds = call.parameters.getAll("accountId")?.map { UUID.fromString(it) }
    val sortOrder = call.parameters["amountSortOrder"]?.lowercase().let {
        when (it) {
            "descending" -> ModelSortOrder.DESCENDING
            "ascending" -> ModelSortOrder.ASCENDING
            else -> ModelSortOrder.NONE
        }
    }

    return TransactionFilters(
        searchPattern = searchPattern,
        categories = categories,
        startDate = startDate,
        startDateHour = startDateHour,
        endDate = endDate,
        endDateHour = endDateHour,
        types = types,
        minAmount = minAmount,
        maxAmount = maxAmount,
        accountIds = accountIds?.map { it.toString() },
        amountSortOrder = sortOrder
    )

}

fun RoutingCall.gatherScheduledTransactionFilters(): ScheduledTransactionFilters {
    val call = this
    val searchPattern = call.parameters["search"]
    val categories = call.parameters.getAll("category")
    val startDate = call.parameters["startDate"]
    val endDate = call.parameters["endDate"]
    val types = call.parameters.getAll("type")?.map { TransactionType.valueOf(it) }
    val minAmount = call.parameters["minAmount"]?.toDoubleOrNull()
    val maxAmount = call.parameters["maxAmount"]?.toDoubleOrNull()
    val accountIds = call.parameters.getAll("accountId")?.map { UUID.fromString(it) }
    val frequencies = call.parameters.getAll("frequency")
    val applyAutomatically = call.parameters["applyAutomatically"]?.toBoolean()

    return ScheduledTransactionFilters(
        searchPattern = searchPattern,
        categories = categories,
        startDate = startDate,
        endDate = endDate,
        types = types,
        minAmount = minAmount,
        maxAmount = maxAmount,
        accountIds = accountIds?.map { it.toString() },
        frequencies = frequencies?.map { Frequency.valueOf(it) },
        applyAutomatically = applyAutomatically
    )
}

fun buildScheduledTransactionFilters(
    userId: Int,
    filters: ScheduledTransactionFilters
): Op<Boolean> {
    var conditions = (ScheduledTransactions.user eq userId) and (ScheduledTransactions.status eq Status.ACTIVE)

    filters.searchPattern?.let {
        conditions = conditions and (ScheduledTransactions.description like "%$it%")
    }

    filters.categories?.let {
        conditions = conditions and (ScheduledTransactions.category inList it)
    }

    filters.types?.let {
        conditions = conditions and (ScheduledTransactions.type inList it)
    }

    filters.accountIds?.let {
        conditions = conditions and (ScheduledTransactions.account inList it.map { UUID.fromString(it) })
    }

    filters.minAmount?.let {
        conditions = conditions and (ScheduledTransactions.amount greaterEq it.toBigDecimal())
    }

    filters.maxAmount?.let {
        conditions = conditions and (ScheduledTransactions.amount lessEq it.toBigDecimal())
    }

    filters.startDate?.let {
        conditions = conditions and (ScheduledTransactions.startDate greaterEq it.toLocalDateTime())
    }

    filters.endDate?.let {
        conditions = conditions and (ScheduledTransactions.startDate lessEq it.toLocalDateTime())
    }

    filters.frequencies?.let {
        conditions = conditions and (ScheduledTransactions.frequency inList it)
    }

    filters.applyAutomatically?.let {
        conditions = conditions and (ScheduledTransactions.applyAutomatically eq it)
    }

    return conditions
}

fun buildTransactionFilters(userId: Int, filters: TransactionFilters): Op<Boolean> {
    val conditions = mutableListOf<Op<Boolean>>()
    conditions += Transactions.user eq userId
    conditions += Transactions.status eq Status.ACTIVE

    filters.searchPattern?.let { pattern ->
        conditions += Transactions.description like "%$pattern%"
    }
    filters.categories?.let { categories ->
        conditions += Transactions.category inList categories
    }
    filters.startDate?.let { start ->
        val startDateTime = start.toLocalDate().atTime(filters.startDateHour?.toLocalTime() ?: LocalTime(0, 0))
        conditions += Transactions.date greaterEq startDateTime
    }
    filters.endDate?.let { end ->
        val endDateTime = end.toLocalDate().atTime(filters.endDateHour?.toLocalTime() ?: LocalTime(23, 59))
        conditions += Transactions.date lessEq endDateTime
    }
    filters.types?.let { types ->
        conditions += Transactions.type inList types
    }
    filters.minAmount?.let { min ->
        conditions += Transactions.amount greaterEq min.toBigDecimal()
    }
    filters.maxAmount?.let { max ->
        conditions += Transactions.amount lessEq max.toBigDecimal()
    }
    filters.accountIds?.let { accountIds ->
        conditions += Transactions.account inList accountIds.map { UUID.fromString(it) }
    }

    return conditions.fold(Op.TRUE as Op<Boolean>) { acc, op -> acc and op }
}

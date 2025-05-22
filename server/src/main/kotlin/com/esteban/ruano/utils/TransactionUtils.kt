package com.esteban.ruano.utils

import com.esteban.ruano.database.entities.Transactions
import com.esteban.ruano.lifecommander.models.finance.SortOrder as ModelSortOrder
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.lifecommander.finance.model.TransactionType
import io.ktor.server.routing.RoutingCall
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
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
fun buildTransactionFilters(userId: Int, filters: TransactionFilters): Op<Boolean> {
    val conditions = mutableListOf<Op<Boolean>>()
    conditions += Transactions.user eq userId

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

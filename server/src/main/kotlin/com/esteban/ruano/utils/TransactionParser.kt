package com.esteban.ruano.utils

import com.esteban.ruano.database.entities.CategoryKeyword
import com.esteban.ruano.database.entities.CategoryKeywords
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.Category.Companion.toCategory
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.math.absoluteValue
import kotlin.text.iterator

object TransactionParser {
    private val months = mapOf(
        "enero" to 1, "febrero" to 2, "marzo" to 3, "abril" to 4,
        "mayo" to 5, "junio" to 6, "julio" to 7, "agosto" to 8,
        "septiembre" to 9, "octubre" to 10, "noviembre" to 11, "diciembre" to 12
    )

    fun parseTransactions(userId:Int,text: String, accountId: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = text.trim().split("\n")

        var currentDescription = ""
        var currentDate = ""
        var currentAmount = ""

        for (line in lines) {
            when {
                line.isBlank() -> {
                    if (currentDescription.isNotBlank() && currentDate.isNotBlank() && currentAmount.isNotBlank()) {
                        transactions.add(createTransaction(userId,currentDescription, currentDate, currentAmount, accountId))
                        currentDescription = ""
                        currentDate = ""
                        currentAmount = ""
                    }
                }

                currentDescription.isBlank() -> currentDescription = line.trim()
                currentDate.isBlank() -> currentDate = line.trim()
                currentAmount.isBlank() -> currentAmount = line.trim()
            }
        }

        // Add the last transaction if exists
        if (currentDescription.isNotBlank() && currentDate.isNotBlank() && currentAmount.isNotBlank()) {
            transactions.add(createTransaction(userId,currentDescription, currentDate, currentAmount, accountId))
        }

        return transactions
    }

    private fun createTransaction(
        userId: Int,
        description: String,
        dateStr: String,
        amountStr: String,
        accountId: String
    ): Transaction {
        val parsedDate = parseSpanishDate(dateStr)
        return Transaction(
            description = description,
            date = DateUtils.formatDateTime(parsedDate),
            amount = parseColombianAmount(amountStr).absoluteValue,
            type = if (amountStr.trim().startsWith("-")) TransactionType.EXPENSE else TransactionType.INCOME,
            category = determineCategory(userId, description).toCategory(),
            accountId = accountId
        )
    }

    private fun parseSpanishDate(dateStr: String): LocalDateTime {
        val regex = """(\d{1,2})\s+de\s+(\w+),\s+(\d{4})""".toRegex()
        val match = regex.find(dateStr.lowercase())
            ?: throw IllegalArgumentException("Invalid date format: $dateStr")

        val (day, month, year) = match.destructured
        val monthNumber = months[month.lowercase()]
            ?: throw IllegalArgumentException("Invalid month: $month")

        return LocalDate(year.toInt(), monthNumber, day.toInt()).atTime(0, 0)
    }

    private fun formatDate(date: LocalDate): String {
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        val year = date.year.toString()
        return "$day/$month/$year"
    }

    private fun parseColombianAmount(amountStr: String): Double {
        val isNegative = amountStr.trim().startsWith("-")
        val cleanAmount = amountStr
            .replace("$", "")
            .replace(".", "")
            .replace(",", ".")
            .replace("-", "")
            .trim()

        return try {
            val amount = cleanAmount.toDouble()
            if (isNegative) -amount else amount
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid amount format: $amountStr")
        }
    }

    fun determineCategory(userId: Int, description: String): String {
        val lowerDescription = description.lowercase()

        return transaction {
            val keywords = CategoryKeyword.find {
                (CategoryKeywords.user eq userId) and
                        (CategoryKeywords.status eq Status.ACTIVE)
            }

            for (keyword in keywords) {
                if (lowerDescription.contains(keyword.keyword.lowercase())) {
                    return@transaction keyword.category.name
                }
            }

            Category.OTHER.name
        }
    }

}
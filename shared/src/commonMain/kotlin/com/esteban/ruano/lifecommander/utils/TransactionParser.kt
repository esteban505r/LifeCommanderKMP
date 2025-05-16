package com.esteban.ruano.utils

import com.lifecommander.finance.model.Category.Companion.toCategory
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionType
import kotlinx.datetime.LocalDate

object TransactionParser {
    private val months = mapOf(
        "enero" to 1, "febrero" to 2, "marzo" to 3, "abril" to 4,
        "mayo" to 5, "junio" to 6, "julio" to 7, "agosto" to 8,
        "septiembre" to 9, "octubre" to 10, "noviembre" to 11, "diciembre" to 12
    )

    fun parseTransactions(text: String, accountId: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = text.trim().split("\n")
        
        var currentDescription = ""
        var currentDate = ""
        var currentAmount = ""
        
        for (line in lines) {
            when {
                line.isBlank() -> {
                    if (currentDescription.isNotBlank() && currentDate.isNotBlank() && currentAmount.isNotBlank()) {
                        transactions.add(createTransaction(currentDescription, currentDate, currentAmount, accountId))
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
            transactions.add(createTransaction(currentDescription, currentDate, currentAmount, accountId))
        }
        
        return transactions
    }

    private fun createTransaction(description: String, dateStr: String, amountStr: String, accountId: String): Transaction {
        val parsedDate = parseSpanishDate(dateStr)
        return Transaction(
            description = description,
            date = formatDate(parsedDate),
            amount = parseColombianAmount(amountStr),
            type = if (amountStr.trim().startsWith("-")) TransactionType.EXPENSE else TransactionType.INCOME,
            category = determineCategory(description).toCategory(),
            accountId = accountId
        )
    }

    private fun parseSpanishDate(dateStr: String): LocalDate {
        val regex = """(\d{1,2})\s+de\s+(\w+),\s+(\d{4})""".toRegex()
        val match = regex.find(dateStr.lowercase())
            ?: throw IllegalArgumentException("Invalid date format: $dateStr")

        val (day, month, year) = match.destructured
        val monthNumber = months[month.lowercase()]
            ?: throw IllegalArgumentException("Invalid month: $month")

        return LocalDate(year.toInt(), monthNumber, day.toInt())
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

    private fun determineCategory(description: String): String {
        return when {
            description.contains("UBER", ignoreCase = true) -> "TRANSPORTATION"
            description.contains("PRIME VIDEO", ignoreCase = true) -> "ENTERTAINMENT"
            description.contains("TECNIPAGOS", ignoreCase = true) -> "SERVICES"
            description.contains("Cajero Automatico", ignoreCase = true) -> "ATM"
            description.contains("Devolucion", ignoreCase = true) -> "REFUND"
            description.contains("Abono", ignoreCase = true) -> "TRANSFER"
            else -> "OTHER"
        }
    }

} 
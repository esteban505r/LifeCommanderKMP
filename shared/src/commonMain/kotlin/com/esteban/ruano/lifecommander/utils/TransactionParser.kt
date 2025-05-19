package com.esteban.ruano.utils

import com.esteban.ruano.lifecommander.models.finance.Category.Companion.toCategory
import com.esteban.ruano.utils.DateUtils.formatDateTime
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

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
            date = formatDateTime(parsedDate),
            amount = parseColombianAmount(amountStr),
            type = if (amountStr.trim().startsWith("-")) TransactionType.EXPENSE else TransactionType.INCOME,
            category = determineCategory(description).toCategory(),
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

        return LocalDate(year.toInt(), monthNumber, day.toInt()).atTime(0,0)
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
            // Transport
            description.contains("UBER", ignoreCase = true) ||
                    description.contains("TRIP", ignoreCase = true) -> "TRANSPORTATION"

            // Subscriptions / Entertainment
            description.contains("PRIME VIDEO", ignoreCase = true) ||
                    description.contains("YouTubePrem", ignoreCase = true) ||
                    description.contains("Google One", ignoreCase = true) -> "BILLS"

            // Bills & Utilities
            description.contains("EMPRESA DE TELECOMUNICACION", ignoreCase = true) ||
                    description.contains("EMPRESA DE ACUEDUCTO", ignoreCase = true) ||
                    description.contains("COMUNICACION CELULAR", ignoreCase = true) -> "BILLS"

            // Food & Dining
            description.contains("HAMBURGUESAS", ignoreCase = true) ||
                    description.contains("RESTA", ignoreCase = true) ||
                    description.contains("BURRITO", ignoreCase = true) ||
                    description.contains("CAFE", ignoreCase = true) ||
                    description.contains("TRATTORIA", ignoreCase = true) ||
                    description.contains("FRUTIMAX", ignoreCase = true) ||
                    description.contains("RAPPI", ignoreCase = true) -> "ENJOYMENT"

            // Grocery & Essentials
            description.contains("CARULLA", ignoreCase = true) ||
                    description.contains("MERKA", ignoreCase = true) ||
                    description.contains("TIENDA D1", ignoreCase = true) ||
                    description.contains("DOLLARCITY", ignoreCase = true) -> "GROCERIES"

            // Books & Learning
            description.contains("LIBROS", ignoreCase = true) ||
                    description.contains("EDICIONES", ignoreCase = true) ||
                    description.contains("PANAMERICANA", ignoreCase = true) -> "EDUCATION"

            // Transfers and deposits
            description.contains("Abono", ignoreCase = true) ||
                    description.contains("Traslado", ignoreCase = true) ||
                    description.contains("Pago de No", ignoreCase = true) ||
                    description.contains("Reversion", ignoreCase = true) -> "TRANSFER"

            // Refunds
            description.contains("Devolucion", ignoreCase = true) -> "REFUND"

            // ATM
            description.contains("Cajero Automatico", ignoreCase = true) -> "ATM"

            // Financial / Bank Operations
            description.contains("BANCOLOMBIA", ignoreCase = true) -> "BANK"

            // Loan payments or charges
            description.contains("Load", ignoreCase = true) ||
                    description.contains("Compensar", ignoreCase = true) -> "DEBT"

            // Shopping
            description.contains("SOTHEN", ignoreCase = true) ||
                    description.contains("BEDAHE", ignoreCase = true) ||
                    description.contains("DOLLARCITY", ignoreCase = true) ||
                    description.contains("Validda", ignoreCase = true) -> "SHOPPING"

            // Services
            description.contains("TECNIPAGOS", ignoreCase = true) ||
                    description.contains("PAGOS", ignoreCase = true) -> "SERVICES"

            // Gaming
            description.contains("Riot Games", ignoreCase = true) -> "ENJOYMENT"

            else -> "OTHER"
        }
    }


} 
package com.esteban.ruano.repository

import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.models.finance.TransactionResponseDTO
import com.esteban.ruano.models.finance.TransactionsResponseDTO
import com.esteban.ruano.service.TransactionService
import com.lifecommander.finance.model.TransactionImportPreview
import com.lifecommander.finance.model.TransactionType
import java.util.*

class TransactionRepository(private val service: TransactionService) {
    fun create(userId: Int, amount: Double, description: String, date: String, type: TransactionType, category: String, accountId: UUID): UUID? =
        service.createTransaction(userId, amount, description, date, type, category, accountId)

    fun getAll(userId: Int, limit: Int = 100, offset: Int = 0, filters: TransactionFilters): TransactionsResponseDTO = service.getTransactionsByUser(userId, limit, offset, filters)

    fun getByAccount(accountId: UUID): List<TransactionResponseDTO> = service.getTransactionsByAccount(accountId)

    fun getByDateRange(userId: Int, startDate: String, endDate: String): List<TransactionResponseDTO> =
        service.getTransactionsByDateRange(userId, startDate, endDate)

    fun update(userId: Int, transactionId: UUID, amount: Double?, description: String?, date: String?, type: TransactionType?, category: String?): Boolean =
        service.updateTransaction(transactionId, userId, amount, description, date, type, category)

    fun delete(userId: Int, transactionId: UUID): Boolean = service.deleteTransaction(transactionId, userId)
    fun importTransactionsFromText(userId: Int, text: String, accountId: String): List<UUID> {
        return service.importTransactionsFromText(userId, text, accountId)
    }

    fun importPreview(userId: Int, text: String,accountId: String): TransactionImportPreview {
        return service.previewTransactionImport(userId, text, accountId)
    }

} 
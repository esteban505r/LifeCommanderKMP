package com.esteban.ruano.repository

import com.esteban.ruano.database.models.AccountType
import com.esteban.ruano.models.finance.AccountResponseDTO
import com.esteban.ruano.service.AccountService
import java.util.UUID

class AccountRepository(private val service: AccountService) {
    fun create(userId: Int, name: String, type: AccountType, initialBalance: Double, currency: String = "USD"): UUID? =
        service.createAccount(userId, name, type, initialBalance, currency)

    fun getAll(userId: Int): List<AccountResponseDTO> = service.getAccountsByUser(userId)

    fun getById(userId: Int, accountId: UUID): AccountResponseDTO? = service.getAccount(accountId, userId)

    fun update(userId: Int, accountId: UUID, name: String?, type: AccountType?, currency: String?): Boolean =
        service.updateAccount(accountId, userId, name, type, currency)

    fun delete(userId: Int, accountId: UUID): Boolean = service.deleteAccount(accountId, userId)

    fun updateBalance(userId: Int, accountId: UUID, amount: Double): Boolean = service.updateBalance(accountId, userId, amount)

    fun getTotalBalance(userId: Int): Double = service.getTotalBalance(userId)
} 
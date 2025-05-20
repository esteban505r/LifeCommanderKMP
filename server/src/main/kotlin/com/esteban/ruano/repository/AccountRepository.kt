package com.esteban.ruano.repository

import com.esteban.ruano.models.finance.AccountResponseDTO
import com.esteban.ruano.service.AccountService
import com.lifecommander.finance.model.AccountType
import java.util.UUID

class AccountRepository(private val service: AccountService) {
    fun create(userId: Int, name: String, type: com.lifecommander.finance.model.AccountType, initialBalance: Double, currency: String = "USD"): UUID? =
        service.createAccount(userId, name, type, initialBalance, currency)

    fun getAll(userId: Int): List<AccountResponseDTO> = service.getAccountsByUser(userId)

    fun getById(userId: Int, accountId: UUID): AccountResponseDTO? = service.getAccount(accountId, userId)

    fun update(userId: Int, accountId: UUID, name: String?, initialBalance:Double?, type: AccountType?, currency: String?): Boolean =
        service.updateAccount(accountId, userId, name, initialBalance,type, currency)

    fun delete(userId: Int, accountId: UUID): Boolean = service.deleteAccount(accountId, userId)


    fun getTotalBalance(userId: Int): Double = service.getTotalBalance(userId)
} 
package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.AccountType
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.finance.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class AccountService : BaseService() {
    fun createAccount(
        userId: Int,
        name: String,
        type: AccountType,
        initialBalance: Double,
        currency: String = "USD"
    ): UUID? {
        return transaction {
            Accounts.insertOperation(userId) {
                insert {
                    it[this.name] = name
                    it[this.type] = type
                    it[this.balance] = initialBalance.toBigDecimal()
                    it[this.currency] = currency
                    it[this.user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getAccountsByUser(userId: Int): List<AccountResponseDTO> {
        return transaction {
            Account.find { Accounts.user eq userId }
                .map { it.toResponseDTO() }
        }
    }

    fun getAccount(accountId: UUID, userId: Int): AccountResponseDTO? {
        return transaction {
            Account.find { (Accounts.id eq accountId) and (Accounts.user eq userId) }
                .firstOrNull()
                ?.toResponseDTO()
        }
    }

    fun updateAccount(
        accountId: UUID,
        userId: Int,
        name: String? = null,
        type: AccountType? = null,
        currency: String? = null
    ): Boolean {
        return transaction {
            val account = Account.findById(accountId)
            if (account != null && account.user.id.value == userId) {
                name?.let { account.name = it }
                type?.let { account.type = it }
                currency?.let { account.currency = it }
                true
            } else {
                false
            }
        }
    }

    fun deleteAccount(accountId: UUID, userId: Int): Boolean {
        return transaction {
            val account = Account.findById(accountId)
            if (account != null && account.user.id.value == userId) {
                account.status = Status.INACTIVE
                true
            } else {
                false
            }
        }
    }

    fun updateBalance(accountId: UUID, userId: Int, amount: Double): Boolean {
        return transaction {
            val account = Account.findById(accountId)
            if (account != null && account.user.id.value == userId) {
                account.balance = (account.balance.toDouble() + amount).toBigDecimal()
                true
            } else {
                false
            }
        }
    }

    fun getTotalBalance(userId: Int): Double {
        return transaction {
            Account.find { Accounts.user eq userId }
                .sumOf { it.balance.toDouble() }
        }
    }
} 
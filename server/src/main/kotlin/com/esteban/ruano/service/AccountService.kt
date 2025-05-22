package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.finance.*
import com.lifecommander.finance.model.TransactionType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class AccountService : BaseService() {
    fun createAccount(
        userId: Int,
        name: String,
        type: com.lifecommander.finance.model.AccountType,
        initialBalance: Double,
        currency: String = "USD"
    ): UUID? {
        return transaction {
            Accounts.insertOperation(userId) {
                insert {
                    it[this.name] = name
                    it[this.type] = type
                    it[this.initialBalance] = initialBalance.toBigDecimal()
                    it[this.currency] = currency
                    it[this.user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getAccountsByUser(userId: Int): List<AccountResponseDTO> {
        return transaction {
            val accounts = Account.find { Accounts.user eq userId }
                .map { it.toResponseDTO() }

            accounts.map {
                val balance = Transaction.find {
                    (Transactions.account eq UUID.fromString(it.id)) and (Transactions.user eq userId).
                    and (Transactions.status eq Status.ACTIVE)
                }.sumOf { r -> 
                    val value = r.amount.toDouble()
                    val valueConverted = if(r.type == TransactionType.EXPENSE) -value else value
                    valueConverted
                }

                it.copy(balance = balance + it.initialBalance)
            }

        }
    }

    fun getAccount(accountId: UUID, userId: Int): AccountResponseDTO? {
        return transaction {
            val balance = Transaction.find {
                (Transactions.account eq accountId) and (Transactions.user eq userId).
                and (Transactions.status eq Status.ACTIVE)
            }.sumOf { it.amount.toDouble() }

            val result = Account.find { (Accounts.id eq accountId) and (Accounts.user eq userId) }
                .firstOrNull()

            result?.toResponseDTO()?.copy(balance = balance.toBigDecimal().plus(result.initialBalance).toDouble())
        }
    }

    fun updateAccount(
        accountId: UUID,
        userId: Int,
        name: String? = null,
        initialBalance: Double? = null,
        type: com.lifecommander.finance.model.AccountType? = null,
        currency: String? = null
    ): Boolean {
        return transaction {
            val account = Account.findById(accountId)
            if (account != null && account.user.id.value == userId) {
                name?.let { account.name = it }
                initialBalance?.let { account.initialBalance = it.toBigDecimal() }
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
                account.initialBalance = (account.initialBalance.toDouble() + amount).toBigDecimal()
                true
            } else {
                false
            }
        }
    }

    fun getTotalBalance(userId: Int): Double {
        return transaction {
            Account.find { Accounts.user eq userId }
                .sumOf { it.initialBalance.toDouble() }
        }
    }
} 
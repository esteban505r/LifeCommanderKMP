package com.esteban.ruano.database.models

enum class AccountType(val value: String) {
    CHECKING("CHECKING"),
    SAVINGS("SAVINGS"),
    CREDIT_CARD("CREDIT_CARD"),
    CASH("CASH"),
    INVESTMENT("INVESTMENT")
} 
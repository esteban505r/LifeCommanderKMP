package com.esteban.ruano.database.models

enum class TransactionType(val value: String) {
    INCOME("INCOME"),
    EXPENSE("EXPENSE"),
    TRANSFER("TRANSFER")
} 
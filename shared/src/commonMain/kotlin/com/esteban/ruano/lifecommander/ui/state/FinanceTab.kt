package com.esteban.ruano.lifecommander.ui.state

enum class FinanceTab {
    ACCOUNTS,
    TRANSACTIONS,
    SCHEDULED,
    BUDGETS;

    companion object {
        fun fromIndex(index: Int): FinanceTab {
            return entries.toTypedArray().getOrElse(index) { ACCOUNTS }
        }

        fun fromName(name: String): FinanceTab {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: ACCOUNTS
        }
    }
}


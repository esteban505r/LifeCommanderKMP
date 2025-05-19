package com.esteban.ruano.lifecommander.models.finance;

import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    // Income categories
    SALARY,
    INVESTMENT,
    FREELANCE,
    GIFT,

    TRANSPORTATION,
    ENTERTAINMENT,
    BILLS,
    FOOD,
    GROCERIES,
    EDUCATION,
    TRANSFER,
    REFUND,
    ATM,
    BANK,
    DEBT,
    SHOPPING,
    SERVICES,
    OTHER,

    ENJOYMENT,
    SAVINGS,
    BUSINESS,

    HOUSING,
    UTILITIES,

    HEALTH,

    TRAVEL;

    companion object {
        fun String.toCategory(): Category {
            return when (this.uppercase()) {
                // Income
                "INCOME", "SALARY"        -> SALARY
                "FREELANCE"               -> FREELANCE
                "GIFT"                    -> GIFT
                "INVESTMENT"              -> INVESTMENT

                // Expenses
                "FOOD", "RESTAURANT"      -> FOOD
                "GROCERIES"               -> GROCERIES
                "SHOPPING", "CLOTHING"    -> SHOPPING
                "TRANSPORT", "TRANSPORTATION" -> TRANSPORTATION
                "HEALTH", "MEDICAL"       -> HEALTH
                "EDUCATION"               -> EDUCATION
                "TRAVEL"                  -> TRAVEL
                "ENTERTAINMENT"           -> ENTERTAINMENT
                "BILLS"                   -> BILLS
                "UTILITIES", "SERVICES"   -> UTILITIES
                "HOUSING", "RENT"         -> HOUSING
                "DEBT", "LOAN"            -> DEBT
                "ATM"                     -> ATM
                "BANK"                    -> BANK
                "REFUND"                  -> REFUND
                "TRANSFER"                -> TRANSFER
                "SAVINGS"                 -> SAVINGS
                "ENJOYMENT"               -> ENJOYMENT
                "BUSINESS"                -> BUSINESS

                else -> OTHER
            }
        }
    }
}
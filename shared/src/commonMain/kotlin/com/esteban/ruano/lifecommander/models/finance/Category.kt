package com.esteban.ruano.lifecommander.models.finance;

import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    // Income categories
    SALARY,
    INVESTMENT,
    FREELANCE,
    GIFT,
    
    // Expense categories
    FOOD,
    TRANSPORTATION,
    HOUSING,
    UTILITIES,
    ENTERTAINMENT,
    SHOPPING,
    HEALTH,
    EDUCATION,
    TRAVEL,
    OTHER;

    companion object{
        fun String.toCategory(): Category {
            return when (this) {
                "INCOME" -> SALARY
                "EXPENSE" -> FOOD
                "HOUSING" -> HOUSING
                "HEALTH" -> HEALTH
                "EDUCATION" -> EDUCATION
                "TRAVEL" -> TRAVEL
                "SHOPPING" -> SHOPPING
                "INVESTMENT" -> INVESTMENT
                "FREELANCE" -> FREELANCE
                "GIFT" -> GIFT
                "OTHER" -> OTHER
                "TRANSPORT" -> TRANSPORTATION
                "UTILITIES" -> UTILITIES
                "RENT" -> HOUSING
                "RESTAURANT" -> FOOD
                "GROCERIES" -> FOOD
                "CLOTHING" -> SHOPPING
                "MEDICAL" -> HEALTH
                "SALARY" -> SALARY
                "FOOD" -> FOOD
                "TRANSPORTATION" -> TRANSPORTATION
                "ENTERTAINMENT" -> ENTERTAINMENT
                "SERVICES" -> UTILITIES
                "ATM" -> TRANSPORTATION
                "REFUND" -> OTHER
                "TRANSFER" -> OTHER
                else -> OTHER
            }
        }
    }
}
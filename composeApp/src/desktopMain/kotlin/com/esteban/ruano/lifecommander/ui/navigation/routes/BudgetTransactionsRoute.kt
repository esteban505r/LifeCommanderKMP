package com.esteban.ruano.lifecommander.ui.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
data class BudgetTransactionsRoute(
    val budgetId: String
)
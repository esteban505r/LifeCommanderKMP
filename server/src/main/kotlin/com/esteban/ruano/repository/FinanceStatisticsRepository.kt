package com.esteban.ruano.repository

import com.esteban.ruano.lifecommander.models.FinanceStatisticsDTO
import com.esteban.ruano.service.FinanceStatisticsService

class FinanceStatisticsRepository(
    private val service: FinanceStatisticsService
) {
    fun getStatistics(userId: Int): FinanceStatisticsDTO {
        return service.getFinanceStatistics(userId)
    }
}


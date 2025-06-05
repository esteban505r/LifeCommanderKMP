package com.esteban.ruano.repository

import com.esteban.ruano.service.PortfolioService
import lopez.esteban.com.models.portfolio.CreatePortfolioDTO
import lopez.esteban.com.models.portfolio.PortfolioDTO
import lopez.esteban.com.models.portfolio.UpdatePortfolioDTO
import java.util.UUID

class PortfolioRepository(private val portfolioService: PortfolioService) {

    fun create(userId: Int, portfolio: CreatePortfolioDTO): UUID? {
        return portfolioService.create(userId, portfolio)
    }

    fun update(userId: Int, id: UUID, portfolio: UpdatePortfolioDTO): Boolean {
        return portfolioService.update(userId, id, portfolio)
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return portfolioService.delete(userId, id)
    }

    fun getByUserId(
        userId: Int,
        limit: Int = 10,
        offset: Long = 0,
        category: String? = null,
        featured: Boolean? = null
    ): List<PortfolioDTO> {
        return portfolioService.getByUserId(userId, limit, offset, category, featured)
    }

    fun getByIdAndUserId(id: UUID, userId: Int): PortfolioDTO? {
        return portfolioService.getByIdAndUserId(id, userId)
    }

    fun getAllPublic(
        limit: Int = 10,
        offset: Long = 0,
        category: String? = null,
        featured: Boolean? = null
    ): List<PortfolioDTO> {
        return portfolioService.getAllPublic(limit, offset, category, featured)
    }
}
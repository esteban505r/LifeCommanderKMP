package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Portfolio
import com.esteban.ruano.utils.formatDate
import com.esteban.ruano.utils.formatDateTime

import kotlinx.serialization.json.Json
import lopez.esteban.com.models.portfolio.CreatePortfolioDTO
import lopez.esteban.com.models.portfolio.PortfolioDTO
import lopez.esteban.com.models.portfolio.UpdatePortfolioDTO

fun Portfolio.toDTO(): PortfolioDTO {
    return PortfolioDTO(
        id = this.id.toString(),
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        projectUrl = this.projectUrl,
        githubUrl = this.githubUrl,
        technologies = this.technologies,
        category = this.category,
        featured = this.featured,
        startDate = this.startDate?.let { formatDate(it) },
        endDate = this.endDate?.let { formatDate(it) },
        createdAt = formatDateTime(this.createdDate),
        updatedAt = formatDateTime(this.updatedDate)
    )
}

fun PortfolioDTO.toCreatePortfolioDTO(): CreatePortfolioDTO {
    return CreatePortfolioDTO(
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        projectUrl = this.projectUrl,
        githubUrl = this.githubUrl,
        technologies = this.technologies,
        category = this.category,
        featured = this.featured,
        startDate = this.startDate,
        endDate = this.endDate,
        createdAt = this.createdAt
    )
}

fun PortfolioDTO.toUpdatePortfolioDTO(): UpdatePortfolioDTO {
    return UpdatePortfolioDTO(
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        projectUrl = this.projectUrl,
        githubUrl = this.githubUrl,
        technologies = this.technologies,
        category = this.category,
        featured = this.featured,
        startDate = this.startDate,
        endDate = this.endDate,
        updatedAt = this.updatedAt
    )
} 
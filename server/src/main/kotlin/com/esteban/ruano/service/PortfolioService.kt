package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Portfolio
import com.esteban.ruano.database.entities.Portfolios
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.utils.parseDate
import lopez.esteban.com.models.portfolio.CreatePortfolioDTO
import lopez.esteban.com.models.portfolio.PortfolioDTO
import lopez.esteban.com.models.portfolio.UpdatePortfolioDTO
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

class PortfolioService : BaseService() {

    fun create(userId: Int, portfolio: CreatePortfolioDTO): UUID? {
        return transaction {
            val id = Portfolios.insertOperation(userId) {
                insert {
                    it[title] = portfolio.title
                    it[description] = portfolio.description
                    it[imageUrl] = portfolio.imageUrl
                    it[projectUrl] = portfolio.projectUrl
                    it[extended_description] = portfolio.extendedDescription ?: ""
                    it[githubUrl] = portfolio.githubUrl
                    it[technologies] = portfolio.technologies
                    it[category] = portfolio.category
                    it[featured] = portfolio.featured
                    it[startDate] = portfolio.startDate?.let { parseDate(it) }
                    it[endDate] = portfolio.endDate?.let { parseDate(it) }
                    it[Portfolios.userId] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            id
        }
    }

    fun update(userId: Int, id: UUID, portfolio: UpdatePortfolioDTO): Boolean {
        return transaction {
            val updatedRow = Portfolios.updateOperation(userId) {
                val updatedRows = update({ (Portfolios.id eq id) and (Portfolios.userId eq userId) }) { row ->
                    portfolio.title?.let { row[title] = it }
                    portfolio.description?.let { row[description] = it }
                    portfolio.extendedDescription?.let { row[extended_description] = it }
                    portfolio.imageUrl?.let { row[imageUrl] = it }
                    portfolio.projectUrl?.let { row[projectUrl] = it }
                    portfolio.githubUrl?.let { row[githubUrl] = it }
                    portfolio.technologies?.let { row[technologies] = it }
                    portfolio.category?.let { row[category] = it }
                    portfolio.featured?.let { row[featured] = it }
                    portfolio.startDate?.let { row[startDate] = parseDate(it) }
                    portfolio.endDate?.let { row[endDate] = parseDate(it) }
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = Portfolios.deleteOperation(userId) {
                val updatedRows = Portfolios.update({ (Portfolios.id eq id) and (Portfolios.userId eq userId) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    fun getByUserId(userId: Int, limit: Int, offset: Long, category: String? = null, featured: Boolean? = null): List<PortfolioDTO> {
        return transaction {
            var query = (Portfolios.userId eq userId) and (Portfolios.status eq Status.ACTIVE)

            category?.let {
                query = query and (Portfolios.category eq it)
            }

            featured?.let {
                query = query and (Portfolios.featured eq it)
            }

            Portfolio.find { query }
                .orderBy(Portfolios.createdDate to SortOrder.DESC)
                .limit(limit).offset(offset*limit)
                .toList()
                .map { it.toDTO() }
        }
    }

    fun getByIdAndUserId(id: UUID, userId: Int): PortfolioDTO? {
        return transaction {
            Portfolio.find {
                (Portfolios.id eq id) and (Portfolios.userId eq userId) and (Portfolios.status eq Status.ACTIVE)
            }.firstOrNull()?.toDTO()
        }
    }

    fun getAllPublic(limit: Int, offset: Long, category: String? = null, featured: Boolean? = null): List<PortfolioDTO> {
        return transaction {
            var query = Portfolios.status eq Status.ACTIVE

            category?.let {
                query = query and (Portfolios.category eq it)
            }

            featured?.let {
                query = query and (Portfolios.featured eq it)
            }

            Portfolio.find { query }
                .orderBy(Portfolios.createdDate to SortOrder.DESC)
                .limit(limit).offset(offset*limit)
                .toList()
                .map { it.toDTO() }
        }
    }

    fun getPublicById(id: UUID): PortfolioDTO? {
        return transaction {
            Portfolio.find {
                (Portfolios.id eq id) and (Portfolios.status eq Status.ACTIVE)
            }.firstOrNull()?.toDTO()
        }
    }
}
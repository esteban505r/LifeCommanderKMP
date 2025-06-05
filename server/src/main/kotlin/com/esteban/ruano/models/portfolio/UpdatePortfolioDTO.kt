package lopez.esteban.com.models.portfolio

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePortfolioDTO(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val projectUrl: String? = null,
    val githubUrl: String? = null,
    val technologies: List<String>? = null,
    val category: String? = null,
    val featured: Boolean? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val updatedAt: String? = null
) 
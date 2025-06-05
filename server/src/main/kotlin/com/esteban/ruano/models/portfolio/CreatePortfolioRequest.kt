package lopez.esteban.com.models.portfolio

import kotlinx.serialization.Serializable

@Serializable
data class CreatePortfolioRequest(
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val projectUrl: String? = null,
    val githubUrl: String? = null,
    val technologies: List<String>,
    val category: String,
    val featured: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null
) 
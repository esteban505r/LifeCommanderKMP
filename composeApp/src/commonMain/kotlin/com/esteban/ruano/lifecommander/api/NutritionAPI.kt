import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.RecipeTrack
/*

interface NutritionAPI {
    suspend fun getRecipes(): List<Recipe>
    suspend fun getRecipe(id: String): Recipe
    suspend fun getRecipeTracks(userId: Int, date: String): List<RecipeTrack>
    suspend fun createRecipe(recipe: Recipe): Recipe
    suspend fun updateRecipe(recipe: Recipe): Recipe

    override suspend fun getRecipe(id: String): Recipe {
        return client.get("nutrition/recipe/$id").body()
    }

    override suspend fun getRecipeTracks(userId: Int, date: String): List<RecipeTrack> {
        return client.get("nutrition/tracks/$userId/$date").body()
    }

    override suspend fun createRecipe(recipe: Recipe): Recipe {
        return client.post("nutrition/recipe") {
            contentType(ContentType.Application.Json)
            setBody(recipe)
        }.body()
    }
} */

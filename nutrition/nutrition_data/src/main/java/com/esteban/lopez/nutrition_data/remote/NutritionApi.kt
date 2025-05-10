package com.esteban.ruano.nutrition_data.remote

import com.esteban.ruano.nutrition_data.remote.model.NutritionDashboardResponse
import com.esteban.ruano.nutrition_data.remote.model.RecipeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NutritionApi {

    @GET("nutrition/dashboard")
    suspend fun getDashboard(
        @Query("date") date: String
    ): NutritionDashboardResponse

    @GET("nutrition/recipes")
    suspend fun getRecipes(
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): List<RecipeResponse>

    @GET("nutrition/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): RecipeResponse

    @POST("nutrition/recipes")
    suspend fun addRecipe(
        @Body recipe: RecipeResponse
    )

    @DELETE("nutrition/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String)

    @PATCH("nutrition/recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: String,@Body recipe: RecipeResponse)

    @GET("nutrition/recipes/byDay/{day}")
    suspend fun getRecipesByDay(
        @Path("day") day: Int,
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): List<RecipeResponse>

}
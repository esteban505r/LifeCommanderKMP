package com.esteban.ruano.nutrition_domain.di


import com.esteban.ruano.nutrition_domain.repository.NutritionRepository
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository
import com.esteban.ruano.nutrition_domain.use_cases.AddRecipe
import com.esteban.ruano.nutrition_domain.use_cases.DeleteRecipe
import com.esteban.ruano.nutrition_domain.use_cases.GetDashboard
import com.esteban.ruano.nutrition_domain.use_cases.GetRecipe
import com.esteban.ruano.nutrition_domain.use_cases.GetRecipes
import com.esteban.ruano.nutrition_domain.use_cases.GetRecipesByDay
import com.esteban.ruano.nutrition_domain.use_cases.GetRecipesDatabase
import com.esteban.ruano.nutrition_domain.use_cases.NutritionUseCases
import com.esteban.ruano.nutrition_domain.use_cases.RecipeUseCases
import com.esteban.ruano.nutrition_domain.use_cases.UpdateRecipe
import com.esteban.ruano.nutrition_domain.use_cases.GetAllRecipes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object RecipesDomainModule {
    @ViewModelScoped
    @Provides
    fun provideRecipeUseCases(
        repository: RecipesRepository
    ): RecipeUseCases {
        return RecipeUseCases(
            getAll = GetAllRecipes(repository),
            getRecipe = GetRecipe(repository),
            addRecipe = AddRecipe(repository),
            updateRecipe = UpdateRecipe(repository),
            deleteRecipe = DeleteRecipe(repository),
            getByDay = GetRecipesByDay(repository),
            getDatabase = GetRecipesDatabase(repository)
        )
    }

    @ViewModelScoped
    @Provides
    fun provideNutritionUseCases(
        repository: NutritionRepository
    ): NutritionUseCases {
        return NutritionUseCases(
            getDashboard = GetDashboard(repository)
        )
    }
}
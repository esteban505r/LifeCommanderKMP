package com.esteban.ruano.nutrition_domain.di


import com.esteban.ruano.nutrition_domain.repository.NutritionRepository
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository
import com.esteban.ruano.nutrition_domain.use_cases.*
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
package com.esteban.ruano.nutrition_data.di

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core.utils.AppConstants
import com.esteban.ruano.core_data.models.Local
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.nutrition_data.datasources.NutritionDataSource
import com.esteban.ruano.nutrition_data.datasources.NutritionRemoteDataSource
import com.esteban.ruano.nutrition_data.datasources.RecipesDataSource
import com.esteban.ruano.nutrition_data.datasources.RecipesRemoteDataSource
import com.esteban.ruano.nutrition_data.remote.NutritionApi
import com.esteban.ruano.nutrition_data.repository.NutritionRepositoryImpl
import com.esteban.ruano.nutrition_data.repository.RecipesRepositoryImpl
import com.esteban.ruano.nutrition_domain.repository.NutritionRepository
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NutritionDataModule {
    @Provides
    @Singleton
    fun provideNutritionApi(client: OkHttpClient): NutritionApi {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create()
    }

    @Remote
    @Provides
    @Singleton
    fun provideRecipesDataSource(api: NutritionApi): RecipesDataSource {
        return RecipesRemoteDataSource(
            api = api
        )
    }

    @Remote
    @Provides
    @Singleton
    fun provideNutritionDataSource(api: NutritionApi): NutritionDataSource {
        return NutritionRemoteDataSource(
            api = api
        )
    }


    //TODO: Implement the local data source
    @Local
    @Provides
    @Singleton
    fun provideNutritionLocalDataSource(
        api: NutritionApi
    ): NutritionDataSource {
        return NutritionRemoteDataSource(
            api = api
        )
    }

    //TODO: Implement the local data source
    @Local
    @Provides
    @Singleton
    fun provideRecipeLocalDataSource(
        api: NutritionApi
    ): RecipesDataSource {
        return RecipesRemoteDataSource(
            api = api
        )
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        @Remote remoteDataSource: RecipesDataSource,
        @Local localDataSource: RecipesDataSource,
        networkHelper: NetworkHelper,
        preferences: Preferences,
    ): RecipesRepository {
        return RecipesRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            networkHelper = networkHelper,
            preferences = preferences,
        )
    }

    @Provides
    @Singleton
    fun provideNutritionRepository(
        @Remote remoteDataSource: NutritionDataSource,
        @Local localDataSource: NutritionDataSource,
        networkHelper: NetworkHelper,
        preferences: Preferences,
    ): NutritionRepository {
        return NutritionRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            networkHelper = networkHelper,
            preferences = preferences,
        )
    }

}
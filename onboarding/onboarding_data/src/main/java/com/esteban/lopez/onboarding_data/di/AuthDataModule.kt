package com.esteban.ruano.onboarding_data.di

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.lopez.core.utils.AppConstants
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.onboarding_data.datasources.AuthDataSource
import com.esteban.ruano.onboarding_data.datasources.AuthRemoteDataSource
import com.esteban.ruano.onboarding_data.repository.AuthRepositoryImpl
import com.esteban.ruano.onboarding_data.remote.AuthApi
import com.esteban.ruano.onboarding_domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import services.auth.AuthService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthDataModule {

    @Provides
    @Singleton
    fun provideTaskApi(client: OkHttpClient): AuthApi {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideService(client: HttpClient): AuthService {
        return AuthService(
            client,
        )
    }

    @Remote
    @Provides
    @Singleton
    fun provideHabitRemoteDataSource(api: AuthApi, service: AuthService): AuthDataSource {
        return AuthRemoteDataSource(
            authApi = api,
            authService = service

        )
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        @Remote remoteDataSource: AuthDataSource,
        networkHelper: NetworkHelper,
        preferences: Preferences
    ): AuthRepository {
        return AuthRepositoryImpl(
            remoteDataSource = remoteDataSource,
            networkHelper = networkHelper,
            preferences = preferences
        )
    }

}
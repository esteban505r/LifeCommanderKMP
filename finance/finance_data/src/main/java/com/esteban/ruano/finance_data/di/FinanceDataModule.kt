package com.esteban.ruano.finance_data.di

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.lopez.core.utils.AppConstants
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.finance_data.datasource.FinanceDataSource
import com.esteban.ruano.finance_data.datasource.FinanceRemoteDataSource
import com.esteban.ruano.finance_data.remote.FinanceApi
import com.esteban.ruano.finance_data.repository.FinanceRepositoryImpl
import com.esteban.ruano.finance_domain.repository.FinanceRepository
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
object FinanceDataModule {

    @Provides
    @Singleton
    fun provideTaskApi(client: OkHttpClient): FinanceApi {
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
    fun provideHabitRemoteDataSource(api: FinanceApi): FinanceDataSource {
        return FinanceRemoteDataSource(
            api = api
        )
    }


    @Provides
    @Singleton
    fun provideFinanceRepository(
        @Remote remoteDataSource: FinanceDataSource,
        networkHelper: NetworkHelper,
        preferences: Preferences

    ): FinanceRepository {
        return FinanceRepositoryImpl(
            dataSource = remoteDataSource,
            networkHelper = networkHelper,
            preferences = preferences
        )
    }


}
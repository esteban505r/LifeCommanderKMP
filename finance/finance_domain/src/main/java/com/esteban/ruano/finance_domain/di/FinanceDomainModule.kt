package com.esteban.ruano.finance_domain.di


import com.esteban.ruano.finance_domain.repository.FinanceRepository
import com.esteban.ruano.finance_domain.use_case.FinanceUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object FinanceDomainModule {

    @ViewModelScoped
    @Provides
    fun provideFinanceUseCases(
        repository: FinanceRepository
    ): FinanceUseCases {
        return FinanceUseCases(
            repository
        )
    }
}
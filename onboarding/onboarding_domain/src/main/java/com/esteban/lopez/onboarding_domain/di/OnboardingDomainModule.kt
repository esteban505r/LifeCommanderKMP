package com.esteban.ruano.onboarding_domain.di

import com.esteban.lopez.onboarding_domain.use_case.RequestReset
import com.esteban.lopez.onboarding_domain.use_case.SetNewPassword
import com.esteban.lopez.onboarding_domain.use_case.VerifyResetPin
import com.esteban.ruano.onboarding_domain.repository.AuthRepository
import com.esteban.ruano.onboarding_domain.use_case.AuthUseCases
import com.esteban.ruano.onboarding_domain.use_case.Login
import com.esteban.ruano.onboarding_domain.use_case.SignUp
import com.esteban.ruano.onboarding_domain.use_case.ValidateNutrients
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object OnboardingDomainModule {

    @Provides
    @ViewModelScoped
    fun provideValidateNutrientsUseCase(): ValidateNutrients {
        return ValidateNutrients()
    }

    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(
        repository: AuthRepository
    ): AuthUseCases {
        return AuthUseCases(
            login = Login(repository),
            signUp = SignUp(repository),
            requestReset = RequestReset(repository),
            verifyPin = VerifyResetPin(repository),
            setNewPassword = SetNewPassword(repository)
        )
    }
}
package com.esteban.ruano.onboarding_data.datasources
import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.local.addDeleteOperation
import com.esteban.ruano.core_data.local.addInsertOperation
import com.esteban.ruano.core_data.local.addUpdateOperation
import com.esteban.ruano.onboarding_domain.model.LoginModel

class AuthLocalDataSource(
) : AuthDataSource {
    override suspend fun login(email: String, password: String): LoginModel {
        throw NotImplementedError("Not yet implemented")
    }

    override suspend fun register(email: String, password: String, name: String) {
        throw NotImplementedError("Not yet implemented")
    }
}

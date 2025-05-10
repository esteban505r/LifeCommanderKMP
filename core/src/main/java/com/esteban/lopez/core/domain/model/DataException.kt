package com.esteban.ruano.core.domain.model

sealed class DataException: Throwable(),
    LifeCommanderException {
    sealed class Network: DataException() {
        data object RequestTimeout : DataException()
        data object ServerException : DataException()
        data object Unknown : DataException()
        data object NoInternet : DataException()
    }
    sealed class Local: DataException() {
        class DiskFull: DataException()
        class NotFound: DataException()
    }
}
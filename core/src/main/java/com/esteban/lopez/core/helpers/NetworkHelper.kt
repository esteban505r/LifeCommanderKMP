package com.esteban.ruano.core.helpers

import android.content.Context
import com.esteban.ruano.core.utils.ConnectionUtils

interface NetworkHelper {
    fun isNetworkAvailable(): Boolean
}
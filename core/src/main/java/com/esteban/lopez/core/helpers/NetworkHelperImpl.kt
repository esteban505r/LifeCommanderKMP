package com.esteban.ruano.core.helpers

import android.content.Context
import com.esteban.ruano.core.utils.ConnectionUtils

class NetworkHelperImpl(val context: Context) : NetworkHelper {
    override fun isNetworkAvailable(): Boolean {
        return ConnectionUtils.isNetworkAvailable(context)
    }
}
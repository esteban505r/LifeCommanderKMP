package com.esteban.ruano.habits_presentation.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.habits_domain.model.Frequency

object FrequencyUtils {

    @Composable
    fun getResourceByFrequency(frequency: Frequency): String {
        return when(frequency){
            Frequency.ONE_TIME -> stringResource(id = R.string.one_time)
            Frequency.DAILY -> stringResource(id = R.string.daily)
            Frequency.WEEKLY -> stringResource(id = R.string.weekly)
            Frequency.MONTHLY -> stringResource(id = R.string.monthly)
            Frequency.YEARLY -> stringResource(id = R.string.yearly)
            else -> ""
        }
    }
}
package com.esteban.ruano.core_ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.core.domain.model.LifeCommanderException
import com.esteban.ruano.core_ui.utils.DateUIUtils.parseDate
import com.esteban.ruano.core_ui.utils.DateUIUtils.timeToIntPair
import com.esteban.ruano.core_ui.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime


@Composable
fun DataException.getResourceStringByError():String{
    return when(this){
        is DataException.Network.RequestTimeout ->{
            stringResource(id = R.string.error_request_timeout)
        }
        is DataException.Network.ServerException ->{
            stringResource(id = R.string.error_server_error)
        }
        DataException.Network.NoInternet ->{
            stringResource(id = R.string.error_no_internet)
        }
        else ->{
            stringResource(id = R.string.error_unknown)
        }
    }
}

@Composable
fun LifeCommanderException.getResourceStringByError():String{
    return when(this){
        is DataException ->{
            getResourceStringByError()
        }
        else ->{
            stringResource(id = R.string.error_unknown)
        }
    }
}


fun getDelayByTime(time: String): Int {
    val now = LocalDateTime.now()
    val time = timeToIntPair(time)
    return Duration.between(
        now,LocalDateTime.of(now.year, now.month, now.dayOfMonth, time.first, time.second)
    ).toHours().toInt()
}

fun compareTimes(t1: String, t2: String): Int {
    val time1 = timeToIntPair(t1)
    val time2 = timeToIntPair(t2)
    return time1.first.compareTo(time2.first).let {
        if (it == 0) time1.second.compareTo(time2.second) else it
    }
}

@Composable
fun LocalDate.toResourceString():String{
    if(this == LocalDate.now()){
        return stringResource(id = R.string.today)
    }
    if(this == LocalDate.now().plusDays(1)){
        return stringResource(id = R.string.tomorrow)
    }
    return this.parseDate()
}

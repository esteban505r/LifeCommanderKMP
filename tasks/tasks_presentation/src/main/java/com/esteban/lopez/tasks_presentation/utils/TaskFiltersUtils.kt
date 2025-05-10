package com.esteban.ruano.tasks_presentation.utils

import com.esteban.ruano.tasks_domain.model.TaskFilters
import com.esteban.ruano.core_ui.R

fun TaskFilters.toResource(): Int{
   return when(this){
        TaskFilters.TODAY -> {
            R.string.today
        }
        TaskFilters.ALL -> {
            R.string.all
        }
        TaskFilters.TOMORROW -> {
            R.string.tomorrow
        }
        TaskFilters.NEXT_WEEK -> {
            R.string.next_week
        }
        TaskFilters.THIS_MONTH -> {
            R.string.this_month
        }
        TaskFilters.NO_DUE_DATE -> {
            R.string.no_due_date
        }
    }
}
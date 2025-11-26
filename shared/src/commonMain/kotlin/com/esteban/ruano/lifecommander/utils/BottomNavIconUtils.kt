package com.esteban.ruano.lifecommander.utils

import com.esteban.ruano.resources.Res
import com.esteban.ruano.resources.ic_finance_selected
import com.esteban.ruano.resources.ic_finance_unselected
import com.esteban.ruano.resources.ic_habits_selected
import com.esteban.ruano.resources.ic_habits_unselected
import com.esteban.ruano.resources.ic_home_selected
import com.esteban.ruano.resources.ic_home_unselected
import com.esteban.ruano.resources.ic_nutrition_selected
import com.esteban.ruano.resources.ic_nutrition_unselected
import com.esteban.ruano.resources.ic_tasks_selected
import com.esteban.ruano.resources.ic_tasks_unselected
import com.esteban.ruano.resources.ic_work_out_selected
import com.esteban.ruano.resources.ic_work_out_unselected
import org.jetbrains.compose.resources.DrawableResource

object BottomNavIconUtils {
    fun getResourceIconByString(icon: String, selected: Boolean): DrawableResource {
        return when (icon) {
            "home" -> if (selected) Res.drawable.ic_home_selected else Res.drawable.ic_home_unselected
            "habits" -> if (selected) Res.drawable.ic_habits_selected else Res.drawable.ic_habits_unselected
            "tasks" -> if (selected) Res.drawable.ic_tasks_selected else Res.drawable.ic_tasks_unselected
            "workout" -> if (selected) Res.drawable.ic_work_out_selected else Res.drawable.ic_work_out_unselected
            "nutrition" -> if (selected) Res.drawable.ic_nutrition_selected else Res.drawable.ic_nutrition_unselected
            "health" -> if (selected) Res.drawable.ic_work_out_selected else Res.drawable.ic_work_out_unselected // Use workout icon for health
            "timers" -> if (selected) Res.drawable.ic_home_selected else Res.drawable.ic_home_unselected // TODO: Add timer icons
            "finance" -> if (selected) Res.drawable.ic_finance_selected else Res.drawable.ic_finance_unselected
            "journal" -> if (selected) Res.drawable.ic_home_selected else Res.drawable.ic_home_unselected // TODO: Add journal icons
            "to_do" -> if (selected) Res.drawable.ic_habits_selected else Res.drawable.ic_habits_unselected
            else -> if (selected) Res.drawable.ic_home_selected else Res.drawable.ic_home_unselected
        }
    }
}

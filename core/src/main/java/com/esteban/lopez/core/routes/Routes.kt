package com.esteban.ruano.core.routes

object Routes {


    const val KEY_ROUTE = "androidx.navigation.compose.KEY_ROUTE"

    object Unauthenticated {
        val LOGIN = "login"
        val SIGN_UP = "signup"
        val FORGOT_PASSWORD = "forgot_password"
    }
    object BASE {
        val ROUTE = "base"
        val HOME = BottomNavRoute("home", "home", "Home")
        val CALENDAR = BottomNavRoute("calendar", "calendar", "Calendar")
        val TO_DO = BottomNavRoute("to_do", "to-do", "To-do")
        val HEALTH = BottomNavRoute("health", "health", "Health")
        val OTHERS = BottomNavRoute("others", "others", "Others")
        val WORKOUT = BottomNavRoute("workout", "workout", "Workout") // Keep for internal navigation
        val NUTRITION = BottomNavRoute("nutrition", "nutrition", "Nutrition") // Keep for internal navigation
        val TIMERS = BottomNavRoute("timers", "timers", "Timers")
        val FINANCE = BottomNavRoute("finance", "finance", "Finance")
        val JOURNAL = BottomNavRoute("journal", "journal", "Journal")
        val STUDY = BottomNavRoute("study", "study", "Study")
        fun getAllRoutes() = listOf(HOME, CALENDAR, TO_DO, HEALTH, OTHERS)
    }

    const val WELCOME = "welcome"

    val HABITS = BottomNavRoute("to_do/habits", "habits", "Habits")
    val TASKS = BottomNavRoute("to_do/tasks", "tasks", "Tasks")

    //Habits
    const val HABIT_DETAIL = "to_do/habits/habit_detail"
    const val NEW_EDIT_HABIT = "to_do/habits/new_habit"
    //Tasks
    const val TASK_DETAIL = "to_do/tasks/task_detail"
    const val NEW_EDIT_TASK = "to_do/tasks/new_task"
    const val TAGS_MANAGEMENT = "to_do/tasks/tags"
    //Journal
    const val JOURNAL_HISTORY = "journal/history"
    //Workout
    const val WORKOUT_DAY_DETAIL = "workout_detail"
    const val WORKOUT_PROGRESS = "workout_progress"
    const val WORKOUT_DAY_EXERCISES = "workout_exercises"
    const val EXERCISES = "exercises"
    const val EXERCISE_DETAIL = "exercise_detail"
    const val NEW_EDIT_EXERCISE = "new_exercise"
    const val ADD_EXERCISES_TO_WORKOUT_DAY = "add_exercises_to_workout_day"
    //Nutrition
    const val RECIPES = "recipes"
    const val NEW_EDIT_RECIPE = "new_recipe"
    const val RECIPE_DETAIL = "recipe_detail"
    const val AGE = "age"
    const val GENDER = "gender"
    const val HEIGHT = "height"
    const val WEIGHT = "weight"
    const val NUTRIENT_GOAL = "nutrient_goal"
    const val ACTIVITY = "activity"
    const val GOAL = "goal"

    const val TRACKER_OVERVIEW = "tracker_overview"
    const val SEARCH = "search"
    //Timers
    const val TIMER_LIST_DETAIL = "timers/list_detail"

    // Study (mobile)
    const val STUDY = "study"
}
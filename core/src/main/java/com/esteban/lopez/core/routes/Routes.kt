package com.esteban.ruano.core.routes

object Routes {

    const val KEY_ROUTE = "androidx.navigation.compose.KEY_ROUTE"

    object BASE {
        val ROUTE = "base"
        val HOME = BottomNavRoute("home", "home", "Home")
        val HABITS = BottomNavRoute("habits", "habits", "Habits")
        val TASKS = BottomNavRoute("tasks", "tasks", "Tasks")
        val WORKOUT = BottomNavRoute("workout", "workout", "Workout")
        val NUTRITION = BottomNavRoute("nutrition", "nutrition", "Nutrition")
        val FINANCE = BottomNavRoute("finance", "finance", "Finance")
        fun getAllRoutes() = listOf(HOME, HABITS, TASKS, WORKOUT, NUTRITION, FINANCE)
    }

    const val WELCOME = "welcome"

    //Habits
    const val HABIT_DETAIL = "habit_detail"
    const val NEW_EDIT_HABIT = "new_habit"
    //Tasks
    const val TASK_DETAIL = "task_detail"
    const val NEW_EDIT_TASK = "new_task"
    //Workout
    const val WORKOUT_DAY_DETAIL = "workout_detail"
    const val WORKOUT_PROGRESS = "workout_progress"
    const val WORKOUT_DAY_EXERCISES = "workout_exercises"
    const val EXERCISES = "exercises"
    const val EXERCISE_DETAIL = "exercise_detail"
    const val NEW_EXERCISE = "new_exercise"
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

}
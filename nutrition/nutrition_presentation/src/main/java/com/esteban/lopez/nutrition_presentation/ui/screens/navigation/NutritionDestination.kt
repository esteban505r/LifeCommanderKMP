import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.core_ui.utils.DateUIUtils.parseDate
import com.esteban.ruano.nutrition_presentation.intent.NutritionEffect
import com.esteban.ruano.nutrition_presentation.intent.NutritionIntent
import com.esteban.ruano.nutrition_presentation.intent.RecipesEffect
import com.esteban.ruano.nutrition_presentation.ui.screens.NutritionScreen
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.NutritionViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate


@Composable
fun NutritionDestination(
    viewModel: NutritionViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onRecipesClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()


    /**
     * Handles navigation based on [RecipesEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: NutritionEffect) {
        when (effect) {
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

    }

    LaunchedEffect(Unit) {
        viewModel.performAction(
            NutritionIntent.GetDashboard(
                date = LocalDate.now().parseDate()
            )
        )
    }

    when {
        state.isError -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        state.isLoading -> {
            Loading()
        }


        else -> {
            NutritionScreen(
                onNavigateUp = {
                    onNavigateUp()
                },
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state,
                onRecipeClick = {
                    onRecipeClick(it)
                },
                onRecipesClick = {
                    onRecipesClick()
                }
            )
        }
    }
}
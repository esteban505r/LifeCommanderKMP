import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.nutrition_presentation.intent.RecipesEffect
import com.esteban.ruano.nutrition_presentation.intent.RecipesIntent
import com.esteban.ruano.nutrition_presentation.ui.screens.RecipesScreen
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.RecipesViewModel
import kotlinx.coroutines.launch


@Composable
fun RecipesDestination(
    viewModel: RecipesViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNewRecipe: () -> Unit,
    onDetailRecipe: (String) -> Unit
) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()


    /**
     * Handles navigation based on [RecipesEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: RecipesEffect) {
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
            RecipesIntent.GetRecipes
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
            RecipesScreen(
                onNavigateUp = {
                    onNavigateUp()
                },
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state,
                onNewRecipe = {
                   onNewRecipe()
                },
                onDetailRecipe = {
                    onDetailRecipe(it)
                }
            )
        }
    }
}
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.finance.ui.components.BudgetTracker
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector
import com.esteban.ruano.lifecommander.ui.components.ExpandableFilterSection
import com.esteban.ruano.lifecommander.ui.components.FilterSidePanel
import com.lifecommander.ui.components.CustomDatePicker
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetScreenWrapper(
    budgets: List<BudgetProgress>,
    onLoadBudgets: () -> Unit,
    onAddBudget: (Budget) -> Unit,
    onEditBudget: (Budget) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
    onBudgetClick: (Budget) -> Unit,
    onFiltersChange: (BudgetFilters) -> Unit,
    filters: BudgetFilters = BudgetFilters()
) {
    var showFilters by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(filters.categories ?: emptyList()) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        Dialog(onDismissRequest = { showDatePicker = false }) {
            Surface {
                CustomDatePicker(
                    selectedDate = filters.startDate?.toLocalDate()
                        ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    onDateSelected = {
                        onFiltersChange(filters.copy(startDate = it.toString()))
                        showDatePicker = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onDismiss = { showDatePicker = false }
                )
            }
        }
    }

    if (showEndDatePicker) {
        Dialog(onDismissRequest = { showEndDatePicker = false }) {
            Surface {
                CustomDatePicker(
                    selectedDate = filters.endDate?.toLocalDate()
                        ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    onDateSelected = {
                        onFiltersChange(filters.copy(endDate = it.toString()))
                        showEndDatePicker = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onDismiss = { showEndDatePicker = false }
                )
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        BudgetTracker(
            budgets = budgets,
            onLoadBudgets = onLoadBudgets,
            onAddBudget = onAddBudget,
            onEditBudget = onEditBudget,
            onDeleteBudget = onDeleteBudget,
            onBudgetClick = onBudgetClick,
            onShowFilters = { showFilters = it },
        )

        FilterSidePanel(
            isVisible = showFilters,
            onDismiss = { showFilters = false },
            onClearFilters = { onFiltersChange(BudgetFilters()) },
            hasActiveFilters = filters != BudgetFilters()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = filters.searchPattern ?: "",
                    onValueChange = { onFiltersChange(filters.copy(searchPattern = it.takeIf { it.isNotBlank() })) },
                    label = { Text("Search Budget Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExpandableFilterSection(
                    title = "Categories",
                    summary = selectedCategories.joinToString(", ").ifEmpty { null }) {
                    EnumChipSelector(
                        enumValues = Category.entries.toTypedArray(),
                        selectedValues = selectedCategories.map { Category.valueOf(it) }.toSet(),
                        onValueSelected = {
                            selectedCategories = it.map { c -> c.name }
                            onFiltersChange(filters.copy(categories = selectedCategories.takeIf { it.isNotEmpty() }))
                        },
                        multiSelect = true,
                        labelMapper = { it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } }
                    )
                }

                ExpandableFilterSection(title = "Amount Range") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = filters.minAmount?.toString() ?: "",
                            onValueChange = { onFiltersChange(filters.copy(minAmount = it.toDoubleOrNull())) },
                            label = { Text("Min Amount") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = filters.maxAmount?.toString() ?: "",
                            onValueChange = { onFiltersChange(filters.copy(maxAmount = it.toDoubleOrNull())) },
                            label = { Text("Max Amount") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                ExpandableFilterSection(title = "Date Range") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(filters.startDate ?: "Start Date")
                        }
                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(filters.endDate ?: "End Date")
                        }
                    }
                }

                FilterChip(
                    selected = filters.isOverBudget == true,
                    onClick = {
                        onFiltersChange(filters.copy(isOverBudget = if (filters.isOverBudget == true) null else true))
                    },
                    content = { Text("Show Over Budget Only") }
                )
            }
        }
    }
}

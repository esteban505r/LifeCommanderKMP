import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.finance.ui.components.BudgetTracker
import com.esteban.ruano.lifecommander.models.finance.*
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector
import com.esteban.ruano.lifecommander.ui.components.ExpandableFilterSection
import com.esteban.ruano.lifecommander.ui.components.FilterSidePanel
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.ui.components.CustomDatePicker
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
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
    onChangeBaseDate: (LocalDate) -> Unit,
    baseDate: LocalDate?,
    filters: BudgetFilters = BudgetFilters(),
    onOpenCategoryKeywordMapper: () -> Unit,
    onCategorizeUnbudgeted: () -> Unit,
    onCategorizeAll: () -> Unit,
) {
    var showFilters by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(filters.categories ?: emptyList()) }
    var showToolsPanel by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 600.dp

        BudgetTracker(
            budgets = budgets,
            onLoadBudgets = onLoadBudgets,
            onAddBudget = onAddBudget,
            onEditBudget = onEditBudget,
            onDeleteBudget = onDeleteBudget,
            onBudgetClick = onBudgetClick,
            onShowFilters = { showFilters = it },
            onOpenCategoryKeywordMapper = { showToolsPanel = true },
            onCategorizeUnbudgeted = onCategorizeUnbudgeted,
            onCategorizeAll = onCategorizeAll,
            baseDate = baseDate ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date,
            filters = filters,
            onFiltersChange = onFiltersChange,
            onToggleDatePicker = { showDatePicker = it },
            isMobile = isMobile,
            onShowToolsPanel = { showToolsPanel = true }
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

                FilterChip(
                    selected = filters.isOverBudget == true,
                    onClick = {
                        onFiltersChange(filters.copy(isOverBudget = if (filters.isOverBudget == true) null else true))
                    },
                    content = { Text("Show Over Budget Only") }
                )
            }
        }

        if (isMobile) {
            if (showToolsPanel) {
                Dialog(onDismissRequest = { showToolsPanel = false }) {
                    Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                        ToolsPanelContent(
                            isMobile = isMobile,
                            onOpenCategoryKeywordMapper = onOpenCategoryKeywordMapper,
                            onCategorizeUnbudgeted = onCategorizeUnbudgeted,
                            onCategorizeAll = onCategorizeAll,
                            onClose = { showToolsPanel = false }
                        )
                    }
                }
            }
        } else {
            if(showToolsPanel){
                Surface(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd),
                    elevation = 4.dp,
                    color = MaterialTheme.colors.surface
                ) {
                    ToolsPanelContent(
                        isMobile = isMobile,
                        onOpenCategoryKeywordMapper = onOpenCategoryKeywordMapper,
                        onCategorizeUnbudgeted = onCategorizeUnbudgeted,
                        onCategorizeAll = onCategorizeAll,
                        onClose = {
                            showToolsPanel = false
                        }
                    )
                }
            }
        }

        if (showDatePicker) {
            Dialog(onDismissRequest = { showDatePicker = false }) {
                Surface {
                    CustomDatePicker(
                        selectedDate = baseDate ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date,
                        onDateSelected = {
                            onChangeBaseDate(it)
                            showDatePicker = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onDismiss = { showDatePicker = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolsPanelContent(
    isMobile: Boolean = false,
    onOpenCategoryKeywordMapper: () -> Unit,
    onCategorizeUnbudgeted: () -> Unit,
    onCategorizeAll: () -> Unit,
    onClose: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction Tools",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            if (!isMobile) {
                IconButton(onClick = { onClose?.invoke() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Tools Panel")
                }
            }
        }
        Button(
            onClick = onOpenCategoryKeywordMapper,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Category, contentDescription = "Category Keywords")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Category Keywords")
        }
        Button(
            onClick = onCategorizeUnbudgeted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = "Categorize Unbudgeted")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Categorize Unbudgeted")
        }
        Button(
            onClick = onCategorizeAll,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoFixNormal, contentDescription = "Categorize All")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Categorize All")
        }
        if (onClose != null && isMobile) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
            ) {
                Text("Close")
            }
        }
    }
}

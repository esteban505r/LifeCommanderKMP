import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.esteban.ruano.core_ui.theme.DarkGray
import com.esteban.ruano.core_ui.theme.SoftBlue
import com.esteban.ruano.core_ui.theme.SoftRed
import com.esteban.ruano.core_ui.theme.SoftYellow

object UiUtils{

    fun getColorByPriority(priority: Int): Color {
        return when (priority) {
            4 -> SoftRed
            3 -> SoftBlue
            2 -> SoftYellow
            else -> DarkGray
        }
    }

    fun getIconByPriority(priority: Int): ImageVector {
        return when {
            priority == 3 -> Icons.Default.KeyboardArrowUp
            priority > 3 -> Icons.Default.KeyboardArrowUp
            else -> Icons.Default.KeyboardArrowDown
        }
    }

}
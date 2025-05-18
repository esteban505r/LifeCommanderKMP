package ui.state

data class AppState(
   val isMinimized: Boolean = false,
   val isOpen: Boolean = true,
   val isDialogOpen: Boolean = false,
   val dialogMessage: String = "",
   val dialogTitle: String = "",
) {
}

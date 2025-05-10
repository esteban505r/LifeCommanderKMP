package services.tasks.models;

enum class Priority(
    val value: Int
) {
    URGENT(4),
    HIGH(3),
    MEDIUM(2),
    LOW(1),
    NONE(0);

    companion object {
        fun Int.toPriority(): Priority {
            return when (this) {
                4 -> URGENT
                3 -> HIGH
                2 -> MEDIUM
                1 -> LOW
                else -> NONE
            }
        }

    }
}
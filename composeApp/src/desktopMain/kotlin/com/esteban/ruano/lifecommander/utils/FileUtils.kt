import java.nio.file.Files

fun extractResourceToTempFile(resourcePath: String, extension: String): String {
    val inputStream = {}::class.java.getResourceAsStream(resourcePath)
        ?: throw IllegalArgumentException("Resource not found: $resourcePath")
    val tempFile = Files.createTempFile("tray_icon", extension)
    tempFile.toFile().deleteOnExit()
    inputStream.use { Files.copy(it, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING) }
    return tempFile.toAbsolutePath().toString()
}
import io.ktor.http.*

fun encodeUrlWithSpaces(url: String, parameters: Parameters): String {
    val encodedParameters = parameters.entries()
        .joinToString("&") { (key, values) ->
            values.joinToString("&") { value ->
                "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
            }
        }
    return "$url?$encodedParameters"
}

private fun String.encodeURLParameter(): String {
    return this.encodeURLPath().replace("+", "%20").replace("/", "%2F").replace(":","%3A")
}

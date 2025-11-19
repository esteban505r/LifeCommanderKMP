package com.esteban.ruano.utils

/**
 * Generates a URL-friendly slug from a name.
 * - Converts to lowercase
 * - Trims whitespace
 * - Replaces spaces and non-alphanumeric characters with hyphens
 * - Removes consecutive hyphens
 * - Removes leading/trailing hyphens
 */
fun generateSlug(name: String): String {
    return name
        .lowercase()
        .trim()
        .replace(Regex("[^a-z0-9]+"), "-")
        .replace(Regex("-+"), "-")
        .removePrefix("-")
        .removeSuffix("-")
}


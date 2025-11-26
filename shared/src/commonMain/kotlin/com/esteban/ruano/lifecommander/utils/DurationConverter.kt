package com.esteban.ruano.lifecommander.utils

/**
 * Centralized utility for converting timer duration between different units.
 * 
 * The database stores duration in MILLISECONDS for better accuracy.
 * This utility provides consistent conversion methods throughout the application.
 */
object DurationConverter {
    /**
     * Convert seconds to milliseconds
     */
    fun secondsToMillis(seconds: Long): Long = seconds * 1000L
    
    /**
     * Convert milliseconds to seconds (rounded down)
     */
    fun millisToSeconds(millis: Long): Long = millis / 1000L
    
    /**
     * Convert milliseconds to seconds (rounded)
     */
    fun millisToSecondsRounded(millis: Long): Long = (millis + 500) / 1000L
    
    /**
     * Parse hours, minutes, and seconds to milliseconds
     */
    fun toMillis(hours: Long, minutes: Long, seconds: Long): Long {
        return (hours * 3600 + minutes * 60 + seconds) * 1000L
    }
    
    /**
     * Parse hours, minutes, and seconds to seconds (for backward compatibility during migration)
     */
    fun toSeconds(hours: Long, minutes: Long, seconds: Long): Long {
        return hours * 3600 + minutes * 60 + seconds
    }
}


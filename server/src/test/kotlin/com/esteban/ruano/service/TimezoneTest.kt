package com.esteban.ruano.service

import kotlinx.datetime.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import kotlin.test.*
import kotlin.time.ExperimentalTime

class TimezoneTest {

    @Test
    fun `test America Bogota timezone casting`() {
        // Test the specific timezone mentioned in the request
        val bogotaTimezone = "America/Bogota"

        // Test successful casting
        val timeZone = TimeZone.of(bogotaTimezone)
        assertEquals("America/Bogota", timeZone.id)

        // Test that we can create a LocalDateTime with this timezone
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(timeZone)

        // Verify the timezone is properly applied
        assertNotNull(localDateTime)
        assertTrue(localDateTime.year > 2020) // Basic sanity check
    }

    @Test
    fun `test America Bogota timezone offset`() {
        val bogotaTimezone = TimeZone.of("America/Bogota")

        // Bogota is typically UTC-5 (EST) or UTC-4 (EDT) depending on daylight saving
        val offset = bogotaTimezone.offsetAt(Clock.System.now())

        // Bogota offset should be either -18000 (UTC-5) or -14400 (UTC-4)
        assertTrue(
            offset.totalSeconds.toLong() == -18000L || offset.totalSeconds.toLong() == -14400L,
            "Bogota timezone offset should be either -18000 (UTC-5) or -14400 (UTC-4), but was ${offset.totalSeconds}"
        )
    }

    @Test
    fun `test timezone casting with fallback to UTC`() {
        // Test invalid timezone string
        val invalidTimezone = "Invalid/Timezone"

        // This should throw an exception
        assertFailsWith<IllegalArgumentException> {
            TimeZone.of(invalidTimezone)
        }

        // Test null timezone (should default to UTC)
        val nullTimezone: String? = null
        val defaultTimezone = TimeZone.of(nullTimezone ?: "UTC")
        assertEquals("UTC", defaultTimezone.id) // TimeZone.of("UTC") returns "UTC"
    }

    @Test
    fun `test common timezone casting`() {
        val commonTimezones = listOf(
            "UTC", // TimeZone.of("UTC") returns "UTC"
            "America/New_York",
            "America/Los_Angeles",
            "Europe/London",
            "Europe/Paris",
            "Asia/Tokyo",
            "Australia/Sydney",
            "America/Bogota"
        )

        commonTimezones.forEach { timezoneString ->
            val timeZone = TimeZone.of(timezoneString)
            assertEquals(timezoneString, timeZone.id)

            // Test that we can create a LocalDateTime with this timezone
            val now = Clock.System.now()
            val localDateTime = now.toLocalDateTime(timeZone)
            assertNotNull(localDateTime)
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone conversion between different zones`() {
        val bogotaTimezone = TimeZone.of("America/Bogota")
        val utcTimezone = TimeZone.of("UTC") // TimeZone.of("UTC") returns "UTC"

        val now = Clock.System.now()
        val bogotaTime = now.toLocalDateTime(bogotaTimezone)
        val utcTime = now.toLocalDateTime(utcTimezone)

        // Convert Bogota time to UTC
        val bogotaToUtc = bogotaTime.toInstant(bogotaTimezone)
        val utcFromBogota = bogotaToUtc.toLocalDateTime(utcTimezone)

        // The converted time should be close to the original UTC time
        // Allow for a small difference due to the conversion process
        val timeDifference =
            kotlin.math.abs((utcTime.toInstant(utcTimezone) - utcFromBogota.toInstant(utcTimezone)).inWholeSeconds)
        assertTrue(timeDifference <= 1, "Time conversion difference should be minimal, but was $timeDifference seconds")
    }

    @Test
    fun `test timezone casting in try-catch block like in TimerCheckerService`() {
        val testCases = listOf(
            "America/Bogota" to "America/Bogota",
            "UTC" to "UTC", // TimeZone.of("UTC") returns "UTC"
            "Invalid/Timezone" to "UTC", // TimeZone.UTC returns "Z"
            null to "UTC" // TimeZone.of("UTC") returns "UTC"
        )

        testCases.forEach { (input, expected) ->
            val userTimeZone = try {
                TimeZone.of(input ?: "UTC")
            } catch (e: Exception) {
                TimeZone.UTC
            }
            
            assertEquals(expected, userTimeZone.id)
        }
    }

    @Test
    fun `test timezone offset consistency for America Bogota`() {
        val bogotaTimezone = TimeZone.of("America/Bogota")

        // Test offset at different times of the year
        val testDates = listOf(
            // January (typically EST - UTC-5)
            Instant.parse("2024-01-15T12:00:00Z"),
            // July (typically EDT - UTC-4, but Bogota doesn't observe DST)
            Instant.parse("2024-07-15T12:00:00Z"),
            // December (typically EST - UTC-5)
            Instant.parse("2024-12-15T12:00:00Z")
        )

        testDates.forEach { instant ->
            val offset = bogotaTimezone.offsetAt(instant)
            // Bogota is always UTC-5 (doesn't observe daylight saving time)
            assertEquals(-18000L,
                offset.totalSeconds.toLong(), "Bogota timezone should always be UTC-5, but was ${offset.totalSeconds} at $instant")
        }
    }

    @Test
    fun `test timezone string validation`() {
        // Valid timezone strings
        val validTimezones = listOf(
            "America/Bogota",
            "America/New_York",
            "UTC", // TimeZone.of("UTC") returns "UTC"
            "GMT",
            "Europe/London"
        )
        
        validTimezones.forEach { timezoneString ->
            assertDoesNotThrow {
                TimeZone.of(timezoneString)
            }
        }
        
        // Invalid timezone strings
        val invalidTimezones = listOf(
            "Invalid/Timezone",
            "Not/A/Timezone",
            "Random/Text",
            ""
        )
        
        invalidTimezones.forEach { timezoneString ->
            assertFailsWith<IllegalArgumentException> {
                TimeZone.of(timezoneString)
            }
        }
    }

    @Test
    fun `test timezone comparison and equality`() {
        val bogota1 = TimeZone.of("America/Bogota")
        val bogota2 = TimeZone.of("America/Bogota")
        val utc = TimeZone.of("UTC") // TimeZone.of("UTC") returns "UTC"
        
        // Same timezone should be equal
        assertEquals(bogota1, bogota2)
        assertEquals(bogota1.hashCode(), bogota2.hashCode())
        
        // Different timezones should not be equal
        assertNotEquals(bogota1, utc)
        assertNotEquals(bogota1.hashCode(), utc.hashCode())
    }

    @Test
    fun `test timezone with specific Bogota time`() {
        val bogotaTimezone = TimeZone.of("America/Bogota")

        // Test a specific time in Bogota
        val specificTime = Instant.parse("2024-01-15T12:00:00Z")
        val bogotaTime = specificTime.toLocalDateTime(bogotaTimezone)

        // Bogota is UTC-5, so 12:00 UTC should be 07:00 in Bogota
        assertEquals(7, bogotaTime.hour)
        assertEquals(0, bogotaTime.minute)
        assertEquals(15, bogotaTime.dayOfMonth)
        assertEquals(1, bogotaTime.monthNumber)
        assertEquals(2024, bogotaTime.year)
    }

    @Test
    fun `debug timezone behavior`() {
        // Test what actually happens with different inputs
        println("TimeZone.of(\"UTC\").id = ${TimeZone.of("UTC").id}")
        println("TimeZone.UTC.id = ${TimeZone.UTC.id}")
        
        // Test the exact pattern from the failing test
        val testInput = "Invalid/Timezone"
        val result = try {
            TimeZone.of(testInput)
        } catch (e: Exception) {
            TimeZone.UTC
        }
        println("Result for invalid timezone: ${result.id}")
        
        // Test null input
        val nullResult = try {
            TimeZone.of(null ?: "UTC")
        } catch (e: Exception) {
            TimeZone.UTC
        }
        println("Result for null timezone: ${nullResult.id}")
        
        // Test "UTC" input
        val utcResult = try {
            TimeZone.of("UTC")
        } catch (e: Exception) {
            TimeZone.UTC
        }
        println("Result for UTC timezone: ${utcResult.id}")
    }
} 
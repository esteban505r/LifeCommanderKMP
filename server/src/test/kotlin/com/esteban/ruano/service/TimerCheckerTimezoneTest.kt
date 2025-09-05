package com.esteban.ruano.service

import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class TimerCheckerTimezoneTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone casting pattern from TimerCheckerService with America Bogota`() {
        // This test mimics the exact pattern used in TimerCheckerService
        val userTimezone = "America/Bogota"

        val userTimeZone = try {
            TimeZone.of(userTimezone)
        } catch (e: Exception) {
            TimeZone.UTC
        }

        // Should successfully cast to America/Bogota
        assertEquals("America/Bogota", userTimeZone.id)

        // Test that we can create a LocalDateTime with this timezone
        val now = Clock.System.now()
        val currentTimeInUserTz = now.toLocalDateTime(userTimeZone)

        // Verify the timezone is properly applied
        assertNotNull(currentTimeInUserTz)
        assertTrue(currentTimeInUserTz.year > 2020)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone casting pattern with null timezone`() {
        // Test the pattern with null timezone (should default to UTC)
        val userTimezone: String? = null

        val userTimeZone = try {
            TimeZone.of(userTimezone ?: "UTC")
        } catch (e: Exception) {
            TimeZone.UTC
        }

        // Should default to UTC (which has ID "UTC" when using TimeZone.of("UTC"))
        assertEquals("UTC", userTimeZone.id)

        val now = Clock.System.now()
        val currentTimeInUserTz = now.toLocalDateTime(userTimeZone)
        assertNotNull(currentTimeInUserTz)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone casting pattern with invalid timezone`() {
        // Test the pattern with invalid timezone (should fallback to UTC)
        val userTimezone = "Invalid/Timezone"

        val userTimeZone = try {
            TimeZone.of(userTimezone)
        } catch (e: Exception) {
            TimeZone.UTC
        }

        // Should fallback to UTC (which has ID "Z" when using TimeZone.UTC)
        assertEquals("UTC", userTimeZone.id)

        val now = Clock.System.now()
        val currentTimeInUserTz = now.toLocalDateTime(userTimeZone)
        assertNotNull(currentTimeInUserTz)
    }

    @Test
    fun `test timezone casting pattern with empty string`() {
        // Test the pattern with empty string (should default to UTC)
        val userTimezone = ""

        val userTimeZone = try {
            TimeZone.of(userTimezone)
        } catch (e: Exception) {
            TimeZone.UTC
        }

        // Should fallback to UTC (which has ID "Z" when using TimeZone.UTC)
        assertEquals("UTC", userTimeZone.id)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone casting pattern with various valid timezones`() {
        val validTimezones = listOf(
            "America/Bogota",
            "America/New_York",
            "America/Los_Angeles",
            "Europe/London",
            "UTC", // TimeZone.of("UTC") returns "UTC"
            "GMT"
        )

        validTimezones.forEach { timezoneString ->
            val userTimeZone = try {
                TimeZone.of(timezoneString)
            } catch (e: Exception) {
                TimeZone.UTC
            }

            // Should successfully cast to the specified timezone
            assertEquals(timezoneString, userTimeZone.id)

            // Test that we can create a LocalDateTime
            val now = Clock.System.now()
            val currentTimeInUserTz = now.toLocalDateTime(userTimeZone)
            assertNotNull(currentTimeInUserTz)
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone conversion pattern used in TimerCheckerService`() {
        // Test the timezone conversion pattern used in the service
        val userTimezone = "America/Bogota"

        val userTimeZone = try {
            TimeZone.of(userTimezone)
        } catch (e: Exception) {
            TimeZone.UTC
        }

        val now = Clock.System.now()
        val currentTimeInUserTz = now.toLocalDateTime(userTimeZone)

        // Test that we can convert between timezones
        val utcTime = now.toLocalDateTime(TimeZone.of("UTC"))

        // Convert current time in user timezone to UTC
        val userTimeAsUtc = currentTimeInUserTz.toInstant(userTimeZone)
        val convertedUtcTime = userTimeAsUtc.toLocalDateTime(TimeZone.of("UTC"))

        // The times should be very close (within 1 second)
        val timeDifference = kotlin.math.abs((utcTime.toInstant(TimeZone.of("UTC")) - convertedUtcTime.toInstant(TimeZone.of("UTC"))).inWholeSeconds)
        assertTrue(timeDifference <= 1, "Time conversion should be accurate, difference was $timeDifference seconds")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone offset calculation for America Bogota`() {
        // Test that America/Bogota has the correct offset
        val userTimezone = "America/Bogota"

        val userTimeZone = try {
            TimeZone.of(userTimezone)
        } catch (e: Exception) {
            TimeZone.UTC
        }

        val now = Clock.System.now()
        val offset = userTimeZone.offsetAt(now)

        // Bogota is UTC-5 (doesn't observe daylight saving time)
        assertEquals(-18000L, offset.totalSeconds.toLong(),"America/Bogota should be UTC-5 (-18000 seconds), but was ${offset.totalSeconds}")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone casting in a loop like TimerCheckerService`() {
        // Simulate the pattern used in TimerCheckerService where multiple users are processed
        val userTimezones = listOf(
            "America/Bogota",
            "America/New_York",
            "UTC", // TimeZone.of("UTC") returns "UTC"
            "Invalid/Timezone",
            null
        )

        val results = mutableListOf<String>()

        for (userTimezone in userTimezones) {
            val userTimeZone = try {
                TimeZone.of(userTimezone ?: "UTC")
            } catch (e: Exception) {
                TimeZone.UTC
            }

            results.add(userTimeZone.id)

            // Test that we can create a LocalDateTime
            val now = Clock.System.now()
            val currentTimeInUserTz = now.toLocalDateTime(userTimeZone)
            assertNotNull(currentTimeInUserTz)
        }

        // Verify results
        assertEquals("America/Bogota", results[0])
        assertEquals("America/New_York", results[1])
        assertEquals("UTC", results[2]) // TimeZone.of("UTC") returns "UTC"
        assertEquals("UTC", results[3]) // Invalid timezone falls back to TimeZone.UTC (ID "Z")
        assertEquals("UTC", results[4]) // null timezone defaults to TimeZone.of("UTC") (ID "UTC")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test timezone casting with specific Bogota time conversion`() {
        // Test a specific time conversion scenario
        val userTimezone = "America/Bogota"

        val userTimeZone = try {
            TimeZone.of(userTimezone)
        } catch (e: Exception) {
            TimeZone.UTC
        }

        // Test a specific UTC time
        val utcInstant = Instant.parse("2024-01-15T12:00:00Z")
        val bogotaTime = utcInstant.toLocalDateTime(userTimeZone)

        // Bogota is UTC-5, so 12:00 UTC should be 07:00 in Bogota
        assertEquals(7, bogotaTime.hour)
        assertEquals(0, bogotaTime.minute)
        assertEquals(15, bogotaTime.dayOfMonth)

        // Convert back to UTC
        val bogotaInstant = bogotaTime.toInstant(userTimeZone)
        val backToUtc = bogotaInstant.toLocalDateTime(TimeZone.UTC)

        // Should be the same as the original UTC time
        assertEquals(12, backToUtc.hour)
        assertEquals(0, backToUtc.minute)
        assertEquals(15, backToUtc.dayOfMonth)
    }
}
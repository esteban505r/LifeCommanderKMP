package com.esteban.ruano.service

import com.esteban.ruano.BaseTest
import com.esteban.ruano.database.entities.Recipe
import com.esteban.ruano.database.entities.RecipeTrack
import com.esteban.ruano.database.entities.RecipeTracks
import com.esteban.ruano.database.models.MealTag
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.nutrition.CreateRecipeTrackDTO
import com.esteban.ruano.testModule
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTestRule
import org.koin.test.inject
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUI

class RecipeTrackServiceTest : BaseTest() {
    private val nutritionService: NutritionService by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(testModule)
    }

    @BeforeTest
    override fun setup() {
        super.setup()
    }

    @Test
    fun `test create recipe track`() {
        val recipeId = createTestRecipe()
        val consumedDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formatDefault().toLocalDateTimeUI()
        
        val recipeTrack = CreateRecipeTrackDTO(
            recipeId = recipeId.toString(),
            consumedDateTime = consumedDateTime.formatDefault()
        )
        
        val trackId = nutritionService.trackRecipeConsumption(userId, recipeTrack)
        assertNotNull(trackId)
        
        transaction {
            val track = RecipeTrack.find { RecipeTracks.id eq trackId }.firstOrNull()
            assertNotNull(track)
            assertEquals(recipeId, track.recipe.id.value)
            assertEquals(consumedDateTime, track.consumedDateTime)
            assertEquals(Status.ACTIVE, track.status)
        }
    }

    @Test
    fun `test get recipes consumed per day this week`() {
        // Create recipes and consume them on different days
        val recipe1 = createTestRecipe()
        val recipe2 = createTestRecipe()
        val recipe3 = createTestRecipe()
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()

        val todayDate = now.toLocalDateTime(timeZone).date

        val daysToSubtract = todayDate.dayOfWeek.ordinal

        val mondayDate = todayDate.minus(daysToSubtract, DateTimeUnit.DAY)

        val mondayInstant = mondayDate.atStartOfDayIn(timeZone)

        val monday = mondayInstant.toLocalDateTime(timeZone)
        
        // Consume recipes on Monday, Wednesday, and Friday
        val track1 = CreateRecipeTrackDTO(
            recipeId = recipe1.toString(),
            consumedDateTime = monday.date.atTime(10, 0).formatDefault()
        )
        val track2 = CreateRecipeTrackDTO(
            recipeId = recipe2.toString(),
            consumedDateTime = monday.date.plus(2, DateTimeUnit.DAY).atTime(10, 0).formatDefault()
        )
        val track3 = CreateRecipeTrackDTO(
            recipeId = recipe3.toString(),
            consumedDateTime = monday.date.plus(4, DateTimeUnit.DAY).atTime(10, 0).formatDefault()
        )
        
        nutritionService.trackRecipeConsumption(userId, track1)
        nutritionService.trackRecipeConsumption(userId, track2)
        nutritionService.trackRecipeConsumption(userId, track3)
        
        val weekStart = monday.formatDefault()
        val weekEnd = mondayInstant.plus(6.days).toLocalDateTime(
            timeZone
        ).formatDefault()
        val tracks = nutritionService.getRecipeTracksByDateRange(userId, weekStart, weekEnd)
        
        assertEquals(3, tracks.size)
    }

    @Test
    fun `test get recipe tracks by recipe`() {
        val recipeId = createTestRecipe()
        val consumedDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // Consume recipe multiple times
        val track1 = CreateRecipeTrackDTO(
            recipeId = recipeId.toString(),
            consumedDateTime = consumedDateTime.formatDefault()
        )
        val track2 = CreateRecipeTrackDTO(
            recipeId = recipeId.toString(),
            consumedDateTime = consumedDateTime.toInstant(
                TimeZone.currentSystemDefault()
            ).plus(1.days).toLocalDateTime(
                TimeZone.currentSystemDefault()
            ).formatDefault()
        )
        
        nutritionService.trackRecipeConsumption(userId, track1)
        nutritionService.trackRecipeConsumption(userId, track2)
        
        val tracks = nutritionService.getRecipeTracksByRecipe(userId, recipeId.toString())
        
        assertEquals(2, tracks.size)
        tracks.forEach { track ->
            assertEquals(recipeId.toString(), track.recipeId)
        }
    }

    @Test
    fun `test delete recipe track`() {
        val recipeId = createTestRecipe()
        val consumedDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        val recipeTrack = CreateRecipeTrackDTO(
            recipeId = recipeId.toString(),
            consumedDateTime = consumedDateTime.formatDefault()
        )
        
        val trackId = nutritionService.trackRecipeConsumption(userId, recipeTrack)
        assertNotNull(trackId)
        
        // Delete track
        val success = nutritionService.deleteRecipeTrack(userId, trackId.toString())
        assertTrue(success)
        
        transaction {
            val track = RecipeTrack.find { RecipeTracks.id eq trackId }.firstOrNull()
            assertNotNull(track)
            assertEquals(Status.DELETED, track.status)
        }
    }

    @Test
    fun `test track recipe consumption with invalid recipe id`() {
        val invalidRecipeId = UUID.randomUUID()
        val consumedDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        val recipeTrack = CreateRecipeTrackDTO(
            recipeId = invalidRecipeId.toString(),
            consumedDateTime = consumedDateTime.formatDefault()
        )
        
        val trackId = nutritionService.trackRecipeConsumption(userId, recipeTrack)
        
        // Should return null for invalid recipe
        assertEquals(null, trackId)
    }

    @Test
    fun `test track recipe consumption with recipe from different user`() {
        val otherUserId = createTestUser()
        val recipeId = createTestRecipeForUser(otherUserId)
        val consumedDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        val recipeTrack = CreateRecipeTrackDTO(
            recipeId = recipeId.toString(),
            consumedDateTime = consumedDateTime.formatDefault()
        )
        
        val trackId = nutritionService.trackRecipeConsumption(userId, recipeTrack)
        
        // Should return null for recipe from different user
        assertEquals(null, trackId)
    }

    @Test
    fun `test get recipe tracks by date range with no tracks`() {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()

        // Convert now to LocalDateTime for weekday math
        val today = now.toLocalDateTime(timeZone)

        // Calculate week start by subtracting days from today
        val weekStartDate = today.date.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)

        // Convert to Instant at start of day
        val weekStart = weekStartDate.atStartOfDayIn(timeZone)

        // Add 6 days as Duration to get week end
        val weekEnd = weekStart.plus((6 * 24 * 60 * 60 * 1000L).milliseconds) // or use Kotlin Duration

        val tracks = nutritionService.getRecipeTracksByDateRange(
            userId,
            weekStart.toLocalDateTime(timeZone).formatDefault(),
            weekEnd.toLocalDateTime(timeZone).formatDefault()
        )

        assertEquals(0, tracks.size)
    }


    private fun createTestRecipe(): UUID {
        return transaction {
            val recipe = Recipe.new {
                user = com.esteban.ruano.database.entities.User[userId]
                name = "Test Recipe ${UUID.randomUUID()}"
                note = "This is a test recipe"
                protein = 25.0
                day = 1 // Monday
                mealTag = MealTag.BREAKFAST
            }
            recipe.id.value
        }
    }

    private fun createTestRecipeForUser(targetUserId: Int): UUID {
        return transaction {
            val recipe = Recipe.new {
                user = com.esteban.ruano.database.entities.User[targetUserId]
                name = "Test Recipe ${UUID.randomUUID()}"
                note = "This is a test recipe for different user"
                protein = 25.0
                day = 1 // Monday
                mealTag = MealTag.BREAKFAST
                status = Status.ACTIVE
            }
            recipe.id.value
        }
    }

    private fun createTestUser(): Int {
        return transaction {
            val user = com.esteban.ruano.database.entities.User.new {
                email = "test${UUID.randomUUID()}@example.com"
                password = "password123"
                name = "Test User ${UUID.randomUUID()}"
                status = Status.ACTIVE
            }
            user.id.value
        }
    }
}
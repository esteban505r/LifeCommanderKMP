package com.esteban.ruano.service.study

import com.esteban.ruano.BaseTest
import com.esteban.ruano.TestKoinModule
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.models.study.*
import com.esteban.ruano.service.StudyService
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudyServiceTest : BaseTest() {

    private val studyService: StudyService by inject()

    @BeforeTest
    override fun setup() {
        super.setup()
        startKoin {
            modules(TestKoinModule.testModule)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `test create and get topic`() {
        val dto = CreateStudyTopicDTO(
            name = "Software Engineering",
            description = "Study software engineering",
            discipline = "CS",
            isActive = true
        )

        val topicId = studyService.createTopic(userId, dto)
        assertNotNull(topicId)

        val topic = studyService.getTopicById(userId, topicId)
        assertNotNull(topic)
        assertEquals("Software Engineering", topic.name)
        assertEquals("CS", topic.discipline)
        assertTrue(topic.isActive)
    }

    @Test
    fun `test update topic`() {
        val userId = createTestUser()
        val topicId = studyService.createTopic(
            userId,
            CreateStudyTopicDTO(name = "Test Topic", isActive = true)
        )!!

        val updated = studyService.updateTopic(
            userId,
            topicId,
            UpdateStudyTopicDTO(name = "Updated Topic", isActive = false)
        )

        assertTrue(updated)
        val topic = studyService.getTopicById(userId, topicId)
        assertEquals("Updated Topic", topic?.name)
        assertEquals(false, topic?.isActive)
    }

    @Test
    fun `test delete topic`() {
        val userId = createTestUser()
        val topicId = studyService.createTopic(
            userId,
            CreateStudyTopicDTO(name = "Test Topic", isActive = true)
        )!!

        val deleted = studyService.deleteTopic(userId, topicId)
        assertTrue(deleted)

        val topic = studyService.getTopicById(userId, topicId)
        assertNull(topic) // Should be soft deleted
    }

    @Test
    fun `test create and get item`() {
        val userId = createTestUser()
        val topicId = studyService.createTopic(
            userId,
            CreateStudyTopicDTO(name = "Test Topic", isActive = true)
        )!!

        val itemDto = CreateStudyItemDTO(
            topicId = topicId.toString(),
            title = "Learn Kotlin",
            obsidianPath = "notes/kotlin.md",
            stage = "PENDING",
            progress = 0
        )

        val itemId = studyService.createItem(userId, itemDto)
        assertNotNull(itemId)

        val item = studyService.getItemById(userId, itemId)
        assertNotNull(item)
        assertEquals("Learn Kotlin", item.title)
        assertEquals("PENDING", item.stage)
        assertEquals(0, item.progress)
    }

    @Test
    fun `test update item progress`() {
        val userId = createTestUser()
        val itemId = studyService.createItem(
            userId,
            CreateStudyItemDTO(
                title = "Test Item",
                stage = "PENDING",
                progress = 0
            )
        )!!

        val updated = studyService.updateItem(
            userId,
            itemId,
            UpdateStudyItemDTO(stage = "IN_PROGRESS", progress = 50)
        )

        assertTrue(updated)
        val item = studyService.getItemById(userId, itemId)
        assertEquals("IN_PROGRESS", item?.stage)
        assertEquals(50, item?.progress)
    }

    @Test
    fun `test create and complete session`() {
        val userId = createTestUser()
        val topicId = studyService.createTopic(
            userId,
            CreateStudyTopicDTO(name = "Test Topic", isActive = true)
        )!!

        val sessionDto = CreateStudySessionDTO(
            topicId = topicId.toString(),
            mode = "INPUT",
            actualStart = getCurrentDateTime(TimeZone.currentSystemDefault()).toString()
        )

        val sessionId = studyService.createSession(userId, sessionDto)
        assertNotNull(sessionId)

        val now = getCurrentDateTime(TimeZone.currentSystemDefault())
        val completed = studyService.completeSession(
            userId,
            sessionId,
            now.toString(),
            "Completed reading chapter 1"
        )

        assertTrue(completed)
        val session = studyService.getSessionById(userId, sessionId)
        assertNotNull(session)
        assertNotNull(session.actualEnd)
        assertNotNull(session.durationMinutes)
    }

    @Test
    fun `test get all items by stage`() {
        val userId = createTestUser()
        
        studyService.createItem(userId, CreateStudyItemDTO(title = "Item 1", stage = "PENDING"))
        studyService.createItem(userId, CreateStudyItemDTO(title = "Item 2", stage = "PENDING"))
        studyService.createItem(userId, CreateStudyItemDTO(title = "Item 3", stage = "IN_PROGRESS"))

        val pendingItems = studyService.getAllItems(userId, stage = "PENDING")
        assertEquals(2, pendingItems.size)

        val inProgressItems = studyService.getAllItems(userId, stage = "IN_PROGRESS")
        assertEquals(1, inProgressItems.size)
    }

    @Test
    fun `test get stats`() {
        val userId = createTestUser()
        val topicId = studyService.createTopic(
            userId,
            CreateStudyTopicDTO(name = "Test Topic", isActive = true)
        )!!

        // Create sessions
        val session1 = studyService.createSession(
            userId,
            CreateStudySessionDTO(
                topicId = topicId.toString(),
                mode = "INPUT",
                actualStart = getCurrentDateTime(TimeZone.currentSystemDefault()).toString()
            )
        )!!

        val now = getCurrentDateTime(TimeZone.currentSystemDefault())
        studyService.completeSession(userId, session1, now.toString())

        val stats = studyService.getStats(userId)
        assertTrue(stats.totalTimeByMode.containsKey("INPUT"))
        assertTrue(stats.totalTimeByTopic.containsKey(topicId.toString()))
    }

}


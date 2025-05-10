package com.esteban.ruano.tasks_presentation.robot

import com.esteban.ruano.test_core.server.TestServerUtils.mockResponse
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.navigation.NavController
import com.esteban.ruano.test_core.base.TestTags
import com.esteban.ruano.test_core.base.UiTestUtils
import com.esteban.ruano.test_core.base.UiTestUtils.sleep
import com.esteban.ruano.test_core.robot.Robot
import com.esteban.ruano.test_core.server.TestFileUtils
import com.esteban.ruano.test_core.server.ServerBasedTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.net.HttpURLConnection


class TasksRobot(
    composeRule: ComposeContentTestRule,
    private val navController: NavController
) : Robot(
    composeRule = composeRule
) {

    val path = "tasks"
    fun ServerBasedTest.addTasksResponse() {
        addResponse("tasks/byDateRange.*", requestHandler = {
            mockResponse(
                TestFileUtils.readFile(
                    UiTestUtils.testContext,
                    "tasks.json"
                )
            )
        })
    }
    fun ServerBasedTest.addTaskDetailResponse() {
        addResponse("tasks/.*", requestHandler = ::tasksDetailHandler)
    }

    private val tasksIdRegex = Regex("tasks/(.*)")

    private fun tasksDetailHandler(request: RecordedRequest): MockResponse {

        val path = request.path ?: ""
        var responseBody = TestFileUtils.readFile(
            UiTestUtils.testContext,
            "tasksDetail.json"
        )

        val match = tasksIdRegex.find(path)
        if (match != null) {
            val tasksId = match.value
            if (tasksId == "1234") {
                responseBody = responseBody.replace("", tasksId)
                responseBody = responseBody.replace("", "150")
                responseBody = responseBody.replace("", "Pam Beesly")
            } else {
                return mockResponse("""{"error":"couldn't find tasksId"}""", HttpURLConnection.HTTP_INTERNAL_ERROR)
            }
            return mockResponse(responseBody)
        }
        return mockResponse("""{"error":"couldn't find tasksId"}""", HttpURLConnection.HTTP_INTERNAL_ERROR)
    }
    fun testTaskList() {
        composeRule.sleep(200)
        assertListCountEqualsByTestTag(TestTags.TASKS_LIST, TestTags.TASK_CHECKABLE_ITEM,3)
    }

    fun testTasksScreen(){
        assertScreenIsDisplayedByTag(TestTags.TASKS_SCREEN)
    }

}
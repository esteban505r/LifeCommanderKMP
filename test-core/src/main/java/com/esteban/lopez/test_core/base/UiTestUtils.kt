package com.esteban.ruano.test_core.base

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry

object UiTestUtils {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val testContext = InstrumentationRegistry.getInstrumentation().context


    @OptIn(ExperimentalTestApi::class)
    fun ComposeTestRule.sleep(
        timeoutMillis: Long
    ) {
        @Suppress("SwallowedException")
        try {
            waitUntilAtLeastOneExists(hasText("NeverFound!"), timeoutMillis = timeoutMillis)
        } catch (t: Throwable) {
            // swallow this exception
        }
    }
}
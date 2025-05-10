package com.esteban.ruano.test_core.robot

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals

abstract class Robot(val composeRule: ComposeContentTestRule) {
    fun clickTextButton(text: String) = composeRule.onNode(hasTextExactly(text)).performClick()
    fun clickTextButtonByTag(tag: String) = composeRule.onNode(hasTestTag(tag)).performClick()

    fun clickIconButton(description: String) = composeRule.onNode(
        hasContentDescription(description).and(
            hasClickAction()
        )
    ).performClick()
    fun goBack() = clickIconButton("Back button") // uses the same description in all app

    fun assertIconButton(description: String) =
        composeRule.onNode(hasContentDescription(description).and(hasClickAction())).assertExists()

    fun assertTextButton(text: String) = composeRule.onNode(hasText(text).and(hasClickAction())).assertExists()

    fun assertTextButtonWithIcon(text: String, description: String) = composeRule.onNode(
        hasText(text).and(hasClickAction()).and(
            hasAnySibling(hasClickAction().and(hasContentDescription(description)))
        )).assertExists()

    fun assertImage(description: String) =
        composeRule.onNode(hasContentDescription(description)).assertExists()
    
    fun assertText(text: String, ignoreCase: Boolean = false, substring: Boolean = false) =
        composeRule.onNode(hasText(text, ignoreCase = ignoreCase, substring = substring))
            .assertExists()

    fun assertDoesNotExistText(
        text: String, ignoreCase: Boolean = false, substring: Boolean = false
    ) = composeRule.onNode(hasText(text, ignoreCase = ignoreCase, substring = substring))
        .assertDoesNotExist()

    fun assertTextBesideImage(text: String, description: String) {
        composeRule.onNode(
            hasText(text).and(
                hasAnySibling(hasContentDescription(description))
            )
        ).assertExists()
    }

    fun assertExistsByTestTag(tag: String){
        composeRule.onNode(hasTestTag(tag)).assertExists()
    }

    fun assertListCountEqualsByTestTag(containerTag: String, itemTag: String, expectedSize: Int) {
        val containerNode = composeRule.onNode(hasTestTag(containerTag))
        val itemCount = containerNode.onChildren().filter(hasTestTag(itemTag)).fetchSemanticsNodes().size
        assertEquals(expectedSize, itemCount)
    }

    fun assertScreenIsDisplayedByTag(tag: String) = composeRule.onNodeWithTag(tag).assertIsDisplayed()

    /*@OptIn(ExperimentalTestApi::class)
    fun waitFor(matcher: SemanticsMatcher) = composeRule.waitUntilExactlyOneExists(matcher)*/
}
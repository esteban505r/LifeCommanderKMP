package com.esteban.ruano.home_presentation.robot

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.navigation.NavController
import com.esteban.ruano.test_core.base.TestTags


class HomeRobot(
    composeRule: ComposeContentTestRule,
    private val navController: NavController
) : com.esteban.ruano.test_core.robot.Robot(
    composeRule = composeRule
) {
    fun testHomeOffers() {

        /*composeRule.setContent {

            HomeScreenDestination(
                navController = navController,
                navEffect = flowOf()
            )
        }*/

        //assertExistsByTestTag(PRODUCT_CAROUSEL_TEST_TAG)
        //assertListCountEqualsByTestTag(PRODUCT_CAROUSEL_TEST_TAG, 6)
    }

    fun testHome(){
        assertScreenIsDisplayedByTag(TestTags.HOME_SCREEN_TEST_TAG)
    }

}
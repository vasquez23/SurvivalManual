package org.ligi.survivalmanual

import android.os.SystemClock
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.NavigationViewActions.navigateTo
import android.support.test.espresso.matcher.ViewMatchers.*
import org.assertj.core.api.Assertions.assertThat

import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.ligi.survivalmanual.model.NavigationEntryMap
import org.ligi.survivalmanual.model.titleResByURLMap
import org.ligi.survivalmanual.ui.MainActivity
import org.ligi.trulesk.TruleskActivityRule
import org.ligi.trulesk.invokeMenu

class TheSurvivalActivityDirectStart {

    @get:Rule
    val activityTestRule = TruleskActivityRule(MainActivity::class.java)

    @Test
    fun thatActivityShouldLaunch() {
    }

    @Test
    fun thatThatHelpOpens() {
        invokeMenu(R.id.menu_help, R.string.introduction)

        onView(withText(R.string.help_title)).check(matches(isDisplayed()))
    }


    @Test
    fun testThatHelpContainsVersion() {
        thatThatHelpOpens()

        onView(withText(containsString(BuildConfig.VERSION_NAME))).check(matches(isDisplayed()))

        activityTestRule.screenShot("help")
    }

    /* TODO remove the need for the sleeps - but without the test was flaky */

    @Test
    fun testWeCanOpenAllTopics() {
        NavigationEntryMap.filter { it.entry.isListed }.forEach {
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
            SystemClock.sleep(500)
            onView(withId(R.id.navigationView)).perform(navigateTo(it.id))
            SystemClock.sleep(500)
            val activity = activityTestRule.activity
            val subtitle = activity.supportActionBar!!.subtitle
            assertThat(subtitle).isEqualTo(activity.getString(titleResByURLMap[it.entry.url]!!))

            activityTestRule.screenShot("topic_" + subtitle!!.toString().replace(" ", "_").replace("/", "_"))
        }

    }

}

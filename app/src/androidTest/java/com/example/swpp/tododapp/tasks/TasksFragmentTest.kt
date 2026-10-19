package com.example.swpp.tododapp.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.R
import com.example.swpp.tododapp.ServiceLocator
import com.example.swpp.tododapp.data.Task
import com.example.swpp.tododapp.data.source.FakeAndroidTestRepository
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Screen-level integration tests for the D-day label on the task list,
 * written from specs/TasksListBindings.spec.md.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class TasksFragmentTest {

    private lateinit var repository: FakeAndroidTestRepository

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun resetRepository() {
        ServiceLocator.resetRepository()
    }

    /** The fake repository publishes LiveData, which must be updated on the main thread. */
    private fun addTasks(vararg tasks: Task) =
        InstrumentationRegistry.getInstrumentation().runOnMainSync { repository.addTasks(*tasks) }

    private fun checkboxOf(title: String) =
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(title))))

    private fun labelOf(title: String) =
        onView(allOf(withId(R.id.due_date_text), hasSibling(withText(title))))

    /** "Shows no label": the text is empty or the view is not visible. */
    private fun noLabel(): Matcher<View> =
        anyOf(withText(""), not(withEffectiveVisibility(Visibility.VISIBLE)))

    /**
     * The list is diffed on a background thread that Espresso does not wait for,
     * so retry the check for a short while before failing.
     */
    private fun eventually(check: () -> Unit) {
        val deadline = System.currentTimeMillis() + 5_000
        while (true) {
            try {
                check()
                return
            } catch (e: Throwable) {
                if (e !is AssertionError && e !is NoMatchingViewException) throw e
                if (System.currentTimeMillis() > deadline) throw e
                Thread.sleep(100)
            }
        }
    }

    @Test
    fun dueDateLabel_checkThenUncheckWithoutDueDate_showsClearThenNoLabel() {
        // GIVEN an active task without a due date on the task list
        addTasks(Task(title = "No due", isCompleted = false, dueDate = null))
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        eventually { labelOf("No due").check(matches(noLabel())) }

        // WHEN the task is checked
        checkboxOf("No due").perform(click())
        // THEN the label is CLEAR
        eventually { labelOf("No due").check(matches(withText("CLEAR"))) }

        // WHEN the task is unchecked
        checkboxOf("No due").perform(click())
        // THEN CLEAR is removed and no label is shown
        eventually { labelOf("No due").check(matches(noLabel())) }
    }

    @Test
    fun dueDateLabel_checkThenUncheckDueInThreeDays_showsClearThenDMinus3() {
        // GIVEN an active task due in 3 days on the task list
        addTasks(Task(title = "Due soon", isCompleted = false, dueDate = LocalDate.now().plusDays(3)))
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        eventually { labelOf("Due soon").check(matches(withText("D-3"))) }

        // WHEN the task is checked
        checkboxOf("Due soon").perform(click())
        // THEN the label is CLEAR
        eventually { labelOf("Due soon").check(matches(withText("CLEAR"))) }

        // WHEN the task is unchecked
        checkboxOf("Due soon").perform(click())
        // THEN the label is D-3 again
        eventually { labelOf("Due soon").check(matches(withText("D-3"))) }
    }
}

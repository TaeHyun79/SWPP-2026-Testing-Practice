package com.example.swpp.tododapp.tasks

import android.view.View
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Integration tests for [setDueDateLabel], written from specs/TasksListBindings.spec.md.
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class TasksListBindingsTest {

    private lateinit var textView: TextView

    @Before
    fun createTextView() {
        onMain { textView = TextView(InstrumentationRegistry.getInstrumentation().targetContext) }
    }

    private fun onMain(block: () -> Unit) =
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)

    private fun bind(task: Task?) = onMain { setDueDateLabel(textView, task) }

    /** "Shows a label X": the text is X and the view is visible. */
    private fun assertShowsLabel(expected: String) {
        assertEquals(expected, textView.text.toString())
        assertEquals(View.VISIBLE, textView.visibility)
    }

    /** "Shows no label": the text is empty or the view is not visible. */
    private fun assertShowsNoLabel() {
        val text = textView.text.toString()
        assertTrue(
            "Expected no label, but \"$text\" is visible",
            text.isEmpty() || textView.visibility != View.VISIBLE
        )
    }

    private fun active(dueDate: LocalDate?, title: String = "Buy milk") =
        Task(title = title, isCompleted = false, dueDate = dueDate)

    private fun completed(dueDate: LocalDate?, title: String = "Buy milk") =
        Task(title = title, isCompleted = true, dueDate = dueDate)

    // ---- Success cases ----

    @Test
    fun setDueDateLabel_activeDueInThreeDays_showsDMinus3() {
        // GIVEN an active task due in 3 days
        val task = active(LocalDate.now().plusDays(3))
        // WHEN it is bound
        bind(task)
        // THEN the label is D-3
        assertShowsLabel("D-3")
    }

    @Test
    fun setDueDateLabel_activeOverdueByFiveDays_showsDPlus5() {
        // GIVEN an active task overdue by 5 days
        val task = active(LocalDate.now().minusDays(5))
        // WHEN it is bound
        bind(task)
        // THEN the label is D+5
        assertShowsLabel("D+5")
    }

    @Test
    fun setDueDateLabel_activeDueToday_showsDDay() {
        // GIVEN an active task due today
        val task = active(LocalDate.now())
        // WHEN it is bound
        bind(task)
        // THEN the label is D-Day
        assertShowsLabel("D-Day")
    }

    @Test
    fun setDueDateLabel_completedWithDueDate_showsClear() {
        // GIVEN a completed task with a due date
        val task = completed(LocalDate.now().plusDays(3))
        // WHEN it is bound
        bind(task)
        // THEN the label is CLEAR
        assertShowsLabel("CLEAR")
    }

    @Test
    fun setDueDateLabel_completedWithoutDueDate_showsClear() {
        // GIVEN a completed task without a due date
        val task = completed(null)
        // WHEN it is bound
        bind(task)
        // THEN the label is CLEAR
        assertShowsLabel("CLEAR")
    }

    @Test
    fun setDueDateLabel_activeWithoutDueDate_showsNoLabel() {
        // GIVEN an active task without a due date
        val task = active(null)
        // WHEN it is bound
        bind(task)
        // THEN no label is shown
        assertShowsNoLabel()
    }

    @Test
    fun setDueDateLabel_nullTask_showsNoLabel() {
        // GIVEN no task
        // WHEN null is bound
        bind(null)
        // THEN no label is shown and nothing is thrown
        assertShowsNoLabel()
    }

    // ---- Edge cases: D-Day boundary ----

    @Test
    fun setDueDateLabel_activeDueYesterday_showsDPlus1() {
        // GIVEN an active task due yesterday
        val task = active(LocalDate.now().minusDays(1))
        // WHEN it is bound
        bind(task)
        // THEN the label is D+1
        assertShowsLabel("D+1")
    }

    @Test
    fun setDueDateLabel_activeDueTomorrow_showsDMinus1() {
        // GIVEN an active task due tomorrow
        val task = active(LocalDate.now().plusDays(1))
        // WHEN it is bound
        bind(task)
        // THEN the label is D-1
        assertShowsLabel("D-1")
    }

    // ---- Edge cases: recycled views ----

    @Test
    fun setDueDateLabel_recycledViewActiveWithoutDueDate_showsNoLabel() {
        // GIVEN a view that shows D-3 for another task
        bind(active(LocalDate.now().plusDays(3)))
        assertShowsLabel("D-3")
        // WHEN it is rebound to an active task without a due date
        bind(active(null, title = "Walk dog"))
        // THEN the old label is cleared
        assertShowsNoLabel()
    }

    @Test
    fun setDueDateLabel_recycledViewNullTask_showsNoLabel() {
        // GIVEN a view that shows D-3
        bind(active(LocalDate.now().plusDays(3)))
        assertShowsLabel("D-3")
        // WHEN it is rebound to null
        bind(null)
        // THEN the old label is cleared
        assertShowsNoLabel()
    }

    @Test
    fun setDueDateLabel_recycledViewWithoutLabelThenDueInThreeDays_showsDMinus3() {
        // GIVEN a view bound to an active task without a due date
        bind(active(null, title = "Walk dog"))
        // WHEN it is rebound to an active task due in 3 days
        bind(active(LocalDate.now().plusDays(3)))
        // THEN the new label is shown
        assertShowsLabel("D-3")
    }

    // ---- Edge cases: checking and unchecking ----

    @Test
    fun setDueDateLabel_checkTaskWithDueDate_showsClear() {
        // GIVEN a view that shows D-3 for an active task
        val dueDate = LocalDate.now().plusDays(3)
        bind(active(dueDate))
        assertShowsLabel("D-3")
        // WHEN the same task is bound as completed
        bind(completed(dueDate))
        // THEN the label is CLEAR
        assertShowsLabel("CLEAR")
    }

    @Test
    fun setDueDateLabel_uncheckTaskWithDueDate_showsDMinus3() {
        // GIVEN a view that shows CLEAR for a completed task due in 3 days
        val dueDate = LocalDate.now().plusDays(3)
        bind(completed(dueDate))
        assertShowsLabel("CLEAR")
        // WHEN the same task is bound as active
        bind(active(dueDate))
        // THEN the label is D-3 again
        assertShowsLabel("D-3")
    }

    @Test
    fun setDueDateLabel_uncheckTaskWithoutDueDate_showsNoLabel() {
        // GIVEN a view that shows CLEAR for a completed task without a due date
        bind(completed(null))
        assertShowsLabel("CLEAR")
        // WHEN the same task is bound as active
        bind(active(null))
        // THEN CLEAR is cleared and no label is shown
        assertShowsNoLabel()
    }
}

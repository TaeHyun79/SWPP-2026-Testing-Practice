package com.example.swpp.tododapp.tasks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [dueDateLabel], written from specs/DueDateUtils.spec.md.
 */
class DueDateUtilsTest {

    private val today = LocalDate.of(2026, 10, 19)

    // ---- Success cases ----

    @Test
    fun dueDateLabel_completedWithDueDate_returnsClear() {
        // GIVEN a completed task with a due date
        val dueDate = LocalDate.of(2026, 10, 25)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = true)
        // THEN it is CLEAR
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_completedWithoutDueDate_returnsClear() {
        // GIVEN a completed task without a due date
        // WHEN the label is built
        val result = dueDateLabel(null, today, isCompleted = true)
        // THEN it is CLEAR
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_activeWithoutDueDate_returnsEmpty() {
        // GIVEN an active task without a due date
        // WHEN the label is built
        val result = dueDateLabel(null, today, isCompleted = false)
        // THEN no label is shown
        assertEquals("", result)
    }

    @Test
    fun dueDateLabel_dueToday_returnsDDay() {
        // GIVEN an active task due today
        val dueDate = LocalDate.of(2026, 10, 19)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D-Day
        assertEquals("D-Day", result)
    }

    @Test
    fun dueDateLabel_dueInSixDays_returnsDMinus6() {
        // GIVEN an active task due in 6 days
        val dueDate = LocalDate.of(2026, 10, 25)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D-6
        assertEquals("D-6", result)
    }

    @Test
    fun dueDateLabel_overdueByFiveDays_returnsDPlus5() {
        // GIVEN an active task overdue by 5 days
        val dueDate = LocalDate.of(2026, 10, 14)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D+5
        assertEquals("D+5", result)
    }

    @Test
    fun dueDateLabel_dueInTwoMonths_returnsDMinus67() {
        // GIVEN an active task due 67 days later (2 months and 6 days)
        val dueDate = LocalDate.of(2026, 12, 25)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN N counts every calendar day
        assertEquals("D-67", result)
    }

    @Test
    fun dueDateLabel_overdueByTwoMonths_returnsDPlus79() {
        // GIVEN an active task overdue by 79 days (2 months and 18 days)
        val dueDate = LocalDate.of(2026, 8, 1)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN N counts every calendar day
        assertEquals("D+79", result)
    }

    // ---- Edge cases: D-Day boundary ----

    @Test
    fun dueDateLabel_overdueByOneDay_returnsDPlus1() {
        // GIVEN an active task due yesterday
        val dueDate = LocalDate.of(2026, 10, 18)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D+1
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_dueTomorrow_returnsDMinus1() {
        // GIVEN an active task due tomorrow
        val dueDate = LocalDate.of(2026, 10, 20)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D-1
        assertEquals("D-1", result)
    }

    // ---- Edge cases: completed around the D-Day boundary ----

    @Test
    fun dueDateLabel_completedOverdueByOneDay_returnsClear() {
        // GIVEN a completed task due yesterday
        val dueDate = LocalDate.of(2026, 10, 18)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = true)
        // THEN it is CLEAR, not D+1
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_completedDueToday_returnsClear() {
        // GIVEN a completed task due today
        val dueDate = LocalDate.of(2026, 10, 19)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = true)
        // THEN it is CLEAR, not D-Day
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_completedDueTomorrow_returnsClear() {
        // GIVEN a completed task due tomorrow
        val dueDate = LocalDate.of(2026, 10, 20)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = true)
        // THEN it is CLEAR, not D-1
        assertEquals("CLEAR", result)
    }

    // ---- Edge cases: one-month boundary ----

    @Test
    fun dueDateLabel_dueInThirtyDays_returnsDMinus30() {
        // GIVEN an active task due one day short of a whole month later
        val dueDate = LocalDate.of(2026, 11, 18)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D-30
        assertEquals("D-30", result)
    }

    @Test
    fun dueDateLabel_dueInExactlyOneMonth_returnsDMinus31() {
        // GIVEN an active task due on the same day-of-month next month
        val dueDate = LocalDate.of(2026, 11, 19)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D-31, not D-Day
        assertEquals("D-31", result)
    }

    @Test
    fun dueDateLabel_dueInOneMonthAndOneDay_returnsDMinus32() {
        // GIVEN an active task due one day more than a whole month later
        val dueDate = LocalDate.of(2026, 11, 20)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D-32, not D-1
        assertEquals("D-32", result)
    }

    @Test
    fun dueDateLabel_overdueByTwentyNineDays_returnsDPlus29() {
        // GIVEN an active task overdue by one day short of a whole month
        val dueDate = LocalDate.of(2026, 9, 20)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D+29
        assertEquals("D+29", result)
    }

    @Test
    fun dueDateLabel_overdueByExactlyOneMonth_returnsDPlus30() {
        // GIVEN an active task due on the same day-of-month last month
        val dueDate = LocalDate.of(2026, 9, 19)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D+30, not D-Day
        assertEquals("D+30", result)
    }

    @Test
    fun dueDateLabel_overdueByOneMonthAndOneDay_returnsDPlus31() {
        // GIVEN an active task overdue by one day more than a whole month
        val dueDate = LocalDate.of(2026, 9, 18)
        // WHEN the label is built
        val result = dueDateLabel(dueDate, today, isCompleted = false)
        // THEN it is D+31, not D+1
        assertEquals("D+31", result)
    }

    // ---- Edge cases: month end ----

    @Test
    fun dueDateLabel_monthEndOverdueByOneDay_returnsDPlus1() {
        // GIVEN today is the last day of October and the task was due the day before
        val result = dueDateLabel(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 10, 31), isCompleted = false)
        // THEN it is D+1
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_monthEndDueNextMonthFirst_returnsDMinus1() {
        // GIVEN today is the last day of October and the task is due on November 1
        val result = dueDateLabel(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 31), isCompleted = false)
        // THEN it is D-1
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_monthStartOverdueByOneDay_returnsDPlus1() {
        // GIVEN today is November 1 and the task was due on October 31
        val result = dueDateLabel(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 1), isCompleted = false)
        // THEN it is D+1
        assertEquals("D+1", result)
    }

    // ---- Edge cases: February ----

    @Test
    fun dueDateLabel_acrossFebruaryCommonYear_returnsDMinus29() {
        // GIVEN January 31 to March 1 in a common year
        val result = dueDateLabel(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 1, 31), isCompleted = false)
        // THEN it is D-29
        assertEquals("D-29", result)
    }

    @Test
    fun dueDateLabel_acrossFebruaryLeapYear_returnsDMinus30() {
        // GIVEN January 31 to March 1 in a leap year
        val result = dueDateLabel(LocalDate.of(2028, 3, 1), LocalDate.of(2028, 1, 31), isCompleted = false)
        // THEN it is D-30
        assertEquals("D-30", result)
    }

    // ---- Edge cases: year end ----

    @Test
    fun dueDateLabel_yearEndDueNextYear_returnsDMinus1() {
        // GIVEN today is December 31 and the task is due January 1
        val result = dueDateLabel(LocalDate.of(2027, 1, 1), LocalDate.of(2026, 12, 31), isCompleted = false)
        // THEN it is D-1
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_yearStartOverdueByOneDay_returnsDPlus1() {
        // GIVEN today is January 1 and the task was due December 31
        val result = dueDateLabel(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1), isCompleted = false)
        // THEN it is D+1
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_dueAcrossYearEndOverOneMonth_returnsDMinus56() {
        // GIVEN today is November 20 and the task is due January 15 of the next year
        val result = dueDateLabel(LocalDate.of(2027, 1, 15), LocalDate.of(2026, 11, 20), isCompleted = false)
        // THEN it is D-56
        assertEquals("D-56", result)
    }

    @Test
    fun dueDateLabel_overdueAcrossYearEndOverOneMonth_returnsDPlus56() {
        // GIVEN today is January 15 and the task was due November 20 of the previous year
        val result = dueDateLabel(LocalDate.of(2026, 11, 20), LocalDate.of(2027, 1, 15), isCompleted = false)
        // THEN it is D+56
        assertEquals("D+56", result)
    }

    // ---- Edge cases: one-year boundary ----

    @Test
    fun dueDateLabel_dueInOneYearMinusOneDay_returnsDMinus364() {
        // GIVEN an active task due one day short of a whole year later
        val result = dueDateLabel(LocalDate.of(2027, 10, 18), today, isCompleted = false)
        // THEN it is D-364
        assertEquals("D-364", result)
    }

    @Test
    fun dueDateLabel_dueInExactlyOneYear_returnsDMinus365() {
        // GIVEN an active task due on the same date next year
        val result = dueDateLabel(LocalDate.of(2027, 10, 19), today, isCompleted = false)
        // THEN it is D-365, not D-Day
        assertEquals("D-365", result)
    }

    @Test
    fun dueDateLabel_dueInOneYearAndOneDay_returnsDMinus366() {
        // GIVEN an active task due one day more than a whole year later
        val result = dueDateLabel(LocalDate.of(2027, 10, 20), today, isCompleted = false)
        // THEN it is D-366, not D-1
        assertEquals("D-366", result)
    }

    // ---- Edge cases: leap day ----

    @Test
    fun dueDateLabel_dueOnLeapDayTomorrow_returnsDMinus1() {
        // GIVEN today is February 28, 2028 and the task is due February 29
        val result = dueDateLabel(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 2, 28), isCompleted = false)
        // THEN it is D-1
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_dueOnLeapDayToday_returnsDDay() {
        // GIVEN today is February 29, 2028 and the task is due the same day
        val result = dueDateLabel(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 2, 29), isCompleted = false)
        // THEN it is D-Day
        assertEquals("D-Day", result)
    }

    @Test
    fun dueDateLabel_leapDayOverdueByOneDay_returnsDPlus1() {
        // GIVEN today is March 1, 2028 and the task was due February 29
        val result = dueDateLabel(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 1), isCompleted = false)
        // THEN it is D+1
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_acrossLeapDay_returnsDMinus2() {
        // GIVEN February 28 to March 1 in a leap year
        val result = dueDateLabel(LocalDate.of(2028, 3, 1), LocalDate.of(2028, 2, 28), isCompleted = false)
        // THEN the leap day is counted: D-2
        assertEquals("D-2", result)
    }

    @Test
    fun dueDateLabel_acrossFebruaryEndCommonYear_returnsDMinus1() {
        // GIVEN February 28 to March 1 in a common year
        val result = dueDateLabel(LocalDate.of(2027, 3, 1), LocalDate.of(2027, 2, 28), isCompleted = false)
        // THEN it is D-1
        assertEquals("D-1", result)
    }
}

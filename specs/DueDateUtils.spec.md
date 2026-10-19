# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the task list: `"CLEAR"` for a completed task (with or without a due date), `""` (no label) for an active task without a due date, otherwise `"D-Day"` / `"D-N"` / `"D+N"`, where N is the total number of calendar days between `today` and `dueDate` — correct across month ends, year ends and leap days, and for spans of a month or more.

**Pre-conditions**
- `today` is the caller's current local calendar date; the caller passes it so the result is deterministic.
- `dueDate` is either `null` (the task has no due date) or any calendar date before, on, or after `today`.
- `isCompleted` is the task's completion state.
- All Tips below are unit tests in `app/src/test/java/com/example/swpp/tododapp/tasks/DueDateUtilsTest.kt`.

**Post-conditions**
- Success cases
  - A completed task with a due date returns `"CLEAR"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 25)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`. → `dueDateLabel_completedWithDueDate_returnsClear`
  - A completed task with no due date (optional `dueDate` left empty) returns `"CLEAR"`. `[Unit]`
    - Tip: Given `dueDate = null`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`. → `dueDateLabel_completedWithoutDueDate_returnsClear`
  - An active task with no due date (optional `dueDate` left empty) returns the empty string, so nothing is shown. `[Unit]`
    - Tip: Given `dueDate = null`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `""`. → `dueDateLabel_activeWithoutDueDate_returnsEmpty`
  - An active task due today returns `"D-Day"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-Day"`. → `dueDateLabel_dueToday_returnsDDay`
  - An active task due N days in the future (within the same month) returns `"D-N"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 25)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-6"`. → `dueDateLabel_dueInSixDays_returnsDMinus6`
  - An active task overdue by N days (within the same month) returns `"D+N"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 14)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+5"`. → `dueDateLabel_overdueByFiveDays_returnsDPlus5`
  - An active task due more than a month in the future returns `"D-N"` with N counting every calendar day in between, not just the leftover days after whole months. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 12, 25)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-67"`. → `dueDateLabel_dueInTwoMonths_returnsDMinus67`
  - An active task overdue by more than a month returns `"D+N"` with N counting every calendar day in between. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 8, 1)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+79"`. → `dueDateLabel_overdueByTwoMonths_returnsDPlus79`
- Error cases
  - None. Every combination of `dueDate` (including `null`), `today` and `isCompleted` is valid input and returns one of the labels above; no exception is thrown. `[Unit]`
    - Tip: covered by every Success and Edge case in this spec — each test calls `dueDateLabel` directly and would fail on an exception.
- Edge cases
  - Boundary between overdue and due today: an active task due 1 day before `today` returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`. → `dueDateLabel_overdueByOneDay_returnsDPlus1`
  - Boundary at due today: an active task due exactly on `today` returns `"D-Day"` (not `"D-0"` or `"D+0"`). `[Unit]`
    - Tested by `DueDateUtilsTest.dueDateLabel_dueToday_returnsDDay` (Success case above).
  - Boundary between due today and future: an active task due 1 day after `today` returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`. → `dueDateLabel_dueTomorrow_returnsDMinus1`
  - Completed boundary, overdue side: a completed task overdue by 1 day returns `"CLEAR"`, not `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`. → `dueDateLabel_completedOverdueByOneDay_returnsClear`
  - Completed boundary, due today: a completed task due today returns `"CLEAR"`, not `"D-Day"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`. → `dueDateLabel_completedDueToday_returnsClear`
  - Completed boundary, future side: a completed task due in 1 day returns `"CLEAR"`, not `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`. → `dueDateLabel_completedDueTomorrow_returnsClear`
  - One whole month ahead, 1 day short: an active task due 30 days later returns `"D-30"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-30"`. → `dueDateLabel_dueInThirtyDays_returnsDMinus30`
  - Exactly one whole month ahead: an active task due on the same day-of-month next month returns `"D-31"` (not `"D-Day"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-31"`. → `dueDateLabel_dueInExactlyOneMonth_returnsDMinus31`
  - One whole month ahead, 1 day over: an active task due 32 days later returns `"D-32"` (not `"D-1"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-32"`. → `dueDateLabel_dueInOneMonthAndOneDay_returnsDMinus32`
  - One whole month overdue, 1 day short: an active task overdue by 29 days returns `"D+29"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 9, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+29"`. → `dueDateLabel_overdueByTwentyNineDays_returnsDPlus29`
  - Exactly one whole month overdue: an active task due on the same day-of-month last month returns `"D+30"` (not `"D-Day"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 9, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+30"`. → `dueDateLabel_overdueByExactlyOneMonth_returnsDPlus30`
  - One whole month overdue, 1 day over: an active task overdue by 31 days returns `"D+31"` (not `"D+1"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 9, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+31"`. → `dueDateLabel_overdueByOneMonthAndOneDay_returnsDPlus31`
  - Month end, overdue side: on the last day of a month, a task due the day before returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 30)`, `today = LocalDate.of(2026, 10, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`. → `dueDateLabel_monthEndOverdueByOneDay_returnsDPlus1`
  - Month end, future side: on the last day of a month, a task due on the first day of the next month returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 1)`, `today = LocalDate.of(2026, 10, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`. → `dueDateLabel_monthEndDueNextMonthFirst_returnsDMinus1`
  - Month start, overdue side: on the first day of a month, a task due on the last day of the previous month returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 31)`, `today = LocalDate.of(2026, 11, 1)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`. → `dueDateLabel_monthStartOverdueByOneDay_returnsDPlus1`
  - Across a short February in a common year: from January 31 to March 1 returns `"D-29"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 3, 1)`, `today = LocalDate.of(2026, 1, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-29"`. → `dueDateLabel_acrossFebruaryCommonYear_returnsDMinus29`
  - Across February in a leap year: from January 31 to March 1 returns `"D-30"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 3, 1)`, `today = LocalDate.of(2028, 1, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-30"`. → `dueDateLabel_acrossFebruaryLeapYear_returnsDMinus30`
  - Year end, future side: on December 31, a task due January 1 of the next year returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 1, 1)`, `today = LocalDate.of(2026, 12, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`. → `dueDateLabel_yearEndDueNextYear_returnsDMinus1`
  - Year end, overdue side: on January 1, a task due December 31 of the previous year returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 12, 31)`, `today = LocalDate.of(2027, 1, 1)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`. → `dueDateLabel_yearStartOverdueByOneDay_returnsDPlus1`
  - Across a year end and more than a month, future side: from November 20 to January 15 of the next year returns `"D-56"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 1, 15)`, `today = LocalDate.of(2026, 11, 20)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-56"`. → `dueDateLabel_dueAcrossYearEndOverOneMonth_returnsDMinus56`
  - Across a year end and more than a month, overdue side: from January 15 back to November 20 of the previous year returns `"D+56"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 20)`, `today = LocalDate.of(2027, 1, 15)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+56"`. → `dueDateLabel_overdueAcrossYearEndOverOneMonth_returnsDPlus56`
  - One whole year ahead, 1 day short: returns `"D-364"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 10, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-364"`. → `dueDateLabel_dueInOneYearMinusOneDay_returnsDMinus364`
  - Exactly one whole year ahead: returns `"D-365"` (not `"D-Day"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 10, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-365"`. → `dueDateLabel_dueInExactlyOneYear_returnsDMinus365`
  - One whole year ahead, 1 day over: returns `"D-366"` (not `"D-1"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 10, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-366"`. → `dueDateLabel_dueInOneYearAndOneDay_returnsDMinus366`
  - Leap day, future side: on February 28 of a leap year, a task due February 29 returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 2, 29)`, `today = LocalDate.of(2028, 2, 28)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`. → `dueDateLabel_dueOnLeapDayTomorrow_returnsDMinus1`
  - Leap day, at the boundary: on February 29, a task due February 29 returns `"D-Day"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 2, 29)`, `today = LocalDate.of(2028, 2, 29)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-Day"`. → `dueDateLabel_dueOnLeapDayToday_returnsDDay`
  - Leap day, overdue side: on March 1 of a leap year, a task due February 29 returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 2, 29)`, `today = LocalDate.of(2028, 3, 1)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`. → `dueDateLabel_leapDayOverdueByOneDay_returnsDPlus1`
  - Leap day counted in between: from February 28 to March 1 of a leap year returns `"D-2"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 3, 1)`, `today = LocalDate.of(2028, 2, 28)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-2"`. → `dueDateLabel_acrossLeapDay_returnsDMinus2`
  - No leap day in between: from February 28 to March 1 of a common year returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 3, 1)`, `today = LocalDate.of(2027, 2, 28)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`. → `dueDateLabel_acrossFebruaryEndCommonYear_returnsDMinus1`

**Undefined behavior** (optional)
- None. Every input combination has a defined label. Which time zone `today` comes from is the caller's decision (see `TasksListBindings.spec.md`).

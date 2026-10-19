# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the task list: `"CLEAR"` for a completed task, `""` for an active task without a due date, otherwise `"D-Day"` / `"D-N"` / `"D+N"`, where N is the total number of calendar days between `today` and `dueDate` (correct across month ends, year ends and leap days, and for spans of a month or more).

**Pre-conditions**
- `today` is the caller's current local calendar date; the function itself never reads the clock, so it is pure and deterministic.
- `dueDate` is either `null` (the task has no due date) or any calendar date before, on, or after `today`.
- `isCompleted` is the task's completion state.

**Post-conditions**
- Success cases
  - A completed task with a due date returns `"CLEAR"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 25)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - A completed task with no due date (optional `dueDate` left empty) returns `"CLEAR"`. `[Unit]`
    - Tip: Given `dueDate = null`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - An active task with no due date (optional `dueDate` left empty) returns the empty string, so nothing is shown. `[Unit]`
    - Tip: Given `dueDate = null`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `""`.
  - An active task due today returns `"D-Day"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-Day"`.
  - An active task due N days in the future (within the same month) returns `"D-N"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 25)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-6"`.
  - An active task overdue by N days (within the same month) returns `"D+N"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 14)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+5"`.
  - An active task due more than a month in the future returns `"D-N"` with N counting every calendar day in between, not just the leftover days after whole months. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 12, 25)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-67"`.
  - An active task overdue by more than a month returns `"D+N"` with N counting every calendar day in between. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 8, 1)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+79"`.
- Error cases
  - None. Every combination of `dueDate` (including `null`), `today` and `isCompleted` is valid input.
- Edge cases
  - Boundary between overdue and due today: an active task due 1 day before `today` returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - Boundary at due today: an active task due exactly on `today` returns `"D-Day"` (not `"D-0"` or `"D+0"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-Day"`.
  - Boundary between due today and future: an active task due 1 day after `today` returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Completed boundary, overdue side: a completed task overdue by 1 day returns `"CLEAR"`, not `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - Completed boundary, due today: a completed task due today returns `"CLEAR"`, not `"D-Day"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - Completed boundary, future side: a completed task due in 1 day returns `"CLEAR"`, not `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - One whole month ahead, 1 day short: an active task due 30 days later returns `"D-30"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-30"`.
  - Exactly one whole month ahead: an active task due on the same day-of-month next month returns `"D-31"` (not `"D-Day"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-31"`.
  - One whole month ahead, 1 day over: an active task due 32 days later returns `"D-32"` (not `"D-1"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-32"`.
  - One whole month overdue, 1 day short: an active task overdue by 29 days returns `"D+29"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 9, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+29"`.
  - Exactly one whole month overdue: an active task due on the same day-of-month last month returns `"D+30"` (not `"D-Day"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 9, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+30"`.
  - One whole month overdue, 1 day over: an active task overdue by 31 days returns `"D+31"` (not `"D+1"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 9, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+31"`.
  - Month end, overdue side: on the last day of a month, a task due the day before returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 30)`, `today = LocalDate.of(2026, 10, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - Month end, future side: on the last day of a month, a task due on the first day of the next month returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 11, 1)`, `today = LocalDate.of(2026, 10, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Month start, overdue side: on the first day of a month, a task due on the last day of the previous month returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 10, 31)`, `today = LocalDate.of(2026, 11, 1)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - Across a short February in a common year: from January 31 to March 1 returns `"D-29"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 3, 1)`, `today = LocalDate.of(2026, 1, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-29"`.
  - Across February in a leap year: from January 31 to March 1 returns `"D-30"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 3, 1)`, `today = LocalDate.of(2028, 1, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-30"`.
  - Year end, future side: on December 31, a task due January 1 of the next year returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 1, 1)`, `today = LocalDate.of(2026, 12, 31)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Year end, overdue side: on January 1, a task due December 31 of the previous year returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2026, 12, 31)`, `today = LocalDate.of(2027, 1, 1)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - One whole year ahead, 1 day short: returns `"D-364"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 10, 18)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-364"`.
  - Exactly one whole year ahead: returns `"D-365"` (not `"D-Day"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 10, 19)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-365"`.
  - One whole year ahead, 1 day over: returns `"D-366"` (not `"D-1"`). `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 10, 20)`, `today = LocalDate.of(2026, 10, 19)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-366"`.
  - Leap day, future side: on February 28 of a leap year, a task due February 29 returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 2, 29)`, `today = LocalDate.of(2028, 2, 28)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Leap day, at the boundary: on February 29, a task due February 29 returns `"D-Day"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 2, 29)`, `today = LocalDate.of(2028, 2, 29)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-Day"`.
  - Leap day, overdue side: on March 1 of a leap year, a task due February 29 returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 2, 29)`, `today = LocalDate.of(2028, 3, 1)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - Leap day counted in between: from February 28 to March 1 of a leap year returns `"D-2"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2028, 3, 1)`, `today = LocalDate.of(2028, 2, 28)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-2"`.
  - No leap day in between: from February 28 to March 1 of a common year returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = LocalDate.of(2027, 3, 1)`, `today = LocalDate.of(2027, 2, 28)`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.

**Undefined behavior** (optional)
- None. The function is pure and every input combination has a defined label.

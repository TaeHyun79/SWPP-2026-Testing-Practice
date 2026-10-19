# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the D-day label of `task` in the `due_date_text` view of its task-list row, using the device's current date as "today": `"D-N"` / `"D-Day"` / `"D+N"` for an active task with a due date, `"CLEAR"` for a completed task (with or without a due date), and no label for an active task without a due date or for no task. The row must always show the label of the task's *current* state, so the label is updated when the user checks or unchecks the task, and a recycled row never keeps a label from an earlier bind.

**Pre-conditions**
- Called by data binding for `app:dueDateLabel="@{task}"` in `task_item.xml`. In a direct call, `textView` is any `TextView` created from an Android `Context` on the main thread (for example inside `InstrumentationRegistry.getInstrumentation().runOnMainSync { }`), and it may already hold the text and visibility of a previous bind because RecyclerView reuses rows.
- `task` is either `null` or a `Task` whose `dueDate` may be `null`.
- "Today" is `LocalDate.now()` in the device's default time zone at the moment of the call, so tests write due dates relative to `LocalDate.now()`.
- **"Shows a label X"** means `textView.text.toString() == X` and `textView.visibility == View.VISIBLE`.
- **"Shows no label"** means the user sees no text: `textView.text.toString() == ""` **or** `textView.visibility != View.VISIBLE`. The intent only says "no label"; it does not prescribe `GONE` versus an empty visible view, so the oracle accepts either.
- Direct-call Tips are instrumented tests in `app/src/androidTest/java/com/example/swpp/tododapp/tasks/TasksListBindingsTest.kt`; screen Tips are in `app/src/androidTest/java/com/example/swpp/tododapp/tasks/TasksFragmentTest.kt`.

**Post-conditions**
- Success cases
  - An active task due in the future shows `"D-N"`. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` / When `setDueDateLabel(textView, task)` is called / Then it shows the label `"D-3"`. → `TasksListBindingsTest.setDueDateLabel_activeDueInThreeDays_showsDMinus3`
  - An active task that is overdue shows `"D+N"`. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().minusDays(5))` / When `setDueDateLabel(textView, task)` is called / Then it shows the label `"D+5"`. → `TasksListBindingsTest.setDueDateLabel_activeOverdueByFiveDays_showsDPlus5`
  - An active task due today shows `"D-Day"`. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now())` / When `setDueDateLabel(textView, task)` is called / Then it shows the label `"D-Day"`. → `TasksListBindingsTest.setDueDateLabel_activeDueToday_showsDDay`
  - A completed task with a due date shows `"CLEAR"`. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = true, dueDate = LocalDate.now().plusDays(3))` / When `setDueDateLabel(textView, task)` is called / Then it shows the label `"CLEAR"`. → `TasksListBindingsTest.setDueDateLabel_completedWithDueDate_showsClear`
  - A completed task with no due date (optional `dueDate` left empty) shows `"CLEAR"`. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = true, dueDate = null)` / When `setDueDateLabel(textView, task)` is called / Then it shows the label `"CLEAR"`. → `TasksListBindingsTest.setDueDateLabel_completedWithoutDueDate_showsClear`
  - An active task with no due date (optional `dueDate` left empty) shows no label. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = null)` / When `setDueDateLabel(textView, task)` is called / Then it shows no label. → `TasksListBindingsTest.setDueDateLabel_activeWithoutDueDate_showsNoLabel`
  - No task (optional `task` left empty) shows no label. `[Integration]`
    - Tip: Given a fresh `TextView` and `task = null` / When `setDueDateLabel(textView, null)` is called / Then it shows no label. → `TasksListBindingsTest.setDueDateLabel_nullTask_showsNoLabel`
- Error cases
  - None. Every `task`, including `null`, is valid input; no exception is thrown. `[Integration]`
    - Tested by `TasksListBindingsTest.setDueDateLabel_nullTask_showsNoLabel` (the only input that could plausibly be rejected).
- Edge cases
  - Day boundary, overdue side: an active task due yesterday shows `"D+1"`. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().minusDays(1))` / When `setDueDateLabel(textView, task)` is called / Then it shows the label `"D+1"`. → `TasksListBindingsTest.setDueDateLabel_activeDueYesterday_showsDPlus1`
  - Day boundary, future side: an active task due tomorrow shows `"D-1"`. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(1))` / When `setDueDateLabel(textView, task)` is called / Then it shows the label `"D-1"`. → `TasksListBindingsTest.setDueDateLabel_activeDueTomorrow_showsDMinus1`
  - Recycled view, label becomes empty (active task with no due date): the previous label is cleared. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` so it shows `"D-3"` / When `setDueDateLabel(textView, Task(title = "Walk dog", isCompleted = false, dueDate = null))` is called / Then it shows no label. → `TasksListBindingsTest.setDueDateLabel_recycledViewActiveWithoutDueDate_showsNoLabel`
  - Recycled view, label becomes empty (null task): the previous label is cleared. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` so it shows `"D-3"` / When `setDueDateLabel(textView, null)` is called / Then it shows no label. → `TasksListBindingsTest.setDueDateLabel_recycledViewNullTask_showsNoLabel`
  - Recycled view, label becomes non-empty: a view that showed no label shows the new label. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Walk dog", isCompleted = false, dueDate = null)` / When `setDueDateLabel(textView, Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3)))` is called / Then it shows the label `"D-3"`. → `TasksListBindingsTest.setDueDateLabel_recycledViewWithoutLabelThenDueInThreeDays_showsDMinus3`
  - Checking a task with a due date: the label switches from `"D-N"` to `"CLEAR"`. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` so it shows `"D-3"` / When `setDueDateLabel(textView, Task(title = "Buy milk", isCompleted = true, dueDate = LocalDate.now().plusDays(3)))` is called / Then it shows the label `"CLEAR"`. → `TasksListBindingsTest.setDueDateLabel_checkTaskWithDueDate_showsClear`
  - Unchecking a task with a due date: the label switches from `"CLEAR"` back to `"D-N"`. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = true, dueDate = LocalDate.now().plusDays(3))` so it shows `"CLEAR"` / When `setDueDateLabel(textView, Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3)))` is called / Then it shows the label `"D-3"`. → `TasksListBindingsTest.setDueDateLabel_uncheckTaskWithDueDate_showsDMinus3`
  - Unchecking a task with no due date: the label switches from `"CLEAR"` to no label. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = true, dueDate = null)` so it shows `"CLEAR"` / When `setDueDateLabel(textView, Task(title = "Buy milk", isCompleted = false, dueDate = null))` is called / Then it shows no label. → `TasksListBindingsTest.setDueDateLabel_uncheckTaskWithoutDueDate_showsNoLabel`
  - On the task list screen, checking then unchecking a task with no due date shows `"CLEAR"`, then no label. `[Integration]`
    - Tip: Given `FakeAndroidTestRepository` holding `Task(title = "No due", isCompleted = false, dueDate = null)` set as `ServiceLocator.tasksRepository`, and `TasksFragment` launched / When the row's `complete_checkbox` is clicked, then clicked again / Then after the first click the row's `due_date_text` shows `"CLEAR"`, and after the second click it shows no label. → `TasksFragmentTest.dueDateLabel_checkThenUncheckWithoutDueDate_showsClearThenNoLabel`
  - On the task list screen, checking then unchecking a task due in 3 days shows `"CLEAR"`, then `"D-3"`. `[Integration]`
    - Tip: Given `FakeAndroidTestRepository` holding `Task(title = "Due soon", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` and `TasksFragment` launched / When the row's `complete_checkbox` is clicked, then clicked again / Then the row's `due_date_text` shows `"CLEAR"`, then `"D-3"`. → `TasksFragmentTest.dueDateLabel_checkThenUncheckDueInThreeDays_showsClearThenDMinus3`

**Undefined behavior** (optional)
- A call that runs across local midnight, or while the device time zone changes, may use either date as "today"; tests must not run at that moment because "today" is read from the device clock.
- Whether an already-bound row refreshes its label when the date changes while it stays on screen is out of scope; the label is only computed when the row is bound.

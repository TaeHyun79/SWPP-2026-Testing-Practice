# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Bind the D-day label of `task` (as defined by `dueDateLabel(task.dueDate, LocalDate.now(), task.isCompleted)`) to the `due_date_text` view of a task-list row: when the label is non-empty, the view shows exactly that text and is `View.VISIBLE`; when there is nothing to show (no task, or an active task without a due date), the view shows `""` and is `View.GONE`, even if the same (recycled) view showed a label before.

**Pre-conditions**
- Called by data binding for `app:dueDateLabel="@{task}"` in `task_item.xml`; in a direct call, `textView` is any `TextView` created from an Android `Context` (for example `TextView(InstrumentationRegistry.getInstrumentation().targetContext)`), which may already hold the text and visibility of a previous bind because RecyclerView reuses rows.
- `task` is either `null` or a `Task` whose `dueDate` may be `null`.
- "Today" is `LocalDate.now()` in the device's default time zone at the moment of the call, so tests write due dates relative to `LocalDate.now()`.

**Post-conditions**
- Success cases
  - An active task due in the future: the view shows `"D-N"` and is visible. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `"D-3"` and `textView.visibility` is `View.VISIBLE`.
  - An active task that is overdue: the view shows `"D+N"` and is visible. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().minusDays(5))` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `"D+5"` and `textView.visibility` is `View.VISIBLE`.
  - An active task due today: the view shows `"D-Day"` and is visible. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now())` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `"D-Day"` and `textView.visibility` is `View.VISIBLE`.
  - A completed task with a due date: the view shows `"CLEAR"` and is visible. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = true, dueDate = LocalDate.now().plusDays(3))` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `"CLEAR"` and `textView.visibility` is `View.VISIBLE`.
  - A completed task with no due date (optional `dueDate` left empty): the view shows `"CLEAR"` and is visible. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = true, dueDate = null)` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `"CLEAR"` and `textView.visibility` is `View.VISIBLE`.
  - An active task with no due date (optional `dueDate` left empty): the view shows nothing and is gone. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = null)` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `""` and `textView.visibility` is `View.GONE`.
  - No task (optional `task` left empty): the view shows nothing and is gone. `[Integration]`
    - Tip: Given a fresh `TextView` and `task = null` / When `setDueDateLabel(textView, null)` is called / Then `textView.text.toString()` is `""` and `textView.visibility` is `View.GONE`.
- Error cases
  - None. Every `task`, including `null`, is valid input.
- Edge cases
  - Day boundary, overdue side: an active task due yesterday shows `"D+1"` and is visible. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().minusDays(1))` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `"D+1"` and `textView.visibility` is `View.VISIBLE`.
  - Day boundary, future side: an active task due tomorrow shows `"D-1"` and is visible. `[Integration]`
    - Tip: Given a fresh `TextView` and `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(1))` / When `setDueDateLabel(textView, task)` is called / Then `textView.text.toString()` is `"D-1"` and `textView.visibility` is `View.VISIBLE`.
  - Label becomes empty on a recycled view (active task with no due date): the previous label is cleared and the view is gone. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` so it shows `"D-3"` and is `View.VISIBLE` / When `setDueDateLabel(textView, Task(title = "Walk dog", isCompleted = false, dueDate = null))` is called / Then `textView.text.toString()` is `""` and `textView.visibility` is `View.GONE`.
  - Label becomes empty on a recycled view (null task): the previous label is cleared and the view is gone. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` so it shows `"D-3"` and is `View.VISIBLE` / When `setDueDateLabel(textView, null)` is called / Then `textView.text.toString()` is `""` and `textView.visibility` is `View.GONE`.
  - Label becomes non-empty on a recycled view: a view that was gone shows the new label and is visible again. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Walk dog", isCompleted = false, dueDate = null)` so it shows `""` and is `View.GONE` / When `setDueDateLabel(textView, Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3)))` is called / Then `textView.text.toString()` is `"D-3"` and `textView.visibility` is `View.VISIBLE`.
  - Completing a task with a due date: the label switches from `"D-N"` to `"CLEAR"` and stays visible. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = false, dueDate = LocalDate.now().plusDays(3))` so it shows `"D-3"` / When `setDueDateLabel(textView, Task(title = "Buy milk", isCompleted = true, dueDate = LocalDate.now().plusDays(3)))` is called / Then `textView.text.toString()` is `"CLEAR"` and `textView.visibility` is `View.VISIBLE`.
  - Un-completing a task with no due date: the label switches from `"CLEAR"` to nothing and the view is gone. `[Integration]`
    - Tip: Given a `TextView` already bound to `Task(title = "Buy milk", isCompleted = true, dueDate = null)` so it shows `"CLEAR"` and is `View.VISIBLE` / When `setDueDateLabel(textView, Task(title = "Buy milk", isCompleted = false, dueDate = null))` is called / Then `textView.text.toString()` is `""` and `textView.visibility` is `View.GONE`.

**Undefined behavior** (optional)
- A call that runs across local midnight, or while the device time zone changes, may use either date as "today"; tests must not run at that moment because the function reads `LocalDate.now()` directly.
- Whether an already-bound row refreshes its label when the date changes while it stays on screen is out of scope; the function only computes the label at bind time.

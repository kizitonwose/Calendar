# 日历视图文档

## 目录

- [快速链接](#快速链接)
- [类信息](#类信息)
- [用法](#用法)
  * [步骤1](#步骤1)
  * [步骤2](#步骤2)
  * [一周的第一天](#一周的第一天和星期标题)
  * [页眉和页脚](#页眉和页脚)
  * [属性](#属性)
  * [性能](#性能)
  * [方法](#方法)
  * [日期点击](#日期点击)
  * [日期选择](#日期选择)
  * [禁用日期](#禁用日期)
- [周视图](#周视图)
- [常见问题解答](#常见问题解答)
- [迁移](#迁移)

## 快速链接

如果尚未查看示例应用，请查看一下。大多数你想要实现的技术在示例中已经完成。

下载示例应用程序，请点击[此处](https://github.com/kizitonwose/Calendar/releases/download/2.0.0/sample.apk)。

阅读示例应用程序的源代码，请点击[此处](https://github.com/kizitonwose/Calendar/tree/main/sample)。

将库添加到你的项目中，请点击[此处](https://github.com/kizitonwose/Calendar/docs_chs/README.chs.md#设置)。

**如果你正在寻找Compose文档，你可以在[这里](https://github.com/kizitonwose/Calendar/blob/main/docs_chs/Compose.chs.md)找到。**

## 类信息

该库可以通过两个类使用：

`CalendarView`：传统的基于月份的日历。

`WeekCalendarView`：基于周的日历。

这两个类都继承自 `RecyclerView`，因此你可以使用所有 `RecyclerView` 的自定义功能，比如装饰器等。

在下面的示例中，我们将主要使用 `CalendarView` 类，因为这两个类共享相同的基本概念。如果你想要一个基于周的日历，在你的 XML/代码中将 `CalendarView` 替换为 `WeekCalendarView`。
`CalendarView` 中带有名称前缀/后缀 `month`（例如 `monthHeaderResource`）的大多数 XML 属性和类属性/方法，在 `WeekCalendarView` 中都会有一个等效的名称前缀/后缀 `week`（例如 `weekHeaderResource`）。

## 用法

#### 步骤 1

像其他视图一样，将 `CalendarView` 添加到你的 XML 文件中。

```xml
<com.kizitonwose.calendar.view.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```
查看所有可用的[属性](#属性)。

在 `res/layout/calendar_day_layout.xml` 中创建你的日视图资源。

```xml
<TextView
    android:id="@+id/calendarDayText"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:textSize="16sp"
    tools:text="22" />
```

创建一个视图容器，它作为每个日期单元的视图持有者。在这里传递的视图是你提供的膨胀的日视图资源。

```kotlin
class DayViewContainer(view: View) : ViewContainer(view) {    
    val textView = view.findViewById<TextView>(R.id.calendarDayText)

    // 使用 ViewBinding
	// val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
}
```

使用你的 `DayViewContainer` 类型为 `CalendarView` 提供一个 `MonthDayBinder`。

```kotlin
calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    // 只有在需要新的容器时才会调用。
    override fun create(view: View) = DayViewContainer(view)
    
    // 在每次需要重用容器时都会调用。
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.textView.text = data.date.dayOfMonth.toString()
    }
}
```

#### 步骤 2

在你的 Fragment 或 Activity 中设置所需的日期：

**`CalendarView` 设置：**

```kotlin
val currentMonth = YearMonth.now()
val startMonth = currentMonth.minusMonths(100)  // 根据需要进行调整
val endMonth = currentMonth.plusMonths(100)  // 根据需要进行调整
val firstDayOfWeek = firstDayOfWeekFromLocale() // 从库中获取
calendarView.setup(startMonth, endMonth, firstDayOfWeek)
calendarView.scrollToMonth(currentMonth)
```

**`WeekCalendarView` 设置:**

```diff
- <com.kizitonwose.calendar.view.CalendarView
+ <com.kizitonwose.calendar.view.WeekCalendarView
    android:id="@+id/weekCalendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

```kotlin
val currentDate = LocalDate.now()
val currentMonth = YearMonth.now()
val startDate = currentMonth.minusMonths(100).atStartOfMonth() // 根据需要进行调整
val endDate = currentMonth.plusMonths(100).atEndOfMonth()  // 根据需要进行调整
val firstDayOfWeek = firstDayOfWeekFromLocale() // 从库中获取
weekCalendarView.setup(startDate, endDate, firstDayOfWeek)
weekCalendarView.scrollToWeek(currentDate)
```

**这就是简单使用所需的全部！但请继续阅读，还有更多内容！**

### 一周的第一天和星期标题

当然，你想要在日历上的相应日期显示星期标题。

`Sun | Mon | Tue | Wed | Thu | Fri | Sat`

这是一个根据用户当前的语言环境生成星期几的函数。

```kotlin
val daysOfWeek = daysOfWeek() // 从库中获取
```

该函数接受一个 `firstDayOfWeek` 参数，以便在生成星期几时，将期望的星期几放在第一个位置。

例如：

```kotlin
val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.THURSDAY)
// 将会生成 => Thu | Fri | Sat | Sun | Mon | Tue | Wed 
```
使用 `daysOfWeek` 列表，你可以设置日历，使一周的第一天是用户期望的星期几，比如星期日、星期一等。最好使用语言环境返回的值，因为这是用户期望的。

使用提供的 `daysOfWeek` 列表设置日历：

```diff
- val firstDayOfWeek = firstDayOfWeekFromLocale()
+ val daysOfWeek = daysOfWeek()
  calendarView.setup(startMonth, endMonth, daysOfWeek.first())
```

你还应该使用 `daysOfWeek` 列表的值来设置星期标题，这样它就与在 `CalendarView` 上显示的内容匹配。

为了设置星期几的标题，你可以使用月标题，它会在每个月份显示标题，并允许标题随着月份滚动，或者你可以在日历上方显示一个静态视图。以下是两种方式的具体实现：

使用静态视图设置星期几：

#### 步骤 1
在 `res/layout/calendar_day_title_text.xml` 中创建你的标题文本视图。

```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_weight="1"
    android:gravity="center" />
```

在 `res/layout/calendar_day_titles_container.xml` 中创建一个容器资源以使用文本。

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:weightSum="7"
    android:orientation="horizontal">
    
    <include layout="@layout/calendar_day_title_text" />

    <include layout="@layout/calendar_day_title_text" />

    <include layout="@layout/calendar_day_title_text" />

    <include layout="@layout/calendar_day_title_text" />

    <include layout="@layout/calendar_day_title_text" />

    <include layout="@layout/calendar_day_title_text" />

    <include layout="@layout/calendar_day_title_text" />

</LinearLayout>
```

在与 `CalendarView` 相同的布局中添加标题容器：

```kotlin
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <include
        android:id="@+id/titlesContainer"
        layout="@layout/calendar_day_titles_container" />

    <com.kizitonwose.calendar.view.CalendarView
        android:id="@+id/calendarView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cv_dayViewResource="@layout/calendar_day_layout" />

</LinearLayout>
```

#### 步骤2

现在，你可以使用之前讨论的 `daysOfWeek` 列表来设置标题：

```kotlin
val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
titlesContainer.children
    .map { it as TextView }
    .forEachIndexed { index, textView ->
        val dayOfWeek = daysOfWeek[index]
        val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        textView.text = title
    }
```

如果要将标题用作月标题，使其随着每个月份滚动，请继续阅读下面的月标题和页脚部分！

### 页眉和页脚

要为每个月份添加页眉或页脚，步骤与我们上面为日期使用 `dayViewResource` 相同，但是不是提供日期资源，而是提供你的 `monthHeaderResource` 或 `monthFooterResource` 属性，然后设置 `CalendarView` 的 `monthHeaderBinder` 或 `monthFooterBinder` 属性。

要将星期几的标题作为月标题添加，我们将标题容器作为页眉资源提供：

```xml
<com.kizitonwose.calendar.view.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout"
    app:cv_monthHeaderResource="@layout/calendar_day_titles_container" /> 

```

现在我们可以使用页眉来显示标题:

```kotlin
class MonthViewContainer(view: View) : ViewContainer(view) {
    // 或者，你可以向容器布局添加一个 ID，并使用 findViewById() 方法
    val titlesContainer = view as ViewGroup 
}

calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
    override fun create(view: View) = MonthViewContainer(view)
    override fun bind(container: MonthViewContainer, data: CalendarMonth) {
        // 请记住，页眉是可重复使用的，因此这将对每个月都调用。
        // 但是，一周的第一天不会改变，
        // 因此不需要在每次重用时都绑定相同的视图.
        if (container.titlesContainer.tag == null) {
            container.titlesContainer.tag = data.yearMonth
            container.titlesContainer.children.map { it as TextView }
                .forEachIndexed { index, textView ->
                    val dayOfWeek = daysOfWeek[index]
                    val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    textView.text = title
                    // 在上面的代码中，我们使用了在设置日历时创建的相同的 `daysOfWeek` 列表。 
					// 然而，我们也可以从月份数据中获取 `daysOfWeek` 列表：
					// val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
					// 或者，您可以获取此特定索引的值：
					// val dayOfWeek = data.weekDays.first()[index].date.dayOfWeek
                }
        }
    }
}
```

你不仅可以将星期几的标题用作页眉，还可以显示月份名称，如果它尚未在日历外部的单独视图上显示。请随意在月标题和页脚上发挥创意！对于更复杂的用法，请参阅示例项目。

### 属性

#### XML（为了清晰起见，所有的前缀都是 `cv_`）

**以下属性适用于`CalendarView`和`WeekCalendarView`类：**

- **dayViewResource**: 用作日单元格视图的 XML 资源。必须提供此项。

- **scrollPaged**: 日历的滚动行为。如果为 `true`，日历将在滚动或滑动操作后自动对齐到最近的月份或周（在 `WeekCalendarView` 中）。如果为 `false`，日历将以正常方式滚动。

- **daySize**: 确定日历上每一天的大小如何计算。可以是以下三个值之一：
    1. **square**: 每一天的宽度和高度都与日历的宽度除以7相匹配。
    2. **rectangle**: 每一天的宽度与日历的宽度除以7相匹配，而高度与日历的高度除以索引中的周数相匹配 - 月份日历可以是4、5或6，周日历为1。
    使用此选项，如果要使每个月或周填充父元素的宽度和高度。
    3. **seventhWidth**: 每一天的宽度与日历的宽度除以7相匹配。该天可以通过设置特定值或使用 `LayoutParams.WRAP_CONTENT` 来确定其高度。
    4. **freeForm**: 该天可以通过设置特定值或使用 `LayoutParams.WRAP_CONTENT` 来确定其宽度和高度。

**以下属性仅适用于`CalendarView`类：**

- **monthHeaderResource**: 用作每个月份页眉的 XML 资源。

- **monthFooterResource**: 用作每个月份页脚的 XML 资源。

- **orientation**: 日历的滚动方向，可以是 `horizontal` 或 `vertical`。默认为 `horizontal`。

- **monthViewClass**: 一个 ViewGroup，用作每个月份的容器。这个类必须有一个只接受 Context 的构造函数。如果启用了代码混淆，你应该排除这个类的名称和构造函数。

- **outDateStyle**: 这确定了如何为日历上的每个月份生成 `outDates`。可以是以下两个值之一：
    1. **endOfRow**: 日历将生成 `outDates` 直到达到月份行的末尾。这意味着如果一个月有5行，它将显示5行，如果一个月有6行，它将显示6行。
    2. **endOfGrid**: 日历将生成 `outDates` 直到达到每个月的 6 x 7 网格的末尾。这意味着所有月份都将有6行。

如果你想知道 `outDates` 和 `inDates` 是什么意思，让我们使用下面的截图作为例子。

<img src="https://user-images.githubusercontent.com/15170090/197358602-c9c6f796-fb28-4c82-9101-458d7a66f3a0.png" alt="in-dates and out-dates" width="300">

在图像中，绿色标注内的日期是 `inDates`，红色标注内的日期是 `outDates`，而没有标注的日期是 `monthDates`。在绑定日历时，你可以检查这一点。要实现图像上的效果，我们可以这样做：

```kotlin
calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.textView.text = data.date.dayOfMonth.toString()
        if (data.position == DayPosition.MonthDate) {
            container.textView.setTextColor(Color.WHITE)
        } else {
            container.textView.setTextColor(Color.GRAY)
        }
    }
}
```

`inDates` 的 `position` 属性设置为 `DayPosition.InDate`

`outDates` 的 `position` 属性设置为 `DayPosition.OutDate`

`monthDates` 的 `position` 属性设置为 `DayPosition.MonthDate` 如上面的代码片段中所示。

- **以下属性仅适用于`WeekCalendarView`类：**
    - **weekHeaderResource**: 用作每周页眉的 XML 资源。

    - **weekFooterResource**: 用作每周页脚的 XML 资源。

    - **weekViewClass**: 一个 ViewGroup，用作每周的容器。这个类必须有一个只接受 Context 的构造函数。如果启用了代码混淆，你应该排除这个类的名称和构造函数。

### 性能

所有上面列出的相应 XML 属性也作为 `CalendarView` 和 `WeekCalendarView` 类的属性可用，因此它们可以通过代码设置。因此，除了上述属性之外，我们还有：

**`CalendarView` 属性：**

- **monthScrollListener**: 当日历滚动到新月份时调用。如果 `scrollPaged` 为 `true`，则大多数情况下是有益的。

- **dayBinder**: 用于管理日期单元格视图的 `MonthDayBinder` 实例。

- **monthHeaderBinder**: 用于管理头部视图的 `MonthHeaderFooterBinder` 实例。头部视图显示在日历上的每个月份上方。

- **monthFooterBinder**: 用于管理页脚视图的 `MonthHeaderFooterBinder` 实例。页脚视图显示在日历上的每个月份下方。

- **monthMargins**: 要在每个月视图上应用的边距，以像素为单位。这可以用于在两个月之间添加空间。

**`WeekCalendarView` 属性：**

- **weekScrollListener**: 当日历滚动到新的一周时调用。如果 `scrollPaged` 为 `true`，则大多数情况下是有益的。
- **dayBinder**: 用于管理日期单元格视图的 `WeekDayBinder` 实例。
  
- **weekHeaderBinder**: 用于管理头部视图的 `WeekHeaderFooterBinder` 实例。头部视图显示在日历上的每周上方。
  
- **weekFooterBinder**: 用于管理页脚视图的 `WeekHeaderFooterBinder` 实例。页脚视图显示在日历上的每周下方。
  
- **weekMargins**: 要在每周视图上应用的边距，以像素为单位。这可以用于在两个周之间添加空间。

### 方法

**`CalendarView` 方法：**

- **scrollToDate(date: LocalDate)**: 滚动到日历上的特定日期。使用 `smoothScrollToDate()` 可以获得平滑的滚动动画。

- **scrollToMonth(month: YearMonth)**: 滚动到日历上的一个月。使用 `smoothScrollToMonth()` 可以获得平滑的滚动动画。

- **notifyDateChanged(date: LocalDate)**: 重新加载指定日期的视图。

- **notifyMonthChanged(month: YearMonth)**: 重新加载指定月份的页眉、主体和页脚视图。

- **notifyCalendarChanged()**: 重新加载整个日历。

- **findFirstVisibleMonth()** 和 **findLastVisibleMonth()**: 分别查找日历上第一个和最后一个可见的月份。

- **findFirstVisibleDay()** 和 **findLastVisibleDay()**: 分别查找日历上第一个和最后一个可见的日期。

- **updateMonthData()**: 在初始设置后更新 `CalendarView` 的起始月份、结束月份或一周的第一天。当前可见的月份会被保留。日历可以处理非常大的日期范围，因此您可能希望设置具有大日期范围的日历，而不是频繁地更新范围。

**`WeekCalendarView` 方法：**

- **scrollToDate(date: LocalDate)**: 滚动到日历上的特定日期。使用 `smoothScrollToDate()` 可以获得平滑的滚动动画。

- **scrollToWeek(date: LocalDate)**: 滚动到包含此日期的星期。使用 `smoothScrollToWeek()` 可以获得平滑的滚动动画。

- **notifyDateChanged(date: LocalDate)**: 重新加载指定日期的视图。

- **notifyWeekChanged(date: LocalDate)**: 重新加载包含此日期的星期的页眉、主体和页脚视图。

- **notifyCalendarChanged()**: 重新加载整个日历。

- **findFirstVisibleWeek()** 和 **findLastVisibleWeek()**: 分别查找日历上第一个和最后一个可见的星期。

- **findFirstVisibleDay()** 和 **findLastVisibleDay()**: 分别查找日历上第一个和最后一个可见的日期。

- **updateWeekData()**: 在初始设置后更新 `WeekCalendarView` 的起始日期、结束日期或一周的第一天。当前可见的星期会被保留。日历可以处理非常大的日期范围，因此您可能希望设置具有大日期范围的日历，而不是频繁地更新范围。

无需列出所有可用的方法或在此重复文档。请查看 [CalendarView](https://github.com/kizitonwose/Calendar/blob/main/view/src/main/java/com/kizitonwose/calendar/view/CalendarView.kt) 和 [WeekCalendarView](https://github.com/kizitonwose/Calendar/blob/main/view/src/main/java/com/kizitonwose/calendar/view/WeekCalendarView.kt) 类，其中包含所有属性和方法的详细文档。

### 日期点击

您应该在提供给视图容器的视图上设置点击监听器。

日期单元格的 XML 文件 `calendar_day_layout.xml`：

```xml
<!--我们将使用这个 TextView 来显示日期-->
<TextView
    android:id="@+id/calendarDayText"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:textSize="16sp"
    tools:text="22" />
```

当然，你需要将这个文件设置为 `CalendarView` 上的 `cv_dayViewResource`：

```xml
<com.kizitonwose.calendarview.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

在 Fragment 或 Activity 中的点击监听器实现：

```kotlin
class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)
    // 当此容器被绑定时将被设置
    lateinit var day: CalendarDay
    
    init {
        view.setOnClickListener {
            // 使用与此容器关联的 CalendarDay。
        }
    }
}

calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        // 为此容器设置日历日期。
		container.day = data
		// 设置日期文本
		container.textView.text = data.date.dayOfMonth.toString()
		// 任何其他绑定逻辑
    }
}
```

### 日期选择

该库没有内置的选中/未选中日期的概念，这使您可以自由选择最适合实现此用例的方式。

实现日期选择就像在日期绑定器上显示特定日期的背景一样简单。请记住，由于 `CalendarView` 和 `WeekCalendarView` 扩展自 `RecyclerView`，您需要取消不需要的日期上的任何特殊效果。

在这个例子中，我希望在日历上只选择最后点击的日期。

首先，让我们保留对所选日期的引用：

```kotlin
private var selectedDate: LocalDate? = null
```

接下来，使用上面日期点击部分已经显示的视图容器上的点击逻辑，我们在点击日期时更新此字段，并在点击的日期上显示选中背景。

```kotlin
view.setOnClickListener {
    // 检查日期位置，因为我们不希望选择 in 或 out 日期。
    if (day.position == DayPosition.MonthDate) {
        // 保留对先前选择的任何引用
		// 以防我们覆盖它并需要重新加载它。
        val currentSelection = selectedDate
        if (currentSelection == day.date) {
            // 如果用户点击相同的日期，则清除选择。
            selectedDate = null
            // 重新加载此日期以便调用 dayBinder
			// 我们可以移除选择背景。
            calendarView.notifyDateChanged(currentSelection)
        } else {
            selectedDate = day.date
           // 重新加载新选择的日期以便调用 dayBinder
			// 我们可以添加选择背景。
            calendarView.notifyDateChanged(day.date)
            if currentSelection != null {
                // 我们还需要重新加载先前选择的日期，
				// 以便我们可以移除选择背景。
                calendarView.notifyDateChanged(currentSelection)
            }
        }
    }
}
```

最后，我们实现 `dayBinder` 来相应地反映选择：

```kotlin
calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.day = data
        val day = data
        val textView = container.textView
        textView.text = day.date.dayOfMonth.toString()
        if (day.position == DayPosition.MonthDate) {
           	// 显示月份日期。请记住，视图是可重用的！
            textView.visibility = View.VISIBLE
            if (day.date == selectedDate) {
                // 如果这是选定的日期，请显示圆形背景并更改文本颜色。
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundResource(R.drawable.selection_background)
            } else {
                // 如果这不是选定的日期，请移除背景并重置文本颜色。.
                textView.setTextColor(Color.BLACK)
                textView.background = null
            }
        } else {
            // 隐藏 in 和 out 日期
            textView.visibility = View.INVISIBLE
        }
    }
}
```

对于更复杂的选择逻辑（例如范围选择），请参阅示例项目。 很简单，神奇之处在于您的绑定逻辑！

### 禁用日期

正如预期的那样，该库不会在内部提供此逻辑，因此您拥有完全的灵活性。

要禁用日期，您只需将这些日期上的文本设置为禁用并忽略对这些日期的点击即可。 例如，如果我们想要显示开始和结束日期，但禁用它们以便无法选择它们，我们只需在`dayBinder`中设置这些日期的 alpha 属性即可达到禁用的效果。 当然，如果您愿意，您可以设置不同的颜色。

继续日期选择部分中的示例，我们已经使用以下逻辑忽略了输入和输出日期的点击：

```kotlin
view.setOnClickListener {
    // 检查日期位置，因为我们不想选择开始或结束日期。
    if (day.position == DayPosition.MonthDate) {
        // 只使用月份日期
    }
}
```

然后在`dayBinder`中，我们检查日期位置并相应地设置文本 alpha 或颜色：

```kotlin
calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.day = data
        val textView = container.textView
        textView.text = data.date.dayOfMonth.toString()
        textView.alpha = if (day.position == DayPosition.MonthDate) 1f else 0.3f
}
```

现在我们已经介绍了典型用法。 这个库的美妙之处在于它无限的可能性。 您不受如何构建用户界面的限制，该库为您提供所需的日历数据逻辑，并且您提供所需的 UI 逻辑。

有关一些复杂的实现，请参阅示例项目。

## 周视图

正如之前讨论的，该库提供了两个类`CalendarView`和`WeekCalendarView`。`WeekCalendarView`类是一个基于周的日历实现。几乎上面提到的所有适用于月历的主题都适用于周历。主要的区别在于xml属性和类的属性/方法将具有略微不同的名称，通常是以`week`为前缀/后缀，而不是`month`。

例如：`monthHeaderResource` => `weekHeaderResource`，`scrollToMonth()` => `scrollToWeek()`，`findFirstVisibleMonth()` => `findFirstVisibleWeek()`等等，但你可以理解这个思路。

要在布局中显示周历，添加以下视图：

```xml
<com.kizitonwose.calendar.view.WeekCalendarView
    android:id="@+id/weekCalendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

然后按照上面的设置说明提供一个日资源/绑定器等，就像您为月历所做的那样。

如果您想在月视图和周视图之间切换日历，请参见示例应用程序，我们在其中使用了`ValueAnimator`来实现此功能。您可以使用您喜欢的任何动画逻辑。结果如下所示：

<img src="https://user-images.githubusercontent.com/15170090/195636303-a99312c9-23a3-44cd-8a38-6ba21b3c4802.gif" alt="Week and month modes" width="250">

如果您希望在周历中一次显示超过或少于7天，您应将`scrollPaged`属性设置为`false`。此外，将`daySize`属性设置为`FreeForm`，这使您可以自定义日单元的首选大小。请阅读`DaySize`类中的文档，以充分了解可用的选项。

来自示例应用程序的周历实现：

<img src="https://user-images.githubusercontent.com/15170090/195638551-dfced7be-c18f-4611-b015-cfefab480cee.png" alt="Week calendar" width="250">

请记住，到目前为止显示的所有截图只是使用该库可以实现的示例，您绝对可以按照自己的喜好构建您的日历。

**使用这个库创建了漂亮的日历吗？在[这里](https://github.com/kizitonwose/Calendar/issues/1)分享一张图片。**

## 常见问题解答

**问**: 我如何在Java项目使用这个库

**答**: 它可以直接使用，但是 `MonthScrollListener` 不是一个接口而是一个 Kotlin 函数。要在 Java 项目中设置 `MonthScrollListener`，请查看 [此链接](https://github.com/kizitonwose/Calendar/issues/74)。

**问**: 如何禁用用户在日历上滚动，以便只能以编程方式滚动？

**答**: 查阅[此处](https://github.com/kizitonwose/Calendar/issues/38#issuecomment-525786644).

## 迁移

请参阅[迁移指南](https://github.com/kizitonwose/calendar/blob/main/docs_chs/MigrationGuide.chs.md)


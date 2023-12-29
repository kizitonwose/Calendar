# Calendar Compose Documentation

## 目录

- [快速链接](#快速链接)
- [Compose 版本](#Compose 版本)
- [日历 Composables](#日历 Composables)
- [使用方法](#使用方法)
  * [日历状态](#日历状态)
  * [一周的第一天](#一周的第一天)
  * [头部和尾部](#头部和尾部)
  * [日历容器](#日历容器)
  * [Composable 参数](#Composable 参数)
  * [状态属性](#状态属性)
  * [状态方法](#状态方法)
  * [日期点击](#日期点击)
  * [日期选择](#日期选择)
  * [禁用日期](#禁用日期)
- [周历](#周历)
- [热力图日历](#热力图日历)

## 快速链接

如果您还没有查看示例应用程序，请务必查看。大多数您想要实现的技术在示例中已经完成。

下载示例应用程序 [此处](https://github.com/kizitonwose/Calendar/releases/download/2.0.0/sample.apk)

阅读示例应用程序的源代码 [此处](https://github.com/kizitonwose/Calendar/tree/main/sample)

将库添加到您的项目 [此处](https://github.com/kizitonwose/Calendar#setup)

**如果您正在寻找基于视图的文档，您可以在[此处](View.chs.md)找到。**

## Compose 版本

确保您使用的库版本与您项目中的Compose UI版本相匹配。如果您使用的库版本具有比您项目中更高版本的Compose UI，Gradle 将通过传递依赖关系升级您项目中的Compose UI版本。

| Compose UI | Calendar Library |
|:----------:|:----------------:|
|   1.2.x    |      2.0.x       |
|   1.3.x    |  2.1.x - 2.2.x   |
|   1.4.x    |      2.3.x       |
|   1.5.x    |      2.4.x       |
|   1.6.x    |      2.5.x       |

## 日历 Composables

该库可以通过四个可组合项使用：

`HorizontalCalendar()`: 水平滚动的基于月份的日历。

`VerticalCalendar()`: 垂直滚动的基于月份的日历。

`WeekCalendar()`: 水平滚动的基于周的日历。

`HeatMapCalendar()`: 水平滚动的热力图日历，用于显示随时间变化的数据。一个常见的例子是 GitHub 上的用户贡献图。

所有可组合项都基于 LazyRow/LazyColumn 实现以提高效率。

在下面的示例中，我们主要会使用基于月份的 `HorizontalCalendar` 和 `VerticalCalendar` 可组合项，因为所有日历可组合项共享相同的基本概念。如果您想要一个基于周的日历，请使用 `WeekCalendar` 可组合项。在基于周的日历中，大多数具有名称前缀/后缀 `month`（例如 `firstVisibleMonth`）的状态属性/方法在基于周的日历中将具有相应的名称前缀/后缀 `week`（例如 `firstVisibleWeek`）。

## 用法

`HorizontalCalendar` 和 `VerticalCalendar`:

```kotlin
@Composable
fun MainScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // 根据需要进行调整
    val endMonth = remember { currentMonth.plusMonths(100) } // 根据需要进行调整
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // 从库中获取

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    HorizontalCalendar(
        state = state,
        dayContent = { Day(it) }
    )

//    如果您需要垂直日历。
//    VerticalCalendar(
//        state = state,
//        dayContent = { Day(it) }
//    )  
}
```

注意：创建状态时可以提供一个附加参数：“outDateStyle”。 这决定了过时的生成方式。 请参阅 [状态类型](#状态类型) 部分以了解此参数。

`WeekCalendar`:

```kotlin
@Composable
fun MainScreen() {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.now() }
    val startDate = remember { currentMonth.minusMonths(100).atStartOfMonth() } // Adjust as needed
    val endDate = remember { currentMonth.plusMonths(100).atEndOfMonth() } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

    val state = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = currentDate,
        firstDayOfWeek = firstDayOfWeek
    )

    WeekCalendar(
        state = state,
        dayContent = { Day(it) }
    )
}
```

您的 `Day` composable 的最简单形式将是:

```kotlin
@Composable
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f), // 这对于方形大小很重要! 
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString())
    }
}
```

在上面的示例中,我们使用 `Modifier.aspectRatio(1f)`,这是因为日历将月份的宽度除以 7 作为每个日期单元格的宽度。没有设置高度,所以您有灵活性来决定什么对您最有效。
 要在日历上获得典型的正方形外观,您使用 `Modifier.aspectRatio(1f)` 告诉框使其高度与分配的宽度相同。

您可以选择设置特定的高度。例如:`Modifier.height(70.dp)`

**这就是您需要的所有简单用法!但是请继续阅读,还有更多内容!**

### 一周的第一天

当然,您希望在日历上显示适当日期的工作日标题。

`Sun | Mon | Tue | Wed | Thu | Fri | Sat`

这是一个根据用户当前语言环境生成工作日的方法。

```kotlin
val daysOfWeek = daysOfWeek() // 从库中获取
```

函数采用 `firstDayOfWeek` 参数,以防您希望生成工作日,以便期望的日期位于第一个位置。

例如:

```kotlin
val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.THURSDAY)
// Will produce => Thu | Fri | Sat | Sun | Mon | Tue | Wed 
```
使用 `daysOfWeek` 列表，您可以设置日历，使一周的第一天符合用户的预期。这可以是星期日、星期一等。最好使用Locale返回的值，因为这是用户所期望的。

使用提供的 `daysOfWeek` 列表设置日历状态的步骤如下：

```diff
- val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
+ val daysOfWeek = remember { daysOfWeek() }
  val state = rememberCalendarState(
      startMonth = startMonth,
      endMonth = endMonth,
      firstVisibleMonth = currentMonth,
-     firstDayOfWeek = firstDayOfWeek
+     firstDayOfWeek = daysOfWeek.first()
  )
```

您还可以使用 `daysOfWeek` 列表的值来设置工作日标题，以便与日历上显示的内容相匹配。

要设置星期几的标题，可以使用显示在每个月份上的月份标题，允许标题随着月份滚动，或者可以在日历上方的静态可组合项上显示标题。下面两种方式都进行了介绍：

使用静态标题可组合项设置星期几：

```kotlin
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}
```

现在，您可以将标题可组合项与日历一起放在列中：

```kotlin
@Composable
fun MainScreen() {
    Column {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek) // 在这使用标题
        HorizontalCalendar(
            state = state,
            dayContent = { Day(it) }
        )
    }
}
```

要将标题用作月标题，使其随每个月份滚动，请继续阅读下面的月标题和页脚部分！

### 头部和尾部

要为每个月份添加页眉或页脚，步骤与我们为日期使用 `dayContent` 日历参数所做的相同，但不同的是，您需要提供 `monthHeader` 或 `monthFooter` 可组合参数，而不是 `dayContent`。

要将星期几的标题作为月份页眉添加，我们可以将上面讨论的相同的 `DaysOfWeekTitle` 可组合参数设置为 `monthHeader`：

```kotlin
@Composable
fun MainScreen() {
    HorizontalCalendar(
        state = state,
        dayContent = { Day(it) },
        monthHeader = {
            DaysOfWeekTitle(daysOfWeek = daysOfWeek) //将月头作为标题
        }
    )
}
```

In the code above, we use the same `daysOfWeek` list that was created when we initialized the calendar state. However, we can also get the `daysOfWeek` list from the month data passed into the `monthHeader` parameter:

```kotlin
@Composable
fun MainScreen() {
    HorizontalCalendar(
        state = state,
        dayContent = { Day(it) },
        monthHeader = { month ->
            // 您可能希望在此处使用 `remember {}`，以便映射不是每次都进行，
			// 因为星期几的顺序只有在状态中设置新值给 `firstDayOfWeek` 时才会更改。
            val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
            MonthHeader(daysOfWeek = daysOfWeek)
        }
    )
}
```

在添加了星期几的标题后，您可以拥有一个类似于这样的日历：

<img src="https://user-images.githubusercontent.com/15170090/195415979-b9e46c16-3652-433e-a85d-e1d05c25ca8b.png" alt="Month calendar" width="300">

您不仅可以将星期几的标题用作标题，还可以在日历之外的其他位置显示月份名称，如果尚未显示的话。随意发挥创意，制作独特的月标题和页脚！对于复杂的用法，请参阅示例项目。

### 日历容器

日历可组合项的两个有趣参数是 `monthBody` 和 `monthContainer`。通常情况下，您不需要这些。但如果您想在渲染日历之前进行一些自定义操作，那么这是适合的地方。

例如，如果您想在渲染所有日期的容器后面绘制一个渐变，并在整个月容器周围添加圆角/边框，并且还想缩小整个月容器，使其不适应屏幕宽度，那么 `monthBody` 和 `monthContainer` 将是：

```kotlin
@Composable
fun MainScreen() {
    HorizontalCalendar(
        // 绘制日期内容的渐变
        monthBody = { _, content ->
            Box(
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFB2EBF2),
                            Color(0xFFB2B8F2)
                        )
                    )
                )
            ) {
                content() // 渲染提供的容器
            }
        },
        // 添加角落/边框和月份宽度。
        monthContainer = { _, container ->
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            Box(
                modifier = Modifier
                    .width(screenWidth * 0.73f)
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
                    .border(
                        color = Color.Black,
                        width = 1.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                container() // 渲染提供的内容
            }
        }
    )
}
```

通过上面的 `monthBody` 和 `monthContainer` 配置，我们将得到这个日历：

<img src="https://user-images.githubusercontent.com/15170090/195417341-fc263e3c-7468-47f0-84be-db6a76e29f8f.png" alt="Background styles" width="250">

### Composable 参数

- **calendarScrollPaged**: 日历的滚动行为。当设置为 `true` 时，在滚动或滑动操作后，日历将对齐到最近的月份。当设置为 `false` 时，日历将正常滚动。
- **userScrollEnabled**: 是否允许通过用户手势或辅助功能操作进行滚动。即使禁用了用户滚动，您仍然可以通过状态进行编程方式的滚动。从 LazyRow/LazyColumn 继承。
- **reverseLayout**: 反转滚动和布局的方向。当设置为 `true` 时，月份将从末尾到开头组成，而 `startMonth` 将位于末尾。从 LazyRow/LazyColumn 继承。
- **contentPadding**: 整个日历周围的内边距。这将在内容被剪切后添加内边距，这是通过 `modifier` 参数不可能实现的。从 LazyRow/LazyColumn 继承。

### 状态类型

在通过 `rememberCalendarState()` 或 `rememberWeekCalendarState()` 创建状态时设置的所有属性都可以通过状态对象中相应的属性在未来进行更新。状态对象中还有其他一些值得一提的有趣属性。

**`HorizontalCalendar` 和 `VerticalCalendar` 的 `CalendarState` 属性:**

- **firstVisibleMonth**: 在日历上可见的第一个月份。

- **lastVisibleMonth**: 在日历上可见的最后一个月份。

- **layoutInfo**: 在上一次布局传递期间计算的 `LazyListLayoutInfo` 的子类。例如，您可以使用它来计算当前可见的项目。

- **isScrollInProgress**: 当前此日历是否正在通过手势、滑动或编程方式进行滚动。

- **outDateStyle**: 确定日历上每个月如何生成 `outDates`。它可以是以下两个值之一：

    1. **EndOfRow**: 日历将生成 `outDates` 直到达到月份行的末尾。这意味着如果一个月有5行，它将显示5行，如果一个月有6行，它将显示6行。
    2. **EndOfGrid**: 日历将生成 `outDates` 直到达到每个月的 6 x 7 网格的末尾。这意味着所有月份都将有6行。

    此值还可以在通过 `rememberCalendarState(outDateStyle = ...)` 初始化日历状态时提供。

如果您想知道 `outDates` 和 `inDates` 是什么意思，让我们以下面的截图为例。

<img src="https://user-images.githubusercontent.com/15170090/197358602-c9c6f796-fb28-4c82-9101-458d7a66f3a0.png" alt="in-dates and out-dates" width="300">

在图像中，绿色注释中的日期是“inDates”，红色注释中的日期是“outDates”，而没有注释的日期是“monthDates”。 您可以在渲染日历日时检查这一点。 为了在图像上实现精确的效果，我们更新了“Day”可组合项：

```kotlin
@Composable
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Color.White else Color.Gray
        )
    }
}
```

- `inDates` 的 `position` 属性设置为 `DayPosition.InDate`

    `outDates` 的 `position` 属性设置为 `DayPosition.OutDate`

    `monthDates` 的 `position` 属性设置为 `DayPosition.MonthDate`，如上述代码片段中所见。

    **`WeekCalendar` 的 `WeekCalendarState` 属性:**

    - **firstVisibleWeek**: 在日历上可见的第一个星期。

    - **lastVisibleWeek**: 在日历上可见的最后一个星期。

    - **layoutInfo**: 在上一次布局传递期间计算的 `LazyListLayoutInfo` 的子类。例如，您可以使用它来计算当前可见的项目。

    - **isScrollInProgress**: 当前此日历是否正在通过手势、滑动或编程方式进行滚动。

### 状态方法

**`CalendarState` 方法:**

- **scrollToMonth(month: YearMonth)**: 立即滚动到日历上的特定月份，无需动画。

- **animateScrollToMonth(month: YearMonth)**: 使用平滑的滚动动画滚动到日历上的一个月。

**`WeekCalendarState` 方法:**

- **scrollToWeek(date: LocalDate)**: 立即滚动到包含给定日期的日历周，无需动画。

- **animateScrollToWeek(date: LocalDate)**: 使用平滑的滚动动画滚动到包含给定日期的日历周。

无需在此重复文档。请查看相应的类以获取所有可用属性和方法的详细文档。

### 日期点击

您可以像处理任何其他可组合项一样通过修饰符处理“Day”可组合项中的点击：

```kotlin
@Composable
fun Day(day: CalendarDay, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString())
    }
}
```

### 日期选择

该库没有内置的选中/未选中日期的概念，这使您可以自由选择如何最好地实现此用例。

实现日期选择就像在 `Day` 可组合项中显示特定日期的背景一样简单。

例如，我只想要在日历上选择最后点击的日期。

首先，我们更新我们的 `Day` 可组合项，以在选择日期时显示一个圆形背景：

```kotlin
@Composable
fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = if (isSelected) Color.Green else Color.Transparent)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString())
    }
}
```

接下来，使用上面日期点击部分已经展示的点击逻辑，我们在每次点击日期时更新选定日期的状态：

```kotlin
@Composable
fun MainScreen() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    HorizontalCalendar(
        state = state,
        dayContent = { day ->
            Day(day, isSelected = selectedDate == day.date) { day ->
                selectedDate = if (selectedDate == day.date) null else day.date
            }
        }
    )
}
```

对于更复杂的选择逻辑，比如范围选择，请参阅示例项目。这相当简单，其中的奥秘都在于您的逻辑！

### 禁用日期

正如预期的那样，该库不会在内部提供此逻辑，因此您具有完全的灵活性。

要禁用日期，您可以简单地将这些日期的文本设置为看起来已禁用，并禁用对这些日期的点击。例如，如果我们想显示 in 和 out 日期但禁用它们以防止选择，我们只需在文本上设置不同的颜色。

实际上，在日期点击部分的示例中，我们已经使用以下逻辑忽略了 in 和 out 日期的点击：

```kotlin
@Composable
fun Day(day: CalendarDay, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(
                enabled = day.position == DayPosition.MonthDate, // 只有月历可以被点击
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) { // 改变 in-dates 和 out-dates 的颜色, 您也可以完全隐藏它们！
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Color.White else Color.Gray
        )
    }
}
```

现在我们已经介绍了典型用法。 图书馆的美妙之处在于它无限的可能性。 您不受如何构建用户界面的限制，该库为您提供所需的日历数据逻辑，并且您提供所需的 UI 逻辑。

有关一些复杂的实现，请参阅示例项目。

## 周历

正如之前讨论的，该库提供了 `HorizontalCalendar`、`VerticalCalendar` 和 `WeekCalendar` 可组合项。`WeekCalendar` 是一个基于周的日历。几乎所有上面介绍的关于月份日历的主题都适用于周历。主要的区别是状态属性/方法通常会有一个略有不同的名称，通常是带有 `week` 前缀/后缀而不是 `month`。

例如：`firstVisibleMonth` => `firstVisibleWeek`，`scrollToMonth()` => `scrollToWeek()` 等等，但您已经get到了这个概念。

我们已经在[用法](#用法)部分中展示了如何使用 `WeekCalendar`，但在最基本的形式中，它是：

```kotlin
@Composable
fun MainScreen() {
    val state = rememberWeekCalendarState(
        startDate = ...,
        endDate = ...,
        firstVisibleWeekDate = ...,
        firstDayOfWeek = ...
    )
    WeekCalendar(
        state = state,
        dayContent = { Day(it) }
    )  
}
```

来自示例应用程序的周历实现：

<img src="https://user-images.githubusercontent.com/15170090/195638551-dfced7be-c18f-4611-b015-cfefab480cee.png" alt="Week calendar" width="250">

如果您想在月份和周模式之间切换日历，请参阅示例应用程序，我们在其中通过动画修改 `Modifier` 的高度以及使用 `AnimatedVisibility` API 进行了切换。

## 热力图日历

这是一个水平滚动的热力图日历实现，用于展示随时间变化的数据。一个常见的例子是 GitHub 上的用户贡献图表。另一个用途可能是展示用户追踪的习惯频率的变化。

示例应用程序中的截图如下：

<img src="https://user-images.githubusercontent.com/15170090/195638552-4c25cf23-d311-4d95-bff0-f1917f4bab8b.png" alt="HeatMap calendar" width="250">

所有在基于月份的日历中的属性在热力图日历中也是可用的，除了 `OutDateStyle` 配置，因为在这种情况下这是不相关的。请注意，日历上有过时的日期，但由于日期是以列而不是行的方式布局的，因此这里不需要两个 `OutDateStyle` 选项 `EndOfRow` 和 `EndOfGrid`。所有其他基于月份的属性都是可用的！

基本的热力图日历用法：

```kotlin
@Composable
fun MainScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // 根据需要进行调整
    val endMonth = remember { currentMonth.plusMonths(100) } // 根据需要进行调整
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // 从库中获取

    val state = rememberHeatMapCalendarState(
        startMonth = startMonth,
        endMonth = startMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
    )
    HeatMapCalendar(
        state = state,
        dayContent = { day, _ -> Day(day) },
        weekHeader = { WeekHeader(it) },
        monthHeader = { MonthHeader(it) }
    )
}
```

请查看 `HeatMapCalendar` 可组合项以获取完整的文档。示例应用程序中也有示例。

请记住，到目前为止显示的所有截图都只是使用该库可以实现的示例，您绝对可以根据自己的需要构建您的日历。

使用这个库创建了漂亮的日历吗？在[这里]()分享一张图片

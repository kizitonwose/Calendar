# Calendar Compose Documentation

## Table of Contents

- [Quick links](#quick-links)
- [Compose Multiplatform Information](#compose-multiplatform-information)
- [Compose UI version compatibility](#compose-ui-version-compatibility)
- [Calendar Composables](#calendar-composables)
- [Usage](#usage)
  * [Calendar state](#usage)
  * [First day of the week](#first-day-of-the-week-and-day-of-week-titles)
  * [Headers and footers](#month-headers-and-footers)
  * [Calendar containers](#calendar-containers)
  * [Composable parameters](#other-composable-parameters)
  * [State properties](#state-properties)
  * [State methods](#state-methods)
  * [Date clicks](#date-clicks)
  * [Date Selection](#date-selection)
  * [Disabling dates](#disabling-dates)
- [Week calendar](#week-calendar)
- [HeatMap calendar](#heatmap-calendar)
- [Year calendar](#year-calendar)

## Quick links

Check out the sample app if you have not yet done so. Most techniques that you would want to implement are already done in the examples.

Download the Android sample app [here](https://github.com/kizitonwose/Calendar/releases/download/2.5.4/sample.apk)

Read the Android sample app's source code [here](https://github.com/kizitonwose/Calendar/tree/main/sample)

View the multiplatform sample project online at https://calendar.kizitonwose.dev

Read the multiplatform sample project's source code [here](https://github.com/kizitonwose/Calendar/tree/main/compose-multiplatform/sample)

Add the library to your project [here](https://github.com/kizitonwose/Calendar#setup)

**If you are looking for the view-based documentation, you can find it [here](https://github.com/kizitonwose/Calendar/blob/main/docs/View.md)**

## Compose Multiplatform Information

The APIs for the compose libraries for Android and Multiplatform projects have been designed such that you can copy examples across both projects and they would work without code changes as the classes have the same names and package declarations. The only difference in some cases would be that the code for the Android calendar library needs to import classes such as `LocalDate`, `YearMonth` and `Year` from the `java.time` package while the multiplaform calendar library needs to import such classes from the [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) library. 

Note that the `Year` class does not yet exist in the `kotlinx-datetime` library, therefore the multiplatform calendar library includes a minimal `Year` class implementation to bridge this gap.

## Compose UI version compatibility

Ensure that you are using the library version that matches the Compose UI version in your project. If you use a version of the library that has a higher version of Compose UI than the one in your project, gradle will upgrade the Compose UI version in your project via transitive dependency. See the compatibility table [here](https://github.com/kizitonwose/Calendar#compose-ui-version-compatibility).

## Calendar Composables

The library can be used via six composables:

`HorizontalCalendar()`: Horizontally scrolling month-based calendar.

`VerticalCalendar()`: Vertically scrolling month-based calendar.

`WeekCalendar()`: Horizontally scrolling week-based calendar.

`HeatMapCalendar()`: Horizontally scrolling heatmap calendar, useful for showing how data changes over time. A popular example is the user contribution chart on GitHub.

`HorizontalYearCalendar()`: Horizontally scrolling year-based calendar.

`VerticalYearCalendar()`: Vertically scrolling year-based calendar.

All composables are based on LazyRow/LazyColumn for efficiency.

In the examples below, we will mostly use the month-based `HorizontalCalendar`
and `VerticalCalendar` composables since all the calendar composables share the same basic concept.
If you need a week-based calendar, use the `WeekCalendar` composable instead. If you need a
year-based calendar, use the `HorizontalYearCalendar` and `VerticalYearCalendar` composables.

Most state properties/methods with the name prefix/suffix `month` (e.g `firstVisibleMonth`) in the
month-base calendar will have an equivalent with the name prefix/suffix `week` (
e.g `firstVisibleWeek`) in the week-based calendar and `year` (e.g `firstVisibleYear`) in the
year-based calendar.

## Usage

`HorizontalCalendar` and `VerticalCalendar`:

```kotlin
@Composable
fun MainScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

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

//    If you need a vertical calendar.
//    VerticalCalendar(
//        state = state,
//        dayContent = { Day(it) }
//    )  
}
```

Note: There is an additional parameter that can be provided when creating the state: `outDateStyle`. This determines how the out-dates are generated. See the [properties](#state-properties) section to understand this parameter.

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

`HorizontalYearCalendar` and `VerticalYearCalendar`:

```kotlin
@Composable
fun MainScreen() {
    val currentYear = remember { Year.now() }
    val startYear = remember { currentYear.minusYears(100) } // Adjust as needed
    val endYear = remember { currentYear.plusYears(100) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

    val state = rememberYearCalendarState(
        startYear = startYear,
        endYear = endYear,
        firstVisibleYear = currentYear,
        firstDayOfWeek = firstDayOfWeek,
    )
    HorizontalYearCalendar(
        state = state,
        dayContent = { Day(it) },
    )

//    If you need a vertical year calendar.
//    VerticalYearCalendar(
//        state = state,
//        dayContent = { Day(it) }
//    )
}
```

Your `Day` composable in its simplest form would be:

```kotlin
@Composable
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f), // This is important for square sizing!
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString())
    }
}
```

In the above example, we use `Modifier.aspectRatio(1f)`, this is because the calendar assigns the width of the month divided by 7 as the width of each day cell. No height is set, so you have the flexibility to decide what works best for you. 
To get the typical square look on a calendar, you use `Modifier.aspectRatio(1f)` which tells the box to make its height the same size as the assigned width.

You can choose to set a specific height if you want. For example: `Modifier.height(70.dp)`

**And that's all you need for simple usage! But keep reading, there's more!**

### First day of the week and Day of week titles.

Of course, you want to show the day of week titles on the appropriate days on the calendar.

`Sun | Mon | Tue | Wed | Thu | Fri | Sat`

Here's a method that generates the weekdays from the user's current Locale. 

```kotlin
val daysOfWeek = daysOfWeek() // Available in the library
```

The function takes a `firstDayOfWeek` parameter in case you want to generate the days of week such that the desired day is at the first position.

For example:

```kotlin
val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.THURSDAY)
// Will produce => Thu | Fri | Sat | Sun | Mon | Tue | Wed 
```
Using the `daysOfWeek` list, you can set up the calendar so the first day of the week is what the user would expect. This could be Sunday, Monday, etc. It is good practice to use what the Locale returns as that's what the user would expect.

To set up the calendar state using the provided `daysOfWeek` list:

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

You should also use the `daysOfWeek` list values to set up the weekday titles, this way it matches what is shown on the calendar.

To set up the day of week titles, you can either use the month header which would show the titles on every month and allow the titles to scroll with the month, or you can show the title on a static composable above the calendar. Both ways are covered below:

Setup days of week using a static title composable:

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

Now you can use the title composable with the calendar in a column:

```kotlin
@Composable
fun MainScreen() {
    Column {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek) // Use the title here
        HorizontalCalendar(
            state = state,
            dayContent = { Day(it) }
        )
    }
}
```

To use the titles as a month header so it scrolls with each month, continue to the month headers and footers section below!

### Month headers and footers.

To add a header or footer to each month, the procedure is the same as we did for the day using the `dayContent` calendar parameter, but instead of the `dayContent`, you have to provide the `monthHeader` or `monthFooter` composable parameter.

To add the days of week titles as the month header, we can set the same `DaysOfWeekTitle` composable discussed above as the `monthHeader` parameter:

```kotlin
@Composable
fun MainScreen() {
    HorizontalCalendar(
        state = state,
        dayContent = { Day(it) },
        monthHeader = {
            DaysOfWeekTitle(daysOfWeek = daysOfWeek) // Use the title as month header
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
            // You may want to use `remember {}` here so the mapping is not done 
            // every time as the days of week order will never change unless 
            // you set a new value for `firstDayOfWeek` in the state.
            val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
            MonthHeader(daysOfWeek = daysOfWeek)
        }
    )
}
```

With the day of week titles added, you can have a calendar that looks like this:

<img src="https://user-images.githubusercontent.com/15170090/195415979-b9e46c16-3652-433e-a85d-e1d05c25ca8b.png" alt="Month calendar" width="300">

You can do more than just use the day titles as the header. For example, you can also show the month name if it is not already shown somewhere outside the calendar. Feel free to get creative with your month headers and footers! For complex usages, please see the sample project.

### Calendar containers

Two interesting parameters of the calendar composable are the `monthBody` and `monthContainer`. You will typically not need these. But if you want to do some customizations before the calendar is rendered, then this is the place for it.

For example, if you want to draw a gradient behind the container where all the days are rendered and add rounded corners/borders to the entire month container and also shrink the entire month container so it does not fit the screen width, the `monthBody` and `monthContainer` will be:

```kotlin
@Composable
fun MainScreen() {
    HorizontalCalendar(
        // Draw the day content gradient.
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
                content() // Render the provided content!
            }
        },
        // Add the corners/borders and month width.
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
                container() // Render the provided container!
            }
        }
    )
}
```

With the above `monthBody` and `monthContainer` configurations, we will have this calendar:

<img src="https://user-images.githubusercontent.com/15170090/195417341-fc263e3c-7468-47f0-84be-db6a76e29f8f.png" alt="Background styles" width="250">

### Other composable parameters.

- **calendarScrollPaged**: The scrolling behavior of the calendar. When `true`, the calendar will snap to the nearest month after a scroll or swipe action. When `false`, the calendar scrolls normally.

- **userScrollEnabled**: Whether scrolling via the user gestures or accessibility actions is allowed. You can still scroll programmatically using the state even when it is disabled. Inherited from LazyRow/LazyColumn.

- **reverseLayout**: reverse the direction of scrolling and layout. When `true`, months will be composed from the end to the start and `startMonth` will be located at the end. Inherited from LazyRow/LazyColumn.

- **contentPadding**: A padding around the whole calendar. This will add padding for the content after it has been clipped, which is not possible via `modifier` parameter. Inherited from LazyRow/LazyColumn.

### State properties

All properties set when creating the state via `rememberCalendarState()` or `rememberWeekCalendarState()` can be updated in the future via the appropriate property in the state object. There are also other interesting properties in the state objects worth mentioning.

**`CalendarState` properties for `HorizontalCalendar` and `VerticalCalendar`:**

- **firstVisibleMonth**: The first month that is visible on the calendar.

- **lastVisibleMonth**: The last month that is visible on the calendar.

- **layoutInfo**: A subclass of `LazyListLayoutInfo` calculated during the last layout pass. For example, you can use it to calculate what items are currently visible.

- **isScrollInProgress**: Whether this calendar is currently scrolling by gesture, fling, or programmatically.

- **outDateStyle**: This determines how outDates are generated for each month on the calendar. It can be one of two values:
    1. **EndOfRow**: The calendar will generate `outDates` until it reaches the end of the month row. This means that if a month has 5 rows, it will display 5 rows and if a month has 6 rows, it will display 6 rows.
    2. **EndOfGrid**: The calendar will generate `outDates` until it reaches the end of a 6 x 7 grid on each month. This means that all months will have 6 rows.

    This value can also be provided when the calendar state is initialized via `rememberCalendarState(outDateStyle = ...)`.

If you are wondering what `outDates` and `inDates` mean, let's use the screenshot below as an example.

<img src="https://user-images.githubusercontent.com/15170090/197358602-c9c6f796-fb28-4c82-9101-458d7a66f3a0.png" alt="in-dates and out-dates" width="300">

In the image, the dates within the green annotation are `inDates`, the ones within the red annotation are `outDates` while those without annotation are `monthDates`. You can check for this when your calendar day is rendered. To achieve the exact effect on the image, we update our `Day` composable: 

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

`inDates` have their `position` property set to `DayPosition.InDate`

`outDates` have their `position` property set to `DayPosition.OutDate`

`monthDates` have their `position` property set to `DayPosition.MonthDate` as seen in the code snippet above.

**`WeekCalendarState` properties for `WeekCalendar`:**

- **firstVisibleWeek**: The first week that is visible on the calendar.

- **lastVisibleWeek**: The last week that is visible on the calendar.

- **layoutInfo**: A subclass of `LazyListLayoutInfo` calculated during the last layout pass. For example, you can use it to calculate what items are currently visible.

- **isScrollInProgress**: Whether this calendar is currently scrolling by gesture, fling, or programmatically.

**`YearCalendarState` properties for `HorizontalYearCalendar` and `VerticalYearCalendar`:**

- **firstVisibleYear**: The first year that is visible on the calendar.

- **lastVisibleYear**: The last year that is visible on the calendar.

- **layoutInfo**: A subclass of `LazyListLayoutInfo` calculated during the last layout pass. For example, you can use it to calculate what items are currently visible.

- **isScrollInProgress**: Whether this calendar is currently scrolling by gesture, fling, or programmatically.

### State methods

**`CalendarState`**

- **scrollToDate(date: LocalDate)**: Instantly scroll to a date on the calendar without an animation.

- **animateScrollToDate(date: LocalDate)**: Scroll to a date on the calendar with smooth scrolling animation.

- **scrollToMonth(month: YearMonth)**: Instantly scroll to a month on the calendar without an animation.

- **animateScrollToMonth(month: YearMonth)**: Scroll to a month on the calendar with smooth scrolling animation.

**`WeekCalendarState`**

- **scrollToDate(date: LocalDate)**: Instantly scroll to a date on the calendar without an animation.

- **animateScrollToDate(date: LocalDate)**: Scroll to a date on the calendar with smooth scrolling animation.

- **scrollToWeek(date: LocalDate)**: Instantly scroll to the week containing the given date on the calendar without an animation.

- **animateScrollToWeek(date: LocalDate)**: Scroll to the week containing the given date on the calendar with smooth scrolling animation.

**`YearCalendarState`**

- **scrollToDate(date: LocalDate)**: Instantly scroll to a date on the calendar without an animation.

- **animateScrollToDate(date: LocalDate)**: Scroll to a date on the calendar with smooth scrolling animation.

- **scrollToMonth(month: YearMonth)**: Instantly scroll to a month on the calendar without an animation.

- **animateScrollToMonth(month: YearMonth)**: Scroll to a month on the calendar with smooth scrolling animation.

- **scrollToYear(year: Year)**: Instantly scroll to a year on the calendar without an animation.

- **animateScrollToYear(year: Year)**: Scroll to a year on the calendar with smooth scrolling animation.

There's no need to repeat the documentation here. Please see the relevant class for all properties and methods available with proper documentation.

### Date clicks

You can handle clicks in your `Day` composable as you would for any other composable via the modifier: 

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

### Date Selection

The library has no inbuilt concept of selected/unselected dates, this gives you the freedom to choose how best you would like to implement this use case.

Implementing date selection is as simple as showing a background on a specific date in the `Day` composable. 

For this example, I want only the last clicked date to be selected on the calendar.

Firstly, we update our `Day` composable to show a circle background if the date is selected:

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

Next, using the click logic already shown in the date click section above, we update the selected date state whenever a date is clicked:

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

For more complex selection logic like range selection, please see the sample project. It's quite simple, the magic is all in your logic!

### Disabling dates

As expected, the library does not provide this logic internally so you have complete flexibility.

To disable dates, you can simply set the texts on those dates to look disabled and disable clicks on those dates. For example, if we want to show in and out dates but disable them so that they cannot be selected, we can just set a different color on the texts.

We actually already did this with the example in the date click section, we already ignore clicks for in and out dates using this logic:

```kotlin
@Composable
fun Day(day: CalendarDay, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(
                enabled = day.position == DayPosition.MonthDate, // Only month-dates are clickable.
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) { // Change the color of in-dates and out-dates, you can also hide them completely!
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Color.White else Color.Gray
        )
    }
}
```

And we've now covered the typical usage. The beauty of the library is in its limitless possibilities. You are not constrained on how to build your user interface, the library provides you with the needed calendar data logic and you provide the desired UI logic. 

See the sample project for some complex implementations.

## Week calendar

The `WeekCalendar` is a week-based calendar. Almost all topics covered above for the month calendar
will apply to the week calendar. The main difference is that state properties/methods will have a
slightly different name, typically with a `week` prefix/suffix instead of `month`.

For example: `firstVisibleMonth` => `firstVisibleWeek`, `scrollToMonth()` => `scrollToWeek()` and many others, but you get the idea.

We already showed how to use the `WeekCalendar` previously in the [usage](#usage) section, but in the most basic form, it is:

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

A week calendar implementation from the sample app:

<img src="https://user-images.githubusercontent.com/15170090/195638551-dfced7be-c18f-4611-b015-cfefab480cee.png" alt="Week calendar" width="250">

If you would like to toggle the calendar between month and week modes, please see the sample app where we did this by animating the Modifier height and alternatively using the `AnimatedVisibility` API.

## HeatMap calendar

This is a horizontally scrolling heatmap calendar implementation, useful for showing how data changes over time. A popular example is the user contribution chart on GitHub. Another usage could be to show changes in the frequency of a habit tracked by a user.

A screenshot from the sample app is shown below:

<img src="https://user-images.githubusercontent.com/15170090/195638552-4c25cf23-d311-4d95-bff0-f1917f4bab8b.png" alt="HeatMap calendar" width="250">

All the properties in the month-based calendar are also available in the HeatMap calendar except `OutDateStyle` configuration as that is not relevant in this case. Note that there are out-dates on the calendar but since the dates are laid out in columns instead of rows, the two `OutDateStyle` options `EndOfRow` and `EndOfGrid` are not needed here. All other month-based properties are available!

Basic HeatMap calendar usage:

```kotlin
@Composable
fun MainScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

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

Please see the `HeatMapCalendar` composable for the full documentation. There are also examples in the sample app.

## Year calendar

The year-based calendar is best suited for large screens and can be used via
the `HorizontalYearCalendar` and `VerticalYearCalendar` composables. All topics covered above for
the month calendar will apply to the year calendar. The main difference is that state
properties/methods will have a slightly different name, typically with a `year` prefix/suffix
instead of `month`.

For example: `firstVisibleMonth` => `firstVisibleYear`, `scrollToMonth()` => `scrollToYear()` and
many others, but you get the idea.

The `monthHeader` and `monthFooter` parameters are available in both the month and year calendars
and serve the same purpose in both cases. The year calendar additionally provides the `yearHeader`
and `yearFooter` parameters to add a header or footer to each year on the calendar.

Basic year calendar usage:

```kotlin
@Composable
fun MainScreen() {
    val currentYear = remember { Year.now() }
    val startYear = remember { currentYear.minusYears(100) } // Adjust as needed
    val endYear = remember { currentYear.plusYears(100) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

    val state = rememberYearCalendarState(
        startYear = startYear,
        endYear = endYear,
        firstVisibleYear = currentYear,
        firstDayOfWeek = firstDayOfWeek,
    )
    HorizontalYearCalendar(
        state = state,
        dayContent = { Day(it) },
        yearHeader = { YearHeader(it) },
        monthHeader = { MonthHeader(it) },
    )

//    If you need a vertical year calendar.
//    VerticalYearCalendar(
//        state = state,
//        dayContent = { Day(it) }
//    )
}
```

There is an additional `outDateStyle` parameter that can be provided when creating the state
via `rememberYearCalendarState`. This determines how the out-dates are generated. See
the [properties](#state-properties) section to understand this parameter.

A year calendar implementation from the sample app:

<img src="https://github.com/user-attachments/assets/b2fcae94-341c-4f35-a997-e8d95a23efb4" alt="Year calendar" width="500">

The year calendar composables also provide a parameter `isMonthVisible` which determines if a month
is added to the calendar year grid. For example, if you want a calendar that starts in the year 2024
and ends in the year 2054, but only shows months from October 2024, the logic would look like
this:

```kotlin
@Composable
fun MainScreen() {
    val october2024 = remember { YearMonth.of(2024, Month.OCTOBER) }
    val startYear = remember { Year.of(2024) }
    val endYear = remember { Year.of(2054) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } 

    val state = rememberYearCalendarState(
        startYear = startYear,
        endYear = endYear,
        firstVisibleYear = startYear,
        firstDayOfWeek = firstDayOfWeek,
    )
    HorizontalYearCalendar(
        state = state,
        dayContent = { Day(it) },
        yearHeader = { YearHeader(it) },
        monthHeader = { MonthHeader(it) },
        isMonthVisible = { data ->
            data.yearMonth >= october2024
        }
    )
}
```

The logic above will produce this result:

<img src="https://github.com/user-attachments/assets/80ff60c0-91c3-4d6f-89d5-c719d1981c0d" alt="Year calendar" width="500">

Remember that all the screenshots shown so far are just examples of what you can achieve with the library and you can absolutely build your calendar to look however you want.

**Made a cool calendar with this library? Share an image [here](https://github.com/kizitonwose/Calendar/issues/1).**

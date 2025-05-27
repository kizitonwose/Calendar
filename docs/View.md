# Calendar View Documentation

## Table of Contents

- [Quick links](#quick-links)
- [Class information](#class-information)
- [Usage](#usage)
  * [Setup](#step-1)
  * [First day of the week](#first-day-of-the-week-and-day-of-week-titles)
  * [Headers and footers](#month-headers-and-footers)
  * [Attributes](#attributes)
  * [Properties](#properties)
  * [Methods](#methods)
  * [Date clicks](#date-clicks)
  * [Date Selection](#date-selection)
  * [Disabling dates](#disabling-dates)
- [Week view](#week-view)
- [Year view](#year-view)
- [FAQ](#faq)
- [Migration](#migration)

## Quick links

Check out the sample app if you have not yet done so. Most techniques that you would want to implement are already done in the examples.

Download the sample app [here](https://github.com/kizitonwose/Calendar/releases/download/2.5.4/sample.apk)

Read the sample app's source code [here](https://github.com/kizitonwose/Calendar/tree/main/sample)

Add the library to your project [here](https://github.com/kizitonwose/Calendar#setup)

**If you are looking for the compose documentation, you can find it [here](https://github.com/kizitonwose/Calendar/blob/main/docs/Compose.md)**

## Class information

The library can be used via three classes: 

`CalendarView`: The typical month-based calendar.

`WeekCalendarView`: The week-based calendar.

`YearCalendarView`: The year-based calendar.

These classes extend from `RecyclerView` so you can use all `RecyclerView` customizations like decorators etc.

In the examples below, we will mostly use the `CalendarView` class since the three classes share the same basic concept. If you want a week-based calendar, replace `CalendarView` in your xml/code with `WeekCalendarView`. If you want a year-based calendar, replace `CalendarView` in your xml/code with `YearCalendarView`. 

Most xml attributes and class properties/methods with the name prefix/suffix `month` (e.g `monthHeaderResource`) in the `CalendarView` will have an equivalent with the name prefix/suffix `week` (e.g `weekHeaderResource`) in the `WeekCalendarView` and the name prefix/suffix `year` (e.g `yearHeaderResource`) in the `YearCalendarView`.

## Usage

#### Step 1

Add CalendarView to your XML like any other view.

```xml
<com.kizitonwose.calendar.view.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```
See all available [attributes](#attributes).

Create your day view resource in `res/layout/calendar_day_layout.xml`.

```xml
<TextView
    android:id="@+id/calendarDayText"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:textSize="16sp"
    tools:text="22" />
```

Create your view container which acts as a view holder for each date cell.
The view passed in here is the inflated day view resource which you provided.

```kotlin
class DayViewContainer(view: View) : ViewContainer(view) {    
    val textView = view.findViewById<TextView>(R.id.calendarDayText)

    // With ViewBinding
    // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
}
```

Provide a `MonthDayBinder` for the CalendarView using your `DayViewContainer` type.

```kotlin
calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    // Called only when a new container is needed.
    override fun create(view: View) = DayViewContainer(view)
    
    // Called every time we need to reuse a container.
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.textView.text = data.date.dayOfMonth.toString()
    }
}
```

#### Step 2

Setup the desired dates in your Fragment or Activity:

**`CalendarView` setup:**
```kotlin
val currentMonth = YearMonth.now()
val startMonth = currentMonth.minusMonths(100) // Adjust as needed
val endMonth = currentMonth.plusMonths(100) // Adjust as needed
val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library
calendarView.setup(startMonth, endMonth, firstDayOfWeek)
calendarView.scrollToMonth(currentMonth)
```

**`WeekCalendarView` setup:**

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
val startDate = currentMonth.minusMonths(100).atStartOfMonth() // Adjust as needed
val endDate = currentMonth.plusMonths(100).atEndOfMonth() // Adjust as needed
val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library
weekCalendarView.setup(startDate, endDate, firstDayOfWeek)
weekCalendarView.scrollToWeek(currentDate)
```

**`YearCalendarView` setup:**

```diff
- <com.kizitonwose.calendar.view.CalendarView
+ <com.kizitonwose.calendar.view.YearCalendarView
    android:id="@+id/yearCalendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

```kotlin
val currentYear = Year.now()
val startYear = currentYear.minusYears(100) // Adjust as needed
val endYear = currentYear.plusYears(100) // Adjust as needed
val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library
yearCalendarView.setup(startYear, endYear, firstDayOfWeek)
yearCalendarView.scrollToYear(currentYear)
```

**And that's all you need for simple usage! But keep reading, there's more!**

### First day of the week and Day of week titles.

Of course, you want to show the day of week titles on the appropriate days on the calendar.

`Sun | Mon | Tue | Wed | Thu | Fri | Sat`

Here's a function that generates the weekdays from the user's current Locale. 

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

To set up the calendar using the provided `daysOfWeek` list:

```diff
- val firstDayOfWeek = firstDayOfWeekFromLocale()
+ val daysOfWeek = daysOfWeek()
  calendarView.setup(startMonth, endMonth, daysOfWeek.first())
```

You should also use the `daysOfWeek` list values to set up the weekday titles, this way it matches what is shown on the CalendarView.

To set up the day of week titles, you can either use the month header which would show the titles on every month and allow the titles to scroll with the month, or you can show the title on a static view above the calendar. Both ways are covered below:

Setup days of week using a static view:

#### Step 1
Create your title text view in `res/layout/calendar_day_title_text.xml`.

```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_weight="1"
    android:gravity="center" />
```

Create a container resource to use the text in `res/layout/calendar_day_titles_container.xml`.

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

Add the titles container in the same layout as the CalendarView:

```xml
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

#### Step 2

Now you can set up the titles using the `daysOfWeek` list discussed previously:

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

To use the titles as a month header so it scrolls with each month, continue to the month headers and footers section below!

### Month headers and footers.

To add a header or footer to each month, the procedure is the same as we did for the day using the `dayViewResource` above, but instead of the day resource, you have to provide your `monthHeaderResource` or `monthFooterResource` attribute, then set the `monthHeaderBinder` or `monthFooterBinder` property of the CalendarView.

To add the days of week titles as the month header, we provide the title container as the header resource:

```xml
<com.kizitonwose.calendar.view.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout"
    app:cv_monthHeaderResource="@layout/calendar_day_titles_container" /> 

```

Now we can use the header to show the title:

```kotlin
class MonthViewContainer(view: View) : ViewContainer(view) {
    // Alternatively, you can add an ID to the container layout and use findViewById()
    val titlesContainer = view as ViewGroup 
}

calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
    override fun create(view: View) = MonthViewContainer(view)
    override fun bind(container: MonthViewContainer, data: CalendarMonth) {
        // Remember that the header is reused so this will be called for each month.
        // However, the first day of the week will not change so no need to bind 
        // the same view every time it is reused.
        if (container.titlesContainer.tag == null) {
            container.titlesContainer.tag = data.yearMonth
            container.titlesContainer.children.map { it as TextView }
                .forEachIndexed { index, textView ->
                    val dayOfWeek = daysOfWeek[index]
                    val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    textView.text = title
                    // In the code above, we use the same `daysOfWeek` list 
                    // that was created when we set up the calendar. 
                    // However, we can also get the `daysOfWeek` list from the month data:
                    // val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
                    // Alternatively, you can get the value for this specific index:
                    // val dayOfWeek = data.weekDays.first()[index].date.dayOfWeek
                }
        }
    }
}
```

You can do more than just use the day titles as the header. For example, you can also show the month name if it is not already shown outside the calendar on a separate view. Feel free to get creative with your month headers and footers! For more complex usages, please see the sample project.

### Attributes

#### XML (All prefixed `cv_` for clarity)

**The following attributes are available for `CalendarView`, `WeekCalendarView` and `YearCalendarView` classes:**

- **dayViewResource**: The xml resource that is inflated and used as the day cell view. This must be provided.

- **scrollPaged**: The scrolling behavior of the calendar. If `true`, the calendar will snap to the nearest month or week (in the WeekCalendarView) after a scroll or swipe action. If `false`, the calendar scrolls normally.

- **daySize**: Determines how the size of each day on the calendar is calculated. Can be one of three values:
    1. **square**: Each day will have both width and height matching the width of the calendar divided by 7.
    2. **rectangle**: Each day will have its width matching the width of the calendar divided by 7, and its height matching the height of the calendar divided by the number of weeks in the index - could be 4, 5 or 6 for the month calendar, and 1 for the week calendar. 
    Use this if you want each month or week to fill the parent's width and height.
    2. **seventhWidth**: Each day will have its width matching the width of the calendar divided by 7. The day is allowed to determine its height by setting a specific value or using `LayoutParams.WRAP_CONTENT`
    3. **freeForm**: This day is allowed to determine its width and height by setting specific values or using `LayoutParams.WRAP_CONTENT`.

**The following attributes are available for `CalendarView` and `YearCalendarView` classes:**

- **monthHeaderResource**: The xml resource that is inflated and used as a header for each month.

- **monthFooterResource**: The xml resource that is inflated and used as a footer for each month.

- **orientation**: The calendar scroll direction, can be `horizontal` or `vertical`. Default is `horizontal`.

- **monthViewClass**: A ViewGroup that is instantiated and used as the container for each month. This class must have a constructor which takes only a Context. You should exclude the name and constructor of this class from code obfuscation if enabled.

- **outDateStyle**: This determines how outDates are generated for each month on the calendar. Can be one of two values:
    1. **endOfRow**: The calendar will generate `outDates` until it reaches the end of the month row. This means that if a month has 5 rows, it will display 5 rows and if a month has 6 rows, it will display 6 rows.
    2. **endOfGrid**: The calendar will generate `outDates` until it reaches the end of a 6 x 7 grid on each month. This means that all months will have 6 rows.

If you are wondering what `outDates` and `inDates` mean, let's use the screenshot below as an example.

<img src="https://user-images.githubusercontent.com/15170090/197358602-c9c6f796-fb28-4c82-9101-458d7a66f3a0.png" alt="in-dates and out-dates" width="300">

In the image, the dates within the green annotation are `inDates`, the ones within the red annotation are `outDates` while those without annotation are `monthDates`. You can check for this when binding your calendar. To achieve the exact effect on the image, we do this: 

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

`inDates` have their `position` property set to `DayPosition.InDate`

`outDates` have their `position` property set to `DayPosition.OutDate`

`monthDates` have their `position` property set to `DayPosition.MonthDate` as seen in the code snippet above.

**The following attributes are available for ONLY `WeekCalendarView` class:**

- **weekHeaderResource**: The xml resource that is inflated and used as a header for every week.

- **weekFooterResource**: The xml resource that is inflated and used as a footer for every week.

- **weekViewClass**: A ViewGroup that is instantiated and used as the container for each week. This class must have a constructor which takes only a Context. You should exclude the name and constructor of this class from code obfuscation if enabled.

**The following attributes are available for ONLY `YearCalendarView` class:**

- **yearHeaderResource**: The xml resource that is inflated and used as a header for each year.

- **yearFooterResource**: The xml resource that is inflated and used as a footer for each year.

- **yearViewClass**: A ViewGroup that is instantiated and used as the container for each year. This class must have a constructor which takes only a Context. You should exclude the name and constructor of this class from code obfuscation if enabled.

- **monthColumns**: The number of month columns in each year. Must be from 1 to 12.

- **monthHorizontalSpacing**: The horizontal spacing between month columns in each year.

- **monthVerticalSpacing**: The vertical spacing between month rows in each year.

- **monthHeight**: This determines how the height of each month row on the calendar is calculated. Can be one of two values:
    1. **followDaySize**: Each month row height is determined by the `daySize` value set on the calendar. Effectively, this is `wrap-content` if the value is `Square`,
    `SeventhWidth`, or `FreeForm`, and will be equal to the calendar height divided by the number of rows if the value is `Rectangle`. When used together with `Rectangle`, 
    the calendar months and days will uniformly stretch to fill the parent's height.
    2. **fill**: Each month row height will be the calendar height divided by the number of rows on the calendar. This means that the calendar months will be distributed
    uniformly to fill the parent's height. However, the day content height will independently determine its height. This allows you to spread the calendar months evenly across the screen while
    a `daySize` value of `Square` if you want square day content or `SeventhWidth` if you want to set a specific height value for the day content.

### Properties

All the respective XML attributes listed above are also available as properties of the CalendarView and WeekCalendarView classes so they can be set via code. So in addition to those, we have:

**`CalendarView`**

- **monthScrollListener**: Called when the calendar scrolls to a new month. Mostly beneficial if `scrollPaged` is `true`.

- **dayBinder**: An instance of `MonthDayBinder` for managing day cell views.

- **monthHeaderBinder**: An instance of `MonthHeaderFooterBinder` for managing header views. The header view is shown above each month on the calendar.

- **monthFooterBinder**: An instance of `MonthHeaderFooterBinder` for managing footer views. The footer view is shown below each month on the calendar.

- **monthMargins**: The margins, in pixels to be applied on each month view. This can be used to add a space between two months.

**`WeekCalendarView`**

- **weekScrollListener**: Called when the calendar scrolls to a new week. Mostly beneficial if `scrollPaged` is `true`.

- **dayBinder**: An instance of `WeekDayBinder` for managing day cell views.

- **weekHeaderBinder**: An instance of `WeekHeaderFooterBinder` for managing the header views shown above each week on the calendar.

- **weekFooterBinder**: An instance of `WeekHeaderFooterBinder` for managing the footer views shown below each week on the calendar.

- **weekMargins**: The margins, in pixels to be applied on each week view. This can be used to add a space between two weeks.

**`YearCalendarView`**

- **yearScrollListener**: Called when the calendar scrolls to a new year. Mostly beneficial if `scrollPaged` is `true`.

- **dayBinder**: An instance of `MonthDayBinder` for managing day cell views.

- **monthHeaderBinder**: An instance of `MonthHeaderFooterBinder` for managing the header views shown above each month on the calendar.

- **monthFooterBinder**: An instance of `MonthHeaderFooterBinder` for managing the footer views shown below each month on the calendar.

- **monthMargins**: The margins, in pixels to be applied on each month view. This can be used to add a space between two months.

- **yearHeaderBinder**: An instance of `YearHeaderFooterBinder` for managing the header views shown above each year on the calendar.

- **yearFooterBinder**: An instance of `YearHeaderFooterBinder` for managing the footer views shown below each year on the calendar.

- **yearMargins**: The margins, in pixels to be applied on each year view. This is the container in which the year header, body and footer are placed. For example, this can be used to add a space between two years.

- **yearBodyMargins**: The margins, in pixels to be applied on each year body view. This is the grid in which the months in each year are shown, excluding the year header and footer.

### Methods

**`CalendarView`**

- **scrollToDate(date: LocalDate)**: Scroll to a specific date on the calendar. Use `smoothScrollToDate()` to get a smooth scrolling animation. 

- **scrollToMonth(month: YearMonth)**: Scroll to a month on the calendar. Use `smoothScrollToMonth()` to get a smooth scrolling animation.

- **notifyDateChanged(date: LocalDate)**: Reload the view for the specified date.

- **notifyMonthChanged(month: YearMonth)**: Reload the header, body and footer views for the specified month.

- **notifyCalendarChanged()**: Reload the entire calendar.

- **findFirstVisibleMonth()** and **findLastVisibleMonth()**: Find the first and last visible months on the calendar respectively.

- **findFirstVisibleDay()** and **findLastVisibleDay()**: Find the first and last visible days on the calendar respectively.

- **updateMonthData()**: Update the calendar's start month or end month or the first day of week after the initial setup. The currently visible month is preserved. The calendar can handle really large date ranges so you may want to setup the calendar with a large date range instead of updating the range frequently.

**`WeekCalendarView`**

- **scrollToDate(date: LocalDate)**: Scroll to a specific date on the calendar. Use `smoothScrollToDate()` to get a smooth scrolling animation. 

- **scrollToWeek(date: LocalDate)**: Scroll to the week containing this date on the calendar. Use `smoothScrollToWeek()` to get a smooth scrolling animation.

- **notifyDateChanged(date: LocalDate)**: Reload the view for the specified date.

- **notifyWeekChanged(date: LocalDate)**: Reload the header, body and footer views for the week containing this date.

- **notifyCalendarChanged()**: Reload the entire calendar.

- **findFirstVisibleWeek()** and **findLastVisibleWeek()**: Find the first and last visible weeks on the calendar respectively.

- **findFirstVisibleDay()** and **findLastVisibleDay()**: Find the first and last visible days on the calendar respectively.

- **updateWeekData()**: Update the calendar's start date or end date or the first day of week after the initial setup. The currently visible week is preserved. The calendar can handle really large date ranges so you may want to setup the calendar with a large date range instead of updating the range frequently.

**`YearCalendarView`**

- **scrollToDate(date: LocalDate)**: Scroll to a specific date on the calendar. Use `smoothScrollToDate()` to get a smooth scrolling animation. 

- **scrollToMonth(month: YearMonth)**: Scroll to a month on the calendar. Use `smoothScrollToMonth()` to get a smooth scrolling animation.

- **scrollToYear(year: Year)**: Scroll to a year on the calendar. Use `smoothScrollToYear()` to get a smooth scrolling animation.

- **notifyDateChanged(date: LocalDate)**: Reload the view for the specified date.

- **notifyMonthChanged(month: YearMonth)**: Reload the header, body and footer views for the specified month.

- **notifyYearChanged(year: Year)**: Reload the header, body (all months in the year) and footer views for the specified year.

- **notifyCalendarChanged()**: Reload the entire calendar.

- **findFirstVisibleYear()** and **findLastVisibleYear()**: Find the first and last visible years on the calendar respectively.

- **findFirstVisibleMonth()** and **findLastVisibleMonth()**: Find the first and last visible months on the calendar respectively.

- **findFirstVisibleDay()** and **findLastVisibleDay()**: Find the first and last visible days on the calendar respectively.

- **updateYearData()**: Update the calendar's start year or end year or the first day of week after the initial setup. The currently visible year is preserved. The calendar can handle really large date ranges so you may want to setup the calendar with a large date range instead of updating the range frequently.

There's no need to list all available methods or repeat the documentation here. Please see
the [CalendarView](https://github.com/kizitonwose/Calendar/blob/main/view/src/main/java/com/kizitonwose/calendar/view/CalendarView.kt), [WeekCalendarView](https://github.com/kizitonwose/Calendar/blob/main/view/src/main/java/com/kizitonwose/calendar/view/WeekCalendarView.kt) and [YearCalendarView](https://github.com/kizitonwose/Calendar/blob/main/view/src/main/java/com/kizitonwose/calendar/view/YearCalendarView.kt)
classes for all properties and methods available with proper documentation.

### Date clicks

You should set a click listener on the view which is provided to the view container. 

XML file for the date cell `calendar_day_layout.xml`:

```xml
<!--We'll use this TextView to show the dates-->
<TextView
    android:id="@+id/calendarDayText"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:textSize="16sp"
    tools:text="22" />
```

Of course, you need to set the file as `cv_dayViewResource` on the CalendarView:

```xml
<com.kizitonwose.calendarview.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

Click listener implementation in your Fragment or Activity:

```kotlin
class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)
    // Will be set when this container is bound
    lateinit var day: CalendarDay
    
    init {
        view.setOnClickListener {
            // Use the CalendarDay associated with this container.
        }
    }
}

calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        // Set the calendar day for this container.
        container.day = data
        // Set the date text
        container.textView.text = data.date.dayOfMonth.toString()
        // Any other binding logic
    }
}
```

### Date Selection

The library has no inbuilt concept of selected/unselected dates, this gives you the freedom to choose how best you would like to implement this use case.

Implementing date selection is as simple as showing a background on a specific date in the date binder. Remember that since CalendarView, WeekCalendarView and YearCalendarView all extend from RecyclerView, you need to undo any special effects on dates where it is not needed. 

For this example, I want only the last clicked date to be selected on the calendar.

Firstly, let's keep a reference to the selected date:

```kotlin
private var selectedDate: LocalDate? = null
```

Next, using the click logic on the view container already shown in the date click section above, we update this field whenever a date is clicked and show the selection background on the clicked date. 

```kotlin
view.setOnClickListener {
    // Check the day position as we do not want to select in or out dates.
    if (day.position == DayPosition.MonthDate) {
        // Keep a reference to any previous selection
        // in case we overwrite it and need to reload it.
        val currentSelection = selectedDate
        if (currentSelection == day.date) {
            // If the user clicks the same date, clear selection.
            selectedDate = null
            // Reload this date so the dayBinder is called
            // and we can REMOVE the selection background.
            calendarView.notifyDateChanged(currentSelection)
        } else {
            selectedDate = day.date
            // Reload the newly selected date so the dayBinder is
            // called and we can ADD the selection background.
            calendarView.notifyDateChanged(day.date)
            if currentSelection != null {
                // We need to also reload the previously selected 
                // date so we can REMOVE the selection background.
                calendarView.notifyDateChanged(currentSelection)
            }
        }
    }
}
```

Lastly, we implement the `dayBinder` to reflect the selection accordingly:

```kotlin
calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.day = data
        val day = data
        val textView = container.textView
        textView.text = day.date.dayOfMonth.toString()
        if (day.position == DayPosition.MonthDate) {
            // Show the month dates. Remember that views are reused!
            textView.visibility = View.VISIBLE
            if (day.date == selectedDate) {
                // If this is the selected date, show a round background and change the text color.
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundResource(R.drawable.selection_background)
            } else {
                // If this is NOT the selected date, remove the background and reset the text color.
                textView.setTextColor(Color.BLACK)
                textView.background = null
            }
        } else {
            // Hide in and out dates
            textView.visibility = View.INVISIBLE
        }
    }
}
```

For more complex selection logic like range selection, please see the sample project. It's quite simple, the magic is all in your binding logic!

### Disabling dates

As expected, the library does not provide this logic internally so you have complete flexibility.

To disable dates, you can simply set the texts on those dates to look disabled and ignore clicks on those dates. For example, if we want to show in and out dates but disable them so that they cannot be selected, we can just set the alpha property for those dates in the `dayBinder` to give the effect of being disabled. Of course, you can set a different color if that's what you prefer.

Continuing with the example in the date selection section, we already ignore clicks for in and out dates using this logic:

```kotlin
view.setOnClickListener {
    // Check the day position as we do not want to select in or out dates.
    if (day.position == DayPosition.MonthDate) {
        // Only use month dates
    }
}
```

Then in the `dayBinder`, we check the day position and set the text alpha or color accordingly:

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

And we've now covered the typical usage. The beauty of the library is in its limitless possibilities. You are not constrained on how to build your user interface, the library provides you with the needed calendar data logic and you provide the desired UI logic. 

See the sample project for some complex implementations.

## Week view

The `WeekCalendarView` class is a week-based calendar implementation. Almost all topics covered above for the month calendar will apply to the week calendar. The main difference is that the xml attributes and class properties/methods will have a slightly different name, typically with a `week` prefix/suffix instead of `month`. 

For example: `monthHeaderResource` => `weekHeaderResource`, `scrollToMonth()` => `scrollToWeek()`, `findFirstVisibleMonth()` => `findFirstVisibleWeek()` and many others, but you get the idea.

To show the week calendar in your layout, add the view:

```xml
<com.kizitonwose.calendar.view.WeekCalendarView
    android:id="@+id/weekCalendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

Then follow the setup instructions above to provide a day resource/binder etc as you would do for the month calendar.

If you would like to toggle the calendar between month and week views, please see the sample app where we did this using a `ValueAnimator`. You can use whichever animation logic you prefer. The result is shown below:

<img src="https://user-images.githubusercontent.com/15170090/195636303-a99312c9-23a3-44cd-8a38-6ba21b3c4802.gif" alt="Week and month modes" width="250">

If you wish to show more or less than 7 days at a time on the week calendar, you should set the `scrollPaged` attribute to `false`. Also, set the `daySize` property to `FreeForm` which gives you the freedom to define a preferred size for your day cells. Please read the documentation in the `DaySize` class to fully understand the available options.

A week calendar implementation from the sample app:

<img src="https://user-images.githubusercontent.com/15170090/195638551-dfced7be-c18f-4611-b015-cfefab480cee.png" alt="Week calendar" width="250">

## Year view

The `YearCalendarView` class is a year-based calendar implementation. All topics covered above for the month calendar will apply to the year calendar. The year calendar also has additional xml attributes and class properties/methods, typically with a `year` prefix/suffix. 

For example: `yearHeaderResource`, `scrollToYear()`, `findFirstVisibleYear()` and many others, but you get the idea.

To show the year calendar in your layout, add the view:

```xml
<com.kizitonwose.calendar.view.YearCalendarView
    android:id="@+id/yearCalendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

Then follow the setup instructions above to provide a day resource/binder etc as you would do for the month calendar.

A year calendar implementation from the sample app:

<img src="https://github.com/user-attachments/assets/b2fcae94-341c-4f35-a997-e8d95a23efb4" alt="Year calendar" width="500">

Remember that all the screenshots shown so far are just examples of what you can achieve with the library and you can absolutely build your calendar to look however you want.

**Made a cool calendar with this library? Share an image [here](https://github.com/kizitonwose/Calendar/issues/1).**

## FAQ

**Q**: How do I use this library in a Java project?

**A**: It works out of the box, however, the `MonthScrollListener`, `WeekScrollListener` and `YearScrollListener` are not interfaces but Kotlin functions. To set the listener in a Java project see [this](https://github.com/kizitonwose/Calendar/issues/74).

**Q**: How do I disable user scrolling on the calendar so I can only scroll programmatically?

**A**: See [this](https://github.com/kizitonwose/Calendar/issues/38#issuecomment-525786644).

## Migration

Please see [the migration guide](https://github.com/kizitonwose/calendar/blob/main/docs/MigrationGuide.md#view).

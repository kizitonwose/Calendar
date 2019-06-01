# CalendarView

A highly customizable calendar library for Android, powered by RecyclerView.

[![Build Status](https://travis-ci.org/kizitonwose/CalendarView.svg?branch=master)](https://travis-ci.org/kizitonwose/CalendarView) 
[![JitPack](https://jitpack.io/v/kizitonwose/CalendarView.svg)](https://jitpack.io/#kizitonwose/CalendarView) 
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/kizitonwose/CalendarView/blob/master/LICENSE.md) 
[![Contact](https://img.shields.io/badge/Contact-@kizitonwose-9C27B0.svg)](https://twitter.com/kizitonwose)


**With this library, your calendar will look however you want it to.**

![Preview](https://raw.githubusercontent.com/kizitonwose/CalendarView/master/images/image-all.png)

## Features

- [x] Single or range selection - The library provides the calendar logic which enables you to implement the view whichever way you like.
- [x] Boundary dates - limit the calendar date range.
- [x] Custom date view - make your day cells look however you want, with any functionality you want.
- [x] Custom calendar view - make your calendar look however you want, with whatever functionality you want.
- [x] Pick any day to be the first day of the week.
- [x] Horizontal or vertical scrolling mode.
- [x] Add headers/footers of any kind on each month.
- [x] Easily scroll to any date or month view using the date.
- [x] Use all RecyclerView customisations(decorators etc) since the CalendarView entends from RecyclerView.
- [x] Design your calendar [however you want.](https://github.com/kizitonwose/CalendarView/issues/1) The library provides the logic, you provide the views.

## Sample project

It's very important to check out the sample app. Most techniques that you would want to implement are already implemented in the examples.

Get the sample apk [here](https://github.com/kizitonwose/CalendarView/releases/download/0.1.0/sample.apk)

View the sample module's source code [here][sample-url]

## Usage

#### Step 1

The library uses `java.time` classes via [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) for backward compatibility since these classes were added in Java 8. Therefore, you need to initialize ThreeTenABP in your application class.

```kotlin
class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}
```

Add CalendarView to your XML like any other view.

```xml
<com.kizitonwose.calendarview.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```
See all available [attributes](#attributes).

#### Step 2

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
    val textView = view.calendarDayText
    
    // Without the kotlin android extensions plugin
    // val textView = view.findViewById<TextView>(R.id.calendarDayText)
}
```

Provide a `DayBinder` for the CalendarView using your `DayViewContainer` type.

```kotlin
calendarView.dayBinder = object : DayBinder<DayViewContainer> {
    // Called only when a new container is needed.
    override fun create(view: View) = DayViewContainer(view)
    
    // Called every time we need to reuse a container.
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.textView.text = day.date.dayOfMonth.toString()
    }
}
```

#### Step 3

Setup the desired dates in your Fragment or Activity:

```kotlin
val currentMonth = YearMonth.now()
val firstMonth = currentMonth.minusMonths(10)
val lastMonth = currentMonth.plusMonths(10)
val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
calendarView.scrollToMonth(currentMonth)
```

**And that's all you need for a simple usage!**

To add a header or footer to each month, the procedure is the same. Just provide your `monthHeaderResource` or `monthFooterResource` attribute, then set the `monthHeaderBinder` or `monthFooterBinder` property of the CalendarView.
For more complex usages, please see the sample project.

In the example above, we get the first day of the week from the current locale, however, we can use a specific day regardless of locale by passing in the value `DayOfWeek.SUNDAY`, `DayOfWeek.MONDAY` etc

### Attributes

#### XML (All prefixed `cv_` for clarity)

- **dayViewResource**: The xml resource that is inflated and used as the day cell view. This must be provided.

- **monthHeaderResource**: The xml resource is inflated and used as a header for every month.

- **monthFooterResource**: The xml resource is inflated and used as a footer for every month.

- **orientation**: The calendar orientation, can be `horizontal` or `vertical`. Default is `vertical`.

- **scrollMode**: The scrolling behavior of the calendar. Can be `paged` or `continuous`. If `paged`, the calendar will snap to the nearest month after a scroll or swipe action. Default value is `continuous`.

- **outDateStyle**: This determines how outDates are generated for you. If `endOfRow `, the calendar will generate outDates until it reaches the first end of a row. This means that if a month has 6 rows, it will display 6 rows and if a month has 5 rows, it will display 5 rows. However, if this value is set to `endOfGrid`, the calendar will generate outDates until it reaches the end of a 6 x 7 grid. This means that all months will have 6 rows.

If you are wondering what `outDates` and `inDates` mean, let's use the screenshot below as an example.

<img src="https://raw.githubusercontent.com/kizitonwose/CalendarView/master/images/screenshot_in_out_dates.png" alt="ExampleMain" width="300">

In the image, the dates with the green annotation are `inDates` while those with the red annotation are `outDates`. You can check for this when binding your calendar. To achieve the exact effect on the image, we do this: 

```kotlin
calendarView.dayBinder = object : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.textView.text = day.date.dayOfMonth.toString()
        if (day.owner == DayOwner.THIS_MONTH) {
            container.textView.setTextColor(Color.WHITE)
        } else {
            container.textView.setTextColor(Color.GRAY)
        }
    }
}
```

`inDates` have their `owner` property set to `DayOwner.PREVIOUS_MONTH`

`outDates` have their `owner` property set to `DayOwner.NEXT_MONTH`

Dates which belong to the month have their `owner` property set to `DayOwner.THIS_MONTH` as seen in the code snippet above.

#### Properties

- **monthScrollListener**: Called when the calendar scrolls to a new month. Mostly beneficial if `scrollMode` is `paged`.

- **dayBinder**: An instance of `DayBinder` for managing day cell views.

- **monthHeaderBinder**: An instance of `MonthHeaderFooterBinder` for managing header views.

- **monthFooterBinder**: An instance of `MonthHeaderFooterBinder` for managing footer views.

- **dayWidth**: The width, in pixels for each day cell view.

- **dayHeight**: The height, in pixels for each day cell view.

Note that setting either `dayWidth` or `dayHeight` to `CalendarView.DAY_SIZE_SQUARE` makes the day cells have equal width and height which is basically the width of the calendar divided by 7. `DAY_SIZE_SQUARE` is the default day width and height value.

#### Methods

- **scrollToDate(date: LocalDate)**: Scroll to a specific date on the calendar.

- **scrollToMonth(month: YearMonth)**: Scroll to a month on the calendar.

- **notifyDateChanged(date: LocalDate)**: Reload the view for the specified date.

- **notifyMonthChanged(month: YearMonth)**: Reload the header, body and footer views for the specified month.

- **notifyCalendarChanged()**: Reload the entire calendar.

There's no need listing all available methods or repeating the documentation here. Please see the [CalendarView](https://github.com/kizitonwose/CalendarView/blob/master/library/src/main/java/com/kizitonwose/calendarview/CalendarView.kt) class for all properties and methods available with proper documentation.

**Made a cool calendar with this library? Share an image [here](https://github.com/kizitonwose/CalendarView/issues/1).**

## Setup

### Gradle

Add the JitPack repository to your project level `build.gradle`:

```groovy
allprojects {
 repositories {
    google()
    jcenter()
    maven { url "https://jitpack.io" }
 }
}
```

Add this to your app `build.gradle`:

```groovy
dependencies {
	implementation 'com.github.kizitonwose:CalendarView:<latest-version>'
}
```
**Note: `<latest-version>` value can be found on the JitPack badge above the preview images.**


## Contributing

Found a bug? feel free to fix it and send a pull request or [open an issue](https://github.com/kizitonwose/CalendarView/issues).

## Inspiration

CalendarView was inspired by the iOS library [JTAppleCalendar][jt-cal-url]. I used JTAppleCalendar in an iOS project but couldn't find anything as customizable on Android so I built this. 
You'll find some similar terms like `endOfRow`, `endOfGrid`, `DayOwner` etc.

## License
CalendarView is distributed under the MIT license. See [LICENSE](https://github.com/kizitonwose/CalendarView/blob/master/LICENSE.md) for details.

[sample-url]: https://github.com/kizitonwose/CalendarView/tree/master/sample
[jt-cal-url]: https://github.com/patchthecode/JTAppleCalendar 

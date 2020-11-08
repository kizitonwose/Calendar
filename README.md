# CalendarView

A highly customizable calendar library for Android, powered by RecyclerView.

[![CI](https://github.com/kizitonwose/CalendarView/workflows/CI/badge.svg?branch=master)](https://github.com/kizitonwose/CalendarView/actions) 
[![JitPack](https://jitpack.io/v/kizitonwose/CalendarView.svg)](https://jitpack.io/#kizitonwose/CalendarView) 
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/kizitonwose/CalendarView/blob/master/LICENSE.md) 
[![Twitter](https://img.shields.io/badge/Twitter-@kizitonwose-9C27B0.svg)](https://twitter.com/kizitonwose)


**With this library, your calendar will look however you want it to.**

![Preview](https://raw.githubusercontent.com/kizitonwose/CalendarView/master/images/image-all.png)

## Features

- [x] Single or range selection - The library provides the calendar logic which enables you to implement the view whichever way you like.
- [x] [Week or month mode](#week-view-and-month-view) - show 1 row of weekdays, or any number of rows from 1 to 6.
- [x] Boundary dates - limit the calendar date range.
- [x] Custom date view - make your day cells look however you want, with any functionality you want.
- [x] Custom calendar view - make your calendar look however you want, with whatever functionality you want.
- [x] Use any day as the first day of the week.
- [x] Horizontal or vertical scrolling mode.
- [x] Add headers/footers of any kind on each month.
- [x] Easily scroll to any date or month view using the date.
- [x] Use all RecyclerView customisations(decorators etc) since CalendarView extends from RecyclerView.
- [x] Design your calendar [however you want.](https://github.com/kizitonwose/CalendarView/issues/1) The library provides the logic, you provide the views.

## Sample project

It's very important to check out the sample app. Most techniques that you would want to implement are already implemented in the examples.

Download the sample app [here](https://github.com/kizitonwose/CalendarView/releases/download/1.0.0/sample.apk)

View the sample app's source code [here](https://github.com/kizitonwose/CalendarView/tree/master/sample)

## Setup

The library uses `java.time` classes via [Java 8+ API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) for backward compatibility since these classes were added in Java 8.

#### Step 1

To setup your project for desugaring, you need to first ensure that you are using [Android Gradle plugin](https://developer.android.com/studio/releases/gradle-plugin#updating-plugin) 4.0.0 or higher.

Then include the following in your app's build.gradle file:

```groovy
android {
  defaultConfig {
    // Required ONLY when setting minSdkVersion to 20 or lower
    multiDexEnabled true
  }

  compileOptions {
    // Flag to enable support for the new language APIs
    coreLibraryDesugaringEnabled true
    // Sets Java compatibility to Java 8
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}

dependencies {
  coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:<latest-version>'
}
```

You can find the latest version of `desugar_jdk_libs` [here](https://mvnrepository.com/artifact/com.android.tools/desugar_jdk_libs).

#### Step 2

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

Add CalendarView to your app `build.gradle`:

```groovy
dependencies {
	implementation 'com.github.kizitonwose:CalendarView:<latest-version>'
}
```

You can find the latest version of `CalendarView` on the JitPack badge above the preview images.

**Note: If you're upgrading from version 0.3.x to 0.4.x or 1.x.x, see the [migration guide](https://github.com/kizitonwose/CalendarView#migration).**

## Usage

#### Step 1

Add CalendarView to your XML like any other view.

```xml
<com.kizitonwose.calendarview.CalendarView
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

#### Step 2

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

- **monthHeaderResource**: The xml resource that is inflated and used as a header for every month.

- **monthFooterResource**: The xml resource that is inflated and used as a footer for every month.

- **orientation**: The calendar orientation, can be `horizontal` or `vertical`. Default is `vertical`.

- **scrollMode**: The scrolling behavior of the calendar. Can be `paged` or `continuous`. If `paged`, the calendar will snap to the nearest month after a scroll or swipe action. Default value is `continuous`.

- **maxRowCount**: The maximum number of rows(1 to 6) to show on each month. If a month has a total of 6 rows and `maxRowCount` is set to 4, there will be two appearances of that month on the calendar, the first one will show 4 rows and the second one will show the remaining 2 rows. To show a week mode calendar, set this value to 1, you may also want to set `hasBoundaries` to false so dates can overflow into the previous/next month for a better experience.

- **hasBoundaries**: Determines if dates of a month should stay in its section or can flow into another month's section.
If `true`, a section can only contain dates belonging to that month, its inDates and outDates. if `false`, the dates are added continuously, irrespective of month sections.

    When this property is `false`, a few things behave slightly differently:
    - If `inDateStyle` is either `allMonths` or `firstMonth`, only the first index will contain inDates.
    - If `outDateStyle` is either `endOfRow` or `endOfGrid`, only the last index will contain outDates.
    - If `outDateStyle` is `endOfGrid`, outDates are generated for the last index until it satisfies the `maxRowCount` requirement.

- **inDateStyle**: This Determines how inDates are generated for each month on the calendar. If set to `allMonths`, the calendar will generate inDates for all months, if set to `firstMonth` inDates will be generated for the first month only and if set to `none`, inDates will not be generated, this means that there will be no offset on any month.

- **outDateStyle**: This determines how outDates are generated for each month on the calendar. If `endOfRow `, the calendar will generate outDates until it reaches the first end of a row. This means that if a month has 6 rows, it will display 6 rows and if a month has 5 rows, it will display 5 rows. However, if this value is set to `endOfGrid`, the calendar will generate outDates until it reaches the end of a 6 x 7 grid. This means that all months will have 6 rows.

If you are wondering what `outDates` and `inDates` mean, let's use the screenshot below as an example.

<img src="https://raw.githubusercontent.com/kizitonwose/CalendarView/master/images/screenshot_in_out_dates.png" alt="inDate and outDates" width="300">

In the image, the dates within the green annotation are `inDates`, the ones within the red annotation are `outDates` while those without annotation are `monthDates`. You can check for this when binding your calendar. To achieve the exact effect on the image, we do this: 

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

`monthDates` have their `owner` property set to `DayOwner.THIS_MONTH` as seen in the code snippet above.

#### Properties

All XML attributes are also available as properties of the CalendarView class via code. So in addition to those, we have:

- **monthScrollListener**: Called when the calendar scrolls to a new month. Mostly beneficial if `scrollMode` is `paged`.

- **dayBinder**: An instance of `DayBinder` for managing day cell views.

- **monthHeaderBinder**: An instance of `MonthHeaderFooterBinder` for managing header views.

- **monthFooterBinder**: An instance of `MonthHeaderFooterBinder` for managing footer views.

- **daySize**: The size, in pixels for each day cell view.

Note that setting the `daySize` property to `CalendarView.SIZE_SQUARE` makes the day cells have equal width and height which is basically the width of the calendar divided by 7. `SIZE_SQUARE` is the default size value.

#### Methods

- **scrollToDate(date: LocalDate)**: Scroll to a specific date on the calendar. Use `smoothScrollToDate()` to get a smooth scrolling animation. 

- **scrollToMonth(month: YearMonth)**: Scroll to a month on the calendar. Use `smoothScrollToMonth()` to get a smooth scrolling animation.

- **notifyDateChanged(date: LocalDate)**: Reload the view for the specified date.

- **notifyMonthChanged(month: YearMonth)**: Reload the header, body and footer views for the specified month.

- **notifyCalendarChanged()**: Reload the entire calendar.

- **findFirstVisibleMonth()** and **findLastVisibleMonth()**: Find the first and last visible months on the CalendarView respectively.

- **findFirstVisibleDay()** and **findLastVisibleDay()**: Find the first and last visible days on the CalendarView respectively.

- **setupAsync()**: Setup the CalendarView, *asynchronously*, useful if your `startMonth` and `endMonth` values are *many* years apart.

- **updateMonthRange()**: Update the CalendarView's `startMonth` and/or `endMonth` values after the initial setup. The currently visible month is preserved. Use `updateMonthRangeAsync()` to do this asynchronously.

- **updateMonthConfiguration()**: Update `inDateStyle`, `outDateStyle`, `maxRowCount` and `hasBoundaries` properties without generating the underlying calendar data repeatedly. Prefer this if setting more than one of these properties at the same time. Use `updateMonthConfigurationAsync()` to do this asynchronously.


There's no need to list all available methods or repeating the documentation here. Please see the [CalendarView](https://github.com/kizitonwose/CalendarView/blob/master/library/src/main/java/com/kizitonwose/calendarview/CalendarView.kt) class for all properties and methods available with proper documentation.

## Week view and Month view

This library has no concept of week/month view. You'll need to configure the calendar to mimic this behavior by changing its state between a 6 or 1 row calendar, depending on your needs. This feature can be seen in Example 1 in the sample app. In summary, here's what you need:

```xml
<!-- Common configurations for both modes. -->
app:cv_orientation="horizontal"
app:cv_outDateStyle="endOfRow"
app:cv_inDateStyle="allMonths"
app:cv_scrollMode="paged"
```

```kotlin
val monthToWeek = monthViewCheckBox.isChecked
if (monthToWeek) { 
    // One row calendar for week mode
    calendarView.updateMonthConfiguration(
        inDateStyle = InDateStyle.ALL_MONTHS,
        maxRowCount = 1,
        hasBoundaries = false
    )
} else {
    // Six row calendar for month mode
    calendarView.updateMonthConfiguration(
        inDateStyle = InDateStyle.FIRST_MONTH,
        maxRowCount = 6,
        hasBoundaries = true
    )
}
```

With the configuration above, you get the result below:

<img src="https://user-images.githubusercontent.com/15170090/59875600-100bd100-9399-11e9-8329-7c24944bb106.gif" alt="Week and month modes" width="250">

If you wish to animate height changes on the CalendarView when switching between week and month modes, please see Example 1 in the sample app where we use a `ValueAnimator`, of course you can use whichever animation logic you prefer.

You can also set `hasBoundaries` to `true` for a week mode calendar. This helps the library make very few optimizations, however, you should also change `scrollMode` to `ScrollMode.CONTINUOUS` as pagination behavior may not be as expected due to boundary limitations. See Example 7 in the sample app for a week mode calendar with this configuration, a screenshot is shown below: 

<img src="https://user-images.githubusercontent.com/15170090/59904118-9f959c00-93fa-11e9-836d-2248f77130ac.png" alt="Week mode" width="260">

Remember that all the screenshots above are just examples of what you can achieve with this library and you can absolutely build your calendar to look however you want.

**Made a cool calendar with this library? Share an image [here](https://github.com/kizitonwose/CalendarView/issues/1).**

## FAQ

**Q**: How do I use this library in a Java project?

**A**: It works out of the box, however, the `MonthScrollListener` is not an interface but a Kotlin function. To set the `MonthScrollListener` in a Java project see [this](https://github.com/kizitonwose/CalendarView/issues/74).

**Q**: How do I disable user scrolling on the calendar so I can only scroll programmatically?

**A**: See [this](https://github.com/kizitonwose/CalendarView/issues/38#issuecomment-525786644).

**Q**: Why am I getting the same `YearMonth` value in the `CalendarMonth` passed into the `MonthScrollListener`?

**A**: This is because you have set `app:cv_hasBoundaries` to `false` in XML or have called `calendarView.hasBoundaries = false` in code. When this is set, the underlying `YearMonth` is undefined on all indices as each index could have multiple months depending on your `maxRowCount` value. If you need the month value with the `hasBoundaries = false` setting, you can get it from any of the `CalendarDay` values in the `CalendarMonth` class. You can always check if the first and last dates are from different months and act accordingly.

## Migration

If you're upgrading from version `0.3.x` to `0.4.x` or 1.x.x, the main change is that CalendarView moved from using [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) to [Java 8 API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) for dates. After following the new [setup](https://github.com/kizitonwose/CalendarView#setup) instructions, the next thing you need to do is change your imports for date/time related classes from `org.threeten.bp.*` to `java.time.*`.

You also need to remove the line `AndroidThreeTen.init(this)` from the `onCreate()` method of your application class as it's no longer needed.

## Contributing

Found a bug? feel free to fix it and send a pull request or [open an issue](https://github.com/kizitonwose/CalendarView/issues).

## Inspiration

CalendarView was inspired by the iOS library [JTAppleCalendar](https://github.com/patchthecode/JTAppleCalendar). I used JTAppleCalendar in an iOS project but couldn't find anything as customizable on Android so I built this. 
You'll find some similar terms like `InDateStyle`, `OutDateStyle`, `DayOwner` etc.

## License
CalendarView is distributed under the MIT license. See [LICENSE](https://github.com/kizitonwose/CalendarView/blob/master/LICENSE.md) for details.

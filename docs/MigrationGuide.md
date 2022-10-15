# Migration Guide

- [View](#view)
  * [1.x.x => 2.x.x](#1xx--2xx)
  * [0.3.x => 0.4.x / 1.x.x](#03x--04x--1xx)
- [Compose](#compose)

## View

### 1.x.x => 2.x.x

If you're upgrading from version `1.x.x` to `2.x.x`, please keep in mind that version 2 is a total rewrite of the library with focus on performance. With that in mind, here are the breaking changes:

- `async()` methods have been removed as the calendar is now capable of handling really large date ranges without performance issues. The calendar data is generated as needed. The removed methods are `setupAsync()` and `updateMonthRangeAsync()`. Please use the methods `setup()` and `updateMonthData()`.

- `updateMonthRange()` method has been renamed to `updateMonthData()` as it now allows you to optionally change the first day of the week if needed.

- `daySize` property is no longer the `Size` class type with `height` and `width` values. It is now a `DaySize` enum with three options - `Square`, `SeventhWidth` and `FreeForm`. Please see the `DaySize` enum documentation to understand each value.

- `inDateStyle` enum property has been removed as it is now redundant. The in-dates are present in each month data, you can choose to hide them on the view or keep them.

- `outDateStyle` enum property is still available. However, there are now two options `EndOfRow` and `EndOfGrid`. The previously available third option `None` has been removed. You can simply hide the out-dates if you don't need them.

- `scrollMode` enum property is now a boolean type named `scrollPaged`. Set this property to `false` to have the previous `ScrollMode.CONTINUOUS` scroll behavior or `true` to have the previous `ScrollMode.PAGED` behavior.

- `DayOwner` enum has been renamed to `DayPosition`. The matching case values are:
    - `DayOwner.PREVIOUS_MONTH` => `DayPosition.InDate`
    - `DayOwner.THIS_MONTH` => `DayPosition.MonthDate`
    - `DayOwner.NEXT_MONTH` => `DayPosition.OutDate`

- `maxRowCount` property has been removed as there is now a `WeekCalendarView` class that should be used if a week-based calendar is needed. The main `CalendarView` class is used for the month calendar implementation.

- `hasBoundaries` property has been removed as it is no longer needed with the introduction of the week and month calendar implementations discussed above.

- `monthMarginStart` | `monthMarginTop` | `monthMarginEnd` | `monthMarginBottom` properties have been merged into one `monthMargins` property.

- `monthPaddingStart` | `monthPaddingTop` | `monthPaddingEnd` | `monthPaddingBottom` properties have been removed as they provided no real benefit. You can create a custom `monthViewClass` and set the paddings there if needed.


### 0.3.x => 0.4.x / 1.x.x

If you're upgrading from version `0.3.x` to `0.4.x` or `1.x.x`, the main change is that the library moved from using [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) to [Java 8 API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) for dates. After following the new [setup](https://github.com/kizitonwose/Calendar#setup) instructions, the next thing you need to do is change your imports for date/time related classes from `org.threeten.bp.*` to `java.time.*`.

You also need to remove the line `AndroidThreeTen.init(this)` from the `onCreate()` method of your application class as it's no longer needed.


## Compose

Nothing to see here... yet 😉

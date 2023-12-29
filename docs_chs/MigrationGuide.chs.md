# 迁移指南

- [View](#view)
  * [1.x.x => 2.x.x](#1xx--2xx)
  * [0.3.x => 0.4.x / 1.x.x](#03x--04x--1xx)
- [Compose](#compose)

## View

### 1.x.x => 2.x.x

- 如果你从版本 `1.x.x` 升级到 `2.x.x`，请记住版本 2 是库的完全重写，重点是性能。在这个背景下，以下是一些破坏性的更改：

    - `async()` 方法已被移除，因为现在日历能够处理非常大的日期范围而不会出现性能问题。日历数据是根据需要生成的。已删除的方法有 `setupAsync()` 和 `updateMonthRangeAsync()`。请使用方法 `setup()` 和 `updateMonthData()`。

    - `updateMonthRange()` 方法已重命名为 `updateMonthData()`，因为它现在允许你在需要时可选地更改一周的第一天。

    - `daySize` 属性不再是具有 `height` 和 `width` 值的 `Size` 类型。它现在是一个带有三个选项的 `DaySize` 枚举 - `Square`、`SeventhWidth` 和 `FreeForm`。请查看 `DaySize` 枚举文档以了解每个值。

    - `inDateStyle` 枚举属性已被移除，因为它现在是多余的。每个月的数据中都包含了 in-dates，你可以选择在视图上隐藏它们或保留它们。

    - `outDateStyle` 枚举属性仍然可用。但现在有两个选项 `EndOfRow` 和 `EndOfGrid`。先前可用的第三个选项 `None` 已被移除。如果不需要 out-dates，你可以简单地隐藏它们。

    - `scrollMode` 枚举属性现在是一个名为 `scrollPaged` 的布尔类型。将此属性设置为 `false` 以获得以前的 `ScrollMode.CONTINUOUS` 滚动行为，或将其设置为 `true` 以获得以前的 `ScrollMode.PAGED` 行为。

    - `DayOwner` 枚举已重命名为 `DayPosition`。匹配的 case 值为：
        - `DayOwner.PREVIOUS_MONTH` => `DayPosition.InDate`
        - `DayOwner.THIS_MONTH` => `DayPosition.MonthDate`
        - `DayOwner.NEXT_MONTH` => `DayPosition.OutDate`

    - `maxRowCount` 属性已被移除，因为现在有一个应该用于周历的 `WeekCalendarView` 类。主要的 `CalendarView` 类用于月历实现。

    - `hasBoundaries` 属性已被移除，因为在上面讨论的周历和月历实现引入后不再需要。

    - `monthMarginStart` | `monthMarginTop` | `monthMarginEnd` | `monthMarginBottom` 属性已合并为一个 `monthMargins` 属性。

    - `monthPaddingStart` | `monthPaddingTop` | `monthPaddingEnd` | `monthPaddingBottom` 属性已被移除，因为它们没有提供真正的好处。如果需要，可以在自定义的 `monthViewClass` 中设置填充。


### 0.3.x => 0.4.x / 1.x.x

如果你从版本 `0.3.x` 升级到 `0.4.x` 或 `1.x.x`，主要的变化是该库从使用 [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) 切换到使用 [Java 8 API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) 来处理日期。在遵循新的 [设置](https://github.com/kizitonwose/Calendar#setup) 说明之后，你需要做的下一步是将与日期/时间相关的类的导入从 `org.threeten.bp.*` 更改为 `java.time.*`。

你还需要从你的应用程序类的 `onCreate()` 方法中删除 `AndroidThreeTen.init(this)` 这一行，因为它不再需要。



## Compose

这里暂时没有什么内容... 😉

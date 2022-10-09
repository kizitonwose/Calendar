# CalendarView

A highly customizable calendar library for Android, powered by RecyclerView for the view system, and LazyRow/LazyColumn for compose.

[![CI](https://github.com/kizitonwose/Calendar/workflows/CI/badge.svg?branch=master)](https://github.com/kizitonwose/Calendar/actions) 
[![JitPack](https://jitpack.io/v/kizitonwose/Calendar.svg)](https://jitpack.io/#kizitonwose/Calendar) 
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/kizitonwose/Calendar/blob/master/LICENSE.md) 
[![Twitter](https://img.shields.io/badge/Twitter-@kizitonwose-9C27B0.svg)](https://twitter.com/kizitonwose)


**With this library, your calendar will look however you want it to.**

![Preview](https://raw.githubusercontent.com/kizitonwose/Calendar/master/images/image-all.png)

## Features

- [x] [Single or range selection](#date-selection) - The library provides the calendar logic which enables you to implement the view/composable whichever way you like.
- [x] [Week or month mode](#week-view-and-month-view) - show a week-based calendar, or the typical month calendar.
- [x] [Disable desired dates](#disabling-dates) - Prevent selection of some dates by disabling them.
- [x] Boundary dates - limit the calendar date range.
- [x] Custom date view/composable - make your day cells look however you want, with any functionality you want.
- [x] Custom calendar view/composable - make your calendar look however you want, with whatever functionality you want.
- [x] [Custom first day of the week](#first-day-of-the-week) - Use any day as the first day of the week.
- [x] Horizontal or vertical scrolling mode.
- [x] [Month headers and footers](#adding-month-headers-and-footers) - Add headers/footers of any kind on each month.
- [x] Easily scroll to any date or month on the calendar via user swipe actions or programmatically.
- [x] Use all RecyclerView/LazyRow/LazyColumn customizations since the calendar extends from RecyclerView for the view system and uses LazyRow/LazyColumn for compose.
- [x] Design your calendar [however you want.](https://github.com/kizitonwose/Calendar/issues/1) The library provides the logic, you provide the views/composables.

## Sample project

It's very important to check out the sample app. There are lots of examples provided for both view and compose implementations. 
Most techniques that you would want to implement are already done in the examples.

Download the sample app [here](https://github.com/kizitonwose/Calendar/releases/download/2.0.0/sample.apk)

View the sample app's source code [here](https://github.com/kizitonwose/Calendar/tree/master/sample)

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

Add the desired calendar library (view or compose) to your app `build.gradle`:

```groovy
dependencies {
    // The view calendar implementation
	implementation 'com.github.kizitonwose.calendar:view:<latest-version>'
  
    // The compose calendar implementation
	implementation 'com.github.kizitonwose.calendar:compose:<latest-version>'
}
```

You can find the latest version of the library on the JitPack badge above the preview images.

**Note: If you're upgrading from version 1.x.x to 2.x.x, see the [migration guide](https://github.com/kizitonwose/Calendar#migration).**

## Usage

You can find the relevant documentations for the library in the links below.

|[View-based documentations](https://github.com/kizitonwose/Calendar/blob/master/docs/View.md)|[Compose documentations](https://github.com/kizitonwose/Calendar/blob/master/docs/Compose.md)|
|:-:|:-:|

## Migration

If you're upgrading from version `1.x.x` to `2.x.x` or 1.x.x, the main change is that CalendarView moved from using [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) to [Java 8 API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) for dates. After following the new [setup](https://github.com/kizitonwose/CalendarView#setup) instructions, the next thing you need to do is change your imports for date/time related classes from `org.threeten.bp.*` to `java.time.*`.

You also need to remove the line `AndroidThreeTen.init(this)` from the `onCreate()` method of your application class as it's no longer needed.

## Share your creations

Made a cool calendar with this library? Share an image [here](https://github.com/kizitonwose/Calendar/issues/1).

## Contributing

Found a bug? feel free to fix it and send a pull request or [open an issue](https://github.com/kizitonwose/CalendarView/issues).

## Inspiration

CalendarView was inspired by the iOS library [JTAppleCalendar](https://github.com/patchthecode/JTAppleCalendar). I used JTAppleCalendar in an iOS project but couldn't find anything as customizable on Android so I built this. 
You'll find some similar terms like `InDateStyle`, `OutDateStyle`, `DayOwner` etc.

## License
CalendarView is distributed under the MIT license. See [LICENSE](https://github.com/kizitonwose/CalendarView/blob/master/LICENSE.md) for details.

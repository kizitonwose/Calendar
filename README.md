# Calendar

A highly customizable calendar library for Android, powered by RecyclerView for the view system, and LazyRow/LazyColumn for compose.

[![CI](https://github.com/kizitonwose/Calendar/workflows/CI/badge.svg?branch=master)](https://github.com/kizitonwose/Calendar/actions) 
[![Maven Central](https://img.shields.io/maven-central/v/com.kizitonwose.calendar/view)](https://repo1.maven.org/maven2/com/kizitonwose/calendar/) 
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/kizitonwose/Calendar/blob/master/LICENSE.md) 
[![Twitter](https://img.shields.io/badge/Twitter-@kizitonwose-9C27B0.svg)](https://twitter.com/kizitonwose)


**With this library, your calendar will look however you want it to.**

![Preview](https://user-images.githubusercontent.com/15170090/195625381-3955abc3-70fa-4577-94c1-54a96eade604.png)

## Features

- [x] Single or range selection - The library provides the calendar logic which enables you to implement the view/composable whichever way you like.
- [x] Week or month mode - show a week-based calendar, or the typical month calendar.
- [x] Disable desired dates - Prevent selection of some dates by disabling them.
- [x] Boundary dates - limit the calendar date range.
- [x] Custom date view/composable - make your day cells look however you want, with any functionality you want.
- [x] Custom calendar view/composable - make your calendar look however you want, with whatever functionality you want.
- [x] Custom first day of the week - Use any day as the first day of the week.
- [x] Horizontal or vertical scrolling calendar.
- [x] Month/Week headers and footers - Add headers/footers of any kind on each month/week.
- [x] Easily scroll to any date/week/month on the calendar via user swipe actions or programmatically.
- [x] Use all RecyclerView/LazyRow/LazyColumn customizations since the calendar extends from RecyclerView for the view system and uses LazyRow/LazyColumn for compose.
- [x] Design your calendar [however you want.](https://github.com/kizitonwose/Calendar/issues/1) The library provides the logic, you provide the views/composables.

## Sample project

It's important to check out the sample app. There are lots of examples provided for both view and compose implementations. 
Most techniques that you would want to implement are already done in the examples.

Download the sample app [here](https://github.com/kizitonwose/Calendar/releases/download/2.0.0/sample.apk)

View the sample app's source code [here](https://github.com/kizitonwose/Calendar/tree/master/sample)

## Setup

The library uses `java.time` classes via [Java 8+ API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) for backward compatibility since these classes were added in Java 8.

#### Step 1

This step is required ONLY if your app's `minSdkVersion` is below 26. Jump to [step 2](#step-2) if this does not apply to you.

To set up your project for desugaring, you need to first ensure that you are using [Android Gradle plugin](https://developer.android.com/studio/releases/gradle-plugin#updating-plugin) 4.0.0 or higher.

Then include the following in your app's build.gradle file:

```groovy
android {
  defaultConfig {
    // Required ONLY if your minSdkVersion is below 21
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

Add the desired calendar library (view or compose) to your app `build.gradle`:

```groovy
dependencies {
    // The view calendar implementation
    implementation 'com.kizitonwose.calendar:view:<latest-version>'
  
    // The compose calendar implementation
    implementation 'com.kizitonwose.calendar:compose:<latest-version>'
}
```

You can find the latest version of the library on the maven central badge above.

**Note: If you're upgrading from version 1.x.x to 2.x.x, see the [migration guide](https://github.com/kizitonwose/Calendar#migration).**

## Usage

You can find the relevant documentation for the library in the links below.

|[View-based documentation](https://github.com/kizitonwose/Calendar/blob/master/docs/View.md)|[Compose documentation](https://github.com/kizitonwose/Calendar/blob/master/docs/Compose.md)|
|:-:|:-:|

## Share your creations

Made a cool calendar with this library? Share an image [here](https://github.com/kizitonwose/Calendar/issues/1).

## Contributing

Found a bug? feel free to fix it and send a pull request or [open an issue](https://github.com/kizitonwose/Calendar/issues).

## License
Calendar library is distributed under the MIT license. See [LICENSE](https://github.com/kizitonwose/Calendar/blob/master/LICENSE.md) for details.

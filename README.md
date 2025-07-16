# Calendar

A highly customizable calendar library for Android and Compose Multiplatform, backed by RecyclerView for the view system, and LazyRow/LazyColumn for compose.

[![Check](https://github.com/kizitonwose/Calendar/actions/workflows/check.yml/badge.svg?branch=main)](https://github.com/kizitonwose/Calendar/actions/workflows/check.yml)
[![License](https://img.shields.io/badge/License-MIT-0097A7.svg)](https://github.com/kizitonwose/Calendar/blob/main/LICENSE.md)
[![Twitter](https://img.shields.io/badge/Twitter-@kizitonwose-9C27B0.svg)](https://twitter.com/kizitonwose)
[![Android Library](https://img.shields.io/badge/dynamic/xml.svg?label=Android%20Library&color=blue&url=https://repo1.maven.org/maven2/com/kizitonwose/calendar/core/maven-metadata.xml&query=(//metadata/versioning/versions/version)[not(contains(text(),%27-%27))][last()])](https://central.sonatype.com/search?q=g:com.kizitonwose.calendar)
[![Multiplatform Library](https://img.shields.io/badge/dynamic/xml.svg?label=Multiplatform%20Library&color=blue&url=https://repo1.maven.org/maven2/com/kizitonwose/calendar/compose-multiplatform/maven-metadata.xml&query=(//metadata/versioning/versions/version)[not(contains(text(),%27-%27))][last()])](https://central.sonatype.com/search?q=g:com.kizitonwose.calendar)

**With this library, your calendar will look however you want it to.**

![Preview Phone](https://user-images.githubusercontent.com/15170090/197389318-b3925b65-aed9-4e1f-a778-ba73007cbdf7.png)

![Preview Web Tablet](https://github.com/user-attachments/assets/df7b11bb-23f8-423a-bbd4-9ade376a14be)

## Features

- [x] Week, month, or year modes - Show a week-based calendar, or the typical month calendar, or a year-based calendar.
- [x] Single, multiple, or range selection - Total flexibility to implement the date selection
  whichever way you like.
- [x] Disable desired dates - Prevent selection of some dates by disabling them.
- [x] Boundary dates - Limit the calendar date range.
- [x] Custom date view/composable - Make your day cells look however you want, with any
  functionality you want.
- [x] Custom calendar view/composable - Make your calendar look however you want, with whatever
  functionality you want.
- [x] Custom first day of the week - Use any day as the first day of the week.
- [x] Horizontal or vertical scrolling calendar.
- [x] HeatMap calendar - Suitable for showing how data changes over time, like GitHub's contribution
  chart.
- [x] Year/Month/Week headers and footers - Add headers/footers of any kind on each year/month/week.
- [x] Easily scroll to any date/week/month/year on the calendar via swipe actions or programmatically.
- [x] Use all RecyclerView/LazyRow/LazyColumn customizations since the calendar extends from
  RecyclerView for the view system and uses LazyRow/LazyColumn for compose.
- [x] Design your calendar [however you want.](https://github.com/kizitonwose/Calendar/issues/1) The
  library provides the logic, you provide the views/composables.

## Sample project

It's important to check out the sample app. There are lots of examples provided for both view and compose implementations. 
Most techniques that you would want to implement are already done in the examples.

Download the Android sample app [here](https://github.com/kizitonwose/Calendar/releases/download/2.5.4/sample.apk)

View the Android sample app's source code [here](https://github.com/kizitonwose/Calendar/tree/main/sample)

View the multiplatform sample project online at https://calendar.kizitonwose.dev

View the multiplatform sample project's source code [here](https://github.com/kizitonwose/Calendar/tree/main/compose-multiplatform/sample)

## Setup

The library provides the following artifacts: 

`com.kizitonwose.calendar:compose`: The compose artifact for Android projects. This uses the [java.time](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) APIs. 

`com.kizitonwose.calendar:compose-multiplatform`: The compose artifact for Compose Multiplatform projects. This uses the [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) library and supports Android, iOS, js, WasmJs and Desktop platforms.

`com.kizitonwose.calendar:view`: The view artifact for Android projects. This uses the [java.time](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) APIs and can exist alongside the Android compose artifact in an Android project if needed.

#### Step 1

**This step is required ONLY if your Android app's `minSdkVersion` is below 26. Jump to [step 2](#step-2) if this does not apply to you.**

Android apps with `minSdkVersion` below 26 have to enable [Java 8+ API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) for backward compatibility since `java.time` classes were added in Java 8 which is supported natively starting from Android SDK 26. To set up your project for desugaring, you need to first ensure that you are using [Android Gradle plugin](https://developer.android.com/studio/releases/gradle-plugin#updating-plugin) 4.0.0 or higher.

Then include the following in your app's `build.gradle` file:

```groovy
android {
  defaultConfig {
    // Required ONLY if your minSdkVersion is below 21
    multiDexEnabled true
  }

  compileOptions {
    // Enable support for the new language APIs
    coreLibraryDesugaringEnabled true
    // Set Java compatibility (version can be higher if desired)
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    // Also add this for Kotlin projects (version can be higher if desired)
    jvmTarget = "1.8"
  }
}

dependencies {
  coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:<latest-version>'
}
```

You can find the latest version of `desugar_jdk_libs` [here](https://mvnrepository.com/artifact/com.android.tools/desugar_jdk_libs).

#### Step 2A - For pure Android projects without multiplatform setup

Add the desired calendar library (view or compose) to your app's `build.gradle.kts`:

```kotlin
dependencies {
  // The view calendar library for Android
  implementation("com.kizitonwose.calendar:view:<latest-version>")

  // The compose calendar library for Android
  implementation("com.kizitonwose.calendar:compose:<latest-version>")
}
```

#### Step 2B - For Compose Multiplatform projects

Add the multiplatform calendar library to your project's `build.gradle.kts`:

```kotlin
commonMain.dependencies {
  // The calendar library for compose multiplatform projects
  // Supports Android, iOS, js, WasmJs and Desktop platforms
  implementation("com.kizitonwose.calendar:compose-multiplatform:<latest-version>")
}
```

You can find the latest version of the library on the maven central badge above.

Snapshots of the development version are available in [Central Portal Snapshots repository](https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/com/kizitonwose/calendar/).

#### Compose UI version compatibility

For the compose calendar library, ensure that you are using the library version that matches the Compose UI version in your project. If you use a version of the library that has a higher version of Compose UI than the one in your project, gradle will upgrade the Compose UI version in your project via transitive dependency.

| Compose UI | Android Calendar Library | Multiplatform Calendar Library |
|:----------:|:------------------------:|:------------------------------:|
|   1.2.x    |          2.0.x           |               -                |
|   1.3.x    |      2.1.x - 2.2.x       |               -                |
|   1.4.x    |          2.3.x           |               -                |
|   1.5.x    |          2.4.x           |               -                |
|   1.6.x    |          2.5.x           |             2.5.x              |
|   1.7.x    |          2.6.x           |             2.6.x              |
|   1.8.x    |      2.7.x - 2.8.x       |         2.7.x - 2.8.x          |

## Usage

You can find the relevant documentation for the library in the links below.

|[View-based documentation](https://github.com/kizitonwose/Calendar/blob/main/docs/View.md)|[Compose documentation](https://github.com/kizitonwose/Calendar/blob/main/docs/Compose.md)|
|:-:|:-:|

## Migration

If you're upgrading from calendar library version 1.x.x to 2.x.x, see the [migration guide](https://github.com/kizitonwose/calendar/blob/main/docs/MigrationGuide.md).

## Share your creations

Made a cool calendar with this library? Share an image [here](https://github.com/kizitonwose/Calendar/issues/1).

## Contributing

Found a bug? feel free to fix it and send a pull request or [open an issue](https://github.com/kizitonwose/Calendar/issues).

## License

Calendar library is distributed under the MIT license.
See [LICENSE](https://github.com/kizitonwose/Calendar/blob/main/LICENSE.md) for details.

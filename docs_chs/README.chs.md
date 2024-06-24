# Calendar

中文翻译：[VinKon](https://github.com/Blackwen)

适用于 Android 的高度可定制的日历库，由用于视图系统的 RecyclerView 和用于撰写的 LazyRow/LazyColumn 提供支持。

[![Tests](https://github.com/kizitonwose/Calendar/workflows/Check/badge.svg?branch=main)](https://github.com/kizitonwose/Calendar/actions)
[![Maven Central](https://img.shields.io/badge/dynamic/xml.svg?label=Maven%20Central&color=blue&url=https://repo1.maven.org/maven2/com/kizitonwose/calendar/core/maven-metadata.xml&query=(//metadata/versioning/versions/version)[not(contains(text(),%27-%27))][last()])](https://repo1.maven.org/maven2/com/kizitonwose/calendar/)
[![Maven Central Beta](https://img.shields.io/badge/dynamic/xml.svg?label=Maven%20Central%20Beta&color=slateblue&url=https://repo1.maven.org/maven2/com/kizitonwose/calendar/core/maven-metadata.xml&query=(//metadata/versioning/versions/version)[contains(text(),%27beta%27)][last()])](https://repo1.maven.org/maven2/com/kizitonwose/calendar/)
[![License](https://img.shields.io/badge/License-MIT-0097A7.svg)](https://github.com/kizitonwose/Calendar/blob/main/LICENSE.md)
[![Twitter](https://img.shields.io/badge/Twitter-@kizitonwose-9C27B0.svg)](https://twitter.com/kizitonwose)
[![cn](https://img.shields.io/badge/Lang-Chinese-blue?color=%23FF0000)](docs_chs/README.chs.md)



**通过使用这个库，你可以使你的日历呈现出你想要的任何样式。**

![Preview](https://user-images.githubusercontent.com/15170090/197389318-b3925b65-aed9-4e1f-a778-ba73007cbdf7.png)

## 特征

- [x] 单选、多选或范围选择 - 完全灵活，可以以你喜欢的方式实现日期的选择。
- [x] 周模式或月模式 - 显示以周为基础的日历，或者传统的月份日历。
- [x] 禁用特定日期 - 通过禁用，防止选择某些日期。
- [x] 边界日期 - 限制日历的日期范围。
- [x] 自定义日期视图/可组合 - 使你的日期单元格呈现出你希望的外观。
- [x] 自定义日历视图/可组合 - 使你的日历呈现出你想要的样子，具备你想要的任何功能。
- [x] 自定义一周的第一天 - 使用任何一天作为一周的第一天。
- [x] 横向或纵向滚动的日历。
- [x] 热力图日历 - 适用于展示随时间变化的数据，比如 GitHub 的贡献图表。
- [x] 月/周标题和页脚 - 在每个月/周上添加任何类型的标题/页脚。
- [x] 通过滑动操作或以编程方式轻松滚动到日历上的任意日期/周/月。
- [x] 充分利用所有 RecyclerView/LazyRow/LazyColumn 的自定义选项，因为该日历是基于 RecyclerView 用于视图系统，并使用 LazyRow/LazyColumn 用于 Compose。
- [x] 按照[你的意愿](https://github.com/kizitonwose/Calendar/issues/1)设计你的日历。该库提供逻辑，由你提供视图/组件。

## 示例项目

查看示例应用程序非常重要。其中提供了许多视图和 Compose 实现的示例。大多数你想要实现的技术在这些示例中已经完成。

下载示例应用程序 [这里](https://github.com/kizitonwose/Calendar/releases/download/2.0.0/sample.apk)

查看示例应用程序的源代码 [这里](https://github.com/kizitonwose/Calendar/tree/main/sample)

## 设置

该库通过 [Java 8+ API desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) 使用 `java.time` 类，以确保向后兼容性，因为这些类是在 Java 8 中添加的。

#### 步骤 1

如果你的应用的 `minSdkVersion` 小于 26，则需要执行这一步。如果不适用，请直接跳转到 [步骤 2](#step-2)。

要设置项目以进行脱糖，您需要首先确保您使用的是 Android Gradle 插件 4.0.0 或更高版本。

然后将以下内容写入到应用程序的 build.gradle 文件中：

```groovy
android {
  defaultConfig {
    // 仅在你的 minSdkVersion 低于 21 时需要
    multiDexEnabled true
  }

  compileOptions {
    // 启用对新语言 API 的支持
    coreLibraryDesugaringEnabled true
    // 设置Java兼容性（如果需要的话版本可以更高）
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    // 同样，对于Kotlin项目，也添加这个（有需要的话版本可以更高）
    jvmTarget = "1.8"
  }
}

dependencies {
  coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:<latest-version>'
}
```

你可以在[这里](https://mvnrepository.com/artifact/com.android.tools/desugar_jdk_libs)找到 `desugar_jdk_libs` 的最新版本。

#### 步骤 2

将所需的日历库（视图或Compose）添加到你的应用的 `build.gradle` 文件中：

```groovy
dependencies {
  // 视图日历库
  implementation 'com.kizitonwose.calendar:view:<latest-version>'

  // Compose日历库
  implementation 'com.kizitonwose.calendar:compose:<latest-version>'
}
```

你可以在上面的 Maven 中央徽章中找到该库的最新版本。

开发版本的快照可以在 [Sonatype 的快照仓库](https://s01.oss.sonatype.org/content/repositories/snapshots/com/kizitonwose/calendar/) 中找到。

如果你正在从版本1.x.x升级到2.x.x，请查阅[迁移指南](https://github.com/kizitonwose/calendar/blob/main/docs/MigrationGuide.md)。

对于 Compose 日历库，请确保你使用的库版本与项目中的 Compose UI 版本匹配。如果你使用的库版本比项目中的 Compose UI 版本更高，Gradle 将通过传递依赖升级项目中的 Compose UI 版本。

| Compose UI | Calendar Library |
|:----------:|:----------------:|
|   1.2.x    |      2.0.x       |
|   1.3.x    |  2.1.x - 2.2.x   |
|   1.4.x    |      2.3.x       |
|   1.5.x    |      2.4.x       |
|   1.6.x    |      2.5.x       |

## 用法

你可以在下面的链接中找到该库的相关文档。

| [视图相关的文档](View.chs.md) | [Compose 文档](Compose.chs.md) |
| :---------------------------: | :----------------------------: |

## 分享你的创作

使用这个库创建了一个酷炫的日历？在[这里](https://github.com/kizitonwose/Calendar/issues/1)分享一张图片。

## 贡献

发现 bug ？请随时修复并发送拉取请求，或者[提出一个问题](https://github.com/kizitonwose/Calendar/issues)。

## 许可证

Calendar 库采用 MIT 许可证分发。详细信息请参阅 [LICENSE](https://github.com/kizitonwose/Calendar/blob/main/LICENSE.md)。


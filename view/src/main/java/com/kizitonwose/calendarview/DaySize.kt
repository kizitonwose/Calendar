package com.kizitonwose.calendarview;

enum class DaySize {
    Square, SeventhWidth, FreeForm;

    val parentDecidesWidth: Boolean
        get() = this == Square || this == SeventhWidth

    val parentDecidesHeight: Boolean
        get() = this == Square
}
package com.kizitonwose.calendar.view

import androidx.annotation.Px

public data class MarginValues(
    @Px val start: Int = 0,
    @Px val top: Int = 0,
    @Px val end: Int = 0,
    @Px val bottom: Int = 0,
) {
    public constructor(
        @Px horizontal: Int = 0,
        @Px vertical: Int = 0,
    ) : this(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical,
    )
}

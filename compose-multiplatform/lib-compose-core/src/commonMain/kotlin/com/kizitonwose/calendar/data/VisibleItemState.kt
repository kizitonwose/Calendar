package com.kizitonwose.calendar.data

import androidx.compose.runtime.Immutable

@Immutable
internal class VisibleItemState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
)

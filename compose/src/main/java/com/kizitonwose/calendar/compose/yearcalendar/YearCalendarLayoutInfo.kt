package com.kizitonwose.calendar.compose.yearcalendar

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import com.kizitonwose.calendar.core.CalendarYear

/**
 * Contains useful information about the currently displayed layout state of the calendar.
 * For example you can get the list of currently displayed years.
 *
 * Use [YearCalendarState.layoutInfo] to retrieve this.
 *
 * @see LazyListLayoutInfo
 */
public class YearCalendarLayoutInfo(
    info: LazyListLayoutInfo,
    private val getIndexData: (Int) -> CalendarYear,
) : LazyListLayoutInfo by info {
    /**
     * The list of [YearCalendarItemInfo] representing all the currently visible years.
     */
    public val visibleYearsInfo: List<YearCalendarItemInfo>
        get() = visibleItemsInfo.map { info ->
            YearCalendarItemInfo(info, getIndexData(info.index))
        }
}

/**
 * Contains useful information about an individual year on the calendar.
 *
 * @param year The year in the list.

 * @see YearCalendarLayoutInfo
 * @see LazyListItemInfo
 */
public class YearCalendarItemInfo(info: LazyListItemInfo, public val year: CalendarYear) :
    LazyListItemInfo by info

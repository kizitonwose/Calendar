package com.kizitonwose.calendar.view

import android.view.View
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay

public open class ViewContainer(public val view: View)

/**
 * Responsible for managing a view for a given [Data].
 * [create] will be called only once but [bind] will
 * be called whenever the view needs to be used to
 * bind an instance of the associated [Data].
 */
public interface Binder<Data, Container : ViewContainer> {
    public fun create(view: View): Container
    public fun bind(container: Container, data: Data)
}

public interface WeekDayBinder<Container : ViewContainer> : Binder<WeekDay, Container>

public interface WeekHeaderFooterBinder<Container : ViewContainer> : Binder<Week, Container>

public interface MonthDayBinder<Container : ViewContainer> : Binder<CalendarDay, Container>

public interface MonthHeaderFooterBinder<Container : ViewContainer> : Binder<CalendarMonth, Container>

public typealias MonthScrollListener = (CalendarMonth) -> Unit

public typealias WeekScrollListener = (Week) -> Unit

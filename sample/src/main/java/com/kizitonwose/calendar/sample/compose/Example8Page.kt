package com.kizitonwose.calendar.sample.compose

//@Composable
//fun Example8Page(horizontal: Boolean = true) {
//    val today = remember { LocalDate.now() }
//    val currentMonth = remember(today) { today.yearMonth }
//    val startMonth = remember { currentMonth.minusMonths(500) }
//    val endMonth = remember { currentMonth.plusMonths(500) }
//    val selections = remember { mutableStateListOf<CalendarDay>() }
//    val daysOfWeek = remember { daysOfWeek() }
//    StatusBarColorUpdateEffect(color = colorResource(id = R.color.example_1_bg_light))
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(colorResource(id = R.color.example_1_bg_light))
//            .padding(top = 20.dp),
//    ) {
//        val state = rememberCalendarState(
//            startMonth = startMonth,
//            endMonth = endMonth,
//            firstVisibleMonth = currentMonth,
//            firstDayOfWeek = daysOfWeek.first(),
//            outDateStyle = OutDateStyle.EndOfGrid,
//        )
//        val coroutineScope = rememberCoroutineScope()
//        val visibleMonth = rememberFirstVisibleMonthAfterScroll(state)
//        // Draw light content on dark background.
//        CompositionLocalProvider(LocalContentColor provides darkColors().onSurface) {
//            SimpleCalendarTitle(
//                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
//                currentMonth = visibleMonth.yearMonth,
//                goToPrevious = {
//                    coroutineScope.launch {
//                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
//                    }
//                },
//                goToNext = {
//                    coroutineScope.launch {
//                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
//                    }
//                },
//            )
//            FullScreenCalendar(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(colorResource(id = R.color.example_1_bg))
//                    .testTag("Calendar"),
//                state = state,
//                horizontal = horizontal,
//                dayContent = { day ->
//                    Day(
//                        day = day,
//                        isSelected = selections.contains(day),
//                        isToday = day.position == DayPosition.MonthDate && day.date == today,
//                    ) { clicked ->
//                        if (selections.contains(clicked)) {
//                            selections.remove(clicked)
//                        } else {
//                            selections.add(clicked)
//                        }
//                    }
//                },
//                // The month body is only needed for ui test tag.
//                monthBody = { _, content ->
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .testTag("MonthBody"),
//                    ) {
//                        content()
//                    }
//                },
//                monthHeader = {
//                    MonthHeader(daysOfWeek = daysOfWeek)
//                },
//                monthFooter = { month ->
//                    val count = month.weekDays.flatten()
//                        .count { selections.contains(it) }
//                    MonthFooter(selectionCount = count)
//                },
//            )
//        }
//    }
//}
//
//@Composable
//private fun FullScreenCalendar(
//    modifier: Modifier,
//    state: CalendarState,
//    horizontal: Boolean,
//    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
//    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
//    monthBody: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
//    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit,
//) {
//    if (horizontal) {
//        HorizontalCalendar(
//            modifier = modifier,
//            state = state,
//            calendarScrollPaged = true,
//            contentHeightMode = ContentHeightMode.Fill,
//            dayContent = dayContent,
//            monthBody = monthBody,
//            monthHeader = monthHeader,
//            monthFooter = monthFooter,
//        )
//    } else {
//        VerticalCalendar(
//            modifier = modifier,
//            state = state,
//            calendarScrollPaged = true,
//            contentHeightMode = ContentHeightMode.Fill,
//            dayContent = dayContent,
//            monthBody = monthBody,
//            monthHeader = monthHeader,
//            monthFooter = monthFooter,
//        )
//    }
//}
//
//@Composable
//private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
//    Row(
//        Modifier
//            .fillMaxWidth()
//            .testTag("MonthHeader")
//            .background(colorResource(id = R.color.example_1_bg_secondary))
//            .padding(vertical = 8.dp),
//    ) {
//        for (dayOfWeek in daysOfWeek) {
//            Text(
//                modifier = Modifier.weight(1f),
//                textAlign = TextAlign.Center,
//                fontSize = 15.sp,
//                text = dayOfWeek.displayText(),
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//private fun MonthFooter(selectionCount: Int) {
//    Box(
//        Modifier
//            .fillMaxWidth()
//            .testTag("MonthFooter")
//            .background(colorResource(id = R.color.example_1_bg_secondary))
//            .padding(vertical = 10.dp),
//        contentAlignment = Alignment.Center,
//    ) {
//        val text = if (selectionCount == 0) {
//            stringResource(R.string.example_8_zero_selection)
//        } else {
//            pluralStringResource(R.plurals.example_8_selection, selectionCount, selectionCount)
//        }
//        Text(text = text)
//    }
//}
//
//@Composable
//private fun Day(
//    day: CalendarDay,
//    isSelected: Boolean,
//    isToday: Boolean,
//    onClick: (CalendarDay) -> Unit,
//) {
//    Box(
//        Modifier
//            .fillMaxWidth()
//            .fillMaxHeight()
//            .clip(RectangleShape)
//            .background(
//                color = when {
//                    isSelected -> colorResource(R.color.example_1_selection_color)
//                    isToday -> colorResource(id = R.color.example_1_white_light)
//                    else -> Color.Transparent
//                },
//            )
//            // Disable clicks on inDates/outDates
//            .clickable(
//                enabled = day.position == DayPosition.MonthDate,
//                showRipple = !isSelected,
//                onClick = { onClick(day) },
//            ),
//        contentAlignment = Alignment.Center,
//    ) {
//        val textColor = when (day.position) {
//            // Color.Unspecified will use the default text color from the current theme
//            DayPosition.MonthDate -> if (isSelected) colorResource(R.color.example_1_bg) else Color.Unspecified
//            DayPosition.InDate, DayPosition.OutDate -> colorResource(R.color.example_1_white_light)
//        }
//        Text(
//            text = day.date.dayOfMonth.toString(),
//            color = textColor,
//            fontSize = 15.sp,
//        )
//    }
//}
//
//@Preview
//@Composable
//private fun Example8Preview() {
//    Example8Page()
//}

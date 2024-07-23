package com.kizitonwose.calendar.core

internal infix fun <A, B, C> Pair<A, B>.toResult(that: C): Triple<A, B, C> = Triple(first, second, that)

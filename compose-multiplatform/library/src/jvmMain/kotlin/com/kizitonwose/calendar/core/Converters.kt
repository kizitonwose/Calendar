@file:Suppress("NewApi")

package com.kizitonwose.calendar.core

import java.time.Year as JavaYear

public fun Year.toJavaYear(): JavaYear = JavaYear.of(value)

public fun JavaYear.toKotlinYear(): Year = Year(value)

package com.kizitonwose.calendar.core

import kotlin.test.Test
import kotlin.test.assertEquals

class YearTest {
    @Test
    fun isLeap() {
        assertEquals(false, Year.isLeap(1999))
        assertEquals(true, Year.isLeap(2000))
        assertEquals(false, Year.isLeap(2001))
        assertEquals(false, Year.isLeap(2007))
        assertEquals(true, Year.isLeap(2008))
        assertEquals(false, Year.isLeap(2009))
        assertEquals(false, Year.isLeap(2010))
        assertEquals(false, Year.isLeap(2011))
        assertEquals(true, Year.isLeap(2012))
        assertEquals(false, Year.isLeap(2095))
        assertEquals(true, Year.isLeap(2096))
        assertEquals(false, Year.isLeap(2097))
        assertEquals(false, Year.isLeap(2098))
        assertEquals(false, Year.isLeap(2099))
        assertEquals(false, Year.isLeap(2100))
        assertEquals(false, Year.isLeap(2101))
        assertEquals(false, Year.isLeap(2102))
        assertEquals(false, Year.isLeap(2103))
        assertEquals(true, Year.isLeap(2104))
        assertEquals(false, Year.isLeap(2105))
        assertEquals(false, Year.isLeap(-500))
        assertEquals(true, Year.isLeap(-400))
        assertEquals(false, Year.isLeap(-300))
        assertEquals(false, Year.isLeap(-200))
        assertEquals(false, Year.isLeap(-100))
        assertEquals(true, Year.isLeap(0))
        assertEquals(false, Year.isLeap(100))
        assertEquals(false, Year.isLeap(200))
        assertEquals(false, Year.isLeap(300))
        assertEquals(true, Year.isLeap(400))
        assertEquals(false, Year.isLeap(500))
    }
}

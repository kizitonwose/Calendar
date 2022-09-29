package com.kizitonwose.calendarcompose.internal

/**
 * Basically [MutableMap.getOrPut] but allows us read the map
 * in multiple places without calling `getOrPut` everywhere.
 */
internal class MonthDataStore(private val create: (offset: Int) -> MonthData) :
    HashMap<Int, MonthData>() {
    override fun get(key: Int): MonthData {
        val value = super.get(key)
        return if (value == null) {
            val data = create(key)
            put(key, data)
            data
        } else {
            value
        }
    }
}
package com.kizitonwose.calendarviewsample

import org.threeten.bp.YearMonth

private typealias Airport = Flight.Airport

fun generateFlights(): List<Flight> {
    val list = mutableListOf<Flight>()
    val currentMonth = YearMonth.now()

    val currentMonth17 = currentMonth.atDay(17)
    list.add(Flight(currentMonth17.atTime(14, 0), Airport("Lagos", "LOS"), Airport("Abuja", "ABV"), R.color.brown_700))
    list.add(Flight(currentMonth17.atTime(21, 30), Airport("Enugu", "ENU"), Airport("Owerri", "QOW"), R.color.blue_grey_700))

    val currentMonth22 = currentMonth.atDay(22)
    list.add(Flight(currentMonth22.atTime(13, 20), Airport("Ibadan", "IBA"), Airport("Benin", "BNI"), R.color.blue_800))
    list.add(Flight(currentMonth22.atTime(17, 40), Airport("Sokoto", "SKO"), Airport("Ilorin", "ILR"), R.color.red_800))

    list.add(
        Flight(
            currentMonth.atDay(3).atTime(20, 0),
            Airport("Makurdi", "MDI"),
            Airport("Calabar", "CBQ"),
            R.color.teal_700
        )
    )

    list.add(
        Flight(
            currentMonth.atDay(12).atTime(18, 15),
            Airport("Kaduna", "KAD"),
            Airport("Jos", "JOS"),
            R.color.cyan_700
        )
    )

    val nextMonth13 = currentMonth.plusMonths(1).atDay(13)
    list.add(Flight(nextMonth13.atTime(7, 30), Airport("Kano", "KAN"), Airport("Akure", "AKR"), R.color.pink_700))
    list.add(Flight(nextMonth13.atTime(10, 50), Airport("Minna", "MXJ"), Airport("Zaria", "ZAR"), R.color.green_700))

    list.add(
        Flight(
            currentMonth.minusMonths(1).atDay(9).atTime(20, 15),
            Airport("Asaba", "ABB"),
            Airport("Port Harcourt", "PHC"),
            R.color.orange_800
        )
    )

    return list
}
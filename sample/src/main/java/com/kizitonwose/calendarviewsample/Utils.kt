package com.kizitonwose.calendarviewsample

import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth

private typealias Airport = Flight.Airport

fun generateFlights(): List<Flight> {
    val list = mutableListOf<Flight>()
    val now = LocalDateTime.now()
    list.add(Flight(now.plusHours(4), Airport("Lagos", "LOS"), Airport("Abuja", "ABV"), R.color.brown_700))
    list.add(Flight(now.plusHours(8), Airport("Enugu", "ENU"), Airport("Owerri", "QOW"), R.color.blue_grey_700))

    val nextTwoWeeks = LocalDateTime.now().plusWeeks(2)
    list.add(Flight(nextTwoWeeks, Airport("Ibadan", "IBA"), Airport("Benin", "BNI"), R.color.orange_700))
    list.add(Flight(nextTwoWeeks.plusHours(3), Airport("Sokoto", "SKO"), Airport("Ilorin", "ILR"), R.color.green_700))

    list.add(
        Flight(
            LocalDateTime.now().minusWeeks(1).minusHours(4),
            Airport("Makurdi", "MDI"),
            Airport("Calabar", "CBQ"),
            R.color.teal_700
        )
    )

    list.add(
        Flight(
            LocalDateTime.now().minusWeeks(2).plusHours(7),
            Airport("Kaduna", "KAD"),
            Airport("Jos", "JOS"),
            R.color.pink_700
        )
    )

    list.add(
        Flight(
            YearMonth.now().plusMonths(1).atDay(13).atTime(7, 30),
            Airport("Kano", "KAN"),
            Airport("Akure", "AKR"),
            R.color.blue_700
        )
    )
    list.add(
        Flight(
            YearMonth.now().plusMonths(1).atDay(13).atTime(10, 50),
            Airport("Minna", "MXJ"),
            Airport("Zaria", "ZAR"),
            R.color.red_700
        )
    )

    list.add(
        Flight(
            YearMonth.now().minusMonths(1).atDay(9).atTime(20, 15),
            Airport("Asaba", "ABB"),
            Airport("Port Harcourt", "PHC"),
            R.color.cyan_700
        )
    )

    return list
}
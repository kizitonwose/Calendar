package com.kizitonwose.calendarviewsample

import org.threeten.bp.YearMonth

private typealias Airport = Flight.Airport

fun generateFlights(): List<Flight> {
    val list = mutableListOf<Flight>()
    val date18 = YearMonth.now().atDay(17).atTime(10, 0)
    list.add(Flight(date18.plusHours(4), Airport("Lagos", "LOS"), Airport("Abuja", "ABV"), R.color.brown_700))
    list.add(Flight(date18.plusHours(8), Airport("Enugu", "ENU"), Airport("Owerri", "QOW"), R.color.blue_grey_700))

    val date23 = YearMonth.now().atDay(22)
    list.add(Flight(date23.atTime(13, 20), Airport("Ibadan", "IBA"), Airport("Benin", "BNI"), R.color.blue_900))
    list.add(Flight(date23.atTime(17, 40), Airport("Sokoto", "SKO"), Airport("Ilorin", "ILR"), R.color.red_900))

    list.add(
        Flight(
            YearMonth.now().atDay(3).atTime(20, 0),
            Airport("Makurdi", "MDI"),
            Airport("Calabar", "CBQ"),
            R.color.teal_700
        )
    )

    list.add(
        Flight(
            YearMonth.now().atDay(12).atTime(18, 15),
            Airport("Kaduna", "KAD"),
            Airport("Jos", "JOS"),
            R.color.cyan_700
        )
    )

    list.add(
        Flight(
            YearMonth.now().plusMonths(1).atDay(13).atTime(7, 30),
            Airport("Kano", "KAN"),
            Airport("Akure", "AKR"),
            R.color.pink_700
        )
    )
    list.add(
        Flight(
            YearMonth.now().plusMonths(1).atDay(13).atTime(10, 50),
            Airport("Minna", "MXJ"),
            Airport("Zaria", "ZAR"),
            R.color.green_700
        )
    )

    list.add(
        Flight(
            YearMonth.now().minusMonths(1).atDay(9).atTime(20, 15),
            Airport("Asaba", "ABB"),
            Airport("Port Harcourt", "PHC"),
            R.color.orange_900
        )
    )

    return list
}
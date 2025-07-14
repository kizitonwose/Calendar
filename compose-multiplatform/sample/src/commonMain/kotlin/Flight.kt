
import androidx.compose.ui.graphics.Color
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atTime
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.onDay

private typealias Airport = Flight.Airport

data class Flight(
    val time: LocalDateTime,
    val departure: Airport,
    val destination: Airport,
    val color: Color,
) {
    data class Airport(val city: String, val code: String)
}

fun generateFlights(): List<Flight> = buildList {
    val currentMonth = YearMonth.now()

    currentMonth.onDay(17).also { date ->
        add(
            Flight(
                date.atTime(14, 0),
                Airport("Lagos", "LOS"),
                Airport("Abuja", "ABV"),
                Color(0xFF1565C0),
            ),
        )
        add(
            Flight(
                date.atTime(21, 30),
                Airport("Enugu", "ENU"),
                Airport("Owerri", "QOW"),
                Color(0xFFC62828),
            ),
        )
    }

    currentMonth.onDay(22).also { date ->
        add(
            Flight(
                date.atTime(13, 20),
                Airport("Ibadan", "IBA"),
                Airport("Benin", "BNI"),
                Color(0xFF5D4037),
            ),
        )
        add(
            Flight(
                date.atTime(17, 40),
                Airport("Sokoto", "SKO"),
                Airport("Ilorin", "ILR"),
                Color(0xFF455A64),
            ),
        )
    }

    currentMonth.onDay(3).also { date ->
        add(
            Flight(
                date.atTime(20, 0),
                Airport("Makurdi", "MDI"),
                Airport("Calabar", "CBQ"),
                Color(0xFF00796B),
            ),
        )
    }

    currentMonth.onDay(12).also { date ->
        add(
            Flight(
                date.atTime(18, 15),
                Airport("Kaduna", "KAD"),
                Airport("Jos", "JOS"),
                Color(0xFF0097A7),
            ),
        )
    }

    currentMonth.plusMonths(1).onDay(13).also { date ->
        add(
            Flight(
                date.atTime(7, 30),
                Airport("Kano", "KAN"),
                Airport("Akure", "AKR"),
                Color(0xFFC2185B),
            ),
        )
        add(
            Flight(
                date.atTime(10, 50),
                Airport("Minna", "MXJ"),
                Airport("Zaria", "ZAR"),
                Color(0xFFEF6C00),
            ),
        )
    }

    currentMonth.minusMonths(1).onDay(9).also { date ->
        add(
            Flight(
                date.atTime(20, 15),
                Airport("Asaba", "ABB"),
                Airport("Port Harcourt", "PHC"),
                Color(0xFFEF6C00),
            ),
        )
    }
}

val flightDateTimeFormatter by lazy {
    LocalDateTime.Format {
        dayOfWeek(DayOfWeekNames(DayOfWeekNames.ENGLISH_ABBREVIATED.names.map { it.uppercase() }))
        char('\n')
        dayOfMonth(Padding.ZERO)
        char(' ')
        monthName(MonthNames(MonthNames.ENGLISH_ABBREVIATED.names.map { it.uppercase() }))
        char('\n')
        hour()
        char(':')
        minute()
    }
}

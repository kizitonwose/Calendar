@file:UseSerializers(LocalDateSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.DayOfWeek
import java.time.LocalDate

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/** Color palette of the app when the wallpaper colors (Material You) are not used. */
@Serializable
enum class ColorPalette {
    BLUE,
    INDIGO,
    VIOLET,
    PINK,
    RED,
    ORANGE,
    AMBER,
    OLIVE,
    GREEN,
    TEAL,
    GRAPHITE,

    /** Hue chosen by the user ([AppearanceSettings.customHue]). */
    CUSTOM,
}

/** Destinations that can be placed in the bottom navigation bar. */
@Serializable
enum class NavItem {
    CALENDAR,
    FINANCE,
    STATS,
    HISTORY,
    UNCONFIRMED,
    ALARM,
    SEARCH,
    SCHEDULE,
    SETTINGS,
    ;

    companion object {
        /** Always shown: the calendar is the start screen, settings give access to everything else. */
        val REQUIRED: Set<NavItem> = setOf(CALENDAR, SETTINGS)
        val DEFAULT: List<NavItem> = listOf(CALENDAR, FINANCE, SETTINGS)
        const val MAX = 5

        /** Keeps the user's order, removes duplicates, adds the required items and limits the count. */
        fun normalize(items: List<NavItem>): List<NavItem> {
            val result = items.distinct().toMutableList()
            if (CALENDAR !in result) result.add(0, CALENDAR)
            if (SETTINGS !in result) result.add(SETTINGS)
            while (result.size > MAX) {
                val extra = result.lastOrNull { it !in REQUIRED } ?: break
                result.remove(extra)
            }
            return result
        }
    }
}

/** When the bottom bar shows the item names. */
@Serializable
enum class NavLabels {
    ALWAYS,
    SELECTED,
    NEVER,
}

@Serializable
data class AppearanceSettings(
    val palette: ColorPalette = ColorPalette.BLUE,
    /** Hue (0–359°) of the [ColorPalette.CUSTOM] palette. */
    val customHue: Int = 258,
    /** Black background in the dark theme (saves power on OLED screens). */
    val pureBlack: Boolean = false,
    val navItems: List<NavItem> = NavItem.DEFAULT,
    val navLabels: NavLabels = NavLabels.ALWAYS,
)

@Serializable
enum class VacationPayMethod {
    /** Do not calculate; the amount is entered manually. */
    NONE,

    /** Pay for planned shifts during the vacation. */
    PLANNED_SHIFTS,

    /** Average daily earnings × vacation days (art. 139 of the Russian Labour Code). */
    AVERAGE_EARNINGS,
}

@Serializable
enum class SickPayMethod {
    /** Do not calculate; the amount is entered manually. */
    NONE,

    /** Average daily earnings × insurance experience percent × days (Federal law 255-FZ). */
    BENEFIT_255FZ,
}

/** Default start date suggested when a schedule change is applied. */
@Serializable
enum class ApplyFromDefault {
    TODAY,
    TOMORROW,
    NEXT_MONTH,
}

@Serializable
data class CalendarSettings(
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val showShiftTimes: Boolean = true,
    val showWorkedHours: Boolean = true,
    val showDayEarnings: Boolean = false,
    val showHolidays: Boolean = true,
    val showMonthSummary: Boolean = true,
)

@Serializable
data class AbsenceRules(
    val vacationMethod: VacationPayMethod = VacationPayMethod.PLANNED_SHIFTS,
    val sickMethod: SickPayMethod = SickPayMethod.BENEFIT_255FZ,
    /** Public holidays inside a vacation are not counted as vacation days. */
    val excludeHolidaysFromVacation: Boolean = true,
    /** Minimum monthly wage used for the minimal sick benefit. */
    val minimumWage: Money = Money.ofRubles(27_093),
    /** Maximum insurance contribution base per year (limits the average earnings). */
    val yearlyCaps: Map<Int, Money> = defaultYearlyCaps(),
    /** Sick days paid by the employer. */
    val employerSickDays: Int = 3,
    /** Working days after the sick leave closes until the social fund pays. */
    val fundPaymentBusinessDays: Int = 10,
    /** Vacation pay reminder: working days before the vacation. */
    val vacationNotifyBusinessDaysBefore: Int = 2,
) {
    companion object {
        fun defaultYearlyCaps(): Map<Int, Money> = mapOf(
            2021 to Money.ofRubles(966_000),
            2022 to Money.ofRubles(1_032_000),
            2023 to Money.ofRubles(1_917_000),
            2024 to Money.ofRubles(2_225_000),
            2025 to Money.ofRubles(2_759_000),
            2026 to Money.ofRubles(2_979_000),
        )
    }
}

@Serializable
data class NotificationSettings(
    val shiftEnd: Boolean = true,
    /** Signed offset from the planned end. Negative values remind before the shift ends. */
    val shiftEndDelayMinutes: Int = 0,
    val unconfirmedReminder: Boolean = true,
    val unconfirmedReminderMinute: Int = 20 * 60,
    val payouts: Boolean = true,
    val payoutMinute: Int = 10 * 60,
    val vacationPay: Boolean = true,
    val sickPay: Boolean = true,
    val absenceMinute: Int = 9 * 60,
    val snoozeMinutes: Int = 60,
)

@Serializable
data class ScheduleSettings(
    /** How many months ahead shifts are generated automatically. */
    val horizonMonths: Int = 3,
    val applyFromDefault: ApplyFromDefault = ApplyFromDefault.TOMORROW,
    /** Keep individually changed days when a schedule is re-applied. */
    val keepUserChanges: Boolean = true,
)

@Serializable
data class SecuritySettings(
    val financeLock: Boolean = false,
    val pinHash: String? = null,
    val pinSalt: String? = null,
    val biometric: Boolean = false,
)

@Serializable
data class AppSettings(
    val onboardingDone: Boolean = false,
    val employmentStartDate: LocalDate? = null,
    /** Insurance experience before the current job, months. */
    val priorExperienceMonths: Int = 0,
    /** Date since which all shifts and earnings are recorded in the app. */
    val trackingStartDate: LocalDate? = null,
    val currency: String = "₽",
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val appearance: AppearanceSettings = AppearanceSettings(),
    val alarm: AlarmSettings = AlarmSettings(),
    val calendar: CalendarSettings = CalendarSettings(),
    val absence: AbsenceRules = AbsenceRules(),
    val notifications: NotificationSettings = NotificationSettings(),
    val schedule: ScheduleSettings = ScheduleSettings(),
    val security: SecuritySettings = SecuritySettings(),
    /** Use the built-in Russian public holidays calendar. */
    val russianHolidays: Boolean = true,
    /** Look for a new version of the app once a day and notify about it. */
    val autoUpdateCheck: Boolean = true,
)

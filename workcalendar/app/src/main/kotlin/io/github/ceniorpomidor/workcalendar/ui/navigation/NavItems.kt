package io.github.ceniorpomidor.workcalendar.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.ceniorpomidor.workcalendar.domain.model.NavItem
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons

/** How a destination of the bottom bar looks and where it leads. */
data class NavItemInfo(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector = icon)

val NavItem.info: NavItemInfo
    get() = when (this) {
        NavItem.CALENDAR -> NavItemInfo("calendar", "Календарь", AppIcons.CalendarMonth, AppIcons.CalendarMonthFilled)
        NavItem.FINANCE -> NavItemInfo("finance", "Финансы", AppIcons.Wallet, AppIcons.WalletFilled)
        NavItem.STATS -> NavItemInfo("finance/stats", "Статистика", AppIcons.BarChart)
        NavItem.HISTORY -> NavItemInfo("finance/history", "История", AppIcons.Receipt)
        NavItem.UNCONFIRMED -> NavItemInfo("unconfirmed", "Отметить", AppIcons.Pending)
        NavItem.ALARM -> NavItemInfo("alarm", "Будильник", AppIcons.Alarm)
        NavItem.SEARCH -> NavItemInfo("search", "Поиск", AppIcons.Search)
        NavItem.SCHEDULE -> NavItemInfo("settings/templates", "График", AppIcons.EventRepeat)
        NavItem.SETTINGS -> NavItemInfo("settings", "Настройки", AppIcons.Settings, AppIcons.SettingsFilled)
    }

/** Longer name used in the settings. */
val NavItem.title: String
    get() = when (this) {
        NavItem.UNCONFIRMED -> "Отметка часов"
        NavItem.HISTORY -> "История операций"
        NavItem.SCHEDULE -> "График работы"
        else -> info.label
    }

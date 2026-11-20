package io.github.ceniorpomidor.workcalendar

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import io.github.ceniorpomidor.workcalendar.notifications.Notifications
import io.github.ceniorpomidor.workcalendar.ui.lock.FinanceLockState
import io.github.ceniorpomidor.workcalendar.ui.navigation.WorkCalendarRoot

/**
 * Single activity. Extends [FragmentActivity] because the biometric prompt of the finance
 * lock requires it. Deep links from notifications and the widget arrive as a route extra.
 */
class MainActivity : FragmentActivity() {
    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) pendingRoute.value = intent?.getStringExtra(Notifications.EXTRA_ROUTE)
        setContent {
            WorkCalendarRoot(
                container = appContainer,
                pendingRoute = pendingRoute.value,
                onRouteHandled = { pendingRoute.value = null },
            )
        }
    }

    override fun onStop() {
        super.onStop()
        // Finance section is locked again when the app leaves the screen.
        if (!isChangingConfigurations) FinanceLockState.lock()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(Notifications.EXTRA_ROUTE)?.let { pendingRoute.value = it }
    }
}

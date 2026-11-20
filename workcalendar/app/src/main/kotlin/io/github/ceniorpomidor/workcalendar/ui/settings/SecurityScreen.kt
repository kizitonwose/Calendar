package io.github.ceniorpomidor.workcalendar.ui.settings

import androidx.compose.runtime.Composable
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.ui.lock.SecurityScreenContent

@Composable
fun SecurityScreen(container: AppContainer, settings: AppSettings, onBack: () -> Unit) {
    SecurityScreenContent(container, settings, onBack)
}

package io.github.ceniorpomidor.workcalendar.ui.lock

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.util.PinHasher
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Unlock state of the finance section; reset when the app goes to the background. */
object FinanceLockState {
    val unlocked = mutableStateOf(false)

    fun lock() {
        unlocked.value = false
    }
}

private fun Context.findActivity(): FragmentActivity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is FragmentActivity) return c
        c = c.baseContext
    }
    return null
}

fun biometricAvailable(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

private fun showBiometricPrompt(context: Context, onSuccess: () -> Unit) {
    val activity = context.findActivity() ?: return
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Доступ к финансам")
        .setSubtitle("Подтвердите, что это вы")
        .setNegativeButtonText("Ввести PIN")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .build()
    prompt.authenticate(info)
}

/** Shows [content] only after the PIN or biometric check when the lock is enabled. */
@Composable
fun FinanceLockGate(container: AppContainer, settings: AppSettings, content: @Composable () -> Unit) {
    val security = settings.security
    val unlocked by FinanceLockState.unlocked
    if (!security.financeLock || security.pinHash == null || security.pinSalt == null || unlocked) {
        content()
        return
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbar.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val canUseBiometric = security.biometric && biometricAvailable(context)
    LaunchedEffect(Unit) {
        if (canUseBiometric) showBiometricPrompt(context) { FinanceLockState.unlocked.value = true }
    }
    SubScreen(title = "Финансы", onBack = null) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Icon(AppIcons.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Раздел защищён", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    pin = it.filter { c -> c.isDigit() }.take(8)
                    error = null
                },
                label = { Text("PIN-код") },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    val entered = pin
                    scope.launchSafely(snackbar) {
                        val ok = withContext(Dispatchers.Default) { PinHasher.verify(entered, security.pinSalt!!, security.pinHash!!) }
                        if (ok) FinanceLockState.unlocked.value = true else error = "Неверный PIN-код"
                    }
                },
                enabled = pin.length >= 4,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Открыть") }
            if (canUseBiometric) {
                OutlinedButton(onClick = { showBiometricPrompt(context) { FinanceLockState.unlocked.value = true } }, modifier = Modifier.fillMaxWidth()) {
                    Icon(AppIcons.Fingerprint, contentDescription = null)
                    Text("  По отпечатку")
                }
            }
        }
    }
}

@Composable
fun SecurityScreenContent(container: AppContainer, settings: AppSettings, onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var setPin by remember { mutableStateOf(false) }
    var disablePin by remember { mutableStateOf(false) }
    val security = settings.security
    SubScreen(title = "Защита финансов", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Блокировка раздела «Финансы»", icon = AppIcons.Lock) {
                SwitchRow(
                    "Запрашивать PIN-код",
                    "При каждом открытии приложения раздел «Финансы» потребует PIN",
                    security.financeLock,
                ) { enable -> if (enable) setPin = true else disablePin = true }
                if (security.financeLock) {
                    val available = biometricAvailable(context)
                    SwitchRow(
                        "Разблокировка отпечатком",
                        if (available) "Вместо ввода PIN" else "Биометрия на устройстве не настроена",
                        security.biometric && available,
                        enabled = available,
                    ) { v -> scope.launchSafely(snackbar) { container.settings.update { it.copy(security = it.security.copy(biometric = v)) } } }
                    TextButton(onClick = { setPin = true }) { Text("Сменить PIN-код") }
                }
            }
            Text(
                "PIN-код хранится только в виде хеша. Если вы его забудете, отключить защиту можно, удалив данные приложения или восстановив резервную копию.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (setPin) {
        PinSetupDialog(onDismiss = { setPin = false }) { pin ->
            scope.launchSafely(snackbar, success = "PIN-код установлен") {
                val salt = PinHasher.newSalt()
                val hash = withContext(Dispatchers.Default) { PinHasher.hash(pin, salt) }
                container.settings.update { it.copy(security = it.security.copy(financeLock = true, pinHash = hash, pinSalt = salt)) }
                FinanceLockState.unlocked.value = true
            }
            setPin = false
        }
    }
    if (disablePin) {
        var pin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { disablePin = false },
            title = { Text("Отключить защиту") },
            text = {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text("Текущий PIN-код") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val salt = security.pinSalt
                    val hash = security.pinHash
                    scope.launchSafely(snackbar) {
                        val ok = salt == null || hash == null || withContext(Dispatchers.Default) { PinHasher.verify(pin, salt, hash) }
                        if (ok) {
                            container.settings.update { it.copy(security = it.security.copy(financeLock = false, pinHash = null, pinSalt = null, biometric = false)) }
                            disablePin = false
                        } else {
                            error = "Неверный PIN-код"
                        }
                    }
                }) { Text("Отключить") }
            },
            dismissButton = { TextButton(onClick = { disablePin = false }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun PinSetupDialog(onDismiss: () -> Unit, onSet: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    val valid = PinHasher.isValidPin(pin) && pin == repeat
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый PIN-код") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text("PIN (4–8 цифр)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = repeat,
                    onValueChange = { repeat = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text("Повторите PIN") },
                    isError = repeat.isNotEmpty() && repeat != pin,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSet(pin) }, enabled = valid) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

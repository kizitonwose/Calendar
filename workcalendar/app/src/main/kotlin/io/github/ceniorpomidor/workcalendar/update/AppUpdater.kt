package io.github.ceniorpomidor.workcalendar.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import io.github.ceniorpomidor.workcalendar.BuildConfig
import io.github.ceniorpomidor.workcalendar.data.repo.SettingsRepository
import io.github.ceniorpomidor.workcalendar.domain.update.AppVersion
import io.github.ceniorpomidor.workcalendar.domain.update.AvailableUpdate
import io.github.ceniorpomidor.workcalendar.domain.update.UpdateCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/**
 * Updates of the app from its GitHub releases: finds a newer version, downloads its APK and hands
 * it to the system installer. The APK is signed with the same key, so it installs over the
 * current version and keeps the data.
 */
class AppUpdater(private val context: Context, private val settings: SettingsRepository) {
    private val prefs by lazy { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    val currentVersion: AppVersion = AppVersion.parse(BuildConfig.VERSION_NAME) ?: AppVersion(listOf(0))

    /** The newest release if it is newer than the installed version, otherwise null. */
    suspend fun check(): AvailableUpdate? = withContext(Dispatchers.IO) {
        val text = request(RELEASES_URL, accept = "application/vnd.github+json").use { connection ->
            connection.inputStream.bufferedReader().readText()
        }
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
        UpdateCheck.newest(UpdateCheck.parseReleases(text), currentVersion)
    }

    /** Downloads the APK of [update]; [onProgress] gets 0..1. Returns the checked file. */
    suspend fun download(update: AvailableUpdate, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "WorkCalendar-${update.version}.apk")
        request(update.apk.url).use { connection ->
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: update.apk.size
            connection.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        done += read
                        if (total > 0) onProgress((done.toFloat() / total).coerceAtMost(1f))
                    }
                }
            }
        }
        if (update.apk.size > 0 && file.length() != update.apk.size) {
            file.delete()
            throw IOException("Файл скачался не полностью, попробуйте ещё раз")
        }
        // Only an APK of this app with a newer version is offered to the installer.
        val info = context.packageManager.getPackageArchiveInfo(file.path, 0)
        if (info == null || info.packageName != context.packageName.removeSuffix(".debug")) {
            file.delete()
            throw IOException("Скачанный файл не является обновлением Рабочего календаря")
        }
        file
    }

    /** Whether the system lets this app start the installer (Android 8+: "install unknown apps"). */
    fun canInstall(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    /** Opens the system setting that allows this app to install updates. */
    fun openInstallPermission(activity: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        activity.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
    }

    /** Opens the system installer for the downloaded [file]. */
    fun install(activity: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, APK_TYPE)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            throw IOException("Не найден установщик приложений")
        }
    }

    /**
     * Called when the app is opened: when allowed in the settings, looks for a newer version
     * (at most once an hour) and returns it unless the user put it off during the last day.
     */
    suspend fun checkOnOpen(): AvailableUpdate? {
        if (!settings.get().autoUpdateCheck) return null
        if (System.currentTimeMillis() - prefs.getLong(KEY_LAST_CHECK, 0L) < OPEN_CHECK_INTERVAL_MS) return null
        val update = try {
            check()
        } catch (e: Exception) {
            android.util.Log.w("WorkCalendar", "Update check failed: ${e.message}")
            return null
        } ?: return null
        val putOff = prefs.getString(KEY_PUT_OFF_VERSION, null) == update.version.toString() &&
            System.currentTimeMillis() - prefs.getLong(KEY_PUT_OFF_AT, 0L) < PUT_OFF_MS
        return if (putOff) null else update
    }

    /** The user chose "Позже" for [version]: do not offer it again for a day. */
    fun putOff(version: String) {
        prefs.edit().putString(KEY_PUT_OFF_VERSION, version).putLong(KEY_PUT_OFF_AT, System.currentTimeMillis()).apply()
    }

    /** Opens [url] and checks the answer; headers are set before the connection is made. */
    private fun request(url: String, accept: String? = null): HttpURLConnection {
        var connection = URL(url).openConnection() as HttpURLConnection
        // GitHub sends release files from another host: follow up to a few redirects.
        repeat(5) {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "WorkCalendar/${BuildConfig.VERSION_NAME}")
            if (accept != null) connection.setRequestProperty("Accept", accept)
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location") ?: throw IOException("Ошибка загрузки: $code")
                connection.disconnect()
                connection = URL(connection.url, location).openConnection() as HttpURLConnection
                return@repeat
            }
            if (code != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                throw IOException(if (code == 403) "GitHub временно ограничил запросы, попробуйте позже" else "Ошибка сервера: $code")
            }
            return connection
        }
        throw IOException("Слишком много перенаправлений")
    }

    private inline fun <T> HttpURLConnection.use(block: (HttpURLConnection) -> T): T = try {
        block(this)
    } finally {
        disconnect()
    }

    companion object {
        const val RELEASES_PAGE = "https://github.com/cenior-pomidor/Calendar/releases"
        private const val RELEASES_URL = "https://api.github.com/repos/cenior-pomidor/Calendar/releases?per_page=20"
        private const val APK_TYPE = "application/vnd.android.package-archive"
        private const val PREFS = "updates"
        private const val KEY_LAST_CHECK = "lastCheck"
        private const val KEY_PUT_OFF_VERSION = "putOffVersion"
        private const val KEY_PUT_OFF_AT = "putOffAt"
        private const val TIMEOUT_MS = 20_000
        private const val OPEN_CHECK_INTERVAL_MS = 60L * 60 * 1000
        private const val PUT_OFF_MS = 24L * 60 * 60 * 1000
    }
}

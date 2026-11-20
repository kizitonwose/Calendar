package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.update.AppVersion
import io.github.ceniorpomidor.workcalendar.domain.update.UpdateCheck
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdateCheckTest {
    private val releases = UpdateCheck.parseReleases(
        """
        [
          {"tag_name": "workcalendar-v1.10.0", "draft": true, "assets": [{"name": "WorkCalendar-1.10.0.apk", "browser_download_url": "https://x/10", "size": 5}]},
          {"tag_name": "workcalendar-v1.4.0", "html_url": "https://x/r/1.4.0", "unknown": 1,
           "body": "## Рабочий календарь\n\n### Что нового в 1.4.0\n- **Обновление** из приложения.\n  Без браузера.\n- Ещё `одно`.\n\n### Установка\n1. Скачайте",
           "assets": [{"name": "WorkCalendar-1.4.0.apk", "browser_download_url": "https://x/14", "size": 3771926}]},
          {"tag_name": "workcalendar-v1.3.0", "assets": [{"name": "WorkCalendar-1.3.0.apk", "browser_download_url": "https://x/13"}]},
          {"tag_name": "library-2.9.0", "assets": [{"name": "lib.apk", "browser_download_url": "https://x/lib"}]},
          {"tag_name": "workcalendar-v1.5.0", "assets": []}
        ]
        """.trimIndent(),
    )

    @Test
    fun `versions compare by numbers`() {
        assertTrue(AppVersion.parse("1.10.0")!! > AppVersion.parse("1.9.2")!!)
        assertEquals(AppVersion.parse("1.3"), AppVersion.parse("1.3.0-debug")?.let { AppVersion(it.parts.take(2)) })
        assertEquals(0, AppVersion.parse("1.3")!!.compareTo(AppVersion.parse("1.3.0")!!))
        assertNull(AppVersion.parse("next"))
    }

    @Test
    fun `newest published release with an apk`() {
        val update = UpdateCheck.newest(releases, AppVersion.parse("1.3.0-debug")!!)!!
        assertEquals("1.4.0", update.version.toString())
        assertEquals("https://x/14", update.apk.url)
        assertEquals(3771926L, update.apk.size)
        assertEquals(listOf("• Обновление из приложения. Без браузера.", "• Ещё одно."), update.notes)
        assertNull(UpdateCheck.newest(releases, AppVersion.parse("1.4.0")!!))
    }
}

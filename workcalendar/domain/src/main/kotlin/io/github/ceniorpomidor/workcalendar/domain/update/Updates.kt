package io.github.ceniorpomidor.workcalendar.domain.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Version like `1.3.0`; a suffix such as `-debug` is ignored. */
data class AppVersion(val parts: List<Int>) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int {
        for (i in 0 until maxOf(parts.size, other.parts.size)) {
            val diff = parts.getOrElse(i) { 0 } - other.parts.getOrElse(i) { 0 }
            if (diff != 0) return diff
        }
        return 0
    }

    override fun toString(): String = parts.joinToString(".")

    companion object {
        fun parse(text: String): AppVersion? {
            val core = text.trim().removePrefix("v").substringBefore('-').substringBefore('+')
            val parts = core.split('.').map { it.toIntOrNull() ?: return null }
            return if (parts.isEmpty()) null else AppVersion(parts)
        }
    }
}

/** A release of the GitHub API (only the fields the app needs). */
@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tag: String,
    val name: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("published_at") val publishedAt: String? = null,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val url: String,
    val size: Long = 0,
)

/** A newer version of the app that can be downloaded. */
data class AvailableUpdate(val version: AppVersion, val release: GitHubRelease, val apk: GitHubAsset) {
    /** "What's new" of the release notes as plain text lines. */
    val notes: List<String> get() = UpdateCheck.notes(release.body.orEmpty())
}

/** Finds updates among the releases of the repository. */
object UpdateCheck {
    /** Releases of the app are tagged `workcalendar-v1.3.0`; the repository also holds other projects. */
    const val TAG_PREFIX = "workcalendar-v"

    private val json = Json { ignoreUnknownKeys = true }

    fun parseReleases(text: String): List<GitHubRelease> = json.decodeFromString(ListSerializer(GitHubRelease.serializer()), text)

    /** The newest published release with an APK that is newer than [current]; null when there is none. */
    fun newest(releases: List<GitHubRelease>, current: AppVersion): AvailableUpdate? = releases
        .asSequence()
        .filter { !it.draft && !it.prerelease && it.tag.startsWith(TAG_PREFIX) }
        .mapNotNull { release ->
            val version = AppVersion.parse(release.tag.removePrefix(TAG_PREFIX)) ?: return@mapNotNull null
            val apk = release.assets.firstOrNull { it.name.endsWith(".apk") } ?: return@mapNotNull null
            AvailableUpdate(version, release, apk)
        }
        .filter { it.version > current }
        .maxByOrNull { it.version }

    /** Lines of the first "Что нового" section of the release notes, without Markdown marks. */
    fun notes(body: String): List<String> {
        val lines = body.lines()
        val start = lines.indexOfFirst { it.trimStart().startsWith("#") && it.contains("Что нового") }
        if (start < 0) return emptyList()
        val result = ArrayList<String>()
        for (line in lines.drop(start + 1)) {
            if (line.trimStart().startsWith("#")) break
            val text = line.trim().replace("**", "").replace("`", "")
            when {
                text.isEmpty() -> Unit
                text.startsWith("- ") || text.startsWith("* ") -> result += "• " + text.drop(2)
                result.isNotEmpty() -> result[result.lastIndex] = result.last() + " " + text
                else -> result += text
            }
        }
        return result
    }
}

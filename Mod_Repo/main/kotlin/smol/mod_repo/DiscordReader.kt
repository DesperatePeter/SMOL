/*
 * This file is distributed under the GPLv3. An informal description follows:
 * - Anyone can copy, modify and distribute this software as long as the other points are followed.
 * - You must include the license and copyright notice with each and every distribution.
 * - You may this software for commercial purposes.
 * - If you modify it, you must indicate changes made to the code.
 * - Any modifications of this code base MUST be distributed with the same license, GPLv3.
 * - This software is provided without warranty.
 * - The software author or license can not be held liable for any damages inflicted by the software.
 * The full license is available from <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package smol.mod_repo

import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import smol.timber.ktx.Timber
import smol.utilities.Jsanity
import smol.utilities.asList
import smol.utilities.prefer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * This file is distributed under the GPLv3. An informal description follows:
 * - Anyone can copy, modify and distribute this software as long as the other points are followed.
 * - You must include the license and copyright notice with each and every distribution.
 * - You may this software for commercial purposes.
 * - If you modify it, you must indicate changes made to the code.
 * - Any modifications of this code base MUST be distributed with the same license, GPLv3.
 * - This software is provided without warranty.
 * - The software author or license can not be held liable for any damages inflicted by the software.
 * The full license is available from <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */
internal object DiscordReader {
    private val baseUrl = "https://discord.com/api"
    val serverId = "187635036525166592"
    val modUpdatesChannelId = "825068217361760306"
    private val urlFinderRegex = Regex(
        """(http|ftp|https):\/\/([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:\/~+#-]*[\w@?^=%&\/~+#-])"""
    )

    internal suspend fun readAllMessages(config: Main.Companion.Config, gsonBuilder: GsonBuilder): List<ScrapedMod>? {
        val authToken = config.discordAuthToken ?: run {
            Timber.w { "No auth token found in ${Main.configFilePath}." }
            return@readAllMessages null
        }
        val dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withLocale(Locale.US)
            .withZone(ZoneId.of("UTC"))
        val jsanity = Jsanity(gsonBuilder
            .registerTypeAdapter<Date> {
                deserialize { Date.from(Instant.from(dateTimeFormatter.parse(it.json.asString))) }
            }
            .create())
        val httpClient =
            HttpClient(CIO) {
                install(Logging)
                install(HttpTimeout)
                install(JsonFeature) {
                    serializer = GsonSerializer()
                }
                this.followRedirects = true
            }

        println("Scraping Discord's #mod_updates...")
        return getMessages(
            httpClient = httpClient,
            authToken = authToken,
            jsanity = jsanity,
            limit = if (config.lessScraping) 50 else Int.MAX_VALUE
        )
            .map { message ->
                val messageLines = message.content?.lines()
                val forumUrl = messageLines
                    ?.mapNotNull { urlFinderRegex.find(it)?.value }
                    ?.firstOrNull { it.contains("fractalsoftworks") }
                    ?.let { kotlin.runCatching { Url(it) }.onFailure { Timber.w(it) }.getOrNull() }

                val downloadUrl = messageLines
                    ?.mapNotNull { urlFinderRegex.find(it)?.value }
                    ?.prefer { it.contains("/releases/download") }
                    ?.prefer { it.contains("/releases") }
                    ?.prefer { it.contains("dropbox") }
                    ?.prefer { it.contains("drive.google") }
                    ?.prefer { it.contains("patreon") }
                    ?.prefer { it.contains("bitbucket") }
                    ?.prefer { it.contains("github") }
                    ?.prefer { it.contains("fractalsoftworks") }
                    ?.firstOrNull()
                    ?.let { kotlin.runCatching { Url(it) }.onFailure { Timber.w(it) }.getOrNull() }
                ScrapedMod(
                    name = messageLines?.firstOrNull()?.removeMarkdownFromName() ?: "(Discord Mod)",
                    summary = messageLines?.drop(1)?.take(2)?.joinToString(separator = "\n"),
                    description = messageLines?.drop(1)?.joinToString(separator = "\n"),
                    modVersion = null,
                    gameVersionReq = "",
                    authors = message.author?.username ?: "",
                    authorsList = message.author?.username.asList(),
                    forumPostLink = forumUrl,
                    link = downloadUrl,
                    urls = listOfNotNull(
                        forumUrl?.let { ModUrlType.Forum to forumUrl },
                        ModUrlType.Discord to Url("https://discord.com/channels/$serverId/$modUpdatesChannelId/${message.id}"),
                    ).toMap(),
                    source = ModSource.Discord,
                    sources = listOf(ModSource.Discord),
                    categories = emptyList(),
                    images = message.attachments
                        ?.filter { it.content_type?.startsWith("image/") ?: false }
                        ?.associate {
                            it.id to Image(
                                id = it.id,
                                filename = it.filename,
                                description = it.description,
                                content_type = it.content_type,
                                size = it.size,
                                url = it.url,
                                proxy_url = it.proxy_url
                            )
                        },
                    dateTimeCreated = message.timestamp,
                    dateTimeEdited = message.edited_timestamp
                )
            }
            .map { cleanUpMod(it) }
            .filter { it.name.isNotBlank() } // Remove any posts that don't contain text.
    }

    private val markdownStyleSymbols = listOf("_", "*", "~", "`")
    private val surroundingMarkdownRegexes = markdownStyleSymbols.map { symbol ->
        Regex("""(.*)[$symbol](.*)[$symbol](.*)""")
    }

    private fun String.removeMarkdownFromName(): String {
        var str = this

        while (true) {
            val replaced: String = surroundingMarkdownRegexes.fold(str) { prev, regex ->
                regex.matchEntire(prev)?.groupValues?.drop(1)?.joinToString(separator = "") ?: prev
            }

            if (str == replaced) return str
            str = replaced
        }
    }

    private suspend fun getMessages(
        httpClient: HttpClient,
        authToken: String,
        jsanity: Jsanity,
        limit: Int = Int.MAX_VALUE
    ): List<Message> {
        val messages = mutableListOf<Message>()
        var runs = 0 // Limit number of runs in case other breaks don't trigger.
        val perRequestLimit = 100

        while (messages.count() < limit && runs < 25) {
            val newMessages = httpClient.request<List<Message>>("$baseUrl/channels/$modUpdatesChannelId/messages") {
                parameter("limit", perRequestLimit.toString())
                header("Authorization", "Bot $authToken")
                accept(ContentType.Application.Json)

                if (messages.isNotEmpty()) {
                    // Grab results from before the oldest message we've gotten so far.
                    parameter("before", messages.last().id)
                }
            }
//                .run { jsanity.fromJson<List<Message>>(this, shouldStripComments = false) }
                .sortedByDescending { it.timestamp }

            Timber.i { "Found ${newMessages.count()} posts in Discord #mod_updates." }
            messages += newMessages
            Timber.v { newMessages.joinToString(separator = "\n") }
            runs++

            if (newMessages.isEmpty() || newMessages.size < perRequestLimit) {
                Timber.i { "Found all ${messages.count()} posts in Discord #mod_updates, finishing scrape." }
                break
            } else {
                delay(200)
            }
        }

        return messages
    }

    private val discordUnrecognizedEmojiRegex = Regex("""(<:.+?:.+?>)""")

    private fun cleanUpMod(mod: ScrapedMod): ScrapedMod =
        mod.copy(
            name = mod.name.replace(discordUnrecognizedEmojiRegex, "").trim(),
            description = mod.description?.replace(discordUnrecognizedEmojiRegex, "")?.trim(),
        )

    private data class Message(
        val id: String,
        val author: User?,
        val content: String?,
        val timestamp: Date?,
        val edited_timestamp: Date?,
        val attachments: List<Attachment>?,
        val embeds: List<Embed>?,
    )

    private data class User(
        val id: String,
        val username: String?,
        val discriminator: String?,
    )

    private data class Attachment(
        val id: String,
        val filename: String?,
        val description: String?,
        val content_type: String?,
        val size: Int?,
        val url: String?,
        val proxy_url: String?
    )

    private data class Embed(
        val title: String?,
        val description: String?,
        val url: String?
    )
}
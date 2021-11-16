package mod_repo

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.URI
import java.nio.file.Path


fun main(args: Array<String>) {
    scrapeModIndexLinks()
        .onEach { println(it) }
    scrapeModdingForumLinks()
        .onEach { println(it) }
}

internal fun scrapeModdingForumLinks(): List<ScrapedMod> {
    println("Scraping Modding Forum...")
    val baseUri = "https://fractalsoftworks.com/forum/index.php"

    return (0 until 80 step 20)
        .flatMap { page ->
            val doc: Document = Jsoup.connect("$baseUri?board=3.$page").get()
            val posts: Elements = doc.select("#messageindex tr")
            val versionRegex = Regex("""\[.*?\]""")

            posts
                .map { postElement ->
                    val titleLinkElement = postElement.select("td.subject a")
                    val authorLinkElement = postElement.select("td.starter a")

                    ScrapedMod(
                        name = titleLinkElement.text().replace(versionRegex, "").trim(),
                        gameVersionReq = versionRegex.find(titleLinkElement.text())?.groupValues?.firstOrNull()?.trim()
                            ?: "",
                        authors = authorLinkElement.text(),
                        forumPostLink = titleLinkElement.attr("href").ifBlank { null }?.let { URI.create(it) },
                        category = null
                    )
                }
                .filter { it.gameVersionReq.isNotEmpty() }
                .filter { !it.name.contains("MOVED", ignoreCase = true) }
        }
}

internal fun scrapeModIndexLinks(): List<ScrapedMod> {
    println("Scraping Mod Index...")
    val doc: Document = Jsoup.parse(
        Path.of("C:/Users/whitm/SMOL/web/Starsector_Index/fractalsoftworks.com/forum/indexebd2.html").toFile(), null
    )
    val categories: Elements = doc.select("ul.bbc_list")

    return categories
        .flatMap { categoryElement ->
            val category =
                categoryElement.previousElementSibling()?.previousElementSibling()?.previousElementSibling()?.text()
                    ?.trimEnd(':') ?: ""

            categoryElement.select("li").map { modElement ->
                val link = modElement.select("a.bbc_link")

                ScrapedMod(
                    name = link.text(),
                    gameVersionReq = modElement.select("strong span").text(),
                    authors = modElement.select("em strong").text(),
                    forumPostLink = link.attr("href").ifBlank { null }?.let { URI.create(it) },
                    category = category
                )
            }
        }
}

data class ScrapedMod(
    val name: String,
    val gameVersionReq: String,
    val authors: String,
    val forumPostLink: URI?,
    val category: String?
)
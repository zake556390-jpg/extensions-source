package eu.kanade.tachiyomi.extension.ar.procomic

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Procomic : ParsedHttpSource() {
    override val name = "Procomic"
    override val baseUrl = "https://procomic.net"
    override val lang = "ar"
    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        .add("Referer", "$baseUrl/")

    // الخريطة الجديدة لروابط الموقع الصحيحة (MangaThemesia)
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/manga/?page=$page&order=popular", headers)
    override fun popularMangaSelector() = ".bsx"
    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        title = element.select("a").attr("title").ifEmpty { element.select(".tt").text() }
        thumbnail_url = element.select("img").attr("abs:src")
    }
    override fun popularMangaNextPageSelector() = ".pagination .next, .hpage .r"

    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/manga/?page=$page&order=update", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/page/$page/?s=$query", headers)
    }
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.select("h1.entry-title").text()
        description = document.select(".entry-content, .summary").text()
        thumbnail_url = document.select(".thumb img").attr("abs:src")
    }

    override fun chapterListSelector() = "#chapterlist li"
    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        name = element.select(".chapternum").text()
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select("#readerarea img").mapIndexed { i, img ->
            Page(i, "", img.attr("abs:src"))
        }
    }

    override fun imageUrlParse(document: Document) = throw UnsupportedOperationException("Not used")
}


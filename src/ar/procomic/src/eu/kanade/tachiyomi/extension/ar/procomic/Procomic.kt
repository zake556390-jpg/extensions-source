package eu.kanade.tachiyomi.extension.ar.procomic

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Procomic : ParsedHttpSource() {
    override val name = "Procomic"
    override val baseUrl = "https://procomic.net"
    override val lang = "ar"
    override val supportsLatest = true

    // الشعبيات (القائمة الرئيسية)
    override fun popularMangaRequest(page: Int) = GET("$baseUrl/popular?page=$page", headers)
    override fun popularMangaSelector() = "div.manga-card"
    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        title = element.select("h3.title").text()
        thumbnail_url = element.select("img").attr("abs:src")
    }
    override fun popularMangaNextPageSelector() = "a.next-page"

    // أحدث الإصدارات
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/latest?page=$page", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // البحث
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList) = GET("$baseUrl/search?q=$query&page=$page", headers)
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // تفاصيل المانجا
    override fun mangaDetailsParse(document: Document) = SManga.create().apply {
        title = document.select("h1.manga-title").text()
        description = document.select("div.summary-text").text()
        thumbnail_url = document.select("div.manga-cover img").attr("abs:src")
    }

    // قائمة الفصول
    override fun chapterListSelector() = "li.chapter-item"
    override fun chapterFromElement(element: Element) = SChapter.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        name = element.select("span.chapter-name").text()
    }

    // صور الفصل
    override fun pageListParse(document: Document): List<Page> {
        return document.select("div.reading-content img").mapIndexed { i, img ->
            Page(i, "", img.attr("abs:src"))
        }
    }

    override fun imageUrlParse(document: Document) = throw UnsupportedOperationException("Not used")
}

package eu.kanade.tachiyomi.extension.ar.procomic

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Procomic : ParsedHttpSource() {
    override val name = "Procomic"
    override val baseUrl = "https://procomic.net"
    override val lang = "ar"
    override val supportsLatest = true

    // إعدادات العرض الشعبي
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/popular?page=$page", headers)
    override fun popularMangaSelector() = "div.manga-card, div.post-item" // جلب أكثر من احتمال لشكل التصميم
    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        title = element.select("h3, .title").text()
        thumbnail_url = element.select("img").attr("abs:src")
    }
    override fun popularMangaNextPageSelector() = "a.next-page, .pagination a.next"

    // أحدث الفصول
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/latest?page=$page", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // البحث عن Keyboard Immortal وغيرها
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/search?q=$query&page=$page", headers)
    }
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // تفاصيل المانجا
    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.select("h1").text()
        description = document.select(".summary-text, .description").text()
        thumbnail_url = document.select(".manga-cover img, .post-thumbnail img").attr("abs:src")
    }

    // الفصول
    override fun chapterListSelector() = "li.chapter-item, .wp-manga-chapter"
    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        name = element.select(".chapter-name, a").text()
    }

    // الصفحات
    override fun pageListParse(document: Document): List<Page> {
        return document.select(".reading-content img, .page-break img").mapIndexed { i, img ->
            Page(i, "", img.attr("abs:src"))
        }
    }

    override fun imageUrlParse(document: Document) = throw UnsupportedOperationException("Not used")
}


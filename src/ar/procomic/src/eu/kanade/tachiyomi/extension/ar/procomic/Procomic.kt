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

    // كاسر الحماية
    override val client: OkHttpClient = network.cloudflareClient

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        .add("Referer", "$baseUrl/")

    // توجيه الإضافة للصفحة الرئيسية مباشرة لتجنب خطأ 404
    override fun popularMangaRequest(page: Int): Request {
        return if (page == 1) {
            GET(baseUrl, headers)
        } else {
            GET("$baseUrl/series?page=$page", headers)
        }
    }
    
    // محاولة اصطياد أكبر قدر من الاحتمالات لتصميم الموقع
    override fun popularMangaSelector() = ".series-box, .manga-card, .post-item, .bsx, .item, article"
    
    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        setUrlWithoutDomain(element.select("a").first()?.attr("href") ?: "")
        title = element.select("h3, .title, .tt, h2, .series-title").text()
        thumbnail_url = element.select("img").attr("abs:src")
    }
    override fun popularMangaNextPageSelector() = ".pagination a.next, .next-page, .nav-next"

    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/latest", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/search?q=$query", headers)
    }
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.select("h1, .series-title").text()
        description = document.select(".summary, .description, .synopsis").text()
        thumbnail_url = document.select(".thumb img, .series-cover img").attr("abs:src")
    }

    override fun chapterListSelector() = "li.chapter-item, .wp-manga-chapter, .chbox, .chapter-list li"
    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        name = element.select(".chapter-name, .chapternum, a").text()
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select(".reading-content img, #readerarea img, .page-break img").mapIndexed { i, img ->
            Page(i, "", img.attr("abs:src"))
        }
    }

    override fun imageUrlParse(document: Document) = throw UnsupportedOperationException("Not used")
}

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

    // ================== 1. Popular Manga (الأكثر قراءة) ==================
    override fun popularMangaRequest(page: Int) = GET("$baseUrl/popular?page=$page", headers)
    
    // ستحتاج لتعديل الـ CSS Selector بناءً على كود الـ HTML الفعلي للموقع
    override fun popularMangaSelector() = "div.manga-card" 
    
    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        manga.setUrlWithoutDomain(element.select("a").attr("href"))
        manga.title = element.select("h3.title").text()
        manga.thumbnail_url = element.select("img").attr("src")
        return manga
    }
    override fun popularMangaNextPageSelector() = "a.next-page"

    // ================== 2. Latest Updates (أحدث الإصدارات) ==================
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/latest?page=$page", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // ================== 3. Manga Details (تفاصيل العمل) ==================
    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        manga.title = document.select("h1.manga-title").text()
        manga.author = document.select("span.author-name").text()
        manga.description = document.select("div.summary-text").text()
        manga.thumbnail_url = document.select("div.manga-cover img").attr("src")
        // يمكنك إضافة الـ Status (مستمر، مكتمل) هنا أيضاً
        return manga
    }

    // ================== 4. Chapter List (قائمة الفصول) ==================
    override fun chapterListSelector() = "li.chapter-item"
    
    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.setUrlWithoutDomain(element.select("a").attr("href"))
        chapter.name = element.select("span.chapter-name").text()
        // إذا كان الموقع يعرض تاريخ الرفع، يمكنك إضافته عبر chapter.date_upload
        return chapter
    }

    // ================== 5. Pages List (صور الفصل - الأهم للقراءة) ==================
    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        
        // استخراج جميع الصور الموجودة داخل حاوية قراءة الفصل
        document.select("div.reading-content img").forEachIndexed { i, img ->
            val url = img.attr("abs:src") // abs:src تجلب الرابط كاملاً مع الـ Domain
            pages.add(Page(i, "", url))
        }
        return pages
    }

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")
    
    // ================== 6. Search (البحث) ==================
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList) = GET("$baseUrl/search?q=$query&page=$page", headers)
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()
}


package com.dizifilmizle


import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*


class dizifilmizle : MainAPI() {
    override var mainUrl = "https://dizifilmizle.net"
    override var name = "dizifilmizle"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasDownloadSupport = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val home = document.select("div.movies-list").mapNotNull {
            it.toSearchResult()
        }
        return HomePageResponse(home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.ml-title a")?.text()?.trim() ?: return null
        val href = this.selectFirst("div.ml-title a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("data-src")
        val quality = this.selectFirst("span.quality")?.text()

        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
            this.quality = quality
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.post(
            "$mainUrl/wp-admin/admin-ajax.php", data = mapOf(
                "action" to "ajaxsearchpro_search",
                "aspp" to query,
                "asid" to "1"
            )
        ).document

        return document.select("div.results div.item").mapNotNull {
            val title = it.selectFirst("h3 a")?.text() ?: return@mapNotNull null
            val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("src")
            val type = if (it.selectFirst("span.type")?.text().equals("Film")) TvType.Movie else TvType.TvSeries

            newMovieSearchResponse(title, href, type) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document.selectFirst("div.content-head h1")?.text() ?: return null
        val poster = document.selectFirst("div.content-head img")?.attr("src")
        val description = document.selectFirst("div.content-desc p")?.text()
        val type = if (document.selectFirst("div.content-type span:contains(Tür:)")?.text()
                ?.contains("Dizi") == true
        ) TvType.TvSeries else TvType.Movie

        val episodes = document.select("div.episodes-list a").map {
            Episode(
                it.attr("href"),
                it.selectFirst("span.ep-num")?.text() ?: "Bölüm"
            )
        }

        val recommendations = document.select("div.movies-list div.ml-item").mapNotNull {
            it.toSearchResult()
        }

        return when (type) {
            TvType.TvSeries -> {
                newTvSeriesLoadResponse(title, url, TvType.TvSeries) {
                    this.posterUrl = poster
                    this.plot = description
                    this.recommendations = recommendations
                    this.episodes = episodes
                }
            }
            else -> {
                newMovieLoadResponse(title, url, TvType.Movie) {
                    this.posterUrl = poster
                    this.plot = description
                    this.recommendations = recommendations
                }
            }
        }
    }
}
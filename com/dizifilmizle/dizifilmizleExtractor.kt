package com.dizifilmizle

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.TauVideoExtractor

class dizifilmizleExtractor : TauVideoExtractor() {
    override var mainUrl = "https://dizifilmizle.net"
    override val name = "dizifilmizle"

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val document = app.get(url).document
        val iframeSrc = document.selectFirst("iframe")?.attr("src") ?: return null
        return super.getUrl(iframeSrc, url)
    }
}
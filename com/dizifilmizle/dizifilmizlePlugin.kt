package com.dizifilmizle

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class dizifilmizlePlugin: Plugin() {
    override fun load(context: Context) {
        // Main API'yi kaydet
        registerMainAPI(dizifilmizle())
        // Video extractor'ı kaydet
        registerExtractorAPI(dizifilmizleExtractor())
    }
}
package com.iosfb.app.web

import android.content.Context
import org.json.JSONObject

/** Builds the script injected into every page from assets/ios_fb.css + ios_fb.js. */
object Injector {
    @Volatile
    private var cached: String? = null

    fun script(context: Context): String =
        cached ?: synchronized(this) {
            cached ?: build(context).also { cached = it }
        }

    private fun build(context: Context): String {
        val css = context.assets.open("ios_fb.css").bufferedReader().use { it.readText() }
        val js = context.assets.open("ios_fb.js").bufferedReader().use { it.readText() }
        return "window.__IOSFB_CSS=" + JSONObject.quote(css) + ";\n" + js
    }
}

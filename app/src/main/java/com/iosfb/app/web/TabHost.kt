package com.iosfb.app.web

import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Owns the five tabs. WebViews are created lazily and then kept alive for instant switching. */
class TabHost(
    private val activity: ComponentActivity,
    private val browser: BrowserHost,
) {
    private val cache = mutableMapOf<FbTab, TabState>()

    var selected by mutableStateOf(FbTab.HOME)

    val current: TabState get() = state(selected)

    fun state(tab: FbTab): TabState = cache.getOrPut(tab) { TabState(tab, activity, browser) }

    fun onActivityPause() {
        cache[selected]?.pause()
        CookieManager.getInstance().flush()
    }

    fun onActivityResume() {
        cache[selected]?.resume()
    }

    fun destroy() {
        cache.values.forEach { it.destroy() }
        cache.clear()
    }
}

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

    private var insetTop = 0f
    private var insetBottom = 0f

    fun state(tab: FbTab): TabState =
        cache.getOrPut(tab) { TabState(tab, activity, browser).also { it.setInsets(insetTop, insetBottom) } }

    /** Space (dp == CSS px) covered by the floating glass bars; pages get it as padding. */
    fun setInsets(topDp: Float, bottomDp: Float) {
        insetTop = topDp
        insetBottom = bottomDp
        cache.values.forEach { it.setInsets(topDp, bottomDp) }
    }

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

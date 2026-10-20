package com.iosfb.app.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * One tab = one WebView wrapped in a pull-to-refresh layout.
 * Observable fields drive the native Compose chrome.
 */
private const val FB_IAB_SUFFIX = " [FB_IAB/FB4A;FBAV/480.0.0.52.80;]"

class TabState(
    val tab: FbTab,
    private val activity: ComponentActivity,
    private val browser: BrowserHost,
) {
    var progress by mutableIntStateOf(0)
        private set
    var canGoBack by mutableStateOf(false)
        private set
    var firstLoaded by mutableStateOf(false)
        private set
    var hasError by mutableStateOf(false)
        private set

    var onFirstContent: (() -> Unit)? = null

    private var insetTopDp = 0f
    private var insetBottomDp = 0f
    private var started = false
    private var historyCleared = false
    private var lastHistoryIndex = -1

    val webView: WebView = buildWebView()
    val layout: SwipeRefreshLayout = buildLayout()

    // ---------------------------------------------------------------- public API

    fun ensureLoaded() {
        if (started) return
        started = true
        webView.loadUrl(BASE_URL + tab.path)
    }

    fun open(path: String) {
        started = true
        webView.loadUrl(BASE_URL + path)
    }

    fun retry() {
        hasError = false
        if (webView.url == null) webView.loadUrl(BASE_URL + tab.path) else webView.reload()
    }

    fun goBack() {
        if (webView.canGoBack()) webView.goBack()
    }

    /** Re-tapping the active tab: scroll to top first, refresh if already there (like iOS). */
    fun scrollTopOrRefresh() {
        if (webView.scrollY > 8) {
            webView.evaluateJavascript("window.scrollTo({top:0,behavior:'smooth'})", null)
        } else {
            layout.isRefreshing = true
            webView.reload()
        }
    }

    /** Returns the layout detached from any stale Compose holder so it can be re-attached. */
    fun detachedLayout(): SwipeRefreshLayout {
        (layout.parent as? ViewGroup)?.removeView(layout)
        return layout
    }

    fun setInsets(topDp: Float, bottomDp: Float) {
        if (topDp == insetTopDp && bottomDp == insetBottomDp) return
        insetTopDp = topDp
        insetBottomDp = bottomDp
        val d = activity.resources.displayMetrics.density
        layout.setProgressViewOffset(false, (topDp * d).toInt(), ((topDp + 64f) * d).toInt())
        applyInsets(webView)
    }

    fun pause() = webView.onPause()
    fun resume() = webView.onResume()

    fun destroy() {
        (layout.parent as? ViewGroup)?.removeView(layout)
        layout.removeAllViews()
        webView.stopLoading()
        webView.destroy()
    }

    // ------------------------------------------------------------------ internals

    private fun buildLayout(): SwipeRefreshLayout = SwipeRefreshLayout(activity).apply {
        setColorSchemeColors(0xFF1877F2.toInt())
        setOnRefreshListener { webView.reload() }
        setOnChildScrollUpCallback { _, _ -> webView.scrollY > 0 }
        addView(
            webView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
    }

    private fun inject(view: WebView) {
        view.evaluateJavascript(Injector.script(activity), null)
        applyInsets(view)
    }

    private fun applyInsets(view: WebView) {
        view.evaluateJavascript(
            "(function(){var s=document.documentElement&&document.documentElement.style;" +
                "if(s){s.setProperty('--iosfb-top','${insetTopDp}px');" +
                "s.setProperty('--iosfb-bottom','${insetBottomDp}px');}})()",
            null,
        )
    }

    /** iOS-like push/pop: new page slides in a little from the side while fading up. */
    private fun animateEnter(forward: Boolean) {
        val w = layout.width
        if (w <= 0) return
        layout.animate().cancel()
        layout.translationX = (if (forward) 1f else -1f) * w * 0.10f
        layout.alpha = 0.55f
        layout.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(240)
            .setInterpolator(DecelerateInterpolator(1.6f))
            .start()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildWebView(): WebView {
        val wv = WebView(activity)
        wv.setBackgroundColor(Color.TRANSPARENT)
        wv.overScrollMode = View.OVER_SCROLL_NEVER
        wv.isVerticalScrollBarEnabled = false
        wv.isHorizontalScrollBarEnabled = false

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            setSupportMultipleWindows(false)
            textZoom = 100
            // Not a plain web page: drop the WebView markers and identify as Facebook's own in-app
            // browser (FB_IAB), so m.facebook.com stops pushing "Open app" banners and web-only chrome.
            userAgentString = userAgentString
                .replace("; wv", "")
                .replace(Regex("Version/\\d+\\.\\d+\\s"), "") + FB_IAB_SUFFIX
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(wv, true)
        }

        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (uri.scheme != "http" && uri.scheme != "https") {
                    browser.openIntent(uri)
                    return true
                }
                val unwrapped = LinkRouter.unwrap(uri)
                val target = unwrapped ?: uri
                return when {
                    LinkRouter.isInternal(target) && unwrapped == null -> false
                    LinkRouter.isInternal(target) -> {
                        view.loadUrl(target.toString())
                        true
                    }
                    else -> {
                        browser.openExternal(target.toString())
                        true
                    }
                }
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                hasError = false
                inject(view)
            }

            override fun onPageCommitVisible(view: WebView, url: String?) = inject(view)

            override fun onPageFinished(view: WebView, url: String?) {
                inject(view)
                layout.isRefreshing = false
                if (!historyCleared && !hasError) {
                    // Redirects on first load must not make the root page look "back-able".
                    historyCleared = true
                    view.clearHistory()
                    lastHistoryIndex = 0
                }
                canGoBack = view.canGoBack()
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                canGoBack = view.canGoBack()
                if (isReload) return
                val index = view.copyBackForwardList().currentIndex
                if (lastHistoryIndex >= 0 && index != lastHistoryIndex) {
                    animateEnter(forward = index > lastHistoryIndex)
                }
                lastHistoryIndex = index
                inject(view)
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    hasError = true
                    layout.isRefreshing = false
                }
            }

            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                // Returning true keeps the app alive instead of crashing with the renderer.
                hasError = true
                return true
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress = newProgress
                if (!firstLoaded && newProgress >= 70) {
                    firstLoaded = true
                    onFirstContent?.invoke()
                }
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: WebChromeClient.FileChooserParams,
            ): Boolean = browser.chooseFiles(fileChooserParams, filePathCallback)

            override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) =
                browser.showCustomView(view, callback)

            override fun onHideCustomView() = browser.hideCustomView()
        }

        wv.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            browser.download(url, userAgent, contentDisposition, mimeType)
        }
        return wv
    }
}
